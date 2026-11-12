#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p .deps build/classes
fetch_jar() {
  local rail_url="$1" rail_file="$2" rail_sha="$3"
  if [[ ! -f ".deps/$rail_file" ]]; then
    curl --fail --location --retry 2 "$rail_url" -o ".deps/$rail_file.tmp"
    mv ".deps/$rail_file.tmp" ".deps/$rail_file"
  fi
  python3 - "$rail_file" "$rail_sha" <<'PY'
from pathlib import Path
import hashlib,sys
p=Path('.deps')/sys.argv[1]
if hashlib.sha256(p.read_bytes()).hexdigest()!=sys.argv[2]:raise SystemExit('Dependency checksum mismatch: '+p.name)
PY
}
fetch_jar https://repo.maven.apache.org/maven2/org/jsoup/jsoup/1.18.3/jsoup-1.18.3.jar jsoup.jar 5be1ccd3228ae5fd6eed1bd6d827bac2bc65b91c20e9957d16ea65f739f15302
fetch_jar https://repo.maven.apache.org/maven2/org/json/json/20240303/json-20240303.jar json.jar 3cf6cd6892e32e2b4c1c39e0f52f5248a2f5b37646fdfbb79a66b46b618414ed
rail_core=engine-core
if [[ ! -d "$rail_core" ]]; then rail_core=../app/src/main/java/vn/duongngang/monitor; fi
javac -encoding UTF-8 -cp '.deps/*' -d build/classes "$rail_core/Rules.java" "$rail_core/WebSource.java" "$rail_core/AlertOptions.java" engine/Engine.java
