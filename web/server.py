#!/usr/bin/env python3
"""Persistent web monitor. Never exposes website credentials to other sessions or static files."""
from __future__ import annotations
import base64, concurrent.futures, contextlib, hashlib, hmac, ipaddress, json, os, secrets, signal, sqlite3, subprocess, threading, time
from datetime import datetime, timedelta
from zoneinfo import ZoneInfo
from pathlib import Path
from urllib.parse import urlsplit, parse_qs
from http.server import ThreadingHTTPServer, BaseHTTPRequestHandler
from http.cookies import SimpleCookie
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.hazmat.primitives import serialization
from database import connect
from audio import Audio

ROOT=Path(__file__).resolve().parent
ZONE=ZoneInfo('Asia/Ho_Chi_Minh')
def now_ms(): return int(time.time()*1000)
def today(): return datetime.now(ZONE).date()
def encode(v): return json.dumps(v,ensure_ascii=False,separators=(',',':'))
def b64(v): return base64.urlsafe_b64encode(v).decode().rstrip('=')

class PublicError(Exception):
    def __init__(self,message,status=400,kind='data'): super().__init__(message);self.status=status;self.kind=kind

class App:
    def __init__(self,data_dir=None,start_worker=True):
        self.data=Path(data_dir or os.environ.get('DATA_DIR',ROOT/'data')).resolve()
        self.data.mkdir(mode=0o700,parents=True,exist_ok=True);self.data.chmod(0o700)
        self.origin=os.environ.get('PUBLIC_ORIGIN','').rstrip('/')
        self.database_url=os.environ.get('DATABASE_URL','')
        if os.environ.get('K_SERVICE') and not self.origin:raise ValueError('Set PUBLIC_ORIGIN for Cloud Run')
        if self.origin and not all(os.environ.get(k) for k in ('DATABASE_URL','APP_KEY','VAPID_PRIVATE_KEY')):
            raise ValueError('Public deployment requires PostgreSQL DATABASE_URL and stable APP_KEY/VAPID_PRIVATE_KEY secrets')
        if self.origin and (urlsplit(self.origin).scheme!='https' or not urlsplit(self.origin).hostname or urlsplit(self.origin).path or urlsplit(self.origin).username or urlsplit(self.origin).query or urlsplit(self.origin).fragment):
            raise ValueError('PUBLIC_ORIGIN must be an HTTPS origin without path')
        self.hosts=set(os.environ.get('WEBSITE_HOSTS','visitec.vn').lower().split(','))
        self.allow_local=os.environ.get('ALLOW_LOCAL_TEST')=='1'
        key=self.data/'app.key'
        if os.environ.get('APP_KEY'):
            self.key=base64.urlsafe_b64decode(os.environ['APP_KEY']+'='*(-len(os.environ['APP_KEY'])%4))
        else:
            self.key=None
        if self.key is None and not key.exists():
            try:
                fd=os.open(key,os.O_WRONLY|os.O_CREAT|os.O_EXCL,0o600)
                with os.fdopen(fd,'wb') as f:f.write(secrets.token_bytes(32))
            except FileExistsError: pass
        self.key=self.key or key.read_bytes();self.cipher=AESGCM(self.key)
        if len(self.key)!=32:raise ValueError('APP_KEY must encode 32 bytes')
        self.subject=os.environ.get('VAPID_SUBJECT','mailto:development@example.invalid')
        if self.origin and self.subject.endswith('example.invalid'): raise ValueError('Set VAPID_SUBJECT to an administrator contact before public deployment')
        private=self.data/'vapid-private.pem'
        if os.environ.get('VAPID_PRIVATE_KEY'):
            fd=os.open(private,os.O_WRONLY|os.O_CREAT|os.O_TRUNC,0o600)
            with os.fdopen(fd,'w') as f:f.write(os.environ['VAPID_PRIVATE_KEY'])
        if not private.exists():
            k=ec.generate_private_key(ec.SECP256R1())
            try:
                fd=os.open(private,os.O_WRONLY|os.O_CREAT|os.O_EXCL,0o600)
                with os.fdopen(fd,'wb') as f:f.write(k.private_bytes(serialization.Encoding.PEM,serialization.PrivateFormat.PKCS8,serialization.NoEncryption()))
            except FileExistsError:pass
        k=serialization.load_pem_private_key(private.read_bytes(),password=None)
        self.vapid_public=b64(k.public_key().public_bytes(serialization.Encoding.X962,serialization.PublicFormat.UncompressedPoint))
        self.stop=threading.Event();self.locks={};self.locks_guard=threading.Lock();self.rates={};self.rate_lock=threading.Lock()
        self.pool=concurrent.futures.ThreadPoolExecutor(max_workers=4,thread_name_prefix='website-reader')
        self.engine_slots=threading.BoundedSemaphore(2)
        self.owner=secrets.token_hex(16)
        self.audio=Audio(ROOT,self.data)
        self.audio_slots=threading.BoundedSemaphore(16)
        self.active=set();self.active_lock=threading.Lock()
        with self.db() as db:
            db.executescript('''
            CREATE TABLE IF NOT EXISTS sessions(id TEXT PRIMARY KEY,csrf TEXT NOT NULL,monitor TEXT,options TEXT NOT NULL,expires REAL NOT NULL);
            CREATE TABLE IF NOT EXISTS monitors(id TEXT PRIMARY KEY,credentials TEXT NOT NULL,state TEXT NOT NULL,next_due REAL NOT NULL,lease_owner TEXT NOT NULL DEFAULT '',lease_until REAL NOT NULL DEFAULT 0);
            CREATE TABLE IF NOT EXISTS events(monitor TEXT NOT NULL,id TEXT NOT NULL,at INTEGER NOT NULL,payload TEXT NOT NULL,PRIMARY KEY(monitor,id));
            CREATE INDEX IF NOT EXISTS events_time ON events(monitor,at);
            CREATE TABLE IF NOT EXISTS days(monitor TEXT NOT NULL,day TEXT NOT NULL,PRIMARY KEY(monitor,day));
            CREATE TABLE IF NOT EXISTS subscriptions(session TEXT PRIMARY KEY,payload TEXT NOT NULL);
            CREATE TABLE IF NOT EXISTS notices(seq INTEGER PRIMARY KEY AUTOINCREMENT,session TEXT NOT NULL,monitor TEXT NOT NULL,payload TEXT NOT NULL,created REAL NOT NULL,sent INTEGER NOT NULL DEFAULT 0,attempts INTEGER NOT NULL DEFAULT 0,next_due REAL NOT NULL DEFAULT 0);
            CREATE TABLE IF NOT EXISTS announced(session TEXT NOT NULL,event TEXT NOT NULL,at INTEGER NOT NULL,PRIMARY KEY(session,event));
            ''')
        self.worker=threading.Thread(target=self.loop,daemon=True,name='background-monitor')
        self.push_worker=threading.Thread(target=self.push_loop,daemon=True,name='push-delivery')
        if start_worker:self.worker.start();self.push_worker.start()

    @contextlib.contextmanager
    def db(self):
        with connect(self.data/'monitor.sqlite3',self.database_url) as db:yield db

    def seal(self,data):
        nonce=secrets.token_bytes(12);return b64(nonce+self.cipher.encrypt(nonce,encode(data).encode(),b'rail-web-v1'))
    def open(self,data):
        raw=base64.urlsafe_b64decode(data+'='*(-len(data)%4));return json.loads(self.cipher.decrypt(raw[:12],raw[12:],b'rail-web-v1'))
    def session(self,sid=None):
        with self.db() as db:
            row=db.execute('SELECT * FROM sessions WHERE id=? AND expires>?',(sid,time.time())).fetchone() if sid else None
            if row:return dict(row),False
            sid=secrets.token_urlsafe(32);csrf=secrets.token_urlsafe(32)
            opts=encode({'mode':'voice','running':False,'rangeFrom':'','rangeTo':'','monitorDate':''})
            db.execute('INSERT INTO sessions VALUES(?,?,NULL,?,?)',(sid,csrf,opts,time.time()+30*86400))
            return dict(db.execute('SELECT * FROM sessions WHERE id=?',(sid,)).fetchone()),True
    def rate(self,key,limit=8):
        with self.rate_lock:
            entries=[t for t in self.rates.get(key,[]) if t>time.time()-60]
            if len(entries)>=limit:raise PublicError('Thao tác quá nhanh. Chờ một phút rồi thử lại.',429)
            self.rates[key]=entries+[time.time()]
            if len(self.rates)>10000:self.rates={k:v for k,v in self.rates.items() if v and v[-1]>time.time()-60}
    def validate_url(self,value):
        try:
            u=urlsplit(str(value).strip());port=u.port or (443 if u.scheme=='https' else 80)
            if u.scheme not in ('http','https') or u.username or u.password or u.query or u.fragment or not u.hostname:raise ValueError()
            if u.hostname.lower() not in self.hosts:raise ValueError()
            try:addr=ipaddress.ip_address(u.hostname)
            except ValueError:addr=None
            if (addr and not addr.is_global) or u.hostname.lower()=='localhost':
                if not self.allow_local:raise ValueError()
            if port not in (80,443,7447) and not (self.allow_local and (u.hostname=='localhost' or addr and addr.is_loopback)):raise ValueError()
            return value.strip().rstrip('/')+'/'
        except ValueError:raise PublicError('Địa chỉ website chưa được máy chủ cho phép. Không dùng URL chứa tài khoản hoặc token.')
    def engine(self,request):
        java=os.environ.get('JAVA_HOME','')
        cmd=[str(Path(java)/'bin/java') if java else 'java','-Xmx256m','-cp',str(ROOT/'build/classes')+os.pathsep+str(ROOT/'.deps/*'),'vn.duongngang.monitor.Engine']
        # Pass inherited proxy and trusted CA to Java without copying credentials.
        proxy=os.environ.get('HTTPS_PROXY')
        if proxy:
            p=urlsplit(proxy)
            cmd[1:1]=[f'-Dhttps.proxyHost={p.hostname}',f'-Dhttps.proxyPort={p.port or 80}',f'-Dhttp.proxyHost={p.hostname}',f'-Dhttp.proxyPort={p.port or 80}','-Dhttp.nonProxyHosts=localhost|127.*|[::1]']
            if Path('/etc/ssl/certs/java/cacerts').exists():cmd.insert(1,'-Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts')
        if not self.engine_slots.acquire(timeout=10):raise PublicError('Máy chủ đang đọc dữ liệu. Thử lại sau.',503,'busy')
        try:
            result=subprocess.run(cmd,input=encode(request),text=True,capture_output=True,timeout=180,check=True,cwd=ROOT)
            out=json.loads(result.stdout)
        except (subprocess.SubprocessError,ValueError):raise PublicError('Chưa hoàn tất đọc dữ liệu. Dữ liệu cũ được giữ.',503,'network')
        finally:self.engine_slots.release()
        if not out.get('ok'):raise PublicError(out.get('error','Chưa đọc được dữ liệu.'),400,out.get('type','data'))
        return out
    def monitor(self,mid):
        with self.db() as db:r=db.execute('SELECT * FROM monitors WHERE id=?',(mid,)).fetchone()
        if not r:raise PublicError('Chưa lưu tài khoản theo dõi.',409)
        return dict(r)
    def lock(self,mid):
        with self.locks_guard:return self.locks.setdefault(mid,threading.Lock())
    def event_rows(self,mid):
        with self.db() as db:return [json.loads(r[0]) for r in db.execute('SELECT payload FROM events WHERE monitor=? ORDER BY at,id',(mid,))]
    def analyze(self,mid,rows,previous,route,user,**extra):
        return self.engine({'op':'analyze','now':now_ms(),'events':rows,'previous':previous,'route':route,'user':user,**extra})

    def configure(self,session,body):
        self.rate('configure:'+session['id'],5)
        old=self.monitor(session['monitor']) if session['monitor'] else None
        creds=self.open(old['credentials']) if old else {}
        url=self.validate_url(body.get('url',creds.get('url','')))
        user=str(body.get('user',creds.get('user',''))).strip()
        password=str(body.get('password',''))
        if not password and creds.get('url')==url and creds.get('user')==user:password=creds.get('password','')
        if not user or not password or len(user)>200 or len(password)>1000:raise PublicError('Nhập website, ID và mật khẩu của anh.')
        creds={'url':url,'user':user,'password':password}
        mid=hmac.new(self.key,encode(creds).encode(),hashlib.sha256).hexdigest()
        with self.db() as db:exists=db.execute('SELECT id FROM monitors WHERE id=?',(mid,)).fetchone()
        data=None
        if not old or old['id']!=mid or body.get('password'):
            data=self.engine({'op':'fetch',**creds,'day':today().isoformat()})
        if not exists:
            # Verify through the same complete-page reader before granting access to cached data.
            data=data or self.engine({'op':'fetch',**creds,'day':today().isoformat()})
            with self.lock(mid):
                with self.db() as db:
                    db.execute('INSERT OR IGNORE INTO monitors(id,credentials,state,next_due) VALUES(?,?,?,?)',(mid,self.seal(creds),encode({'lastSuccess':0,'route':data['route']}),time.time()))
                self.save_rows(mid,data['events'])
                result=self.analyze(mid,self.event_rows(mid),{},data['route'],user)
                result.update(lastSuccess=now_ms(),lastFull=now_ms(),status='Đã cập nhật',connectionLost=False,loading=False)
                result['alarmBasis']=[e['id'] for e in result['faults']]
                with self.db() as db:
                    db.execute('UPDATE monitors SET state=?,next_due=? WHERE id=?',(encode(result),time.time()+30,mid))
                    db.execute('INSERT OR IGNORE INTO days VALUES(?,?)',(mid,today().isoformat()))
        options=json.loads(session['options']);options.update({k:body[k] for k in ('mode','rangeFrom','rangeTo','monitorDate') if k in body})
        self.validate_options(options,self.monitor(mid))
        options['running']=True
        with self.db() as db:db.execute('UPDATE sessions SET monitor=?,options=?,expires=? WHERE id=?',(mid,encode(options),time.time()+30*86400,session['id']))
        return {'ok':True}
    def validate_options(self,o,monitor):
        if o.get('mode') not in ('voice','bell','silent'):raise PublicError('Chế độ âm không hợp lệ.')
        if o.get('monitorDate'):self.valid_day(o['monitorDate'])
        points=json.loads(monitor['state']).get('route',[])
        for k in ('rangeFrom','rangeTo'):
            if o.get(k) and o[k] not in points:raise PublicError('Phạm vi không thuộc tuyến tài khoản.')
        if o.get('rangeFrom') and o.get('rangeTo') and points.index(o['rangeFrom'])>points.index(o['rangeTo']):raise PublicError('Phạm vi đầu phải trước phạm vi cuối.')
    def valid_day(self,value):
        try:
            day=datetime.strptime(value,'%Y-%m-%d').date()
            if not today()-timedelta(days=29)<=day<=today():raise ValueError()
            return day
        except (ValueError,TypeError):raise PublicError('Chỉ chọn trong 30 ngày, gồm hôm nay.')
    def save_rows(self,mid,rows):
        floor=int(datetime.combine(today()-timedelta(days=29),datetime.min.time(),ZONE).timestamp()*1000)
        added=[]
        with self.db() as db:
            for e in rows:
                if e['at']<floor:continue
                cur=db.execute('INSERT OR IGNORE INTO events VALUES(?,?,?,?)',(mid,e['id'],e['at'],encode(e)))
                if cur.rowcount:added.append(e['id'])
        return added
    def update_analysis(self,mid,result,previous,rows):
        # Save checkpoint and current state in the SAME transaction before pruning raw events.
        with self.db() as db:
            for eid in result.get('discard',[]):db.execute('DELETE FROM events WHERE monitor=? AND id=?',(mid,eid))
            for e in result.get('derived',[]):db.execute('INSERT OR IGNORE INTO events VALUES(?,?,?,?)',(mid,e['id'],e['at'],encode(e)))
            db.execute('UPDATE monitors SET state=? WHERE id=?',(encode(result),mid))
            db.execute('DELETE FROM events WHERE monitor=? AND at<?',(mid,result['floor']))
            db.execute('DELETE FROM announced WHERE session IN (SELECT id FROM sessions WHERE monitor=?) AND at<?',(mid,result['floor']))
            db.execute('DELETE FROM days WHERE monitor=? AND day<?',(mid,(today()-timedelta(days=29)).isoformat()))
            db.execute('DELETE FROM notices WHERE created<?',(time.time()-86400,))
    def in_scope(self,e,options,route):
        key=e['crossing'].removeprefix('Km ').strip()
        positions={p.split(' (')[0]:i for i,p in enumerate(route)}
        pos=positions.get(key)
        if pos is None:return False
        start=route.index(options['rangeFrom']) if options.get('rangeFrom') in route else 0
        end=route.index(options['rangeTo']) if options.get('rangeTo') in route else len(route)-1
        return start<=pos<=end

    def poll(self,mid):
        with self.lock(mid):
            monitor=self.monitor(mid);previous=json.loads(monitor['state']);creds=self.open(monitor['credentials'])
            loading=dict(previous,loading=True,lastAttempt=now_ms())
            with self.db() as db:db.execute('UPDATE monitors SET state=? WHERE id=?',(encode(loading),mid))
            try:
                # Maintenance even while offline, with checkpoint persisted before deletion.
                before=self.event_rows(mid)
                maintained=self.analyze(mid,before,previous,previous.get('route',[]),creds['user'])
                maintained.update({k:previous.get(k) for k in ('lastSuccess','status','connectionLost','coverage','alarmBasis','lastFull')});maintained['loading']=True
                self.update_analysis(mid,maintained,previous,before)
                known=[e['id'] for e in before if not e['id'].startswith(('self:','axle:'))]
                full=not previous.get('lastFull') or now_ms()-previous['lastFull']>=900000
                data=self.engine({'op':'fetch',**creds,'day':today().isoformat(),'known':[] if full else known})
                rows=list(data['events'])
                if not previous.get('coverage') or not previous.get('lastSuccess') or now_ms()-previous['lastSuccess']>=86400000:
                    old=self.engine({'op':'fetch',**creds,'day':(today()-timedelta(days=1)).isoformat()})
                    rows=old['events']+rows
                added=self.save_rows(mid,rows);all_rows=self.event_rows(mid)
                result=self.analyze(mid,all_rows,maintained,data['route'],creds['user'],added=added,forceReset=data.get('connected') is False,earliest=min((e['at'] for e in rows),default=now_ms()))
                result.update(lastSuccess=now_ms(),lastFull=now_ms() if full else previous.get('lastFull',0),lastAttempt=loading['lastAttempt'],status='Đã cập nhật',connectionLost=False,loading=False,coverage=True)
                self.update_analysis(mid,result,previous,all_rows)
                with self.db() as db:
                    db.execute('INSERT OR IGNORE INTO days VALUES(?,?)',(mid,today().isoformat()))
                    db.execute('UPDATE monitors SET next_due=? WHERE id=?',(time.time()+30,mid))
                old_faults=set(previous.get('alarmBasis') or [e['id'] for e in previous.get('faults',[])])
                new=[e for e in result['faults'] if e['id'] not in old_faults] if previous.get('lastSuccess') else []
                result['alarmBasis']=[e['id'] for e in result['faults']]
                with self.db() as db:db.execute('UPDATE monitors SET state=? WHERE id=?',(encode(result),mid))
                self.announce(mid,result,new)
            except PublicError as error:
                state=json.loads(self.monitor(mid)['state']);state.update(loading=False,connectionLost=error.kind=='network' or previous.get('connectionLost',False),status=str(error),lastAttempt=loading['lastAttempt'])
                with self.db() as db:
                    db.execute('UPDATE monitors SET state=?,next_due=? WHERE id=?',(encode(state),time.time()+(120 if error.kind=='network' else 30),mid))
                    if error.kind=='auth':
                        for s in db.execute('SELECT id,options FROM sessions WHERE monitor=?',(mid,)).fetchall():
                            options=json.loads(s['options']);options['running']=False;db.execute('UPDATE sessions SET options=? WHERE id=?',(encode(options),s['id']))
    def announce(self,mid,state,events):
        with self.db() as db:
            for s in db.execute('SELECT * FROM sessions WHERE monitor=? AND expires>?',(mid,time.time())).fetchall():
                options=json.loads(s['options'])
                if not options.get('running') or options.get('monitorDate') not in ('',today().isoformat()):continue
                fresh=[]
                for e in events:
                    if self.in_scope(e,options,state['route']) and db.execute('INSERT OR IGNORE INTO announced VALUES(?,?,?)',(s['id'],e['id'],e['at'])).rowcount:fresh.append(e)
                if fresh:db.execute('INSERT INTO notices(session,monitor,payload,created) VALUES(?,?,?,?)',(s['id'],mid,encode(fresh),time.time()))
    def retry(self,mid):
        with self.db() as db:db.execute('UPDATE monitors SET next_due=? WHERE id=?',(time.time(),mid))
    def loop(self):
        while not self.stop.wait(.5):
            try:
                with self.db() as db:
                    db.execute('UPDATE monitors SET lease_until=? WHERE lease_owner=?',(time.time()+60,self.owner))
                    due=db.execute('SELECT DISTINCT m.id FROM monitors m JOIN sessions s ON s.monitor=m.id WHERE m.next_due<=? AND s.expires>? AND json_extract(s.options,\'$.running\')=1',(time.time(),time.time())).fetchall()
                for r in due:
                    mid=r['id']
                    with self.active_lock:
                        if mid in self.active:continue
                        with self.db() as db:
                            claimed=db.execute('UPDATE monitors SET lease_owner=?,lease_until=? WHERE id=? AND lease_until<? AND next_due<=?',(self.owner,time.time()+60,mid,time.time(),time.time())).rowcount
                        if not claimed:continue
                        self.active.add(mid)
                    self.pool.submit(self.run_task,mid)
            except Exception:
                # Keep the monitor alive; never log credential-bearing requests or parser HTML.
                self.stop.wait(2)
    def push_loop(self):
        while not self.stop.wait(2):
            try:self.deliver()
            except Exception:self.stop.wait(3)
    def run_task(self,mid):
        try:self.poll(mid)
        except Exception:
            with self.db() as db:
                state=json.loads(self.monitor(mid)['state']);state.update(loading=False,status='Chưa hoàn tất cập nhật. Dữ liệu cũ được giữ.')
                db.execute('UPDATE monitors SET state=?,next_due=? WHERE id=?',(encode(state),time.time()+120,mid))
        finally:
            with self.db() as db:db.execute('UPDATE monitors SET lease_owner=?,lease_until=0 WHERE id=? AND lease_owner=?',('',mid,self.owner))
            with self.active_lock:self.active.discard(mid)

    def subscribe(self,sid,subscription):
        try:
            u=urlsplit(subscription['endpoint']);host=(u.hostname or '').lower()
            allowed=host in ('fcm.googleapis.com','web.push.apple.com') or host.endswith(('.push.apple.com','.push.services.mozilla.com','.notify.windows.com'))
            if not allowed or u.scheme!='https' or u.port not in (None,443) or u.username or u.password:raise ValueError()
            for name,size in [('p256dh',65),('auth',16)]:
                v=subscription['keys'][name]
                if len(base64.urlsafe_b64decode(v+'='*(-len(v)%4)))!=size:raise ValueError()
            ec.EllipticCurvePublicKey.from_encoded_point(ec.SECP256R1(),base64.urlsafe_b64decode(subscription['keys']['p256dh']+'='*(-len(subscription['keys']['p256dh'])%4)))
        except (ValueError,KeyError,TypeError):raise PublicError('Đăng ký thông báo không hợp lệ.')
        with self.db() as db:db.execute('INSERT OR REPLACE INTO subscriptions VALUES(?,?)',(sid,self.seal(subscription)))
    def deliver(self):
        from pywebpush import webpush, WebPushException
        with self.db() as db:
            rows=db.execute('SELECT n.*,p.payload AS subscription,s.options FROM notices n JOIN subscriptions p ON p.session=n.session JOIN sessions s ON s.id=n.session WHERE n.sent=0 AND n.next_due<=? AND n.created>? AND s.expires>? ORDER BY n.seq LIMIT 8',(time.time(),time.time()-1800,time.time())).fetchall()
        for r in rows:
            options=json.loads(r['options']);state=json.loads(self.monitor(r['monitor'])['state']);open_ids={e['id'] for e in state.get('faults',[])}
            alerts=[e for e in json.loads(r['payload']) if e['id'] in open_ids and self.in_scope(e,options,state.get('route',[]))]
            if not alerts or not options.get('running') or options.get('monitorDate') not in ('',today().isoformat()):
                with self.db() as db:db.execute('UPDATE notices SET sent=1 WHERE seq=?',(r['seq'],))
                continue
            payload={'seq':r['seq'],'title':f'{len(alerts)} sự cố mới','body':alerts[0]['crossing']+' · '+alerts[0]['text'][:350],'silent':options.get('mode')=='silent','tag':'rail-'+str(r['seq'])}
            try:
                webpush(subscription_info=self.open(r['subscription']),data=encode(payload),vapid_private_key=str(self.data/'vapid-private.pem'),vapid_claims={'sub':self.subject},ttl=300,timeout=15)
                with self.db() as db:db.execute('UPDATE notices SET sent=1 WHERE seq=?',(r['seq'],))
            except WebPushException as e:
                code=e.response.status_code if e.response is not None else 0
                with self.db() as db:
                    if code in (404,410):db.execute('DELETE FROM subscriptions WHERE session=?',(r['session'],))
                    db.execute('UPDATE notices SET attempts=attempts+1,next_due=? WHERE seq=?',(time.time()+min(1800,30*2**min(r['attempts'],6)),r['seq']))
            except Exception:pass

    def snapshot(self,session,query):
        options=json.loads(session['options'])
        if not session['monitor']:return {'configured':False,'options':options,'seq':0,'audioStreamAvailable':self.audio.available}
        m=self.monitor(session['monitor']);creds=self.open(m['credentials']);state=json.loads(m['state'])
        selected=(query.get('day') or [options.get('monitorDate') or today().isoformat()])[0]
        day=self.valid_day(selected)
        historical=day!=today()
        with self.db() as db:
            ready=db.execute('SELECT 1 FROM days WHERE monitor=? AND day=?',(m['id'],selected)).fetchone() is not None
            seq=db.execute('SELECT COALESCE(MAX(seq),0) FROM notices WHERE session=?',(session['id'],)).fetchone()[0]
            since=max(int((query.get('since') or [seq])[0]),int(options.get('confirmedThrough',0)))
            alerts=[e for n in db.execute('SELECT payload FROM notices WHERE session=? AND seq>? ORDER BY seq',(session['id'],since)) for e in json.loads(n[0])]
            push=db.execute('SELECT 1 FROM subscriptions WHERE session=?',(session['id'],)).fetchone() is not None
        if historical or query.get('crossing'):
            end=int(datetime.combine(day+timedelta(days=1),datetime.min.time(),ZONE).timestamp()*1000)-1
            if not historical:end=now_ms()
            rows=[e for e in self.event_rows(m['id']) if e['at']<=end]
            derived=self.engine({'op':'analyze','now':end,'historical':historical,'user':creds['user'],'route':state.get('route',[]),'events':rows,'previous':state,'crossing':(query.get('crossing') or [''])[0]})
            for key in ('faults','incidents','segments','occupancies','trains','devices','groups','axles','records'):state[key]=derived[key]
        route=state.get('route',[])
        open_ids={e['id'] for e in state.get('faults',[])}
        for key in ('faults','records','trains','axles'):
            if key in state:state[key]=[e for e in state[key] if self.in_scope(e,options,route)]
        state['incidents']=[e for e in state.get('incidents',[]) if self.in_scope(e['fault'],options,route)]
        alerts=[e for e in alerts if e['id'] in open_ids and self.in_scope(e,options,route)]
        public={k:v for k,v in state.items() if k not in ('checkpoint','checkpointAt','travel','pending','derived','discard','floor','baseline','coverage','alarmBasis','ok')}
        public.update(configured=True,options=options,settings={'url':creds['url'],'user':creds['user']},seq=seq,alerts=[] if historical else alerts,selectedDay=selected,historyReady=ready,historical=historical,pushEnabled=push,serverTime=now_ms(),audioStreamAvailable=self.audio.available)
        return public
    def history(self,session,day):
        self.valid_day(day);mid=session['monitor'];self.rate('history:'+session['id'],4)
        credentials=self.open(self.monitor(mid)['credentials'])
        data=self.engine({'op':'fetch',**credentials,'day':day})
        with self.lock(mid):
            monitor=self.monitor(mid);credentials=self.open(monitor['credentials'])
            self.save_rows(mid,data['events'])
            previous=json.loads(monitor['state']);rows=self.event_rows(mid)
            selected=self.valid_day(day)
            if selected<today():
                start=int(datetime.combine(selected,datetime.min.time(),ZONE).timestamp()*1000)
                end=int(datetime.combine(selected+timedelta(days=1),datetime.min.time(),ZONE).timestamp()*1000)
                # Historical inference must be stored before replaying the live checkpoint.
                # Replace only that day's derived self evidence; retain original equipment faults.
                historical_rows=[e for e in rows if e['at']<end and not (start<=e['at'] and e['id'].startswith('self:'))]
                historical=self.engine({'op':'analyze','now':end-1,'historical':True,'events':historical_rows,'previous':{},'route':data['route'],'user':credentials['user']})
                with self.db() as db:
                    db.execute("DELETE FROM events WHERE monitor=? AND at>=? AND at<? AND id LIKE ?",(mid,start,end,'self:%'))
                    for e in historical['derived']:db.execute('INSERT OR IGNORE INTO events VALUES(?,?,?,?)',(mid,e['id'],e['at'],encode(e)))
                rows=self.event_rows(mid)
            state=self.analyze(mid,rows,previous,data['route'],credentials['user'],earliest=min((e['at'] for e in data['events']),default=now_ms()))
            state.update({k:previous.get(k) for k in ('lastSuccess','status','connectionLost','loading','coverage','alarmBasis','lastFull')})
            self.update_analysis(mid,state,previous,rows)
            with self.db() as db:db.execute('INSERT OR IGNORE INTO days VALUES(?,?)',(mid,day))
        return {'ok':True}

class Handler(BaseHTTPRequestHandler):
    server_version='RailWeb'
    protocol_version='HTTP/1.1'
    def log_message(self,*args):pass
    def reply(self,status,data,content_type='application/json; charset=utf-8',cookie=None,cache='no-store'):
        body=encode(data).encode() if content_type.startswith('application/json') else data
        self.send_response(status)
        if status>=400:self.close_connection=True;self.send_header('Connection','close')
        self.send_header('Content-Type',content_type);self.send_header('Content-Length',str(len(body)))
        self.send_header('Cache-Control',cache);self.send_header('X-Content-Type-Options','nosniff');self.send_header('Referrer-Policy','same-origin')
        self.send_header('Content-Security-Policy',"default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; media-src 'self' blob:; connect-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'none'")
        self.send_header('Permissions-Policy','camera=(), microphone=(), geolocation=()')
        if cookie:self.send_header('Set-Cookie',cookie)
        if self.path.startswith('/sw.js'):self.send_header('Service-Worker-Allowed','/')
        self.end_headers()
        try:self.wfile.write(body)
        except (BrokenPipeError,ConnectionResetError):pass
    def session(self):
        try:c=SimpleCookie(self.headers.get('Cookie',''));sid=c['rail_session'].value if 'rail_session' in c else None
        except Exception:sid=None
        s,created=self.server.app.session(sid)
        secure='; Secure' if self.server.app.origin else ''
        return s,f'rail_session={s["id"]}; Path=/; HttpOnly; SameSite=Strict; Max-Age=2592000{secure}' if created else None
    def do_GET(self):
        path=urlsplit(self.path).path;app=self.server.app
        try:
            if path=='/health':
                with app.db() as db:db.execute('SELECT 1').fetchone()
                ready=app.worker.is_alive() and app.push_worker.is_alive() and (ROOT/'build/classes/vn/duongngang/monitor/Engine.class').exists()
                return self.reply(200 if ready else 503,{'ok':ready})
            if path.startswith('/api/'):
                session,cookie=self.session()
                if path=='/api/session':return self.reply(200,{'csrf':session['csrf'],'vapidPublicKey':app.vapid_public,'configured':bool(session['monitor'])},cookie=cookie)
                if path=='/api/state':return self.reply(200,app.snapshot(session,parse_qs(urlsplit(self.path).query)),cookie=cookie)
                if path=='/api/audio/stream':return self.audio_stream(session)
                raise PublicError('Không tìm thấy chức năng.',404)
            target=ROOT/'public'/('index.html' if path=='/' else path.lstrip('/'))
            target=target.resolve()
            if not target.is_relative_to((ROOT/'public').resolve()) or not target.is_file():raise PublicError('Không tìm thấy trang.',404)
            import mimetypes
            kind=mimetypes.guess_type(target)[0] or 'application/octet-stream'
            self.reply(200,target.read_bytes(),kind,cache='no-cache' if target.suffix in ('.html','.js','.webmanifest') else 'public, max-age=3600')
        except PublicError as e:self.reply(e.status,{'error':str(e)})
        except Exception:self.reply(500,{'error':'Chưa hoàn tất yêu cầu. Thử lại sau.'})
    def audio_stream(self,session):
        app=self.server.app
        if not session['monitor']:raise PublicError('Lưu tài khoản trước khi nghe cảnh báo.',409)
        if not app.audio.available:raise PublicError('Máy chủ chưa có bộ giọng Việt/âm thanh.',503)
        if not app.audio_slots.acquire(blocking=False):raise PublicError('Đã đủ số phiên nghe. Thử lại sau.',429)
        try:
            with app.db() as db:
                cursor=db.execute('SELECT COALESCE(MAX(seq),0) FROM notices WHERE session=?',(session['id'],)).fetchone()[0]
                options=json.loads(db.execute('SELECT options FROM sessions WHERE id=?',(session['id'],)).fetchone()[0]);token=secrets.token_urlsafe(16);options['streamToken']=token
                db.execute('UPDATE sessions SET options=? WHERE id=?',(encode(options),session['id']))
            silence=app.audio.silence()
            self.send_response(200);self.send_header('Content-Type','audio/mpeg');self.send_header('Cache-Control','no-store');self.send_header('Transfer-Encoding','chunked');self.send_header('X-Content-Type-Options','nosniff');self.end_headers()
            deadline=time.monotonic()+3300  # Cloud Run request timeout is 3600 seconds.
            while not app.stop.is_set() and time.monotonic()<deadline:
                with app.db() as db:
                    current=db.execute('SELECT options FROM sessions WHERE id=? AND expires>?',(session['id'],time.time())).fetchone()
                    notices=db.execute('SELECT seq,payload FROM notices WHERE session=? AND seq>? AND created>? ORDER BY seq LIMIT 1',(session['id'],cursor,time.time()-300)).fetchall()
                if not current:break
                options=json.loads(current[0])
                if options.get('streamToken')!=token or not options.get('running') or options.get('mode')=='silent' or options.get('monitorDate') not in ('',today().isoformat()):break
                data=silence
                if notices:
                    notice=notices[0];cursor=notice['seq'];state=json.loads(app.monitor(session['monitor'])['state'])
                    open_ids={e['id'] for e in state.get('faults',[])}
                    alerts=[e for e in json.loads(notice['payload']) if e['id'] in open_ids and app.in_scope(e,options,state.get('route',[]))]
                    if alerts and cursor>int(options.get('confirmedThrough',0)):data=app.audio.clip(alerts,state.get('announcements',{}),options.get('mode','voice'))
                # Pace MP3 bytes at 32 kbit/s; native media playback continues without page timers.
                for offset in range(0,len(data),4096):
                    with app.db() as db:active=db.execute('SELECT options FROM sessions WHERE id=?',(session['id'],)).fetchone()
                    if not active:break
                    active=json.loads(active[0])
                    if active.get('streamToken')!=token or not active.get('running') or active.get('mode')!=options.get('mode') or (notices and cursor<=int(active.get('confirmedThrough',0))):break
                    chunk=data[offset:offset+4096]
                    self.wfile.write(f'{len(chunk):X}\r\n'.encode()+chunk+b'\r\n');self.wfile.flush()
                    if app.stop.wait(len(chunk)/4000):break
            self.wfile.write(b'0\r\n\r\n');self.wfile.flush()
        except (BrokenPipeError,ConnectionResetError,OSError):pass
        finally:app.audio_slots.release();self.close_connection=True
    def do_POST(self):
        app=self.server.app
        try:
            session,cookie=self.session()
            if not hmac.compare_digest(self.headers.get('X-CSRF-Token',''),session['csrf']):raise PublicError('Phiên thao tác không hợp lệ. Mở lại trang.',403)
            origin=self.headers.get('Origin')
            expected=app.origin or 'http://'+self.headers.get('Host','')
            if origin and origin!=expected:raise PublicError('Nguồn yêu cầu không hợp lệ.',403)
            length=int(self.headers.get('Content-Length','0'))
            if not 0<length<=32768:raise PublicError('Yêu cầu quá lớn.',413)
            body=json.loads(self.rfile.read(length));path=urlsplit(self.path).path
            if path=='/api/config':app.rate('configure-ip:'+self.client_address[0],20);result=app.configure(session,body)
            elif path=='/api/subscribe':app.subscribe(session['id'],body);result={'ok':True}
            elif path=='/api/unsubscribe':
                with app.db() as db:db.execute('DELETE FROM subscriptions WHERE session=?',(session['id'],))
                result={'ok':True}
            elif not session['monitor']:raise PublicError('Lưu tài khoản trước khi theo dõi.',409)
            elif path=='/api/options':
                options=json.loads(session['options']);options.update({k:body[k] for k in ('running','mode','rangeFrom','rangeTo','monitorDate') if k in body})
                if not isinstance(options.get('running'),bool):raise PublicError('Trạng thái không hợp lệ.')
                app.validate_options(options,app.monitor(session['monitor']))
                with app.db() as db:db.execute('UPDATE sessions SET options=? WHERE id=?',(encode(options),session['id']))
                if options.get('monitorDate'):app.history(session,options['monitorDate'])
                result={'ok':True}
            elif path=='/api/retry':app.rate('retry:'+session['id'],6);app.retry(session['monitor']);result={'ok':True}
            elif path=='/api/history':result=app.history(session,body.get('day'))
            elif path=='/api/confirm':
                with app.db() as db:
                    options=json.loads(session['options']);options['confirmedThrough']=db.execute('SELECT COALESCE(MAX(seq),0) FROM notices WHERE session=?',(session['id'],)).fetchone()[0]
                    db.execute('UPDATE sessions SET options=? WHERE id=?',(encode(options),session['id']))
                    db.execute('UPDATE notices SET sent=1 WHERE session=?',(session['id'],))
                result={'ok':True}
            elif path=='/api/logout':
                with app.db() as db:
                    db.execute('DELETE FROM subscriptions WHERE session=?',(session['id'],));db.execute('DELETE FROM sessions WHERE id=?',(session['id'],))
                result={'ok':True};cookie='rail_session=; Path=/; HttpOnly; SameSite=Strict; Max-Age=0'+('; Secure' if app.origin else '')
            else:raise PublicError('Không tìm thấy chức năng.',404)
            self.reply(200,result,cookie=cookie)
        except PublicError as e:self.reply(e.status,{'error':str(e)})
        except (ValueError,TypeError,KeyError):self.reply(400,{'error':'Dữ liệu gửi lên không hợp lệ.'})
        except Exception:self.reply(500,{'error':'Chưa hoàn tất yêu cầu. Dữ liệu đã lưu được giữ nguyên.'})

def main():
    os.umask(0o077)
    app=App();server=ThreadingHTTPServer((os.environ.get('BIND','127.0.0.1'),int(os.environ.get('PORT','8080'))),Handler);server.app=app
    def stop(*args):app.stop.set();threading.Thread(target=server.shutdown,daemon=True).start()
    signal.signal(signal.SIGTERM,stop);signal.signal(signal.SIGINT,stop)
    print('Rail web ready',flush=True)
    try:server.serve_forever()
    finally:app.stop.set();app.pool.shutdown(wait=True,cancel_futures=True);server.server_close()
if __name__=='__main__':main()
