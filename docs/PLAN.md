# Discovery and delivery plan

G1 update (2026-10-25): owner authorized Wordish, Bee and primary integration.
Both generators and real daily/additional worker/save adapters are implemented.
Other three games retain labeled starters because their generator modules have
not been delivered. Counts/behavior: PRODUCT G1; actual checks/phone/fairness gaps:
VERIFICATION. No new dependencies. The original S1 plan below remains historical.

G1 verification covers two real-source 1,000-seed reports, independent Bee source
filtering, semantic exclusions, pinned fixtures, legacy snapshot retention,
source-error/cancel/retry/double-tap/date-boundary tests and recovery-copy cleanup.
Use tools/test-emulator.ps1 with explicit -Serial and optional -ReportPath for
offline UI/session/update checks; never clear player data. Full five-game closure
needs the other three handoffs and real UI checks. Human phone/fairness and process
death during incomplete save/rollback remain unverified.

Status: S1 authorized and implemented 2026-10-25, with ten samples per game.
Verification and device acceptance are tracked in CONTINUE and VERIFICATION.
Current feedback F1 is authorized and specified in FEEDBACK-01. Input now uses
immediate game buffers and an ordered background save lane, drained on background
transition with bounded wait. New Bee policies are versioned; old snapshots stay.
Connections content can proceed in isolated staging via agent-prompts; crossword
layout/clue-bank expansion is proposed in CROSSWORD-EXPANSION, not yet built.

## Approach

First establish a real play session and smallest successful replacement. Then
resolve workflows, puzzle rules, content quality, interrupted play and durability
before choosing technology. No workout architecture is inherited.

## State and content ownership

Proposed S1: primary-owned attempt repository stores puzzle identity/content
snapshot, rules version, mode/date, inputs, result and timestamps in app-private
local storage. Game modules own rule state and validation; shared history/calendar
reads saved attempts. No deletion/reset in S1. Backup/reinstall recovery remains
unpromised. Full proposal and acceptance: PRODUCT's S1 baseline.

## Risks and unknowns

- R-005/R-006: Different games have very different generation and editorial
  needs. Large puzzle counts alone do not establish quality.
- R-005: Word-list suitability, licensing and content provenance need review.
- No ready-to-bundle large editorial puzzle pool has been verified. Prior pool
  suggestions describe an approach, not an available licensed corpus. WordNet
  is a candidate building block, not finished puzzle content; see GAME-OPTIONS.
- R-009: Fairness needs game-specific standards, including word familiarity and
  clue/category ambiguity. Do not equate a mechanically valid puzzle with a
  satisfying puzzle.
- R-008: Define daily identity/reset and additional-play stats separately.
- R-010: Define preserved history and whether daily and additional results have
  separate statistics; recovery/backup is still unagreed.
- A passive friend system could introduce accounts, connectivity, access
  control, deletion and ongoing service maintenance. Active sharing was rejected
  by the owner; do not substitute it for passive activity. Owner requested an
  effort estimate only; no accounts or service approved.
- R-004: Fun and polish need acceptance on the wife's actual Android device.
- R-007: Temporary names must be replaced before final release.
- All five core games and full offline/no-account operation are required for
  MVP. Variants, device support and distribution remain open.
- A short-day target cannot establish editorial content quality or manual
  acceptance. Identify content bottlenecks before committing to a schedule.

## Dependencies and permissions

Approved S1 build/Compose/test dependencies are selected and pinned below. No
runtime permissions or services added. New proposals explain benefits and
alternatives before approval. No network service assumed.
R-015 requires all MVP runtime features to function without connectivity. Bundle
licensed/reviewed content and use local generation where suitable; do not assume
cloud AI, online dictionary lookups, first-run downloads, login or remote daily
distribution. Development-time content tooling remains subject to implementation
authorization. No internet permission is currently justified or proposed.
Owner permits suitable reusable/adaptable content, including attribution-bearing
datasets (R-012); no repeated approval needed just for acceptable attribution.
Before inclusion, record content license, provenance, source/version, changes and
notice obligations. R-013 requires an attribution screen once any dataset is used.

## Difficulty proposal (R-009/R-014)

Use game-specific difficulty profiles; assistance and intrinsic puzzle difficulty
must be labeled accurately. Do not equate board count or size with calibrated
difficulty. Candidate controls: word-guessing hints/guess allowance and vetted
answer difficulty; Bee attainable score targets and pangram assistance;
Connections direct versus wordplay relationships reviewed for ambiguity;
Strands theme explicitness and hints; mini-crossword direct versus indirect clues.
All are proposals, not settled rules or implemented algorithms.

Owner's daily target is medium-plus (about 3.5/5), usually winnable with effort.
Calibrate against representative wife playtesting rather than invent a precise
algorithmic score. Define completion for open-ended games such as Bee.
Recommendation: one fixed daily challenge per game at this target, with difficulty
selection for additional puzzles. This keeps daily history comparable and avoids
multiplying daily content requirements. Daily override policy remains open. Store mode,
variant, difficulty and assistance with results if approved; never reclassify old
results silently after difficulty tuning. Difficulty requires content checks and
human play review, beyond verifying that a selector changes a label.

## Authorized technical baseline for S1

Native Android, Kotlin and Jetpack Compose with Material 3. Android identifies
[Compose as its recommended native UI toolkit](https://developer.android.com/compose).
Use AndroidX Activity/lifecycle support and Kotlin tooling; avoid a game engine,
dependency-injection framework, analytics, network client or third-party database.
Implemented app-private storage uses a bounded, checksummed binary snapshot
codec (schema 1), atomic replacement and a retained last-good copy. This keeps
rule/state round-trip tests independent of Android JSON. On failed save/load show a
recoverable error and preserve existing files; never silently reset storage.
Test serialization/recovery; do not promise protection from uninstall/device loss.

Approval proposal includes Android Gradle/Kotlin/Compose build plugins, Gradle
wrapper, AndroidX Activity/lifecycle/Compose UI/Material3 dependencies, JUnit and
AndroidX test tooling for relevant rule/device checks. Build-time dependency
downloads are distinct from app runtime networking. Select compatible stable
versions during authorized setup; record them in build files, not guessed here.
No runtime permissions or external services proposed. Attribution-bearing content
reuse is already permitted; SDK/service additions outside this list need approval.

Provisional internal-test minimum: Android 8/API 26, with target/compile version
chosen from compatible installed stable SDK/tooling during setup. Confirm wife's
device support before acceptance; this is not a final release-support promise.
Use project-local/process-local build configuration, not personal/global settings.
No global tool or SDK installation included; if needed, report concrete missing
packages and request scoped authorization after existing-tool options are checked.

Read-only discovery found Android Studio with a bundled jbr directory, SDK
platform directories android-36 and android-37.0, plus Git. Java/Gradle/adb were
not found on PATH, but this does not establish absence. No executables, build
commands, installations or compatibility tests have been run. Build readiness
and downloads remain unverified until implementation is authorized.

## Named slices

- S1 — Five-game offline playable baseline: includes the minimal foundation,
  five functional games with at least ten original sample records each,
  daily/additional flow, local save/history and prior-daily calendar/resume.
  Scope/rules/defaults: PRODUCT S1. Acceptance A-S1-01 through A-S1-07, linked
  to R-001–R-010/R-012/R-013/R-015/R-016. Scope/tooling approved in D-017.
  Implemented; device/manual acceptance tracked separately. Packages: TODO.
  Future expansion and difficulty detail: FUTURE-DEVELOPMENT.
- S2 — Content scale and fairness: licensed content inventory and evaluated
  generation/curation per game, repetition/exhaustion policy, growing reviewed
  pools (R-005/R-009/R-012/R-013). Precise goals need agreement; no guaranteed
  infinite editorial content. Small disposable spikes require prior approval.
- S3 — Variants and difficulty: select named per-game variants, six-board
  Wordish and calibrated difficulty profiles; clarify daily overrides and
  assistance/history (R-011/R-014). Separate approval after S1 feedback.
- S4 — MVP acceptance and release preparation: usability/accessibility/device
  checks, agreed durability/backup rules, release-name changes, full regression
  and distribution decision (R-001–R-016). No app release authorization implied.

S1 is deliberately below full MVP acceptance for R-005/R-011/R-014 and polish.
Do not close those requirements by creating TODOs. Later slices are not automatic.

## Proposed parallel execution after authorization

Recommend a shared app foundation with five separate game modules, rather than
a generic game engine or a runtime plugin system. Shared visual conventions can
cover daily/additional entry, help, progress/resume and results/history; each game
keeps its appropriate board and input behavior. No software framework chosen.

Establish only the shared contract actually needed: puzzle identity and content
version, daily/additional/variant mode, saved progress, completion result and
history ownership. Primary agent owns these shared areas and integration.
Avoid speculative five-game scaffolding before content constraints are known.

Content ownership proposal: share reusable source vocabulary and filtering tools
where suitable, but give each game its own versioned content profile. Word
guessing separates accepted guesses and familiar answers; Bee has an inclusion
policy; Connections has relationships/categories; Strands has themes/spanning
answers; crossword has clue/answer/sense records. Do not force these into one
universal dictionary format or duplicate entire datasets without need. Track
source licenses/modifications for attribution. Reproducibility and preserving
in-progress puzzles across content updates need agreement before implementation.

Then delegate bounded game or content packages with input requirements, isolated
file ownership, acceptance examples and a final report of actual checks. A game
package closes with playable behavior and meaningful rule/persistence checks,
not placeholder screens. A Connections content package closes with a reviewed
sample, provenance, ambiguity notes and agreed validation criteria before any
large corpus is commissioned. Agents do not independently change shared state
or add dependencies. Manual fairness/device acceptance remains distinct.
One agent may own a game at a time; rotate assignments through available agent
capacity rather than assuming all five must run simultaneously. A Connections
content bank and a game screen can be separate assignments when file ownership
and inputs are explicit. Primary agent retains integration and review ownership.

Completed delegated task: research-only five-game content feasibility; no files,
code or datasets created by the agent. Entry: R-005/R-006/R-009. Closure: report
on dictionary versus editorial needs and a bounded Connections content work
package received and integrated in GAME-OPTIONS. No generator was verified.

## Verification and recovery

Rule/content, file round-trip/failure/date tests and Android integration checks
are implemented; actual outcomes are in VERIFICATION and CONTINUE. Backup,
restore and migration policies need agreement before broader promises.
For R-015 include fresh-launch and restart checks with connectivity disabled,
offline daily rollover and access to help/credits. Exercise every included game,
mode and difficulty. Separately define repeat/exhaustion behavior for finite
content pools; offline availability does not establish unlimited unique content.

During S1 use appropriate rule tests and saved-state/date/content validation,
build/lint and available instrumented checks. Write exact verified commands only
after project setup. Produce an installable debug APK if the build succeeds.
Retain source/file-history checkpoints before substantial changes; a source
checkpoint does not back up player data. Do not uninstall/clear app data to
resolve persistence issues. Future schema changes must preserve existing attempts.

Readiness review: authorized bounded S1 defaults address identity, repeats, Bee
completion, late completion, unfinished attempts and errors. Build tooling is
operational; wife/device acceptance and full-MVP content/difficulty/variants
are explicitly unverified or deferred, not claimed solved. A parallel read-only
agent reviewed S1 and supplied deterministic examples integrated in TODO.
