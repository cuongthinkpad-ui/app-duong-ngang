"""Opt-in live MP3 receiver: server-driven alerts, not a hidden JavaScript polling loop.

Native iPhone media playback may continue on lock. Push remains the independent fallback.
Actual iOS background/reconnect behavior must be acceptance-tested on the deployed HTTPS site.
"""
import hashlib,os,shutil,subprocess,tempfile,threading
from pathlib import Path

class Audio:
    def __init__(self,root,data):
        self.root=root;self.path=data/'audio';self.path.mkdir(mode=0o700,exist_ok=True)
        self.ffmpeg=shutil.which('ffmpeg');self.espeak=shutil.which('espeak-ng')
        self.lock=threading.Lock()
        self.available=bool(self.ffmpeg and self.espeak)
    def command(self,args,**kw):
        subprocess.run(args,check=True,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL,timeout=90,**kw)
    def silence(self):
        dest=self.path/'silence.mp3'
        with self.lock:
            if not dest.exists():
                self.command([self.ffmpeg,'-y','-f','lavfi','-i','anullsrc=r=22050:cl=mono','-t','2','-c:a','libmp3lame','-b:a','32k','-write_xing','0','-id3v2_version','0',str(dest)])
        return dest.read_bytes()
    def clip(self,alerts,announcements,mode):
        # Cache key contains no plaintext report/account name.
        text=' '.join(announcements.get(e['id'],f"Đường ngang {e['crossing']}. {e['text']}.") for e in alerts)
        digest=hashlib.sha256((mode+'\n'+text).encode()).hexdigest()
        dest=self.path/(digest+'.mp3')
        with self.lock:
            if dest.exists():return dest.read_bytes()
            with tempfile.TemporaryDirectory(dir=self.path) as folder:
                folder=Path(folder);out=folder/'alert.mp3'
                if mode=='bell':
                    command=[self.ffmpeg,'-y','-i',str(self.root/'public/assets/rail_alarm.wav')]
                else:
                    voice=folder/'voice.wav'
                    self.command([self.espeak,'-v','vi','-s','155','-w',str(voice),'--stdin'],input=((text+' ')*6).encode())
                    command=[self.ffmpeg,'-y','-i',str(self.root/'public/assets/rail_intro.wav'),'-i',str(voice),'-filter_complex','[0:a]aresample=22050,aformat=channel_layouts=mono[a];[1:a]aresample=22050,aformat=channel_layouts=mono[b];[a][b]concat=n=2:v=0:a=1[out]','-map','[out]']
                self.command(command+['-ac','1','-ar','22050','-c:a','libmp3lame','-b:a','32k','-write_xing','0','-id3v2_version','0',str(out)])
                os.replace(out,dest);dest.chmod(0o600)
        return dest.read_bytes()
    def prune(self):
        import time
        for p in self.path.glob('*.mp3'):
            if p.name!='silence.mp3' and p.stat().st_mtime<time.time()-86400:
                try:p.unlink()
                except FileNotFoundError:pass
