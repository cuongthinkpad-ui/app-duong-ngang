#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/env.sh
rail_voice_serial=$(adb get-serialno)
if [[ "$rail_voice_serial" != emulator-* ]]; then
  echo 'Voice checks require a dedicated emulator with a Vietnamese TTS engine.' >&2
  exit 2
fi
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
rail_voice_checks=$(adb shell am instrument -w -e mode voice vn.duongngang.monitor.test/vn.duongngang.monitor.RouteStoreChecks)
printf '%s\n' "$rail_voice_checks"
[[ "$rail_voice_checks" =~ [1-9][0-9]*\ Vietnamese\ voice\ checks\ passed ]]
