#!/usr/bin/env python3
"""Whitelist-only standalone source export. Never reads signing keys or runtime data."""
import hashlib,json,shutil,zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parent
FILES=['server.py','database.py','audio.py','requirements.txt','requirements.lock','requirements-dev.txt','build-engine.sh','Dockerfile','.dockerignore','.gitignore','package.json','README.md','AI_STUDIO_HANDOFF.md']
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
    sums={str(p.relative_to(out)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(out.rglob('*')) if p.is_file()}
    (out/'SOURCE_SHA256.json').write_text(json.dumps(sums,indent=2)+'\n')
    target=ROOT/'releases/DuongNgang-Web-1.0.0-source.zip';target.parent.mkdir(exist_ok=True)
    with zipfile.ZipFile(target,'w',zipfile.ZIP_DEFLATED) as z:
        for p in sorted(out.rglob('*')):
            if p.is_file():z.write(p,Path('DuongNgang-Web')/p.relative_to(out))
    (target.parent/(target.name+'.sha256')).write_text(hashlib.sha256(target.read_bytes()).hexdigest()+'  '+target.name+'\n')
    print('Exported',target.name,len(sums),'source files')
    return out
if __name__=='__main__':bundle()
