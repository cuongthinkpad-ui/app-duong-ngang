#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
bash build-engine.sh
rail_tests=tests/core
if [[ ! -d "$rail_tests" ]]; then rail_tests=../tests; fi
mkdir -p build/tests build/reference
rail_reference=reference/android/app/src/main/java/vn/duongngang/monitor
if [[ ! -d "$rail_reference" ]]; then rail_reference=../app/src/main/java/vn/duongngang/monitor; fi
javac -encoding UTF-8 -cp '.deps/*' -d build/reference "$rail_reference/Rules.java" "$rail_reference/WebSource.java" "$rail_reference/AlertOptions.java"
javac -encoding UTF-8 -cp 'build/reference:.deps/*' -d build/tests "$rail_tests/RulesTest.java" "$rail_tests/ParserTest.java" "$rail_tests/WebSourceReadTest.java" "$rail_tests/AlertOptionsTest.java"
for rail_test in RulesTest ParserTest WebSourceReadTest AlertOptionsTest; do java -cp 'build/reference:build/tests:.deps/*' "$rail_test"; done
