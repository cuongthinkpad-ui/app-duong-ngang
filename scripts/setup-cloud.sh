#!/usr/bin/env bash
set -euo pipefail
# Retained toolchains are reused; installation never alters application sources.
rail_tools=/workspace/toolchains
mkdir -p "$rail_tools"
rail_tmp=$(mktemp -d /tmp/rail-setup.XXXXXX)
trap 'rm -rf "$rail_tmp"' EXIT
if [[ ! -x "$rail_tools/jdk-21.0.8/bin/javac" ]]; then
  curl -fsSL https://download.oracle.com/java/21/archive/jdk-21.0.8_linux-x64_bin.tar.gz -o "$rail_tmp/jdk.tar.gz"
  curl -fsSL https://download.oracle.com/java/21/archive/jdk-21.0.8_linux-x64_bin.tar.gz.sha256 -o "$rail_tmp/jdk.sha256"
  python - "$rail_tmp" <<'PY'
import sys,pathlib,hashlib
p=pathlib.Path(sys.argv[1]);assert hashlib.sha256((p/'jdk.tar.gz').read_bytes()).hexdigest()==(p/'jdk.sha256').read_text().split()[0]
PY
  tar -xzf "$rail_tmp/jdk.tar.gz" -C "$rail_tools"
fi
if [[ ! -x "$rail_tools/gradle-8.9/bin/gradle" ]]; then
  curl -fsSL https://downloads.gradle.org/distributions/gradle-8.9-bin.zip -o "$rail_tmp/gradle.zip"
  curl -fsSL https://downloads.gradle.org/distributions/gradle-8.9-bin.zip.sha256 -o "$rail_tmp/gradle.sha256"
  python - "$rail_tmp" <<'PY'
import sys,pathlib,hashlib
p=pathlib.Path(sys.argv[1]);assert hashlib.sha256((p/'gradle.zip').read_bytes()).hexdigest()==(p/'gradle.sha256').read_text().strip()
PY
  unzip -q "$rail_tmp/gradle.zip" -d "$rail_tools"
fi
if [[ ! -x "$rail_tools/android-sdk/cmdline-tools/latest/bin/sdkmanager" ]]; then
  curl -fsSL https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -o "$rail_tmp/cli.zip"
  curl -fsSL https://dl.google.com/android/repository/repository2-1.xml -o "$rail_tmp/repository.xml"
  python - "$rail_tmp" <<'PY'
import sys,pathlib,hashlib,xml.etree.ElementTree as E
p=pathlib.Path(sys.argv[1]);found=False
for a in E.parse(p/'repository.xml').getroot().iter('archive'):
 c=a.find('complete')
 if c is not None and c.findtext('url')=='commandlinetools-linux-11076708_latest.zip':
  x=c.find('checksum');assert hashlib.new(x.attrib.get('type','sha1'),(p/'cli.zip').read_bytes()).hexdigest()==x.text;found=True
assert found
PY
  unzip -q "$rail_tmp/cli.zip" -d "$rail_tmp/unpack"
  mkdir -p "$rail_tools/android-sdk/cmdline-tools"
  mv "$rail_tmp/unpack/cmdline-tools" "$rail_tools/android-sdk/cmdline-tools/latest"
fi
cd /workspace/app-duong-ngang
source scripts/env.sh
mkdir -p "$ANDROID_USER_HOME"
# SDK terms are accepted for this user-requested Android build environment.
set +o pipefail
yes | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --sdk_root="$ANDROID_HOME" --licenses > "$rail_tmp/licenses.log" 2>&1
rail_license_status=${PIPESTATUS[1]}
set -o pipefail
if [[ "$rail_license_status" != 0 ]]; then cat "$rail_tmp/licenses.log"; exit "$rail_license_status"; fi
"$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --sdk_root="$ANDROID_HOME" 'platforms;android-35' 'build-tools;34.0.0' 'build-tools;35.0.0' 'platform-tools'
./scripts/test-rules.sh
./scripts/build-cloud.sh assembleDebug lintDebug
