# Future development proposals

Prepared 2026-10-25. This document describes proposed work after the authorized
S1 five-game playable baseline. It does not authorize the backlog, new
dependencies, network access or services. All runtime features must remain
offline. No SDK/service is selected here. PRODUCT remains the requirements source.

S1 has ten starter records per game, not an unlimited reviewed corpus. Wordish's
accepted guesses are derived from ENABLE; its ten familiar answer selections are
a separate small pool. Generation, editorial quality, difficulty calibration and
variants need their own closures. Engine correctness is distinct from device
acceptance and the wife's assessment of fairness/fun.

## Content contracts to establish before expansion

Use a shared provenance envelope and game-specific records. Suggested common
fields: stable content ID, immutable revision, schema version, language/dialect,
source ID/version, source URL, source file hash, license, notices, transformation
recipe/version, authorship, review status, reviewer/date and rejection notes.
Record original authored content explicitly; do not invent an external license
or claim an unreviewed draft has editorial approval. Include required notices in
offline credits. Provenance describes rights and reproducibility, not quality.

| Game | Proposed game-specific information |
|---|---|
| Wordish | Normalized word ID/text; accepted-guess eligibility separate from answer eligibility; word length; familiarity evidence/source; inflection/variant/spelling flags; sensitive-content policy; reviewed answer tier. Puzzle records retain selected answers and guess-policy version. |
| Spelling Meh | Vocabulary entries with inclusion/exclusion reason and dialect; seven distinct letters, mandatory center, complete accepted-answer set under that policy, pangram IDs, per-word score inputs, maximum score, completion target and scoring-policy version. Keep broad bonus vocabulary distinct if such a feature is later approved. |
| Connektions | Stable category ID, label, relation type, four or more candidate member IDs, explicit sense/phrase expansion, explanation, cultural/reference prerequisites, wordplay flag, known overlaps and reviewed difficulty observations. Board records freeze four groups of four distinct displayed tiles and their explanations. |
| Strand-ish | Theme ID, clue text, theme explanation, theme-answer IDs, spanning answer, dimensions, letter grid and exact ordered cell path for every answer. Preserve accepted incidental-word policy separately. Record clue/word familiarity and validation/review status. |
| Minuscule Crossword | Answer entry with normalized fill, display form, intended sense, original clue IDs/text, clue style and prerequisites. Board record stores dimensions, blocks, canonical cell letters, numbered Across/Down runs, clue-to-run mapping and check/reveal policy. A definition alone is not assumed to be a good clue. |

Do not flatten all five into a universal word list. Share normalized vocabulary
and source metadata where useful, but keep relationship, theme, clue and scoring
data owned by the appropriate game. A word can be a valid guess without being a
fair answer; a category can be valid without making every combined board fair.

## Versioning, saves and migration invariants

- A content revision is immutable once distributed. Corrected content gets a new
  revision. A stable puzzle ID plus revision identifies an exact puzzle.
- Preserve the started attempt's complete puzzle snapshot, rules version,
  dictionary/policy version, game/mode/variant, chosen difficulty and assistance
  facts. Do not reconstruct an old puzzle from today's pool ordering or seed.
- Retain any dictionary/policy version needed to continue old attempts, or
  explicitly migrate with equivalence checks. Merely storing a version string
  does not ensure that the old rules remain available after an update.
- A generator seed is sufficient for reproducibility only alongside the exact
  generator, source content and selection-policy versions. Store the generated
  record itself for active/history attempts.
- Source facts and derived values stay distinguishable. Preserve historical
  results under their original scoring/completion rules; do not silently update
  old scores, difficulty labels, completion time or assistance after tuning.
- Keep daily identity independent of pool index. Daily/additional/variant results
  must not overwrite each other. A 6 a.m. rollover exposes today's daily while
  retaining unfinished started dailies on their original calendar date.
- A migration must preserve IDs and unknown-compatible fields, validate before
  replacing durable data and leave recoverable errors for unsupported records.
  Never reset storage to bypass a migration failure. Test old-save fixtures,
  partial/invalid input and repeated migration. Backup/export behavior remains
  separately agreed; this document makes no recovery promise.

## From ten starters to sustained offline content

Proposed pipeline: source/author → rights and provenance review → normalize →
game-specific candidate creation → structural/solver checks → novelty and
ambiguity review → human sample play → versioned content package → release gate.
Every rejection should retain a reason useful for improving the next batch.

1. Wordish: expand a familiar-answer whitelist independently of the broad guess
   list. Use deterministic scheduling and controlled repeats. Reject answers
   absent from the applicable guess policy. Multiple boards draw distinct
   answers and must be evaluated together, not just individually.
2. Bee: enumerate candidate seven-letter sets/centers from approved vocabulary;
   compute the complete answer set, score and pangrams; reject empty, trivial or
   excessive sets under agreed thresholds. Review the exclusions users are most
   likely to find surprising. Do not use a sampled answer list as a full set.
3. Connections: expand reviewed category records, then assemble four compatible
   groups. Detect duplicate tiles, known cross-category memberships and alternate
   complete partitions permitted by the stored relationship model. That model
   cannot prove no plausible human interpretation exists. Keep human ambiguity
   review and clear explanations as release gates. Prefer fewer accepted boards
   over mass-producing opaque categories.
4. Strands: author/review coherent theme sets and clues, then search for valid
   full-cover layouts with a spanning answer. Validate every cell/path and bound
   generation work; use validated bundled fallback content on failure. Grid
   solvability does not establish that the theme clue is fair.
5. Mini: choose a small set of legal grid templates, fill with vetted answer
   entries, attach sense-matched clues and check crossings. Reject poor fills
   rather than forcing obscure words to complete a grid. Precompute reviewed
   records if runtime generation cannot meet quality/latency goals.

Before scaling, agree a measurable unique-puzzle target per game and a repeat
policy. A finite pool must report exhaustion honestly and offer a labeled replay
or another agreed action. Never equate permutations of tile order with new
puzzles. Local generation must have bounded time/memory and a recoverable result
when no candidate qualifies; it must not freeze the UI or silently relax rules.

## Meaningful checks

Maintain deterministic fixtures plus corpus reports. Report actual counts,
rejection rates and worst cases; do not describe an unexecuted suite as passed.

| Area | Proposed checks |
|---|---|
| Provenance | Every included source has a recorded hash/version/license and required offline notices; deterministic transformation reproduces the output hash; unknown reuse terms block release. |
| Wordish | Duplicate-letter feedback never credits more occurrences than the answer contains; exact positions win before yellow allocation; all answers accepted; no duplicate answers in a multi-board puzzle. Test repeated-letter fixtures and arbitrary generated answer/guess pairs. |
| Bee | Every accepted word uses only the seven letters and contains center; all pangrams use all seven; exhaustive filtering of source policy matches the stored answer set; repeated words do not rescore; target/rounding and maximum score recompute exactly. |
| Connections | Exactly sixteen distinct tiles/four groups; complete explanations; repeated wrong sets order-independent; terminal state immutable; alternate-partition search against known relations and human review of sampled combinations. |
| Strands | In-bounds adjacent paths, no repeated cells in a path, exact letter spelling, no overlap across intended answer paths, complete coverage, spanning answer touches opposite sides; reverse/tap/drag equivalence under agreed rules. |
| Mini | Across/Down runs match blocks and numbering; every fill belongs to the intended entry/sense; crossing letters agree; clue mapping total; wrong full grid stays incomplete; checks/reveals preserve edits and record assistance. |
| Shared state | Round-trip every meaningful state including partially typed/selected input; update old-save fixtures; cancellation affects only transient work; 05:59:59/06:00:00 rollover, previous-day resume and separate additional history. |
| Corpus quality | Duplicate/near-duplicate themes/categories/clues; answer frequency and repeat interval; outlier vocabulary; missing explanations; culturally narrow clusters; pilot wife feedback and rejected-puzzle reasons. |

Property checks must test invariants independently of the generator where
possible. Testing a generator only with its own validator can reproduce the same
bug twice. Keep a set of manually verified invalid records that each validator
must reject. Semantic uniqueness remains partly a human judgment.

## Difficulty: proposed features and calibration

The owner's daily target is medium-plus, about 3.5/5: challenging but usually
winnable with effort. This is qualitative feedback, not a validated numeric
prediction. Any initial score is a **proxy**, with feature values and uncertainty
recorded; do not label arbitrary weighted sums as measured difficulty.

| Game | Candidate predictors; not approved scoring formulas |
|---|---|
| Wordish | Answer familiarity, repeated letters, spelling patterns, candidate-family size and simulated guess-search effort; multi-board answer interactions. More boards is a variant, not automatically a harder calibrated level. |
| Bee | Familiarity distribution, proportion of score in familiar words, pangram accessibility, word-length mix and completion target. Lowering the target changes the task; hints are assistance, not intrinsic word-set difficulty. |
| Connections | Direct versus wordplay relationship, required background knowledge, plausible decoy overlaps, category specificity and ambiguity-review findings. Ambiguity is a defect candidate, not a reliable way to make a puzzle hard. |
| Strands | Theme-clue explicitness, answer familiarity, word/path length, turns and misleading incidental words. Validate how much these features actually affect play. |
| Mini | Clue directness, answer familiarity, ambiguous senses, cross-letter support and theme knowledge. Grid size alone is insufficient. |

Calibrate with a small diverse reviewed sample per game: collect the wife's
easy/just-right/frustrating judgment, completion, assistance and optional active
solve duration without analytics or network collection. Discuss local test
recording before adding it to the app. Include failures and abandoned puzzles;
avoid fitting only completed favorites. Tune and hold out a different sample for
confirmation. Keep a daily medium-plus pool separate from exploratory tiers until
feedback supports the labels. No guaranteed win rate has been agreed.

## Variant packages and parallel agent contracts

First establish variant rules, persistence/result representation and fixed
acceptance fixtures; then assign isolated files. Primary owns shared navigation,
attempt storage/history/calendar, build/dependencies and integration. Game agents
own game modules/tests. Content agents own a specified staging corpus/manifest,
not another agent's live game code. Review/integrate before changing shared
contracts. Every assignment has an entry, output, closure and explicit non-goals.

### Six-board shared-guess Wordish (first candidate)

Entry: confirm that "6-wordle" means six simultaneous five-letter answer boards;
agree shared guess budget, when solved boards lock, whether a valid shared guess
continues against unsolved boards only, terminal condition and aggregate result.
Define additional versus daily availability and keyboard feedback per board.
This is not authorization to choose those rules silently.

Output: six distinct frozen answers, one shared ordered guess history, derived
per-board duplicate-letter feedback, per-board solved-at index, accessible board
switching/overview and stable aggregate result with variant ID. Avoid six
independent guess histories that can diverge from the shared action.

Closure: deterministic fixture solves different boards on different guesses;
each board applies duplicate-letter rules independently; solved boards stay
solved; invalid/repeated inputs follow agreed budget rules; exhaustion records
partial success honestly; last-guess solve wins according to agreed aggregate
rule; save/restart restores all boards, active view and remaining budget.
Single-board behavior and prior history must remain unchanged. Human closure
checks six-board readability and input comfort on the wife's phone.

### Other candidates (not yet selected)

- Bee: alternate letter counts or timed sessions need new pangram/target and
  interruption rules. Entry is a chosen mode, not merely a new label. Closure
  includes exhaustive answer-set/scoring checks and preserved mode history.
- Connections: three groups of three is a candidate, with budget and category
  overlap rules agreed first. Closure includes content validation and independent
  state/result tests for both board sizes; do not hardcode sixteen everywhere.
- Strands: alternative dimensions or clearer theme clues require a valid corpus,
  path/coverage rules and distinct mode identity. Close with generated/invalid
  layout fixtures, saved selection and review for comfortable touch targets.
- Mini: alternate dimensions or clue styles require grid/clue generation and
  numbering/crossing tests. Timed play would need interruption/background policy;
  do not add a timer as a cosmetic variant without agreeing its consequences.

### Bounded content-agent example

Connections pilot entry: agreed relation schema, difficulty audience, sources and
ambiguity rubric. Deliver 20 original category records and five candidate
complete boards in an assigned staging folder, with explanations/provenance and
known overlaps. Close with structural report, alternate-partition checks under
the stored model and independent review outcomes. Drafts are not automatically
approved. Parent accepts records and integrates the content package. Scaling is
a separate task after this pilot demonstrates the intended quality.

## Content release gates

Before moving a content/variant package into the distributed app:

1. Rights/provenance complete; offline credits and required notices verified.
2. Schema, independent structural checks and source/output reproducibility pass.
3. Existing saves/history and all five standard game regressions remain valid.
4. Content count/repeats/exhaustion and runtime bounds meet agreed targets.
5. Explanations/clues and a representative sample receive actual fairness review;
   explicitly record what remains unreviewed rather than claiming whole-corpus
   human review from a sample.
6. Offline first launch/restart/update behavior and target-phone interaction
   checks pass; known failures are reported. No remote fallback may gate play.

Keep rejected drafts out of approved selection pools. The overall MVP is ready
only when PRODUCT's requirements and actual acceptance are satisfied, not when
every future idea has a TODO entry or every game has a placeholder selector.
