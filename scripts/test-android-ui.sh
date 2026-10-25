#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/env.sh
# Dedicated emulator only; UI checks use separate device-protected data.
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
rail_ui_checks=$(adb shell am instrument -w -e mode ui vn.duongngang.monitor.test/vn.duongngang.monitor.RouteStoreChecks)
printf '%s\n' "$rail_ui_checks"
[[ "$rail_ui_checks" == *'57 UI refresh checks passed'* ]]
