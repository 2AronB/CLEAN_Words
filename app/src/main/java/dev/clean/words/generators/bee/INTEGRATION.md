# Bee opening-score integration handoff — 2026-10-26

## Current primary integration and activation

Owner now explicitly chose in the primary chat: **Activate scoring now without
an additional ratio filter**. This supersedes the original gate described below.
OfflinePuzzleGeneration calls generateOpening with ratio 0 and approval version
owner-no-ratio-filter-2026-10-26-v1, pinned with opening generator/profile/benchmark/
rounding/source versions in daily seed identity. It uses the original index and
3,636-word familiarity input; the optional supplement is not loaded. Full ENABLE
acceptance, original base quality criteria and all legacy snapshots remain.

AttemptPresentation exposes opening/goal labels, calendar marks, errors and resume
need. Prior Bee daily resume remains available until opening recorded AND goal
reached. Shared status/completedAt still mean word-goal completion; no schema or
extra score/timestamp added. History, home, hub, calendar and result surfaces show
both milestones. Existing v1/v2 daily resumes unchanged; hub explains Another
puzzle starts the new scored mode immediately. AttemptRules refuses malformed
old/new maps, truncated/reordered accepted prefixes and stale/conflicting edit
revisions, preserving original state/results. AppSession reports rejected edits.

Primary app/device verification and APK: docs/VERIFICATION.md and CONTINUE.md.
Primary checks now pass: 154 JVM tests, 1,000 active v3 seeds (719 identities,
266 sets, zero failures), 13 offline Android tests (143.351 seconds), final
compact-card scored UI smoke 1/1 (30.218 seconds), builds and lint (0 errors,
1 existing warning). APK versionCode 4 / 0.4.0-bee-opening keeps the original
signature. Physical-phone/fairness/accessibility acceptance remains pending.
Human cross-board score fairness remains provisional. The remainder records the
original side-task delivery and decisions BEFORE this activation; its instructions
to keep production gated and await shared integration are historical.

This task authorizes Bee-local implementation, not shared integration or shipped
app closure. Primary confirmed earlier Bee agents finished/released ownership;
active Wordish chat confirmed no Gradle work. No shared/general project files,
dependencies, permissions, services, settings, branches, staging, commits, pushes
or devices were changed. The original v2 handoff below is historical reference.

## Owner decisions and activation gate

First ten distinct accepted submissions score against the best ten accepted
words. Existing points and equal-credit ceil(70% familiar count) completion stay.
After ten, opening is fixed; exploration still adds completion and all-word points.
Opening recording and completion-goal achievement are independent milestones.

The exhaustive current-source audit covers all 2,170 candidates / 1,489 qualified
boards. A proposed 60% familiar/absolute floor retained 266 boards / 77 sets, removing
82.14%. **Owner did not approve that floor**, replying: "Let's rework generation
to something that won't reject 82% of the games."

Owner then chose: **preserve full ENABLE acceptance and expand/review familiarity
coverage before choosing a filter; scored generation stays gated meanwhile.**
No default threshold, acceptance narrowing, word-length cap, relabeling to meet a
target or human fairness approval follows from this decision. The separately
versioned supplement is original AI editorial, not empirical frequency evidence
or human-reviewed content. No unfinished frequency work is a dependency.

## APIs, snapshots and state

- `BeeGenerator.generate(request,cancel)` keeps exact existing v2 outputs/identity.
  `generateOpening(request,quality: BeeOpeningQuality?,cancel)` explicitly opts
  into v3. Null quality returns `InvalidInput` awaiting owner decision. Fixture
  and evaluation thresholds in tests do not activate production. Explicit quality
  uses integer permille and an approval version; both enter snapshot/identity.
- Candidate filters compare familiarBenchmark*1000 against absoluteBenchmark*
  minimumRatioPermille, require ten accepted/familiar words and retain the base
  profile. Rejections remain diagnostic; no quality relaxation or fallback.
  `Exhausted`, `SearchLimit`, cancellation and invalid input stay honest.
- `BeeOpening.freeze(board,pins,quality)` makes `bee-opening10-v3`, revision3.
  It freezes full accepted/familiar sets, opening length, benchmark witness words
  and points, familiar benchmark, completion goal, rounding, source/dictionary/
  familiarity/generator/profile versions/hashes, threshold and approval version.
  Validation recomputes benchmark/goal/quality against frozen content, refusing
  missing pins, unsupported policies/revision and malformed/inconsistent lists.
- `BeeOpening.bestTen` sorts points descending then ASCII word ascending for
  deterministic ties. `BeeOpening.progress(board,state)` returns ordered opening
  words, points/denominator, percent, recorded flag, total found, goal, goalReached
  and all-word points. Display rounds half-up to two decimals; rational totals
  are exposed. Invalid board/state throws; call validate/stateErrors first.
- `BeeFamiliaritySupplement.prepare(base,text,pin,cancel)` is an explicit pure
  worker boundary, with exact base fingerprint, supplement SHA/count, familiarity
  version and evidence pins. It only augments familiarity; all accepted ENABLE
  words remain unchanged. Composite fingerprints distinguish future sources.
  See FAMILIARITY-REVIEW.md; no auto-upgrade or runtime IO/network.
- v3 state stores `statePolicy=bee-ordered-v1`, `acceptedOrder` CSV, entry/message
  and an `editRevision` acknowledgement sequence (not a score or word order).
  Found set, score, totals and milestone flags are derived, never independently
  persisted. Initial empty state/draft may omit the policy; first acceptance sets
  it. Opening input buffers initialize the empty ordered state on entry, so even
  drafts before the first acceptance carry the sequence. Rejected/duplicate
  words consume no slot. Clear/Delete edit draft only.
  Map and full LibraryCodec round trips preserve order. Late acknowledgements
  cannot roll back accepted prefixes or newer drafts with the same accepted
  order; conflicting order blocks for recovery. Sequence is retained on restart;
  absent sequences default to zero, malformed/negative/exhausted ones fail.
- Missing order with existing words cannot be inferred from legacy sorted sets.
  Duplicate/blank/unknown words, conflicting fields, unknown state policies and
  inconsistent benchmark block score/UI edits. Malformed decoded maps retain raw
  content. No reset, partial salvage or alphabetical migration occurs. The result
  honestly says recovery required and has no numeric score. Primary must retain
  original invalid data/results, rather than overwrite them with reinterpretation.
- v1/v2 rules, accepted lists, goals, scores/maxScores, summaries, state maps and
  starter records are unchanged. No old opening is inferred or retrofitted.

## Smallest shared changes — primary-owned

1. Keep v2 generation active until the reviewed input and consequential threshold
   are approved. Then explicitly load pinned input on the worker, call
   `generateOpening`, include full new input/scoring/generator/profile/approval
   pins in daily seeds. Existing daily snapshots always win. Keep durable
   save-before-open, cancellation/errors and semantic extra exclusions.
2. v3 `BeeRules.result.status` means **completion goal only**. Numeric score and
   maxScore are null; summary truthfully includes opening, completion and total
   points. Shared completion timestamp remains goal achievement. Use pure Bee
   progress for opening recording; do not mark tenth submission COMPLETED when
   goal is not reached, or freeze opening when goal is reached before ten.
3. History/daily/calendar/resume must show provisional/recorded opening separately.
   A seven/nine-word goal-complete opening remains unfinished and resumable.
   Prior daily resume should include attempts until both milestones are reached.
   Current shared status-only resume filtering misses this case. If a recording
   timestamp is wanted, add a separate 9→10 milestone without rewriting completion
   time or storing a second score. Current app already enables continued Bee play.
4. Keep existing ENABLE notice/credits: public-domain ENABLE by Alan Beale with
   M. Cooper/contributors, `dictionaries/ENABLE-NOTICE.txt`. Editorial familiarity
   remains original CLEAN Words content, human/frequency review pending. Credit
   a supplement version only if actually used. No new licensed source introduced.
5. Coordinate app checks and explicit disposable-emulator UI/restart/daily/extra/
   history tests, then batch human fairness review. Never use a personal phone or
   clear data. Local UI retains honeycomb/pastels/animations/buffering/ordered
   controls/equal-credit goal/length bars; opening instructions precede input.

## Verification checkpoint

Commands actually executed (JAVA_HOME is process-local):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --offline :app:testDebugUnitTest --tests 'dev.clean.words.games.bee.*' --tests 'dev.clean.words.generators.bee.*' :app:compileDebugKotlin
```

Initial sandbox run could not access the existing wrapper cache lock; approved
offline tooling then ran with filesystem escalation. First run: 42 tests, 1 wrong
test expectation (`TARANTELLA` is not a pangram; 10 points). Corrected fixture and
added cases. Next run passed 45 tests: BeeRulesTest 17, BeeOpeningTest 15,
BeeGeneratorTest 7, BeeOpeningGeneratorTest 6; Kotlin compilation passed.
Final coordinated command (after supplement and state validation changes):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --offline :app:testDebugUnitTest :app:compileDebugKotlin
node tools/generation/bee/prepare-index.mjs --check
node tools/generation/bee/opening-benchmark.mjs --check
node tools/generation/bee/prepare-familiarity-supplement.mjs --check
git diff --check
```

**All 144 JVM tests passed, zero failures/errors; Kotlin compilation passed.**
Final Bee coverage is 54 tests: original rules 17, opening rules/state/codec 17,
original generator 7, opening generator 6, familiarity supplement 7. Counts were
read from the actual JUnit XML. An earlier full run passed 142 tests in 29 seconds.
Read-only review then identified same-order late acknowledgements rolling draft
text back after the newest acknowledgement had drained pending edits. Added the
ordered-state edit revision and two regression cases, including restart and the
initial draft before any acceptance. The final full run passed in 22 seconds.
All three
Node reproducibility checks and whitespace checks passed. Existing CRLF warning
on BeeGame is informational; no whitespace error. Local Markdown links checked.

The optional `BeeEditorialSupplement.path/input/compositeSourceHash` expose the
exact loader pins from FAMILIARITY-REVIEW.md: 346 additions, 3,982 augmented
familiar entries, unchanged 98,144 accepted indexed words. The real-source JVM
test verifies SHA/count/composite pins, unchanged acceptance against an independent
raw ENABLE filter, source evidence freezing, legacy results and null-quality gate.
No code automatically loads this supplement or upgrades a saved snapshot.

The expanded audit covers every 438 sets / 3,066 centers, yielding 2,192 prospective
base-qualified boards. On the original 1,489-board cohort, median familiar/absolute
ratio improves 47.96% → 54.48%. A hypothetical 60% floor still rejects 64.34% of
that cohort (68.02% of the expanded pool); it remains unapproved and inactive.
705 future goals would increase under the optional input (p95 +3, maximum +7),
so vocabulary coverage and session length both need review. No existing goal
changes. Do not claim that this bounded editorial pass solved human fairness.

Node audit commands/results: OPENING-EVALUATION.md. No Android/device tests,
screenshots or release acceptance claimed. Still unverified: shared integration,
UI/touch/restart on Android, frequency evidence, human fairness and new activation.
Pure state/LibraryCodec round trips do not establish device persistence behavior.

Exact next primary action: read this handoff, the audit and familiarity review;
preserve the owner's vocabulary/gating decision; integrate independent milestone
presentation without activating a cutoff; review expanded evidence before asking
for a concrete quality choice. Do not require reapproval of existing scope.

## Original v2 integration reference (historical)

1. On a worker, read the bundled `BeeBundledSource.indexPath`, then call
   `BeeVocabulary.fromBundledIndex(text, isCancelled)`. Cache the immutable
   prepared vocabulary for subsequent requests. Source failure must preserve
   saved play and show an honest retry state. Preparation cancellation throws
   `BeePreparationCancelled`; generation cancellation returns `Cancelled`.
2. Call `BeeGenerator(vocabulary).generate(BeeRequest(seed, BeeProfile(), recent),
   isCancelled)`. The API is package-local in purpose, without a shared universal
   dictionary. Its Kotlin declarations are public for the primary adapter.
3. Map `Success.puzzle` directly into the existing attempt/save snapshot. Validate
   through `BeeGame.validate`, use existing `BeeGame.result`, and finish durable
   saving before opening successful new play. The generator does not write saves.
4. Map `Exhausted`, `SearchLimit`, `Cancelled` and `InvalidInput` honestly. No
   repeated starter or weakened profile is returned as generation success.
5. Use the canonical `Success.signature` for additional-play exclusions. For an
   old starter snapshot derive the same signature from its `letters`/`center`;
   never derive novelty from `puzzle.id` or shuffled outer letters. Daily seed
   identity uses pinned public source/generator/profile policy constants and date,
   independent of user extra history. Existing saved daily identity wins.

The minimum pinned daily policy includes `generatorVersion`, `selectionPolicy`,
`indexVersion`/`indexSha256`, `dictionaryPolicy`, `familiarityVersion`, and the
full default profile version. If default thresholds change, bump profile/version
and preserve existing puzzle snapshots; do not recalculate old goals. The source
is 1,764,601 bytes of plain TSV and no first-run download is required.

No changes are requested to BeePuzzle, BeeRules, honeycomb, immediate input,
animations, keyboard, controls, or legacy schema. Additions to the `data` map are
diagnostic/provenance; the complete `goalWords` and `validWords` in the recognized
v2 schema freeze gameplay. Independent corpus/rule/legacy tests are supplied.

Offline credits must retain the existing ENABLE notice and attribute Alan Beale
with M. Cooper/contributors; source is the already bundled public-domain ENABLE
enable1.txt. Add an entry for original CLEAN Words AI editorial familiarity
shortlists: 3,636 words, zero human-reviewed, empirical familiarity unknown,
provisional difficulty. The longer/four/six-letter original shortlist is in
`tools/generation/bee/familiar-draft.json`; five-letter entries were copied during
preprocessing from the original Wordish shortlist at its pinned TSV hash. No new
external source, SDK, service, permission or runtime network access is introduced.

Primary owns updating live game CONTENT.md and PRODUCT/DECISIONS/PLAN/CONTINUE/
TODO/VERIFICATION, installing only on an explicitly selected disposable emulator,
and the actual app conversion. This package alone is integration-ready module
work; it does not establish that daily/additional UI, cancellation/save recovery,
Android packaging or human fairness have been accepted.
