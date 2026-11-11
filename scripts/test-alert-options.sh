#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/env.sh
mkdir -p build/alert-tests
javac -encoding UTF-8 -d build/alert-tests app/src/main/java/vn/duongngang/monitor/Rules.java app/src/main/java/vn/duongngang/monitor/AlertOptions.java tests/AlertOptionsTest.java
java -cp build/alert-tests AlertOptionsTest
