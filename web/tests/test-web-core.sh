#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
bash build-engine.sh
mkdir -p build/web-tests
javac -encoding UTF-8 -cp 'build/classes:.deps/*' -d build/web-tests tests/WebAxleTest.java
java -cp 'build/classes:build/web-tests:.deps/*' WebAxleTest
