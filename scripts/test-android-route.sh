#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/env.sh
# Start an emulator first. Tests use separate device-protected preferences/database.
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
rail_android_checks=$(adb shell am instrument -w vn.duongngang.monitor.test/vn.duongngang.monitor.RouteStoreChecks)
printf '%s\n' "$rail_android_checks"
[[ "$rail_android_checks" == *'62 account storage checks passed'* ]]
