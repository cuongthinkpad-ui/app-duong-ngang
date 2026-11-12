#!/usr/bin/env python3
"""Export only explicit, tracked reference files from the immutable Android commit."""
from pathlib import Path
import csv,hashlib,io,json,re,subprocess,zipfile
ROOT=Path(__file__).resolve().parent
REPO=ROOT.parent
BASE='ee1affe5ea024590ce356e0f1e23aafbf17df669'
NAME='BAN_GIAO_DAY_DU_VIET_LAI_WEB_0.27.3'
ZIP='DuongNgang-DacTa-VietLaiWeb-0.27.3.zip'

def read_git(path):
    return subprocess.check_output(['git','show',BASE+':'+path],cwd=REPO)

def export():
    doc=(ROOT/(NAME+'.md')).read_text()
    (ROOT/(NAME+'.txt')).write_text(doc,encoding='utf-8')
    cases=[]
    for line in doc.splitlines():
        if re.match(r'^\| (?:SRC|TRN|FLT|SELF|AXLE|ALM|DB|UI|BG|IOS)-\d+ \|',line):
            columns=[v.strip() for v in line.strip('|').split('|')]
            if len(columns)!=3:raise ValueError('Malformed acceptance row: '+line)
            cases.append(columns)
    csv_file=io.StringIO(newline='');writer=csv.writer(csv_file,lineterminator="\n")
    writer.writerow(['Ma_ca','Tinh_huong','Ket_qua_bat_buoc','Trang_thai','Bang_chung']);writer.writerows([*c,'Chưa kiểm thử bản web mới',''] for c in cases)
    (ROOT/'MA_TRAN_NGHIEM_THU.csv').write_text(csv_file.getvalue(),encoding='utf-8-sig')
    blobs={}
    for name in [NAME+'.md',NAME+'.txt','README_GUI_AI_STUDIO.txt','MA_TRAN_NGHIEM_THU.csv','run-reference-tests.sh']:
        blobs[name]=(ROOT/name).read_bytes()
    src='app/src/main/java/vn/duongngang/monitor/'
    for name in ['Rules.java','WebSource.java','Store.java','MonitorService.java','AlertOptions.java','VoiceAlarm.java','AlarmSound.java','MainActivity.java']:
        blobs['reference/android/java/vn/duongngang/monitor/'+name]=read_git(src+name)
    for name in ['RulesTest.java','ParserTest.java','WebSourceReadTest.java','AlertOptionsTest.java']:
        blobs['reference/tests/'+name]=read_git('tests/'+name)
    for name in ['RouteStoreChecks.java','HistoryUiChecks.java','UiRefreshChecks.java','VoiceChecks.java','UpgradeChecks.java','TelemetryUiChecks.java','TabSwitchChecks.java']:
        blobs['reference/android-tests/'+name]=read_git('app/src/androidTest/java/vn/duongngang/monitor/'+name)
    for name in ['rail_alarm.wav','rail_intro.wav']:
        blobs['reference/assets/'+name]=read_git('app/src/main/res/raw/'+name)
    for name in ['ic_train_header.xml','ic_launcher_foreground.xml','ic_train_mark.xml']:
        blobs['reference/assets/'+name]=read_git('app/src/main/res/drawable/'+name)
    docs=['BAN_GIAO.md','LICH_SU_TRAO_DOI.md','TIEP_TUC_0.27_AI_STUDIO.md','FIX_DIAGRAM_AFTER_0.22.md']
    docs += ['UI_REVIEW_'+v+'.md' for v in ['0.27.3','0.27.2','0.27.1','0.27','0.26','0.25','0.24','0.22','0.21','0.20','0.19','0.18','0.17','0.16','0.15','0.14','0.12']]
    for name in docs:blobs['reference/history/'+name]=read_git('docs/'+name)
    for name in ['telemetry-overview-0.14.png','telemetry-crossings-0.14.png','telemetry-incidents-0.14.png','telemetry-settings-0.14.png','axle-detail-0.13.png']:
        blobs['reference/screenshots/'+name]=read_git('docs/screenshots/'+name)
    blobs['reference/screenshots/README.txt']='''Ảnh lịch sử từ các bài kiểm thử fixture, dùng đối chiếu phong cách/chi tiết.
Không phải ảnh tài khoản vận hành. Ảnh cũ chưa có mọi thay đổi 0.27.3.
Bố cục/nút/chu kỳ/ngày cuối cùng phải theo đặc tả chính, không theo ảnh cũ.
'''.encode()
    blobs['reference/README.txt']=('Nguồn Android nguyên byte tại commit '+BASE+'.\n'
        'Java core độc lập: Rules.java, WebSource.java, AlertOptions.java.\n'
        'Các file Activity/Service/Store/Voice là tham chiếu Android, không chạy trực tiếp trong Safari.\n'
        'run-reference-tests.sh cần Python 3, curl và JDK 21; tải jsoup 1.18.3 có kiểm SHA-256.\n'
        'Test Android instrumentation cần Android, chỉ đưa vào để đối chiếu hành vi.\n'
        'Tài liệu history ưu tiên thấp hơn đặc tả chính; không khôi phục yêu cầu cũ.\n').encode()
    manifest={'android_commit':BASE,'acceptance_cases':len(cases),'files':{n:hashlib.sha256(v).hexdigest() for n,v in sorted(blobs.items())}}
    blobs['SOURCE_SHA256.json']=(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n').encode()
    target=ROOT/ZIP
    with zipfile.ZipFile(target,'w',zipfile.ZIP_DEFLATED,compresslevel=9) as archive:
        for name,data in sorted(blobs.items()):
            item=zipfile.ZipInfo('DuongNgang-VietLaiWeb/'+name,date_time=(2026,11,12,0,0,0))
            item.compress_type=zipfile.ZIP_DEFLATED;item.external_attr=(0o100755 if name.endswith('.sh') else 0o100644)<<16
            archive.writestr(item,data)
    (ROOT/(ZIP+'.sha256')).write_text(hashlib.sha256(target.read_bytes()).hexdigest()+'  '+ZIP+'\n')
    print(f'Exported {len(blobs)} files; {len(cases)} acceptance cases; {len(doc.splitlines())} specification lines; ZIP {target.stat().st_size:,} bytes')
if __name__=='__main__':export()
