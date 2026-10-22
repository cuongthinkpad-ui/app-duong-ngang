#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/env.sh
rail_jsoup=$(rg --files "$GRADLE_USER_HOME/caches/modules-2/files-2.1/org.jsoup/jsoup/1.18.3" -g '*.jar' | head -n 1)
if [[ -z "$rail_jsoup" ]]; then echo 'Build first to resolve the pinned jsoup dependency.' >&2; exit 1; fi
mkdir -p build/parser-tests
javac -encoding UTF-8 -cp "$rail_jsoup" -d build/parser-tests app/src/main/java/vn/duongngang/monitor/Rules.java app/src/main/java/vn/duongngang/monitor/WebSource.java tests/ParserTest.java tests/WebSourceReadTest.java
java -cp "build/parser-tests:$rail_jsoup" ParserTest
java -cp "build/parser-tests:$rail_jsoup" WebSourceReadTest
