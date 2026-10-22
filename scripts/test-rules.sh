#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build/rules-tests
javac -encoding UTF-8 -d build/rules-tests app/src/main/java/vn/duongngang/monitor/Rules.java tests/RulesTest.java
java -cp build/rules-tests RulesTest
