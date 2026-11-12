"""Container-only test: actual native HTTP MP3 response, no browser timers."""
import sys,os,tempfile,threading,time,json,urllib.request
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1];sys.path.insert(0,str(ROOT))
from server import App,Handler,ThreadingHTTPServer,encode,now_ms

def run():
    with tempfile.TemporaryDirectory() as folder:
        app=App(folder,start_worker=False);assert app.audio.available,'ffmpeg/espeak-ng required'
        sid,_=app.session();event=dict(id='stream-event',at=now_ms(),crossing='Km 101+000',text='Chuông 1 hỏng',red=True,counts=[])
        state=dict(route=['100+000','101+000'],faults=[event],lastSuccess=now_ms(),announcements={})
        options=json.loads(sid['options']);options.update(running=True,mode='bell')
        with app.db() as db:
            db.execute('INSERT INTO monitors(id,credentials,state,next_due) VALUES(?,?,?,?)',('test',app.seal({'url':'http://visitec.vn:7447/','user':'fixture','password':'local-test-only'}),encode(state),time.time()+30))
            db.execute('UPDATE sessions SET monitor=?,options=? WHERE id=?',('test',encode(options),sid['id']))
        server=ThreadingHTTPServer(('127.0.0.1',0),Handler);server.app=app;threading.Thread(target=server.serve_forever,daemon=True).start()
        base='http://127.0.0.1:%s'%server.server_port
        try:
            request=urllib.request.Request(base+'/api/audio/stream',headers={'Cookie':'rail_session='+sid['id']})
            with urllib.request.urlopen(request,timeout=15) as stream:
                assert stream.headers['Content-Type']=='audio/mpeg';stream.read(4096)
                bell=app.audio.clip([event],{},'bell');app.announce('test',state,[event]);received=stream.read(24000)
                assert bell[500:1500] in received,'real new notification must appear on already open media stream'
            clip=app.audio.clip([event],{},'voice');path=Path(folder)/'decode.mp3';path.write_bytes(clip)
            import subprocess
            subprocess.run(['ffmpeg','-v','error','-i',str(path),'-f','null','-'],check=True)
            print('PASS native HTTP MP3 receives new alarm without JavaScript; Vietnamese six-reading MP3 decodes')
        finally:app.stop.set();app.pool.shutdown();server.shutdown();server.server_close()
if __name__=='__main__':run()
