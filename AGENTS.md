# Continuing this project

Read `docs/BAN_GIAO.md`, `docs/LICH_SU_TRAO_DOI.md` and `README.md` before editing. They contain the owner's agreed requirements, the current app version, build commands and remaining verification. Historical review documents describe earlier behavior; the latest user instructions take precedence.

- This is a Vietnamese native Android railway crossing monitoring app. Preserve the exact website fault text and equipment identities.
- Keep the full account route when filtering monitoring scope. Stations are points with parenthesized suffixes. The owner confirmed a new station absent from the website: add Km 303+350 (Nghi Long) only for account ID Chosy. Apply this supplement after validating the complete original website inventory, and to cached routes on upgrade; never create station report events. Prefer the website's own point if it later publishes the same chainage.
- Refresh data within existing overview views; do not replace the whole screen on each poll.
- Display the Nghi Long station at Km 303+350 as **Ga NL** (owner's explicit request after 0.17). Keep its canonical route/cache key unchanged; this is a display-only abbreviation.
- The current fault panel resets at Vietnamese midnight; preserve the archive and train occupation.
- Diagram revision approved on 26/10/2026: pair successful barrier activity at the next crossing with an existing incoming train; isolated maintenance never creates a train. Confirmed passage/restoration reconciles older missing visits only on the same directed interval, preserves later following trains and other sections, and never clears original equipment/network faults. Keep continuous occupation without a timeout. `travelVersion=4` rebuilds old checkpoint/diagram caches from raw on upgrade.
- Do not put website credentials, authenticated captures or private signing keys in Git.
- The APK signing key is not in this repo. Read the signing section in `docs/BAN_GIAO.md` before preparing an install-over update in a new environment.
- Record material changes, validation and remaining limitations in the handoff/review documents. Do not present emulator checks as physical Xiaomi validation.

- Exclude app-derived axle notes/statistics/alerts at Km 280+500, 307+507, 309+056, 320+445, 338+900 and 343+350, including cached old alerts. These are the owner's explicit exclusions; 320+445 has more than six sensors and its own counting mechanism. Preserve original website faults, train tracking and other features at all six points.
- Self-trigger evidence includes completed numbered barrier lowering/raising and same-direction train passage at the upstream crossing, grouped into one visit and used once. Keep the 30-second-only setting and station/end boundaries. Supplemental evidence can disprove an inferred alert; it never clears original red equipment faults.
- Owner preference, 24/10/2026: during requirement discussions, restate the intended behavior and wait for their confirmation before editing the app. On 26/10 the owner confirmed the four-part diagram repair, including recovery from a wholly missing visit when the next train is confirmed. Implementation, verification and normal release are authorized; do not request the same approval again.
