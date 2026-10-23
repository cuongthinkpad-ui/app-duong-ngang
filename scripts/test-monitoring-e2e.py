#!/usr/bin/env python3
"""Exercise the real Android service against a local fixture, only on an emulator.

Preserves/restores the emulator app's data and proxy. Never contacts the company web,
never prints credentials. Build/install the debug APK and start an emulator first.
"""
import json, os, pathlib, re, subprocess, tempfile, threading, time
import xml.etree.ElementTree as ET
from datetime import datetime, timedelta, timezone
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import parse_qs

ADB = os.environ.get('ADB', '/workspace/toolchains/android-sdk/platform-tools/adb')
PKG = 'vn.duongngang.monitor'

def adb(*args, binary=False):
    result = subprocess.run([ADB, *args], check=True, capture_output=True, timeout=45)
    return result.stdout if binary else result.stdout.decode(errors='replace')

def stop_service():
    # Shell cannot stop the non-exported service directly on every Android image.
    # This helper runs only after the emulator guard, and stops the whole test app.
    adb('shell', 'am', 'force-stop', PKG)

def preferences_xml():
    for attempt in range(8):
        raw = adb('exec-out', 'run-as', PKG, 'cat', 'shared_prefs/monitor.xml', binary=True)
        try:
            ET.fromstring(raw)
            return raw
        except ET.ParseError:
            # Retry an empty ADB read or an atomic settings-file replacement.
            if attempt == 7:
                raise
            time.sleep(0.3)

def metadata():
    root = ET.fromstring(preferences_xml())
    return {e.get('name'): e.get('value', e.text or '') for e in root}

def wait_for(predicate, label, timeout=110):
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        state = metadata()
        if predicate(state):
            print('PASS:', label, flush=True)
            return state
        time.sleep(1)
    raise AssertionError(label + ' (status=' + metadata().get('status', '') + ')')

def faults(state):
    return json.loads(state.get('activeFaults', '[]'))

AXLE_EXEMPT_POINTS = ('280+500', '307+507', '309+056', '320+445', '338+900', '343+350')

phase = {'value': 0, 'at': int(time.time()), 'manual': False, 'pre_restore': False,
         'axles': 0, 'axle_other': False, 'axle_large': False, 'axle_exact': False, 'bell_recovered': False,
         'axle_at': int(time.time()), 'exempt_at': int(time.time())-30}

class Fixture(BaseHTTPRequestHandler):
    def log_message(self, *args):
        pass

    def do_GET(self):
        self.respond((datetime.fromtimestamp(time.time(), timezone.utc).replace(tzinfo=None) + timedelta(hours=7)).date())

    def do_POST(self):
        fields = parse_qs(self.rfile.read(int(self.headers.get('Content-Length', '0'))).decode())
        self.respond(datetime.strptime(fields['date'][0], '%d/%m/%Y').date())

    def respond(self, selected):
        # The emulator uses Asia/Ho_Chi_Minh timestamps, independent of the host timezone.
        now = datetime.fromtimestamp(time.time(), timezone.utc).replace(tzinfo=None) + timedelta(hours=7)
        point_options = "<select name='route'><option value='all' selected>-- Tất cả --</option>" + ''.join(
            '<option>Km '+point+'</option>' for point in ('100+000', '101+000')+AXLE_EXEMPT_POINTS) + '</select>'
        rows = []
        current = dict(phase)
        if selected == now.date():
            te = datetime.fromtimestamp(current['exempt_at'], timezone.utc).replace(tzinfo=None) + timedelta(hours=7)
            for point in AXLE_EXEMPT_POINTS:
                for i in range(7):
                    rows.append((point, 'Khôi phục hệ thống', te+timedelta(seconds=i), False,
                                 [40,40,28 if i == 6 else 37,40,40,40]))
        if selected == now.date() and current['value']:
            t1 = datetime.fromtimestamp(current['at'], timezone.utc).replace(tzinfo=None) + timedelta(hours=7)
            rows += [('101+000', 'Tàu lẻ', t1, False), ('309+056', 'Chuông 2 hỏng', t1, True)]
            if current['manual']:
                rows += [('101+000', 'Ấn nút Phục hồi', t1+timedelta(seconds=3), False)]
            if current['pre_restore']:
                rows += [('101+000', 'Khôi phục hệ thống', t1+timedelta(seconds=2), False)]
            if current['value'] >= 2:
                rows += [('100+000', 'Tàu lẻ', t1-timedelta(seconds=1), False),
                         ('309+056', 'Chuông 2 khôi phục tốt', t1+timedelta(seconds=1), False)]
            if current['value'] >= 3:
                rows += [('100+000', 'Chuông 1 hỏng', t1+timedelta(seconds=2), True)]
            if current['bell_recovered']:
                rows += [('100+000', 'Chuông 1 khôi phục tốt', t1+timedelta(seconds=5), False)]
        if selected == now.date() and current['axles']:
            ta = datetime.fromtimestamp(current['axle_at'], timezone.utc).replace(tzinfo=None) + timedelta(hours=7)
            for i in range(current['axles']):
                rows.append(('100+000','Khôi phục hệ thống',ta+timedelta(seconds=i),False,[40,40,37,40,40,40]))
            if current['axle_other']:
                for i in range(6):
                    rows.append(('100+000','Khôi phục hệ thống',ta+timedelta(seconds=20+i),False,[40,40,40,37,40,40]))
            if current['axle_large']:
                rows.append(('100+000','Khôi phục hệ thống',ta+timedelta(seconds=30),False,[40,40,28,40,40,40]))
            if current['axle_exact']:
                rows.append(('100+000','Khôi phục hệ thống',ta+timedelta(seconds=40),False,[40,40,30,40,40,40]))
        table = "<table id='FixtureGdata'>"
        for entry in reversed(rows):
            point, text, at, red = entry[:4]
            counts = entry[4] if len(entry) > 4 else []
            table += f"<tr><td>Km {point}</td><td><font color={'red' if red else 'blue'}>{text}</font></td><td>{selected:%d/%m}</td><td>{at:%H:%M:%S}</td>" + ''.join(f'<td>{n}</td>' for n in counts) + '</tr>'
        body = (f"<form method='post' action='/'><h1>BÁO CÁO ĐƯỜNG NGANG CBTĐ</h1><input name='date' value='{selected:%d/%m/%Y}'>"
                + point_options + table + '</table></form>').encode()
        self.send_response(200)
        self.send_header('Content-Type', 'text/html; charset=UTF-8')
        self.send_header('Content-Length', str(len(body)))
        self.end_headers()
        self.wfile.write(body)


def main():
    assert adb('shell', 'getprop', 'ro.kernel.qemu').strip() == '1', 'Emulator required; physical phones are refused'
    assert adb('get-serialno').strip().startswith('emulator-'), 'Use a dedicated emulator'
    previous = metadata()
    previous_running = previous.get('running') == 'true'
    old_proxy = adb('shell', 'settings', 'get', 'global', 'http_proxy').strip()
    stop_service()
    adb('shell', 'am', 'force-stop', PKG)
    server = ThreadingHTTPServer(('127.0.0.1', 8888), Fixture)
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()
    with tempfile.TemporaryDirectory(prefix='rail-monitor-test-') as temp:
        folder = pathlib.Path(temp)
        os.chmod(folder, 0o700)
        original_prefs = preferences_xml()
        pref_root = ET.fromstring(original_prefs)
        for node in pref_root:
            if node.get('name') == 'running':
                node.set('value', 'true' if previous_running else 'false')
        (folder/'original.xml').write_bytes(ET.tostring(pref_root, encoding='utf-8'))
        original_db = subprocess.run([ADB, 'exec-out', 'run-as', PKG, 'cat', 'databases/events.db'], capture_output=True, timeout=30)
        if original_db.returncode == 0:
            (folder/'original.db').write_bytes(original_db.stdout)
        root = ET.Element('map')
        ET.SubElement(root, 'string', name='url').text = 'http://10.0.2.2:8888/'
        ET.SubElement(root, 'string', name='user').text = 'local-fixture'
        for name, value in [('demoUi', 'false'), ('running', 'true'), ('sound', 'true')]:
            ET.SubElement(root, 'boolean', name=name, value=value)
        ET.SubElement(root, 'int', name='interval', value='30')  # Self-trigger detection intentionally runs only at the real 30-second setting.
        (folder/'fixture.xml').write_bytes(ET.tostring(root, encoding='utf-8'))
        def install(local, remote):
            adb('push', str(local), '/data/local/tmp/rail-e2e-file')
            adb('shell', 'run-as', PKG, 'cp', '/data/local/tmp/rail-e2e-file', remote)
        try:
            install(folder/'fixture.xml', 'shared_prefs/monitor.xml')
            adb('shell', 'run-as', PKG, 'rm', '-f', 'databases/events.db')
            adb('shell', 'settings', 'put', 'global', 'http_proxy', ':0')
            adb('shell', 'am', 'start', '-n', PKG+'/.MainActivity')
            empty = wait_for(lambda s: int(s.get('lastSuccess','0')) > 0, 'initial snapshot accepted silently')
            assert int(empty.get('lastAlarm', '0')) == 0, 'Empty snapshot must not sound'
            assert not faults(empty), 'Exempt axle reports must not create faults'
            print('PASS: six qualifying errors and one large deviation at all six exempt points remain silent', flush=True)
            phase.update(value=1, at=int(time.time())-3)
            wait_for(lambda s: any(row[3]=='Chuông 2 hỏng' for row in faults(s)) and int(s.get('lastAlarm','0'))>0, 'original red bell message becomes active with app-owned audible playback')
            wait_for(lambda s: any(row[3]=='Nghi tự kích' for row in faults(s)), 'self-trigger confirmed after elapsed grace')
            notes=adb('shell','dumpsys','notification','--noredact')
            assert re.search(r'Notification\(channel=faults_sound', notes), 'Expected sound-channel notification'
            print('PASS: active alerts use sound channel', flush=True)
            phase['manual'] = True
            cleared = wait_for(lambda s: len(faults(s))==1 and faults(s)[0][3]=='Chuông 2 hỏng'
                               and all(row[7] for row in json.loads(s.get('travelState','[]'))),
                               'manual recovery clears self-trigger and diagram while keeping original bell fault')
            assert any(row[10] for row in json.loads(cleared['travelState'])), 'Expected persisted self-trigger classification'
            print('PASS: manually completed self-trigger retains its classification and completion record', flush=True)
            # Backfill an earlier generic restore: it must not hide the later manual reset
            # when deciding which completed inference evidence to retain at longer intervals.
            phase['pre_restore'] = True
            stop_service()
            state_root = ET.fromstring(preferences_xml())
            for node in state_root:
                if node.get('name') == 'interval': node.set('value', '120')
                if node.get('name') == 'running': node.set('value', 'true')
            (folder/'manual-disabled.xml').write_bytes(ET.tostring(state_root, encoding='utf-8'))
            install(folder/'manual-disabled.xml', 'shared_prefs/monitor.xml')
            adb('shell', 'am', 'start', '-n', PKG+'/.MainActivity')
            wait_for(lambda s: int(s.get('lastSuccess','0')) > int(cleared['lastSuccess'])
                     and all(row[7] for row in json.loads(s.get('travelState','[]')))
                     and any(row[10] for row in json.loads(s.get('travelState','[]'))),
                     'switching to 2-minute polling keeps manually reset self interval off')
            stop_service()
            state_root = ET.fromstring(preferences_xml())
            for node in state_root:
                if node.get('name') == 'interval': node.set('value', '30')
                if node.get('name') == 'running': node.set('value', 'true')
            (folder/'manual-enabled.xml').write_bytes(ET.tostring(state_root, encoding='utf-8'))
            install(folder/'manual-enabled.xml', 'shared_prefs/monitor.xml')
            phase['value']=2
            adb('shell', 'am', 'start', '-n', PKG+'/.MainActivity')
            wait_for(lambda s: not faults(s) and json.loads(s.get('pendingSelf','{}'))=={}, 'late upstream evidence removes inference; explicit bell recovery clears bell')
            last_audible = int(metadata().get('lastAlarm', '0'))
            # Change only the global sound preference; restart the same account to prove no replay.
            stop_service();adb('shell','am','force-stop',PKG)
            state_root=ET.fromstring(preferences_xml())
            for node in state_root:
                if node.get('name')=='sound':node.set('value','false')
                if node.get('name')=='running':node.set('value','true')
            (folder/'silent.xml').write_bytes(ET.tostring(state_root,encoding='utf-8'))
            install(folder/'silent.xml','shared_prefs/monitor.xml')
            adb('shell','am','start','-n',PKG+'/.MainActivity')
            phase['value']=3
            wait_for(lambda s:any(row[3]=='Chuông 1 hỏng' for row in faults(s)), 'subsequent fault stays red with global sound disabled')
            notes=adb('shell','dumpsys','notification','--noredact')
            assert re.search(r'Notification\(channel=faults_silent',notes),'Expected silent-channel notification'
            print('PASS: later fault uses silent channel', flush=True)
            assert int(metadata().get('lastAlarm', '0')) == last_audible, 'Global mute must also suppress the app-owned player'
            print('PASS: global mute suppresses app-owned audible playback', flush=True)
            # Reopen with an already active fault: this was the reported silent-start bug.
            stop_service()
            state_root = ET.fromstring(preferences_xml())
            for node in state_root:
                if node.get('name') in ('sound', 'running'):
                    node.set('value', 'true')
                if node.get('name') == 'interval':
                    node.set('value', '120')
            (folder/'audible-start.xml').write_bytes(ET.tostring(state_root, encoding='utf-8'))
            install(folder/'audible-start.xml', 'shared_prefs/monitor.xml')
            adb('shell', 'am', 'start', '-n', PKG+'/.MainActivity')
            wait_for(lambda s: any(row[3]=='Chuông 1 hỏng' for row in faults(s)) and int(s.get('lastAlarm','0')) > last_audible,
                     'monitoring start sounds for an already active red fault, including at 2-minute polling')
            # Statistical axle alerts: real polling, per-sensor daily suppression and new large error.
            metadata_before_axles = metadata()
            stop_service()
            state_root = ET.fromstring(preferences_xml())
            for node in state_root:
                if node.get('name') in ('sound','running'): node.set('value','true')
                if node.get('name') == 'interval': node.set('value','30')
            (folder/'axle-start.xml').write_bytes(ET.tostring(state_root,encoding='utf-8'))
            install(folder/'axle-start.xml','shared_prefs/monitor.xml')
            phase.update(axles=5, axle_at=int(time.time())-20, bell_recovered=True)
            adb('shell','am','start','-n',PKG+'/.MainActivity')
            five = wait_for(lambda s: not faults(s) and int(s.get('lastSuccess','0')) > int(metadata_before_axles.get('lastSuccess','0')),
                            'five qualifying axle errors remain notes without alert')
            phase['axles'] = 6
            sixth = wait_for(lambda s: any(row[0].startswith('axle:3:') and 'thiếu 3 trục' in row[3] for row in faults(s))
                            and int(s.get('lastAlarm','0')) > int(five.get('lastAlarm','0')),
                            'sixth error for CB3 raises red warning with audible playback')
            seventh_at = int(sixth['lastSuccess'])
            sixth_alarm = int(sixth['lastAlarm'])
            phase['axles'] = 7
            seventh = wait_for(lambda s: int(s.get('lastSuccess','0')) > seventh_at,
                               'seventh measurement processed')
            assert int(seventh['lastAlarm']) == sixth_alarm, 'Seventh daily error must not replay warning'
            print('PASS: same sensor daily warning does not repeat after further errors',flush=True)
            stop_service()
            state_root = ET.fromstring(preferences_xml())
            for node in state_root:
                if node.get('name') == 'running': node.set('value','true')
            (folder/'axle-restart.xml').write_bytes(ET.tostring(state_root,encoding='utf-8'))
            install(folder/'axle-restart.xml','shared_prefs/monitor.xml')
            adb('shell','am','start','-n',PKG+'/.MainActivity')
            restarted = wait_for(lambda s: int(s.get('lastSuccess','0')) > int(seventh['lastSuccess']),
                                 'monitoring restart reuses axle notice ledger')
            assert int(restarted['lastAlarm']) == sixth_alarm, 'Restart must not replay already notified daily axle warning'
            print('PASS: restart does not replay daily axle warning',flush=True)
            phase['axle_other'] = True
            other = wait_for(lambda s: any(row[0].startswith('axle:4:') for row in faults(s))
                            and int(s.get('lastAlarm','0')) > sixth_alarm,
                            'another sensor reaching six errors creates a new audible alert')
            phase['axle_large'] = True
            large = wait_for(lambda s: any(row[0].startswith('axle:3:') and 'thiếu 12 trục trong 1 lần' in row[3] for row in faults(s))
                     and int(s.get('lastAlarm','0')) > int(other['lastAlarm']),
                     'new deviation above ten axles alerts even on previously notified sensor')
            phase['axle_exact'] = True
            wait_for(lambda s: any(row[0].startswith('axle:3:') and 'thiếu 10 trục trong 1 lần' in row[3] for row in faults(s))
                     and int(s.get('lastAlarm','0')) > int(large['lastAlarm']),
                     'exactly ten missing axles create another audible alert on previously warned sensor')
            print('22 monitoring checks passed', flush=True)
        finally:
            stop_service();adb('shell','am','force-stop',PKG)
            install(folder/'original.xml','shared_prefs/monitor.xml')
            if original_db.returncode==0:install(folder/'original.db','databases/events.db')
            else:adb('shell','run-as',PKG,'rm','-f','databases/events.db')
            adb('shell','settings','put','global','http_proxy',old_proxy if old_proxy not in ('null','') else ':0')
            server.shutdown();server.server_close()
            adb('shell','am','start','-n',PKG+'/.MainActivity')

if __name__ == '__main__':
    main()
