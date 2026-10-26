## Crossita X1 verification — 2026-10-26

D-028 authorizes larger crossword coding/integration with autonomous assumptions.
New daily/extra 7×7, 16 layouts, 10–12 clues; original 394 answers/788 clues. Source/hash/
limits/actual reuse and qualification: generators/crossword/REPORT.md. No new
SDK/dependency/permission/network/service/global setting or save schema.

- Final full JVM suite: 182 tests, 0 failures/errors. Includes dimension numbering,
  row/down boundaries, high-index (48) checks/corrections, paste/deletion, mixed legacy/new
  codec/daily/result retention; generator reproduction/source/sense/cancellation/
  impossible-bank/cooldown/search-budget checks; source-pin equality and adapters.
- Source preparation --check and independent JS fill smoke pass on 32 masks. Three
  100-seed runs: 5×5 81 fills, 7×7 90 fills, rolling-history 7×7 100 distinct fills, 0 failures.
  Fifteen layouts sampled per size. Report includes partial-fill rejection counts,
  answer/clue reuse and host timing. Exact source JSON SHA256:
  5d0771002642a4fcd841da1e1c6ef415b6e2469fb9e8cc9e3e5f31d2a568d690.
  Typed hash 1a93c3ac57d2db83686e7f79f35577edeb09b36007cb237335dd55ec3ae95aad.
- Independent AI reviewer read all 394 pairs, corrected AGO/ACORN/ALBUM wording;
  zero human editorial reviews. Some alternate clues paraphrase the same fact,
  four-letter coverage is narrow, and no 6/7-letter answer bank is included.
- Offline assembleDebug/assembleDebugAndroidTest and final lint pass, 0 errors,
  1 existing SDK warning. Final full JVM/lint rerun also passes after test refinements.
- Explicit project-local emulator-5556 booted with existing code 5 app, Wi-Fi/data
  disabled, update installed with install -r. No uninstall/storage clear/phone use.
- Initial full Android run: 14/15 passed; larger crossword focus scrolled generation
  status label out of view in allFourGeneratedGames... assertion. Test now scrolls
  to the label just as its recreation assertion already did. No game rule/input
  or quality restriction changed. Initial log retained: artifacts/crossword-device-test.txt. Focused four-game UI flow passes 1/1 (54.420 seconds); phone input screenshot
  smoke 1/1 (14.913 seconds). Final complete Android suite **15/15 passes in 157.662 seconds**:
  artifacts/crossword-final-device-test.txt. No failing test was skipped.
- Android covers all four generated daily/extra UI starts and frozen recreation;
  larger crossword actual rapid phone events/paste/DEL then built-in Q; legacy 5×5 daily
  and completed history coexistence; high-index crossing/Check edits durable across
  restart; full generated completion saved with unchanged snapshot; existing save-
  failure/cancel/rollover/credits/permission/all 50 starter regressions.
- artifacts/crossword-generated-board.png and crossword-phone.png inspected. Full
  7×7 board, clue and ordered controls fit with puzzle keyboard; smaller board,
  active clue and edit controls remain above IME. Actual phone/large text remains.
- APK asset bytes independently match pinned source JSON. apksigner verification
  succeeds; certificate SHA256 5d01ad4ef91eff366eab754f3853d8460b26007bce1a80a5416a0b5d7ed20916
  equals prior update signer. Delivered and tested-built APK hashes match.

Commands used process-local JAVA_HOME=C:\Program Files\Android\Android Studio\jbr:

```powershell
.\gradlew.bat --offline :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
node tools/generation/crossword/prepare-bank.mjs --check
node tools/generation/crossword/check-fill.mjs
# Explicit offline emulator only; install -r both app/test APKs, then:
adb -s emulator-5556 shell am instrument -w dev.clean.words.test/androidx.test.runner.AndroidJUnitRunner
```

APK: artifacts/CLEAN-Words-crossword-debug.apk, 0.6.0-crossword/versionCode 6,
12,772,546 bytes; SHA256: 1dafbcaf847ed56fa8372f80c539b7bf15581bfb2bc877e2b1924669b7849130.
Install as update, preserve all player data. Four games generated; Stranded still
uses starters. No public release. Human accuracy/fairness/familiarity/desired
challenge, physical-phone/layout/large-text/motion/keyboard and actual overnight/
clock-zone acceptance remain open. Separate clean-device first install was not
rerun; fresh isolated test libraries and code 5 update passed. Existing incomplete-
rollback/process-death and device-loss limitations remain. Manual batch: X1.

## Connektions C2 verification — 2026-10-26

Owner authorized remaining slices and Connections-only integration (D-027).
Pure decoy-first composer, compiled pinned bank, worker adapter, semantic recent
exclusions, credits and version 0.5.0-connections/code 5 are implemented. No new
dependency/permission/service/network/global setting or storage schema.

- 171 JVM tests pass, zero failures/errors. Includes 15 focused composer tests,
  runtime Node/Kotlin source-pin equality, legacy daily retention, full snapshots,
  cancellation, budget/exhaustion distinction, exact-three decoy enforcement and
  independent competing-partition checks.
- Real bank: 200 independent seeds -> 198 semantic boards; 200 rolling latest-100
  starts -> 200 boards; zero failures. Every successful result is independently
  checked against all recorded quartets/partitions. Nine of 20 categories and
  four decoy pairs appear; zero human reviews. Full timing/reuse/rejections and
  source pins: generators/connections/REPORT.md and tools/generation/connections/
  qualification.txt. Mechanical variety does not establish editorial variety.
- Node pilot validator/self-tests and typed bank --check pass. Converter excludes
  authored board fixtures; test alias and runtime use the same actual bank.
- Offline assembleDebug/assembleDebugAndroidTest and final lint pass (0 errors,
  one prior target SDK warning).
- Update installed on explicit offline project-local emulator-5556 from code 4,
  using install -r, no uninstall or storage clear. New empty test-library sessions
  cover generated daily/extra and restart. Existing saved main-app history remains.
- Initial Android run had four UI timeouts behind an observed Pixel Launcher ANR.
  After dismissing the emulator dialog, test-harness issues were corrected: wrong
  guesses retain selection under existing rules, and recreated accessibility nodes
  require fresh cache/visible targeting with a real-touch fallback. No game rule
  or production input behavior was changed to satisfy a test. Earlier failed logs
  remain in artifacts/connections-device-test.txt, connections-device-retry.txt,
  and connections-focused-device-test*.txt.
- Focused all-three-generated UI flow passes 1/1 (48.561 seconds), including
  Connections wrong/repeated guesses, Clear, solved group persistence, recreation
  and frozen content. Final full Android suite passes **14/14 in 175.357 seconds**
  on offline emulator-5556, including the updated all-three UI test, existing
  Bee opening tests, all fifty starter rule/save checks, permissions/credits,
  legacy Connections daily/save-retry tests and crossword rapid-input regression.
  Final log: artifacts/connections-final-device-test.txt. No failures were skipped.
- Generated board screenshot artifacts/connections-generated-board.png inspected:
  solved-group explanation, remaining tile text, controls and saved-state label
  fit the emulator viewport. Actual phone/large-text review remains open.

APK: artifacts/CLEAN-Words-connections-debug.apk, versionCode 5.
SHA256 69cb6d6399fd1ac803ebfb4f83ada3cf75cc67c7e92177cf4f5e14d897e29f9c.
Signing certificate SHA256 5d01ad4ef91eff366eab754f3853d8460b26007bce1a80a5416a0b5d7ed20916,
identical to prior Bee-opening APK. Install as an update, never clear player data.
Delivered APK SHA256 equals the installed built APK; production source did not
change during test-helper repairs. Project emulator was shut down after checks.

Unverified: human ambiguity/fairness/desired difficulty, category breadth and actual
phone/text-size/input/motion review; real overnight/time-zone interruption and
process death during incomplete save rollback. C2 uses upgrade installation and
new empty per-test libraries; separate clean-device first installation was not
rerun. Stranded/Crossita remain honest ten-starter cycles, not generated games.
No all-five conversion, public release or reviewed-content claim. Manual batch:
MANUAL-ACCEPTANCE C2. Older verification sections below retain their own scope.

## Connections slice 1 verification — 2026-10-26

Development-only tooling: `node tools/generation/connections/author-pilot.mjs`
and `node tools/generation/connections/validate.mjs --self-test` pass. Eight checks:
valid pilot, duplicate tile rejection, missing sense rejection, duplicate decoy
rejection, insufficient decoy rejection, missing overlap rejection, semantic
signature invariance to group/member ordering, competing-full-partition rejection.
Report: tools/generation/connections/validation-report.json. 20 intended categories
(121 member records), five decoy relations (30 members), two board examples,
zero validation errors. Household fixture: four candidate quartets/one partition;
compound fixture: eight candidate quartets/one partition. Both have two distinct
three-member cross-group decoys. Source hash is pinned in the report.

Bounded read-only agent independently inspected source/staging/snapshot mapping
and reviewed examples. Original AI draft content only; zero human/frequency
reviews. This proves uniqueness only under recorded relationships, not all human
interpretations. Twelve categories have no complete fixture yet. Wider five-board
pilot/120-category targets are unmet. No generator, 200-seed report, runtime
integration, app build, APK, emulator or phone verification belongs to slice 1.
Prior Bee and other checks below describe their own earlier implementation.

# Bee opening B1 verification — 2026-10-26

Owner approved activation with **no additional familiarity-ratio filter** (D-025).
New daily/extra starts use bee-opening10-v3 with original 98,144-word ENABLE index
and 3,636 familiar inputs. Optional 346-word supplement is not loaded. Base board
filters retain all 1,489 prior qualified set/center identities; no 60% floor or
accepted-vocabulary narrowing. Fairness remains provisional, not calibrated.

154 JVM tests pass, zero failures/errors. Includes module v3 rules/state/recovery,
nine shared milestone/persistence/legacy tests and a 1,000-seed active v3 run:
719 distinct set/center identities across 266 sets, zero failures, worst inspected
6, sampled goals 9–43. Full inventory still has goals 9–47. Independent raw ENABLE
filtering verifies unchanged acceptance; source and familiarity pins remain base.
Both milestone orders and simultaneous completion, immutable opening, sticky
goal time, codec order, invalid saved/proposed maps, stale edit revisions and
rewritten accepted prefixes are covered. Legacy v1/v2 and old daily wins remain.

Offline app/test builds and lint pass: zero errors, one existing OldTargetApi
warning. Three Bee Node --check pipelines/audits pass. Explicit emulator-5556
(workspace-local CLEAN_Generation_Fresh) update test, Wi-Fi/data disabled:
**13/13 Android tests pass in 143.351 seconds**. Personal phone was connected but
was not targeted. No data clearing/uninstall or global settings/dependencies.

Android coverage includes four new session/file tests: actual daily/extra v3,
ordered first-ten freeze and post-ten play, source-free restart, v2 daily retention,
goal-before-ten prior resume through restart, save-failure/retry with consecutive
input and malformed-state preservation. Actual UI test submits the board's best
ten to get 100%, spends no slots for invalid/duplicate guesses, recreates at eight,
continues to twelve without changing score, then reopens from history. Existing
daily/extra/calendar/recreation, 50 starters and rapid crossword tests also pass.

Commands (process-local Android Studio JBR, existing offline approved cache):

```powershell
.\gradlew.bat --offline :app:testDebugUnitTest :app:assembleDebug :app:lintDebug :app:assembleDebugAndroidTest
node tools/generation/bee/prepare-index.mjs --check
node tools/generation/bee/opening-benchmark.mjs --check
node tools/generation/bee/prepare-familiarity-supplement.mjs --check
.\tools\test-emulator.ps1 -Serial emulator-5556 -ReportPath artifacts/bee-opening-final-device-test.txt
```

Initial lint found indentation in the result panel; fixed, no baseline suppression.
First Android run failed 3/13: cold emulator System UI ANR and accessibility targets
after scrolling. UI-only retry failed 1/5 locating the offscreen Calendar action.
Added cold-System-UI recovery in the test, show-on-screen/re-fetched click targets,
and stable Enter-word/Calendar accessibility labels. Corrected a test-only Android
show-on-screen constant reference. Final full 13-test run passes; no storage reset
or weakened assertions. Screenshot review led to compact score-card text, with
the full formula still in Help; final exact-APK UI smoke recorded below.

Shared home/hub/history/calendar/result labels distinguish opening vs word goal;
old daily snapshots explain Another puzzle uses new scoring. No extra save schema,
numeric-score overload or recording timestamp. Completion timestamp stays goal-only.
Record order/state/errors remain frozen and invalid data is recovery-only.

Manual phone, larger text, screen reader, comfort of goals and cross-board score
fairness remain unverified. No frequency evidence or human-reviewed familiarity
claim. Prior incomplete/failed-cancellation rollback process-death limitation is
unchanged; see G1 below. No public app release.

## B1 final APK and smoke

artifacts/CLEAN-Words-bee-opening-debug.apk, 0.4.0-bee-opening/versionCode 4,
12,690,947 bytes. SHA-256:
`b7b955eb1bb0b7d41a22f2c8d22a5967c9becd7b570c1928560055c7bcc5978d`.
Final compact-card UI smoke passes 1/1 in 30.218 seconds on emulator-5556,
including best-ten 100%, invalid/duplicate handling, eight-word recreation,
post-ten continuation, history and reopening. Report:
artifacts/bee-opening-final-smoke.txt. Captured/inspected compact card screenshot
artifacts/bee-opening-recorded.png. Final assembleDebug/assembleDebugAndroidTest/
lintDebug pass; no errors/one existing target warning. Debug certificate remains
`5d01ad4ef91eff366eab754f3853d8460b26007bce1a80a5416a0b5d7ed20916`.
Install as update without clearing data. Old saved daily retains old rules; use
Another puzzle for immediate v3 play. Artifacts are ignored; source/tests/reports
are versioned. [B1 manual checklist](MANUAL-ACCEPTANCE.md) remains for the owner.

## Prior G1 verification — 2026-10-25

Current scope is **two generated games**, tWordle and Speling Bee, through real
daily/additional UI. Connektions/Stranded/Crossita still use labeled ten-record
starter cycles; no generator handoffs exist for them. The all-five generated
closure in INTEGRATION.md remains partial. Rules and saved v1/v2 content were
preserved; no new dependency, permission, SDK, runtime network or service.

## Current checks

114 JVM tests pass; offline assembleDebug, assembleDebugAndroidTest and lintDebug
pass (zero lint errors, one existing OldTargetApi warning for target 36).
Both preprocessing `--check` commands and git diff --check pass. The exact commands
use Android Studio's bundled JBR and existing approved caches:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --offline :app:testDebugUnitTest :app:assembleDebug :app:lintDebug :app:assembleDebugAndroidTest
node tools/generation/wordish/prepare-bank.mjs --check
node tools/generation/bee/prepare-index.mjs --check
.\tools\test-emulator.ps1 -Serial emulator-5554 -ReportPath artifacts/generation-upgrade-device-test.txt
.\tools\test-emulator.ps1 -Serial emulator-5556 -ReportPath artifacts/generation-first-install-device-test.txt
.\tools\test-emulator.ps1 -Serial emulator-5556 -ReportPath artifacts/generation-final-device-test.txt
```

Eight Android tests pass on emulator-5554 after an update install, Wi-Fi/data
disabled (64.115 seconds). Eight also pass on **first install**, emulator-5556
with project-local CLEAN_Generation_Fresh AVD, Wi-Fi/data disabled before app
installation (69.8 seconds). Confirmed no CLEAN Words package existed before
that install; no player data was cleared. No connected personal phone targeted.
Fresh AVD uses the installed API 37 image and process-local ANDROID_AVD_HOME;
no global/personal emulator configuration or new SDK downloads.
An initial second-instance launch of the existing AVD failed; creating the new
workspace-local AVD resolved that without changing or clearing existing data.

The Android suite checks all 50 legacy starters solved/saved, five real boards,
Help/recreation, permissions/credits and rapid crossword input; new tests exercise
real generated daily/extra UI, frozen snapshot recreation, independent extra
signatures, eight durable starts in a fresh session, duplicate-tap coalescing,
generation/index preparation off main, rollover cancellation, visible corrupt-
index failure/retry, save-failure baseline retention/retry and restart without
accessing source inputs. Session failure fixtures use isolated app cache folders.
Final strengthened UI suite: **8/8 pass, 67.246 seconds**, offline emulator-5556,
including daily/additional starts, generated typing, calendar reopening of the
same frozen daily and Activity recreation. Report:
artifacts/generation-final-device-test.txt. Captured/inspected
artifacts/generation-wordish.png and generation-bee.png: generated labels, full
Wordish board/keyboard/actions, Bee honeycomb/input/goal remain readable and usable
at this emulator size. This does not replace physical-phone/text-size acceptance.
Five current Markdown relative links resolve; whitespace checks pass. Historical
starter-reference copies were excluded from documentation link checking.
Final packaging normalized checksum-input line endings to Git-pinned LF and
regenerated source-report hashes; answer/index content hashes are unchanged.
Both source pipelines pass --check; packaged answer/index/policy/ENABLE notice
bytes match their source files. The exact delivered APK also passes the focused
generated daily/extra/typing/calendar/recreation UI test (1/1, 19.126 seconds),
after update installation offline; artifacts/generation-final-apk-smoke.txt.

Core checks cover queued-callback cancellation, newer request/date/source identity,
daily independence from extras, old daily wins, semantic signatures, exhausted and
tiny banks, unknown policies, failed start/rollback retry and actual last-good
recovery-copy cleanup after simulated primary corruption. Generation and saving
have separate workers; full generated content persists before opening.

## Content evaluation and limitations

[Wordish report](../app/src/main/java/dev/clean/words/generators/wordish/REPORT.md):
1,387 AI editorial answer candidates, zero human/frequency reviewed. Seeds 0–999
produce 731 distinct answers/17 repeated-letter patterns; zero failures; worst
scan 1,387; measured p50/p95/max 576/1,395/5,516 µs. Exclusion drain selects all 1,387
without repetition, then Exhausted. Existing enable-five-v1 guesses unchanged.

[Bee report](../app/src/main/java/dev/clean/words/generators/bee/REPORT.md):
98,144 indexed ENABLE words, 3,636 AI editorial familiarity candidates; inventory
1,489 qualifying set/center identities across 283 sets. Seeds 0–999 produce 719
identities/266 sets, zero failures/unattainable goals; worst inspected 6; measured
p50/p95/max 672/2,714/83,434 µs, cold preparation 917,690 µs. Trial accepted counts
min/p50/p95/max 37/173/351/397; familiar 12/21/38/61; goals 9/15/27/43. Every distinct
output independently matched exhaustive source filtering. Inventory goal range
9–47. Runtime rejection counts and every preprocessing rejection are reported.
Both banks are original AI editorial evidence with **zero human/frequency review**,
provisional difficulty and no calibrated medium-plus claim. Timings are desktop
measurements, not phone latency guarantees. All sources/notices operate offline.

New daily seed pins game/source/generator/profile and the local 6 a.m. date independently
of history; extra seeds use UUIDs and exclude semantic signatures from latest 100
starts. Daily dates can repeat content. No silent starter-as-generated fallback
or weakened quality thresholds. No new variants or difficulty selectors.

Normal canceled starts and successful retry purge current/recovery copies. A
process kill/power loss while rollback is pending/failed can leave staged content
on disk: keep open/retry errors; no durable launch journal or power-loss guarantee.
No physical-phone/wife fairness, goal-length comfort, screen-reader/larger-text,
overnight/clock-zone visual acceptance, hardware disk-failure or release acceptance.
Use the [manual checklist](MANUAL-ACCEPTANCE.md); [PRODUCT](PRODUCT.md) and
[contract](CONTRACT.md) define current scope. No app public release authorization.

## Current APK

artifacts/CLEAN-Words-generation-debug.apk, version 0.3.0-generation/versionCode 3,
12,653,692 bytes. SHA-256:
`1834f5e87f8da21e6930f09e5cc8efd9516dec3e50448df8bde540043a0eade1`.
apksigner verifies the same debug signer as the prior names APK, SHA-256 cert
`5d01ad4ef91eff366eab754f3853d8460b26007bce1a80a5416a0b5d7ed20916`.
Install as update without uninstalling/clearing data. Artifacts remain Git-ignored;
source/tests/tooling/content/required notices are versioned. Prior checkpoints follow.

# Prior verification — 2026-10-25

## Historical F1 feedback checkpoint

Artifact `artifacts/CLEAN-Words-F1-debug.apk`, versionCode 2/version 0.2.0-f1.
SHA256 `8d09b66e35c5232d43c135e23c134a978ab8befd81d9d3de2101fbe34a616eb7`.
Source scope: FEEDBACK-01 (R-017–R-023). Full offline MVP still incomplete.

Actual final outcomes:

- 85 JVM tests pass: storage/date 10, ordered IO 3, Wordish 12, Connections 10,
  Bee 17, Strands 13, crossword 20. Includes legacy Bee snapshots, equal-credit word goal,
  word-length buckets, polygon geometry, rapid input reducers, keyboard evidence,
  dead-end/viable alternate Strands routes, clue advancement and Backspace.
- Debug app/test APKs build; lint zero errors/one existing OldTargetApi warning.
- Four Android tests pass on the explicit temporary emulator, with networking
  disabled. Previous three integration checks plus actual crossword phone-input
  hardware burst, multi-character paste, DEL, disk-state retention and no implicit
  Check/assistance. Last complete run: 46.038 seconds, `OK (4 tests)`.
- Used tools/test-emulator.ps1, which rejects physical-device serials and targets
  emulator-5554 explicitly. A personal phone was connected and was not targeted.
  The script parses JUnit success rather than trusting adb's process exit alone.
- Visual QA screenshots inspected locally: pastel honeycomb is actual center+6
  hexagons; compact crossword and optional IME leave the board visible. These
  checks cover the emulator viewport, not every phone/font-size configuration.

Commands: process-local JAVA_HOME as below, `gradlew.bat --offline
:app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug`,
then `tools/test-emulator.ps1 -Serial emulator-5554`. Reports/logs are in ignored
build/artifacts folders. No dependencies/permissions/services added in F1.

Save behavior now uses immediate buffers and ordered off-main writes; worker
tests verify ordering, nonblocking submit, stop-on-failure and explicit retry.
Latest state remains in memory on save error. MainActivity drains queued saves
on background transition with a bounded 1500 ms wait. Abrupt power loss during a
pending write still cannot be promised to retain the latest unsaved edit.

Still needs human phone review: visual preference/motion pacing, actual IME
feel/large text, difficulty/fairness, multi-route Strands experience and Bee goal
understanding. Existing completed/history records must remain interpretable under
their old scoring versions. New Connections corpus/varied crossword templates,
more sizes and clue variants are not implemented; see staging prompt/expansion.

## Historical S1 checkpoint

S1 implemented with ten distinct puzzle records per game, 50 total. This is a
playable development baseline, not full MVP acceptance. No physical phone/wife
acceptance has been performed. Git/source control is not player-data backup.

## Actual successful checks

- `:app:testDebugUnitTest`: 54 tests, zero failures/errors/skips. Storage/date 10;
  Wordish 10; Connektions 9; Bee 9; Strands 8; crossword 8.
- `:app:assembleDebug`: successful installable debug APK, API26 minimum/target 36.
- `:app:lintDebug`: zero errors, one OldTargetApi warning because target 36 is
  below the newest SDK known to lint. No lint baseline/suppression used to hide
  errors. Target-version upgrade/physical-device coverage belongs before release.
- `:app:connectedDebugAndroidTest`: all three tests pass on the installed
  Medium_Phone_API_37.0 emulator (Android17/API 37), read-only/headless with Wi-Fi
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

## Display-name update — 2026-10-25

Offline :app:assembleDebug and :app:compileDebugAndroidTestKotlin passed.
Display titles and UI selectors match D-021; internal IDs/snapshots unchanged.
Artifact: artifacts/CLEAN-Words-names-debug.apk; SHA256 6067a8e66451ec3ef0670ec43dd8ec0ca70f37c6961585a88cffe1e9b1f5fad6.
Device/emulator tests and visual review were not rerun for this presentation change.
