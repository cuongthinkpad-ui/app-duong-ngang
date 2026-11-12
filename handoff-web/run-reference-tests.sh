#!/usr/bin/env bash
# Run the original, unchanged Java reference tests in the extracted handoff ZIP.
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p reference/.deps reference/build
rail_jar=reference/.deps/jsoup-1.18.3.jar
if [[ ! -f "$rail_jar" ]]; then
  curl --fail --location --retry 2 https://repo.maven.apache.org/maven2/org/jsoup/jsoup/1.18.3/jsoup-1.18.3.jar -o "$rail_jar"
fi
python3 - "$rail_jar" <<'PY'
import hashlib,sys
from pathlib import Path
assert hashlib.sha256(Path(sys.argv[1]).read_bytes()).hexdigest()=='5be1ccd3228ae5fd6eed1bd6d827bac2bc65b91c20e9957d16ea65f739f15302','Unexpected jsoup checksum'
PY
rail_src=reference/android/java/vn/duongngang/monitor
javac -encoding UTF-8 -cp "$rail_jar" -d reference/build \
  "$rail_src/Rules.java" "$rail_src/WebSource.java" "$rail_src/AlertOptions.java" \
  reference/tests/RulesTest.java reference/tests/ParserTest.java \
  reference/tests/WebSourceReadTest.java reference/tests/AlertOptionsTest.java
for rail_class in RulesTest ParserTest WebSourceReadTest AlertOptionsTest; do
  java -cp "reference/build:$rail_jar" "$rail_class"
done
