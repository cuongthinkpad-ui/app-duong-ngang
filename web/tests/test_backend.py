import sys,os,tempfile,unittest,json,threading,time,html,urllib.request,urllib.error
from http.cookiejar import CookieJar
from datetime import datetime
from pathlib import Path
from http.server import ThreadingHTTPServer,BaseHTTPRequestHandler
sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
from server import App,Handler,PublicError,today,now_ms,encode

class Website(BaseHTTPRequestHandler):
    rows=[];down=False
    def log_message(self,*args):pass
    def do_POST(self):
        from urllib.parse import parse_qs
        fields=parse_qs(self.rfile.read(int(self.headers.get('Content-Length',0))).decode());self.selected=datetime.strptime(fields['date'][0],'%d/%m/%Y').date();self.do_GET()
    def do_GET(self):
        if self.down:self.send_response(503);self.end_headers();return
        day=getattr(self,'selected',today());route=['100+000','101+000','102+000 (Vị)','280+500','303+350 (Nghi Long)']
        body="<form method='post'><h1>BÁO CÁO ĐƯỜNG NGANG CBTĐ</h1><input name='date' value='%s'><select name='route'><option>-- Tất cả --</option>"%(day.strftime('%d/%m/%Y'))
        body+=''.join('<option>Km '+p+'</option>' for p in route)+"</select><table id='FixtureGdata'>"
        for e in reversed(self.rows if day==today() else []):
            at=datetime.fromtimestamp(e['at']/1000,__import__('server').ZONE)
            body+='<tr><td>'+html.escape(e['crossing'])+'</td><td><font color="'+('red' if e['red'] else 'blue')+'">'+html.escape(e['text'])+'</font></td><td>'+day.strftime('%d/%m')+'</td><td>'+at.strftime('%H:%M:%S')+'</td></tr>'
        data=(body+'</table></form>').encode();self.send_response(200);self.send_header('Content-Type','text/html; charset=utf-8');self.send_header('Content-Length',str(len(data)));self.end_headers();self.wfile.write(data)

def event(id,text='Chuông 1 hỏng',point='Km 101+000',at=None,red=True):return dict(id=id,at=at or now_ms(),crossing=point,text=text,red=red,counts=[])

class Backend(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        os.environ['ALLOW_LOCAL_TEST']='1';os.environ['WEBSITE_HOSTS']='127.0.0.1'
        cls.site=ThreadingHTTPServer(('127.0.0.1',0),Website);threading.Thread(target=cls.site.serve_forever,daemon=True).start()
    @classmethod
    def tearDownClass(cls):cls.site.shutdown();cls.site.server_close()
    def setUp(self):
        self.tmp=tempfile.TemporaryDirectory();self.app=App(self.tmp.name,start_worker=False);Website.rows=[];Website.down=False
        self.s,_=self.app.session();self.url='http://127.0.0.1:%s/'%self.site.server_port
    def tearDown(self):self.app.stop.set();self.app.pool.shutdown();self.tmp.cleanup()
    def config(self,user='fixture'):
        self.app.configure(self.s,dict(url=self.url,user=user,password='local-test-password',mode='voice'));self.s,_=self.app.session(self.s['id']);return self.s['monitor']
    def state(self):return self.app.snapshot(self.s,{})
    def test_real_parser_config_encryption_isolation(self):
        Website.rows=[event('old')];mid=self.config();state=self.state()
        self.assertEqual(len(state['faults']),1);self.assertEqual(state['alerts'],[]);self.assertEqual(len(state['route']),5)
        stored=self.app.monitor(mid)['credentials'];self.assertNotIn('local-test-password',stored);self.assertEqual(self.app.open(stored)['password'],'local-test-password')
        other,_=self.app.session();self.assertFalse(self.app.snapshot(other,{})['configured']);self.assertNotIn('password',encode(state));self.assertNotIn('checkpoint',state)
    def test_retry_120_preserves_cache_and_recovers(self):
        Website.rows=[event('old')];mid=self.config();initial=self.state();Website.down=True
        start=time.time();self.app.poll(mid);m=self.app.monitor(mid);s=self.state()
        self.assertTrue(s['connectionLost']);self.assertEqual(s['lastSuccess'],initial['lastSuccess']);self.assertEqual(len(s['faults']),1)
        self.assertGreaterEqual(m['next_due'],start+120);self.assertLess(m['next_due'],time.time()+121)
        self.app.retry(mid);self.assertLessEqual(self.app.monitor(mid)['next_due'],time.time());Website.down=False;self.app.poll(mid);self.assertFalse(self.state()['connectionLost']);self.assertLess(self.app.monitor(mid)['next_due'],time.time()+31)
    def test_only_new_alerts_confirmation_keeps_faults(self):
        Website.rows=[event('old',at=now_ms()-10000)];mid=self.config();self.app.poll(mid);self.assertEqual(self.state()['seq'],0)
        Website.rows.append(event('new','Đèn 1 hỏng',at=now_ms()-1000));self.app.poll(mid);s=self.state();self.assertGreater(s['seq'],0)
        alerts=self.app.snapshot(self.s,{'since':['0']})['alerts'];self.assertEqual(len(alerts),1);self.assertEqual(alerts[0]['text'],'Đèn 1 hỏng')
        self.app.poll(mid);self.assertEqual(self.state()['seq'],s['seq']);self.assertEqual(len(self.state()['faults']),2)
        self.assertEqual(self.state()['alerts'],[],'reopen has no stale audio')
    def test_worker_without_browser_and_auth_failure(self):
        mid=self.config();Website.rows=[event('new','Đèn 1 hỏng',at=now_ms()-1000)]
        self.app.retry(mid)
        worker=App(self.tmp.name,start_worker=True)
        try:
            deadline=time.time()+15
            while time.time()<deadline and not self.state()['seq']:time.sleep(.15)
            self.assertGreater(self.state()['seq'],0,'server worker polls with no browser or frontend timer')
        finally:
            worker.stop.set();worker.worker.join(5);worker.push_worker.join(5);worker.pool.shutdown()
        real=self.app.engine
        def reject(request):
            if request['op']=='fetch':raise PublicError('Không đăng nhập được website.',400,'auth')
            return real(request)
        self.app.engine=reject;self.app.poll(mid);s,_=self.app.session(self.s['id']);self.assertFalse(json.loads(s['options'])['running'])
        self.assertEqual(len(self.state()['faults']),1)

    def test_push_queue_no_duplicate_and_new_alarm(self):
        from unittest.mock import patch
        from cryptography.hazmat.primitives.asymmetric import ec
        from cryptography.hazmat.primitives import serialization
        from server import b64
        mid=self.config();key=ec.generate_private_key(ec.SECP256R1()).public_key().public_bytes(serialization.Encoding.X962,serialization.PublicFormat.UncompressedPoint)
        self.app.subscribe(self.s['id'],{'endpoint':'https://web.push.apple.com/local-test','keys':{'p256dh':b64(key),'auth':b64(b'0123456789abcdef')}})
        e=event('new');state=json.loads(self.app.monitor(mid)['state']);state['faults']=[e];self.app.announce(mid,state,[e])
        with self.app.db() as db:db.execute('UPDATE monitors SET state=? WHERE id=?',(encode(state),mid))
        with patch('pywebpush.webpush') as send:
            self.app.deliver();self.assertEqual(send.call_count,1);payload=json.loads(send.call_args.kwargs['data']);self.assertEqual(payload['title'],'1 sự cố mới')
            self.app.deliver();self.assertEqual(send.call_count,1,'delivered notices are not resent')
        seq=self.state()['seq'];self.app.announce(mid,state,[e]);self.assertEqual(self.state()['seq'],seq)
        newer=event('next','Đèn 1 hỏng');state['faults'].append(newer);self.app.announce(mid,state,[newer]);self.assertGreater(self.state()['seq'],seq,'new fault remains eligible')

    def test_java_bridge_history_devices_axles_and_station(self):
        from server import ZONE
        start=int(datetime.combine(today(),datetime.min.time(),ZONE).timestamp()*1000)
        rows=[event('fuse','Cầu chì 50A bị đứt',point='Km 304+000',at=start+1000,red=False),event('recover','Cầu chì 50A khôi phục tốt',point='Km 304+000',at=start+2000,red=False),event('sensor','Điện áp cảm biến 7 < 5V',point='Km 313+800',at=start+3000)]
        for i in range(7):
            counts=['40']*6;counts[2]=str(43 if i<6 else 60)
            rows.append(dict(event('counts'+str(i),'Khôi phục hệ thống',point='Km 313+800',at=start+(i+5)*60000,red=False),counts=counts))
        rows.append(dict(event('exempt','Khôi phục hệ thống',point='Km 320+445',at=start+600000,red=False),counts=['40','40','60','40','40','40']))
        s=self.app.engine(dict(op='analyze',now=now_ms(),historical=True,user='Chosy',route=['300+000','304+000','313+800','320+445'],events=rows,previous={}))
        self.assertIn('303+350 (Nghi Long)',s['route']);self.assertEqual([p['label'] for p in s['points'] if p['station']],['Ga NL'])
        self.assertNotIn('fuse',[e['id'] for e in s['faults']]);self.assertEqual(len([e for e in s['faults'] if e['id'].startswith('axle:')]),2)
        self.assertEqual(len(s['devices']['313+800']['Cảm biến']),3)
        self.assertFalse(any(e['crossing'].endswith('320+445') for e in s['faults']))

    def test_maintenance_preserves_archived_inference(self):
        archived=event('self:archived','Đường ngang tự đóng',at=now_ms()-3*86400000)
        s=self.app.engine(dict(op='analyze',now=now_ms(),user='fixture',route=['100+000','101+000'],events=[archived],previous={}))
        self.assertNotIn('self:archived',s['discard'],'rolling inference correction must not delete older retained history')

    def test_imported_history_persists_inference_before_checkpoint_rebuild(self):
        from server import ZONE
        from datetime import timedelta
        mid=self.config();day=today()-timedelta(days=3);start=int(datetime.combine(day,datetime.min.time(),ZONE).timestamp()*1000)
        arrival=event('archived-arrival','Tàu lẻ',at=start+60000,red=False);real=self.app.engine
        def fixture(request):
            if request['op']=='fetch':return dict(ok=True,route=['100+000','101+000','102+000'],events=[arrival],connected=True)
            return real(request)
        self.app.engine=fixture;self.app.history(self.s,day.isoformat())
        self.assertTrue(any(e['id']=='self:archived-arrival' for e in self.app.event_rows(mid)),'backfilled self inference must participate in live checkpoint replay')
        self.assertEqual(self.state()['seq'],0,'history import stays silent')

    def test_train_list_uses_selected_day_without_truncation(self):
        from server import ZONE
        start=int(datetime.combine(today(),datetime.min.time(),ZONE).timestamp()*1000)
        rows=[event('train'+str(i),'Tàu lẻ',at=start+(i+1)*1000,red=False) for i in range(125)]
        rows.append(event('yesterday','Tàu lẻ',at=start-1000,red=False))
        result=self.app.engine(dict(op='analyze',now=now_ms(),route=['100+000','101+000','102+000'],events=rows,previous={}))
        self.assertEqual(len(result['trains']),125);self.assertNotIn('yesterday',[e['id'] for e in result['trains']])

    def test_security_url_and_push(self):
        for url in ['http://169.254.169.254/','file:///etc/passwd','https://visitec.vn.evil/','http://user:pass@127.0.0.1/','http://127.0.0.1/?token=x']:
            with self.assertRaises(PublicError):self.app.validate_url(url)
        for endpoint in ['http://web.push.apple.com/a','https://web.push.apple.com.evil/a','https://127.0.0.1/a']:
            with self.assertRaises(PublicError):self.app.subscribe(self.s['id'],{'endpoint':endpoint,'keys':{}})
        with self.assertRaises(PublicError):self.app.valid_day('2000-01-01')
    def test_checkpoint_before_prune_and_long_timestamp(self):
        mid=self.config();old=event('expired',at=now_ms()-40*86400000)
        with self.app.db() as db:db.execute('INSERT INTO events VALUES(?,?,?,?)',(mid,old['id'],old['at'],encode(old)))
        self.app.poll(mid)
        self.assertNotIn('expired',[e['id'] for e in self.app.event_rows(mid)])
        state=json.loads(self.app.monitor(mid)['state']);self.assertIn('checkpoint',state);self.assertIn('checkpointAt',state)
    def test_http_csrf_static_no_private_files(self):
        http=ThreadingHTTPServer(('127.0.0.1',0),Handler);http.app=self.app;threading.Thread(target=http.serve_forever,daemon=True).start()
        try:
            jar=CookieJar();op=urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar));base='http://127.0.0.1:%s'%http.server_port
            s=json.load(op.open(base+'/api/session'));self.assertTrue(s['csrf'])
            request=urllib.request.Request(base+'/api/config',data=b'{}',headers={'Content-Type':'application/json'})
            with self.assertRaises(urllib.error.HTTPError) as e:op.open(request)
            self.assertEqual(e.exception.code,403)
            for path in ['/server.py','/../data/app.key','/data/app.key']:
                with self.assertRaises(urllib.error.HTTPError):op.open(base+path)
            r=op.open(base+'/');self.assertIn("frame-ancestors 'none'",r.headers['Content-Security-Policy']);self.assertIn(b'X\xc3\x81C NH\xe1\xba\xacN',r.read())
        finally:http.shutdown();http.server_close()
if __name__=='__main__':unittest.main()
