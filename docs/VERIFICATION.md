# S1 verification — 2026-10-25

S1 implemented with ten distinct puzzle records per game, 50 total. This is a
playable development baseline, not full MVP acceptance. No physical phone/wife
acceptance has been performed. Git/source control is not player-data backup.

## Actual successful checks

- `:app:testDebugUnitTest`: 54 tests, zero failures/errors/skips. Storage/date 10;
  Wordish 10; Connektions 9; Bee 9; Strands 8; crossword 8.
- `:app:assembleDebug`: successful installable debug APK, API26 minimum/target36.
- `:app:lintDebug`: zero errors, one OldTargetApi warning because target36 is
  below the newest SDK known to lint. No lint baseline/suppression used to hide
  errors. Target-version upgrade/physical-device coverage belongs before release.
- `:app:connectedDebugAndroidTest`: all three tests pass on the installed
  Medium_Phone_API_37.0 emulator (Android17/API37), read-only/headless with Wi-Fi
  and mobile data disabled before app testing.
- Android test 1 validates all 50 records, completes each through actual game
  rule actions, saves/reloads intermediate state to real Android app-private
  files, verifies all completion results and frozen content snapshots.
- Android test 2 checks the merged package has no Internet permission or dangerous
  runtime permission, and verifies the dataset public-domain notice is packaged.
- Android test 3 opens each actual game board via Compose accessibility actions,
  reads/dismisses Help, recreates Activity on each game, navigates Back, starts
  independent Wordish extra play, types a letter, verifies it on disk and
  recreates the Activity. It does not play all 50 puzzles through touch input.
- Generated Wordish/Bee/Strands content reproduced with matching hashes during
  authoring. Wordish source has 8,636 five-letter guesses; all ten chosen answers
  are dictionary accepted. Bee's ten goal sets all validate against ENABLE.
- `aapt dump permissions` independently reports only AndroidX's app-local
  signature-protected receiver permission; no Internet or dangerous permissions.
- `apksigner verify --print-certs` succeeds with the Android Debug certificate.
  This is a debug install artifact, not production release signing.

Artifact: `artifacts/CLEAN-Words-S1-debug.apk`, 12,186,858 bytes.
SHA-256: `c9bce9697708782ebb7bb06be3ca642855b6c80d77b9ba417c44e8471d3b50b6`.
The artifact folder is ignored by Git; source/build instructions are versioned.

Successful Windows build environment, process-local JAVA_HOME only:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --offline :app:testDebugUnitTest :app:assembleDebug
.\gradlew.bat --offline :app:lintDebug :app:connectedDebugAndroidTest
```

Offline flags use this machine's already populated dependency cache; a new
development machine may need build dependency downloads. Runtime offline behavior
does not depend on those tools. No personal/global settings were altered.

## What the tests protect

Duplicate-letter feedback and rejected guesses; distinct mistake budgets and
duplicate wrong submissions; Bee repeat scoring, goal/bonus/target/pangrams;
Strands adjacency/no reuse/overlap/spanning/full coverage/undo; crossword crossing
edits, wrong fills, Check non-deletion and completion locking. Ten content
records per game have structural validation and engine acceptance examples.

Shared tests protect 05:59:59/06:00:00 local rollover including a DST example,
stable daily identity and independent extra IDs, late completion original date,
single completion timestamp/result, Unicode state/content snapshots, checksums,
truncation, atomic save/last-good recovery, failed-write preservation and blocked
unsupported future versions. Game snapshots are checked before being rendered.

## Failures found and addressed

- Initial assembly waited for a concurrently authored Bee module; no placeholder
  module was substituted to get a green build.
- Lint caught SDK path escaping; corrected ignored local.properties. Explicit
  Android12+ backup exclusions and a platform placeholder icon were added.
- Read-only review caught future-format downgrade fallback, saved-snapshot
  validation, pending-memory rotation retention and defensive game matching;
  addressed. Unknown future save schemas block instead of reverting/resetting.
- Cold emulator displayed a System UI stall dialog. Woke/dismissed the temporary
  emulator state; no app exception was observed. UI helper was corrected to wait
  for real boards and distinguish keyboard buttons from matching board letters.
  Final three-test Android run passed; earlier failures are not concealed.

## Not established / next human acceptance

Use MANUAL-ACCEPTANCE on the actual phone. Still unverified: subjective fairness,
medium-plus difficulty, comfortable complete touch/keyboard solving, larger text,
screen readers, older Android/device coverage and actual long-lived bedtime use.
The daily calendar's end-to-end visual rollover and process-kill/OS-reclaim flows
need manual review; date/state/file invariants and Activity recreation are tested.
Not tested as guarantees: power-loss/filesystem failure on hardware, device-loss,
uninstall/reinstall recovery, export/import or future migrations.

Full snapshot encoding/fsync runs synchronously for the small starter library;
profile input latency/history growth before scaling content or thousands of
attempts. A future asynchronous persistence queue needs durable ordering/failure
and background-interruption tests, not an unreviewed performance refactor.

S1 does not implement variants/six-board Wordish, selectable difficulty, calibrated
3.5/5 dailies, abundant unique puzzles, streaks or final polish. Documentation of
those tasks does not close R-005/R-011/R-014 or authorize future slices.
