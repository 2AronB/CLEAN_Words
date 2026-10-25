# Continue CLEAN Words

Updated: 2026-10-25. Current checkpoint: F1 initial-playtest improvements built/tested.

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
never uninstall/clear player data. Hash in VERIFICATION. Emulator cleanup and Git
checkpoint will be recorded after final packaging.

Use tools/test-emulator.ps1 with explicit emulator serial, not unfiltered connected
Android tests: a physical phone may be connected. User's untracked
'gradle/gradle-daemon-jvm.properties' predates F1 and was not changed/staged.

## Parallel development and next action

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
