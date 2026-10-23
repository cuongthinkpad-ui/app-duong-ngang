# Continuing this project

Read `docs/BAN_GIAO.md`, `docs/LICH_SU_TRAO_DOI.md` and `README.md` before editing. They contain the owner's agreed requirements, the current app version, build commands and remaining verification. Historical review documents describe earlier behavior; the latest user instructions take precedence.

- This is a Vietnamese native Android railway crossing monitoring app. Preserve the exact website fault text and equipment identities.
- Keep the full account route when filtering monitoring scope. Stations are points with parenthesized suffixes.
- Refresh data within existing overview views; do not replace the whole screen on each poll.
- The current fault panel resets at Vietnamese midnight; preserve the archive and train occupation.
- Do not put website credentials, authenticated captures or private signing keys in Git.
- The APK signing key is not in this repo. Read the signing section in `docs/BAN_GIAO.md` before preparing an install-over update in a new environment.
- Record material changes, validation and remaining limitations in the handoff/review documents. Do not present emulator checks as physical Xiaomi validation.

- Exclude app-derived axle notes/statistics/alerts at Km 280+500, 307+507, 309+056, 320+445, 338+900 and 343+350, including cached old alerts. These are the owner's explicit exclusions; 320+445 has more than six sensors and its own counting mechanism. Preserve original website faults, train tracking and other features at all six points.
