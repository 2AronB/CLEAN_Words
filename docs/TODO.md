# Agent work queue

## Bee B1 — 2026-10-26

Owner explicitly approved activation without an additional familiarity-ratio
filter (D-025), superseding the original side-task gate.

- [x] Integrate new v3 daily/extra generation with pinned ratio 0/full acceptance,
  original familiar source and no automatic optional supplement.
- [x] Ordered opening score, immutable first ten, separate completion/exploration,
  independent milestone presentation and resume until both are complete.
- [x] Refuse stale/reordered/malformed persistence updates; keep legacy saved
  content/results. Core integration/rule/corpus tests pass; app checks recorded
  in VERIFICATION after execution.
- [ ] Human fairness, actual-phone input/layout/accessibility and goal comfort.
  No measured frequency or calibrated cross-board difficulty claim.

## G1 module/integration closure — authorized 2026-10-25

Owner requested Wordish -> Bee -> Integration. This supersedes the prompt-only
checkpoint below for those tasks. Both modules/shared runtime are implemented;
actual checks/counts and partial integration: PRODUCT G1 and VERIFICATION.

- [x] Wordish: original 1,387-answer bank/pipeline, real seeded selector, exclusions,
  cancellation, legacy policies/snapshots and 1,000-seed report in
  app/src/main/java/dev/clean/words/generators/wordish/REPORT.md.
- [x] Bee: licensed source-backed index/original familiarity shortlist, real
  seven-letter/center generator, independent exhaustive accepted-word tests,
  frozen v2 equal-credit goals and 1,000-seed report in
  app/src/main/java/dev/clean/words/generators/bee/REPORT.md.
- [x] Primary: generated daily/additional starts for both modules on separate
  worker; pinned seeds/recent signatures; tap coalescing/cancel/retry/errors;
  durable-before-open saves; old daily resume and recovery-copy cleanup tests.
- [ ] Full all-five INTEGRATION closure: Connections/Strands/Crossword generator
  handoffs absent; keep explicit starter flows. Do not claim generated conversion
  for those games or implement their prompts automatically.
- [ ] Human vocabulary/fairness/Bee goal-length/phone acceptance. Both banks have
  zero human/frequency reviews. Difficulty calibration/variants remain separate.
- [ ] If crash-proof interrupted-start cancellation is required, add a durable
  launch journal; present rollback follows the existing keep-open/retry policy.

Updated 2026-10-25. S1 authorized in D-017 with TEN puzzles per game. Shared
foundation, five game modules/content and rule tests implemented. Verification
and remaining manual acceptance: VERIFICATION. Future entries are not automatic
implementation authorization. PRODUCT and PLAN govern scope.

## Coordination and closure

Primary agent establishes a compiling shared foundation and documents the exact
game contract and file ownership before concurrent game implementation. Shared
attempt identity, repository, calendar/history, build files and dependency choices
remain primary-owned. Each game agent owns only its game rules/UI/content files;
report needed shared changes instead of racing edits. Rotate through available
agent slots. One assignment may cover a game; content can be separated later.

Every assignment receives relevant requirement/acceptance IDs, contract version,
assigned files and fixture policy. Close with actual code/content, meaningful
checks/results, limitations and integration notes. Primary reviews/integrates;
manual phone/fairness acceptance remains distinct. No new dependencies/services
or unapproved scope. Preserve unrelated work and never clear user data.

## S1 packages (authorized; delivery/manual closure tracked below)

- [x] T-001 Primary: foundation and contract. Entry: S1/product/tooling approval.
  Deliver native Android build, navigation/theme, help conventions, attempt
  identity/mode/rules/content snapshot contract and file ownership. Close: app
  builds, basic home/Back works, contract documented and game packages unblocked.
  R-001/R-003/R-004/R-006/R-015; A-S1-01/A-S1-07.
- [x] T-002 Primary: storage, daily rollover, history and calendar. Entry: T-001
  contract. Autosave, versioned records, independent daily/additional attempts,
  started-date calendar and most-recent prior unfinished resume link. Close:
  05:59:59 versus 06:00:00 tests, round-trip/restart checks, no duplicate result
  or overwritten prior puzzle; failed writes/invalid records preserve existing
  data and report recoverable errors. R-008/R-010/R-015/R-016; A-S1-02/03/04.
- [x] T-003 Game agent: Wordish. Entry: shared contract, approved S1 rules.
  Deliver playable single-board game plus at least ten original puzzle records
  and valid-guess policy. Close: rules/invalid-input tests, win/loss and save
  round-trip. Fixture example: APPLE versus ALLEY -> exact, present, absent,
  present, absent. Confirm dictionary rejection consumes no guess. R-006/R-009;
  A-S1-01/03/05. Multi-board and large dictionary are separate future tasks.
- [x] T-004 Game agent: Connektions. Entry: shared contract and S1 rules.
  Deliver 16-word board, four explanations, four-mistake handling, selection and
  at least ten original sample boards with documented ambiguity review. Close:
  correct groups lock, fewer than four cannot submit, repeated wrong set has no
  second penalty, save retains solved groups/mistakes. Structural checks do not
  prove unique fair interpretation. R-006/R-009; A-S1-01/03/05.
- [x] T-005 Game agent: Spelling Meh. Entry: contract/S1 scoring and completion.
  Deliver seven-letter play, accepted words, scores/target, found list and at
  least ten original sample records with pangrams. Close: invalid/missing-center
  and repeated words cannot score; pangram bonus and rounded target correct;
  score/found words restore and play continues after target. Test an explicit
  valid CHAIN fixture with A/C/E/H/I/N/T, reject HINT (no A) and TRAIN (R).
  R-006/R-009; A-S1-01/03/05. Vocabulary coverage and difficulty remain open.
- [x] T-006 Game agent: Strand-ish. Entry: contract/S1 path semantics.
  Deliver touch and tap-friendly path selection, themed answer detection and at
  least ten original full-cover boards with valid spanning answer paths.
  Close: fixture coverage/adjacency/no repeated cells checks; reject nonadjacent
  jumps, accept intended theme paths, preserve found words. Explicit cancel
  clears current selection only. R-004/R-006/R-009; A-S1-01/03/05/07.
- [x] T-007 Game agent: Minuscule Crossword. Entry: contract/S1 grid rules.
  Deliver clue/cell navigation, editable 5x5 grid and at least ten original
  fills/clue sets. Close: crossing entries share one cell, full incorrect grid
  stays unfinished, correct grid completes once, Check never erases entries,
  save restores edits. R-004/R-006/R-009; A-S1-01/03/05/07.
- [x] T-008 Primary/content reviewer: provenance and offline credits. Entry:
  content from game packages. Record sources/licenses/version/modifications,
  original authorship/review and dataset credits if any; reject unclear reuse.
  Close: all bundled material accounted for, required notices readable offline,
  sample/repetition limits explicit. R-005/R-012/R-013/R-015; A-S1-04/06.
- [ ] T-009 Primary: integration and delivery. Entry: T-001–008 integrated.
  Run available rule/state/content tests, build/lint and device checks; record
  exact commands/results. Package debug APK if successful, report unmet checks
  and manual checklist. No completion claim from screens alone. R-001–R-010,
  R-012/R-013/R-015/R-016; A-S1-01–07. Human closure: wife/device input, layout,
  interruption/calendar checks; fairness/polish reviewed separately.

## S1 closure status

T-001 through T-008 are implemented and mechanically checked; test outcomes and
scope limits are in VERIFICATION. T-009 automated build/54-unit/3-Android checks
pass; human phone/fairness closure remains pending. Ten records per game, not
three. No feature is considered finished merely because this list exists.

## Future packages — define/authorize after S1

Generator implementation prompt pack: agent-prompts/generation/README.md. Five
game tasks have disjoint ownership and shared constraints; INTEGRATION.md owns
the app's runtime selection/loading/cancellation/save wiring. Prompts prepared
only; requesting these documents did not authorize executing generator work.

Initial playtest F1 changes implemented; see FEEDBACK-01 and VERIFICATION for
results/manual gaps. Connections content may run concurrently using
agent-prompts/CONNECTIONS-CONTENT.md with staged ownership and validation before
production integration. Crossword layout/clue-bank proposal: CROSSWORD-EXPANSION.
Triangle/square Bee modes remain future content variants, despite reusable
polygon geometry now present. Generic difficulties/corpus scale remain open.

- [ ] Content sourcing and generators, per-game quality and repetition policy.
  R-005/R-009/R-012/R-013. Connections pilot: 20 original categories/five boards,
  explanation/provenance/overlap review; see GAME-OPTIONS. Close only against an
  agreed quality rubric, not merely a large generated file.
- [ ] Wordish six-board variant; then choose additional per-game variants.
  R-011. Entry: variant rules/guess budgets and history behavior agreed.
- [ ] Game-specific difficulty and medium-plus daily calibration. R-014/R-009.
  Entry: agreed completion and assistance policy; closure includes wife feedback.
- [ ] Rich history: selected stats, streak/late credit and calendar/archive rules.
  R-010/R-016. Never silently reclassify old results.
- [ ] Durability policy: updates/migrations, export/import/backup/recovery and
  deletion effects agreed before implementation or promises.
- [ ] Accessibility/visual polish: larger text, non-color feedback, screen reader,
  light/dark and comfortable bedtime interaction on target phone. R-004.
- [ ] Release: rename deliberately similar development names (R-007), verify all
  content notices, device/version coverage, distribution and full MVP acceptance.
- [ ] Before corpus/history scale: profile synchronous snapshot IO/input latency,
  define a tested durable IO queue if needed and preserve failure/retry behavior.
- [ ] Choose source/app license and complete transitive dependency notices before
  public release; public Git visibility alone does not grant content reuse rights.
- [ ] Optional later private friends activity only after separate scope/privacy/
  service approval; offline games must remain independent. Not part of MVP.
