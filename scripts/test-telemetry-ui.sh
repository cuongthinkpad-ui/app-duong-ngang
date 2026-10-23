#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/env.sh
# Dedicated emulator only; all readings and accounts in this test are synthetic.
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
rail_telemetry_checks=$(adb shell am instrument -w -e mode telemetry vn.duongngang.monitor.test/vn.duongngang.monitor.RouteStoreChecks)
printf '%s\n' "$rail_telemetry_checks"
[[ "$rail_telemetry_checks" == *'20 telemetry UI checks passed'* ]]
