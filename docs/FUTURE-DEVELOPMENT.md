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
| Spelling Meh | Vocabulary entries with inclusion/exclusion reason/dialect; letter set/center, complete accepted answers, pangrams, per-length counts, visible word-count goal and scoring-policy version. F1 v2 counts every accepted word equally; familiar seed count sets a modest goal but membership never restricts credit. Preserve v1 goal/bonus rules only for legacy snapshots. |
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

## Difficulty: provisional recipes and calibration

Everything in this section is a **proposal, not yet authorized implementation**.
The owner's daily target is medium-plus, about 3.5/5: challenging but usually
winnable with effort. The formulas below are explicit, testable **content-ranking
proxies**, not measured human difficulty, validated predictions or win
probabilities. Initial weights and normalization anchors are hypotheses.

### Common scale and missing data

Normalize every feature to 0..1 with higher values provisionally meaning harder.
Define `N(x; low, high) = clamp((x - low) / (high - low), 0, 1)` with `high > low`.
For each game, `q = sum(weight * feature)` with nonnegative weights summing to 1;
the displayed *internal proxy* is `Dproxy = 1 + 4*q`. Do not put this number on a
player-facing difficulty selector before calibration. Save the feature values,
recipe version, normalization constants, source versions and missing-data flags.

Proposed word unfamiliarity `U(word)` is `1 - percentile(log(1 + frequency))`
against a frozen, documented reference vocabulary; define ties consistently.
No suitable frequency corpus has been selected, licensed or bundled. ENABLE
membership is not frequency or familiarity evidence. Until a corpus is accepted,
reviewers may assign U using a recorded rubric: 0 everyday, .25 familiar, .5
less common, .75 specialist, 1 obscure. Mark this as manual proxy evidence; do not
mix it silently with corpus percentiles. An unrated/missing word is **unknown**,
not U=0. Missing required features yield an incomplete score and exclude that
record from automatic tier selection; do not hide gaps by renormalizing weights.

### Per-game recipes

| Game | Provisional composite and precisely defined features |
|---|---|
| Wordish | `q = .40*U + .35*G + .15*F + .10*R`. U is answer unfamiliarity. G is `N(g;2,6)`, where g is guesses used by a frozen deterministic solver/starting-word policy against the exact eligible-answer/guess versions; unsolved within six maps to 1 and is flagged. F is `N(n;1,20)`, where n counts eligible answers including the target differing from it in at most one position. R is `N(5 - distinctLetters(answer);0,3)`. Solver policy, neighborhood computation and familiarity ratings are not implemented; the five-letter vocabulary alone cannot supply g or U. |
| Spelling Meh (F1 v2) | `q = .40*Uf + .35*E + .15*L + .10*R`. Let k be the visible word-count goal. Rank valid answers by modeled effort `1 + 4*U + N(length;4,12)`, and take the k cheapest. Uf is their mean U; E is N(sum of their effort;5,50); L is their fraction with at least eight letters; R is N(k/acceptedAnswerCount;.01,.30). This is a minimum-effort proxy, not actual search behavior. Familiarity U and rank computation are not implemented, so the proxy remains incomplete. Pangrams score extra points but are not required for the count goal and must not be inserted as a false required difficulty feature. |
| Connektions | `q = .35*K + .30*W + .20*D + .15*Ut`. K is mean prerequisite-knowledge rating per category: 0 everyday, .5 common hobby/culture, 1 specialist. W is mean wordplay-step rating: 0 literal relation, .5 one transformation, 1 two or more. D is `N(d;0,12)`, where d counts tile/category memberships outside the intended group recorded as plausible *partial* decoys in reviewed relationship data. Ut is mean displayed-tile U in its intended sense. K/W/known memberships/U require editorial annotations not present in the starter schema. Alternate valid full partitions or unexplained associations fail the fairness gate regardless of proxy score; never reward a broken board as hard. |
| Strand-ish | `q = .40*A + .25*Ut + .20*T + .15*J`. A is theme-clue abstraction: 0 explicit theme, .5 indirect but familiar association, 1 multi-step/wordplay clue, with written reviewer rationale. Ut is mean U of theme answers plus the spanning answer, excluding incidental bonus vocabulary. T is the fraction of internal path steps whose direction differs from the preceding step, pooled across intended paths (0 if no internal turns are possible). J is diagonal-step fraction across intended answer paths. Exact paths allow T/J calculation; A/U annotations are missing. Geometry is only a hypothesis about search effort; test whether its weights help instead of assuming more turns means more difficulty. |
| Minuscule Crossword | `q = .45*C + .30*Ua + .15*K + .10*S`. C is mean clue-indirection rubric: 0 direct definition, .5 familiar oblique clue, 1 multi-step/wordplay. Ua is mean answer U in the clued sense. K is mean prerequisite-knowledge rubric using the Connections scale. S is mean fraction of an entry's crossing letters whose *other* crossing clue has C >= .5, representing weak direct-clue support; use 0 for entries without crossings and flag the exceptional grid. Current cells/clue mappings support S only after C is annotated. Sense-aware familiarity, clue C and prerequisite K are unavailable without review. Multiple defensible answers with inadequate crosses are fairness defects, not bonus difficulty. |

For example, a complete recipe with weighted q=.625 produces Dproxy=3.5 by
arithmetic. That does **not** demonstrate that the wife will perceive 3.5/5.
The example demonstrates the scale only; it is not a scored puzzle or a claimed
measurement. Very different feature combinations can produce the same number.

### Intrinsic challenge, task goal, assistance and variants

Intrinsic proxies rank fixed puzzle content under a fixed rules/task profile.
Store the profile alongside the proxy. Changing guess allowance, Bee target,
board count or grid dimensions changes the task/variant and needs separate
calibration; it cannot silently reuse a standard-mode label. Hints/checks/reveals
are assistance events and belong separately in the result. Do not make an Easy
label merely mean that hints were used, or rewrite the original label afterward.

Bee F1 uses `bee-wordcount-v2`: goal `ceil(.70 * familiar seed-word count)`, but
EVERY accepted word advances it equally. Per-length bars describe all answers,
not required completion. Optional points/pangrams are separate from the count
goal. The recipe evaluates reaching that visible goal, not exhausting ENABLE.
Legacy `bee-goal70-bonus-v1` snapshots keep their old rules/results. Change goals
only with a new policy version and appropriate calibration; never revoke old
completion when tuning or when a player continues after the goal.

Six-board Wordish needs its own shared-budget solver/features; do not add a
constant to single-board difficulty or average six single-board values and call
the outcome calibrated. The hardest unresolved board and shared information can
matter more than the average. Similar caution applies to alternate grid sizes.

### Calibrating toward the wife's 3.5/5 target

1. First produce complete feature/provenance records and manually reject unfair
   puzzles. Propose a pilot of 20 diverse reviewed puzzles per game, not just the
   ten starter examples. Before feedback, reserve five per game as a held-out
   confirmation set. These sample counts are a small exploratory plan, not a
   claim of statistical validation or an authorized corpus-generation task.
2. On the other fifteen per game, ask the wife for a 1..5 perceived challenge
   rating plus easy/just-right/frustrating and a short reason. Record completion,
   abandonment and assistance; optional active duration is secondary. Include
   failures rather than only favorite completed puzzles. Use manual notes first;
   adding local telemetry or app instrumentation requires a scoped proposal.
3. Compare proxy ordering with her ratings game by game. Review mislabeled words,
   clues and defects before tuning weights. Keep normalized features and sums
   testable; drop features that show no useful relation instead of adding false
   precision. Record every recipe change and avoid fitting many weights to a
   handful of observations.
4. Fit a simple monotonic mapping from q to her observed rating bands on the
   fifteen development examples, merging bands where evidence is sparse. Select
   candidates for the medium-plus pool where the mapped qualitative band spans
   about 3 to 4 and she judged them challenging-but-fair/achievable with effort.
   A raw Dproxy of 3.5 alone does not qualify a puzzle.
5. Check the five held-out examples without retuning to those results. Report
   disagreements and the small sample explicitly. If she finds them frustrating
   or the ordering is inconsistent, do not claim calibration; revise the recipe
   and recruit a fresh held-out set. Confidence is personal to this user and
   profile, not a population-wide score.

No numeric win-rate target or predicted probability is agreed or inferred.
Until this process provides useful evidence, keep only honestly labeled standard
starter play and editorial notes; do not invent selectable difficulty tiers.

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
