# Continue CLEAN Words

Updated: 2026-10-25. Current checkpoint: G1 Wordish/Bee generation integrated and
verified; five-game generation conversion remains partial. Older checkpoints below
are historical; use this section and PRODUCT G1 for current scope.

## Active generated-play task — 2026-10-25

Owner explicitly requested WORDISH.md -> BEE.md -> INTEGRATION.md (D-023).
Those two module implementations and shared integration are authorized/completed.
Earlier prompt preparation had no generator code authorized or verified; at this
task's entry unfinished Wordish files had not been verified. Do not repeat that
approval question. Other module prompts are not automatically authorized.

tWordle: 1,387 original AI editorial answer candidates; unchanged 8,636 ENABLE
guesses/six-guess rules. Bee: 98,144-word source index and 3,636 AI familiarity
candidates; 1,489 qualifying set/center identities across 283 sets. Both banks have
zero human/frequency review; difficulty/goal comfort remain provisional. Bee v2
freezes all accepted words/goal count; old v1/v2 snapshots are unchanged.
Module APIs/provenance/results are in generators/wordish/ and generators/bee/.

New daily/extra starts use separate generation worker and ordered durable save
before UI opening. Daily seeds pin game, policy and local 6 a.m. puzzle date and ignore history;
extra UUID seeds exclude semantic content from latest 100 starts. Existing daily
snapshots always win. Loading/cancel/retry/exhaustion/source errors are honest;
normal cancellation/failed-save retry purge active/recovery copies. Incomplete
or failed rollback still requires keep-open/retry; process death at that point
is not guaranteed. No durable start journal, silent starter fallback, new variant,
dependency/permission/service/network/global-setting change or public app release.

Missing: Connections/Strands/Crossword generator handoffs. Their labeled 10-starter
cycles remain functional; do not claim all-five generated closure. Preserve all
old player snapshots and the owner's display names (D-022). Untracked user
gradle/gradle-daemon-jvm.properties remains unchanged/un-staged.

Checks: 114 JVM tests pass; build/test APK/lint pass (0 errors/1 prior target 36
warning). Two real-source 1,000-seed reports; both preprocessing --check pass.
Android 8/8 pass on offline emulator-5554 update install (64.115 seconds) and offline
fresh project-local emulator-5556 first install (69.8 seconds), without clearing data.
Final strengthened suite also passes 8/8 (67.246 seconds), including calendar
reopen/recreation; generated-board screenshots inspected. Details: VERIFICATION.
Final delivered APK smoke also passes (1/1, 19.126 seconds); packaged source bytes
and unchanged signing certificate verified after LF input normalization. Physical phone
not targeted. Human fairness/Bee goal-length/input/motion/text/accessibility and
actual overnight/clock-zone interruption acceptance remain open.

APK artifacts/CLEAN-Words-generation-debug.apk, 0.3.0-generation/code 3; same signer
verified against prior APK. SHA256 1834f5e87f8da21e6930f09e5cc8efd9516dec3e50448df8bde540043a0eade1.
Exact next action: install this APK as an update without clearing data and batch
MANUAL-ACCEPTANCE for generated tWordle/Bee and old history. To complete all-five
generation later, obtain the other three module deliveries under their separate task scope, inspect
their real APIs/contracts, then extend the explicit adapters/tests/credits. Do
not implement undocumented variants or the whole backlog automatically.

## Prior checkpoints (historical)


## Display-name update — 2026-10-25

D-022: tWordle, Speling Bee, Stranded and Crossita are the exact owner-requested
names. Connektions unchanged. Module titles and offline credits updated; Android
UI selectors, PRODUCT and manual checklist aligned. Internal IDs, content and
saved snapshots unchanged. This small presentation change was handled locally.
Offline assembleDebug and compileDebugAndroidTestKotlin pass; git diff --check
passes. Device/emulator UI tests were not rerun for this rename.
APK: artifacts/CLEAN-Words-names-debug.apk (same versionCode 2; install as update).
SHA256: 6067a8e66451ec3ef0670ec43dd8ec0ca70f37c6961585a88cffe1e9b1f5fad6.
Next: install this APK as an update without clearing data; review names on home,
game headers, Help and calendar alongside the existing F1 manual acceptance.
Previous F1 artifact below predates this rename. No new dependencies or permissions.

## Authority and constraints

S1 approved in D-017; initial phone feedback authorizes F1 in D-019/FEEDBACK-01.
Routine bug fixes, standard approved Android/Compose build dependencies, bounded
agents and Git pushes to https://github.com/2AronB/CLEAN_Words until release are
authorized. Do not request that permission again. No ads/tracking/accounts/runtime
internet, new services/permissions/dependencies, personal/global settings changes
or public app release. Preserve player data, old puzzle snapshots and score policy.
Future corpus/variant/difficulty tasks do not authorize their own implementation.

## Current implementation

Five games, ten starter records each. Local 6 a.m. daily/extra play, save/history and
calendar with previous-puzzle resume persist. F1 adds strong pastel palettes,
entry/reveal/selection/result animation and ordered Clear/Enter/Delete controls.
Wordish tiles/keys use green/yellow/gray strongest evidence; keys remain selectable.
Bee has actual honeycomb, unified new word-count goal, all-word per-length bars
and a single found list. New v2 records dispatch bee-wordcount-v2; old v1 saved
puzzles/history keep original rules. Triangle/square other-letter modes are not
implemented, only reusable geometry. Strands draws paths, tests that alternate
routes leave a full remaining cover, and distinguishes impossible from bounded
search exhaustion. Undo remains for older accepted routes.
Crossword has immediate buffered input, auto next unfinished clue without Check,
Backspace navigation, default built-in keyboard and optional phone keyboard with
IME-aware board sizing. Ten boards still share one figure-eight-like template; multiple
patterns/sizes and clue-bank generation are proposed, not built.

Games are keyed by attempt ID for local buffers. AppSession updates UI immediately
and writes complete snapshots through an ordered background worker; failures stop
queued writes until retry and keep latest memory. onStop drains up to 1500 ms. Atomic
checksummed files/last-good copy/future-schema blocking remain. No data reset.
No promise of device-loss/reinstall backup or retaining an edit during power loss.

## Verified and artifact

85 JVM tests pass; four Android tests pass on explicitly selected emulator-5554
with Wi-Fi/data disabled (last run 46.038 seconds). Includes all 50 records solved/saved,
five actual boards/Help/recreation, permissions/credits and real crossword rapid
phone input/paste/DEL with disk retention and no implicit Check. Build succeeds;
lint zero errors/one target SDK 36 warning. Honeycomb and phone-keyboard screenshot
QA inspected locally. Full detail: VERIFICATION. Physical phone not automated.
Artifact: artifacts/CLEAN-Words-F1-debug.apk, 0.2.0-f1/versionCode 2. Install as update;
never uninstall/clear player data. Hash in VERIFICATION. Implementation commit
44994c2 pushed to origin/main; test emulator shut down at handoff. Physical phone
untouched. Only the pre-existing untracked Gradle JVM file remains outside Git.

Use tools/test-emulator.ps1 with explicit emulator serial, not unfiltered connected
Android tests: a physical phone may be connected. User's untracked
'gradle/gradle-daemon-jvm.properties' predates F1 and was not changed/staged.

## Parallel development and next action

Latest user request: prompts to convert each game into generated play. Prepared
docs/agent-prompts/generation/ with COMMON, five isolated module prompts and one
primary INTEGRATION prompt. This preparing chat changed Markdown only; no generator
code/build/data conversion was executed or authorized by requesting prompts.
When the user pastes a prompt as an implementation task, that authorizes its
bounded scope. Five pure module deliveries require primary runtime integration;
do not call them completed app conversion before daily/extra flows actually use
them. Existing F1 bug-fix authority remains; future generators are not automatic.

Primary owns core/shared UI/build/integration; game agents own isolated folders.
CONTRACT and FEEDBACK-01 apply. Ready-to-paste concurrent Connections task:
docs/agent-prompts/CONNECTIONS-CONTENT.md. It owns only content-staging/connections/
and tools/connections-staging/, starts 20 categories/5 boards then targets 120/30,
validates modeled decoys/unique partitions and never ingests drafts into production.
No Connections corpus expansion is claimed. CROSSWORD-EXPANSION proposes varied
patterns, answer/sense/multiple-clue records and separate clue/answer cooldowns.
FUTURE-DEVELOPMENT has updated versioned difficulty/calibration/generator proposals.

Next: user/wife reviews new phone APK for input comfort, motion/pastels, word-length
bars and route behavior; run MANUAL-ACCEPTANCE in one batch. Fix F1 regressions
within existing authority. Larger collections, six-board Wordish, selectable
calibrated difficulty and final release acceptance remain open.

Reusable starter templates were improved earlier only in development/
PROJECT-AGENTS-TEMPLATE.md (STARTER-IMPROVEMENTS); no global settings changed.
Original starter snapshots retained. App code/framework choices are project-local.
