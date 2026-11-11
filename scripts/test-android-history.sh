#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/env.sh
# Select a dedicated emulator with ANDROID_SERIAL when more than one device exists.
rail_serial=$(adb get-serialno)
if [[ "$rail_serial" != emulator-* ]]; then
  echo 'Historical UI checks require a dedicated emulator.' >&2
  exit 2
fi
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
rail_history_checks=$(adb shell am instrument -w -e mode history vn.duongngang.monitor.test/vn.duongngang.monitor.RouteStoreChecks)
printf '%s\n' "$rail_history_checks"
[[ "$rail_history_checks" == *'24 historical UI checks passed'* ]]
