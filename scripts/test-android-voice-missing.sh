#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/env.sh
rail_voice_serial=$(adb get-serialno)
rail_test_engine=$(adb shell settings get secure tts_default_synth | tr -d '\r')
if [[ "$rail_voice_serial" != emulator-* || "$rail_test_engine" != com.reecedunn.espeak ]]; then
  echo 'Missing-voice checks require the dedicated emulator and disposable eSpeak test engine.' >&2
  exit 2
fi
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
restore_voice_engine() {
  adb shell pm enable com.reecedunn.espeak >/dev/null
  adb shell settings put secure tts_default_synth "$rail_test_engine"
}
trap restore_voice_engine EXIT
adb shell pm disable-user --user 0 com.reecedunn.espeak >/dev/null
rail_missing_checks=$(adb shell am instrument -w -e mode voice-missing vn.duongngang.monitor.test/vn.duongngang.monitor.RouteStoreChecks)
printf '%s\n' "$rail_missing_checks"
[[ "$rail_missing_checks" == *'4 unavailable-voice checks passed'* ]]
