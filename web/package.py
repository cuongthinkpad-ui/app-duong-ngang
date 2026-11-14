#!/usr/bin/env python3
"""Whitelist-only standalone source export. Never reads signing keys or runtime data."""
import hashlib,json,shutil,zipfile,subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parent
FILES=['server.py','database.py','requirements.txt','requirements.lock','requirements-dev.txt','build-engine.sh','Dockerfile','.dockerignore','.gitignore','package.json','README.md','AI_STUDIO_HANDOFF.md','README_START_HERE.md','PROMPT_AI_STUDIO.md']
DIRS=['public','engine','tools','deploy','docs','tests']
def bundle():
    out=ROOT/'.bundle/DuongNgang-Web';shutil.rmtree(out.parent,ignore_errors=True);out.mkdir(parents=True)
    for name in FILES:shutil.copy2(ROOT/name,out/name)
    for name in DIRS:shutil.copytree(ROOT/name,out/name,ignore=shutil.ignore_patterns('__pycache__','*.pyc','test-results'))
    (out/'engine-core').mkdir()
    origin=ROOT/'engine-core'
    if not origin.exists():origin=ROOT.parent/'app/src/main/java/vn/duongngang/monitor'
    for name in ['Rules.java','WebSource.java','AlertOptions.java']:shutil.copy2(origin/name,out/'engine-core'/name)
    coretests=ROOT.parent/'tests'
    if (coretests/'RulesTest.java').is_file():
        (out/'tests/core').mkdir(exist_ok=True)
        for name in ['RulesTest.java','ParserTest.java','WebSourceReadTest.java','AlertOptionsTest.java']:shutil.copy2(coretests/name,out/'tests/core'/name)
    shutil.copy2(ROOT/'package.py',out/'package.py')
    spec=ROOT.parent/'handoff-web/BAN_GIAO_DAY_DU_VIET_LAI_WEB_0.27.3.txt'
    if spec.is_file():shutil.copy2(spec,out/'docs'/spec.name)
    reference=out/'reference/android'
    if (ROOT.parent/'app/src').is_dir():
        for name in ['app/src','tests']:
            shutil.copytree(ROOT.parent/name,reference/name,ignore=shutil.ignore_patterns('__pycache__','*.pyc'))
        for name in ['AGENTS.md','README.md','build.gradle','settings.gradle','app/build.gradle']:
            target=reference/name;target.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(ROOT.parent/name,target)
        for name in ['docs','scripts']:
            for source in (ROOT.parent/name).rglob('*'):
                if source.is_file() and source.suffix in ('.md','.txt','.sh','.py'):
                    target=reference/source.relative_to(ROOT.parent);target.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(source,target)
    elif (ROOT/'reference/android').is_dir():
        shutil.copytree(ROOT/'reference/android',reference)
    provenance={'version':'1.2.0','web_base_revision':'78d7988a7d670a30f02773477915736fa95e910f','android_reference_revision':'ee1affe5ea024590ce356e0f1e23aafbf17df669','web_rules':'engine-core/Rules.java is a deliberate variant; Android reference is unchanged','revision_note':'SOURCE_SHA256.json identifies the exact exported files including uncommitted release metadata; archive commit appears on GitHub'}
    if (ROOT.parent/'.git').is_dir():provenance['export_parent_revision']=subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT.parent,text=True).strip()
    (out/'SOURCE_REVISION.json').write_text(json.dumps(provenance,ensure_ascii=False,indent=2)+'\n')
    if spec.is_file():
        target=out/'docs'/spec.name
        target.write_text('> LƯU Ý 1.2.0: đọc LATEST_REQUIREMENTS_1.2.0.md trước. Mốc web hiện hành 10/10/50 giây; bỏ dẫn xuất ±1/±2; không audio liên tục. Phần trái với yêu cầu mới trong tài liệu lịch sử này đã bị thay thế.\n\n'+target.read_text())
    sums={str(p.relative_to(out)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(out.rglob('*')) if p.is_file()}
    (out/'SOURCE_SHA256.json').write_text(json.dumps(sums,indent=2)+'\n')
    target=ROOT/'releases/DuongNgang-Web-1.2.0-source.zip';target.parent.mkdir(exist_ok=True)
    with zipfile.ZipFile(target,'w',zipfile.ZIP_DEFLATED) as z:
        for p in sorted(out.rglob('*')):
            if p.is_file():z.write(p,Path('DuongNgang-Web')/p.relative_to(out))
    (target.parent/(target.name+'.sha256')).write_text(hashlib.sha256(target.read_bytes()).hexdigest()+'  '+target.name+'\n')
    print('Exported',target.name,len(sums),'source files')
    return out
if __name__=='__main__':bundle()
