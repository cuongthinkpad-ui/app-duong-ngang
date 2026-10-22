#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/env.sh
proxy_options=()
if [[ -n "${HTTPS_PROXY:-}" ]]; then
  read -r rail_proxy_host rail_proxy_port < <(node -e 'const u=new URL(process.env.HTTPS_PROXY); console.log(u.hostname,u.port || 80)')
  proxy_options+=("-Dhttps.proxyHost=$rail_proxy_host" "-Dhttps.proxyPort=$rail_proxy_port" "-Dhttp.proxyHost=$rail_proxy_host" "-Dhttp.proxyPort=$rail_proxy_port")
fi
if [[ -f /etc/ssl/certs/java/cacerts ]]; then
  proxy_options+=("-Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts")
fi
gradle --no-daemon --max-workers=2 "${proxy_options[@]}" "${@:-assembleDebug}"
