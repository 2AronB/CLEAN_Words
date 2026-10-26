# Ten-word opening benchmark evaluation

Current primary decision (2026-10-26): owner subsequently approved activation
without an additional ratio filter. Runtime now uses explicit ratio 0, original
inputs and full ENABLE acceptance. No 60% floor or optional supplement is loaded.
The audit/proposal/gating narrative below is historical; fairness remains
provisional. Current integration/checks: INTEGRATION.md and docs/VERIFICATION.md.

Evaluated 2026-10-26 using the actual bundled corpus, offline. This is an audit
and proposal history; **no threshold is approved or activated by this report**.
Owner response on 2026-10-26: “Let's rework generation to something that won't
reject 82% of the games.” The 60% recommendation below was **not approved**.
The owner subsequently chose to preserve full ENABLE acceptance and expand and
review familiarity coverage before choosing a filter; **scored generation stays
gated meanwhile**. A narrower curated accepted bank was not selected. The first
bounded optional supplement and its comparative audit are recorded in
[FAMILIARITY-REVIEW.md](FAMILIARITY-REVIEW.md). It improves coverage without
establishing human fairness or approving a threshold. Do not activate the earlier
recommendation.

## Inputs and exhaustive method

The [evaluation script](../../../../../../../../../tools/generation/bee/opening-benchmark.mjs)
uses Node built-ins only. Run from the repository root:

```powershell
node tools/generation/bee/opening-benchmark.mjs
node tools/generation/bee/opening-benchmark.mjs --check
```

Both commands were executed successfully. The first writes the report; the
second recomputes it and checks exact deterministic output bytes. A separate
JSON parse check confirmed all 2,170 rows and 1,489 qualified rows. No Gradle,
Android, network, dependency installation or device operation was used here.

Pinned input: `bee-mask-index-v1`, SHA-256
`d9dcf290c0c057c0df9277bc1be855771336445991da6f09e60133829ba20143`;
ENABLE SHA-256
`3f16130220645692ed49c7134e24a18504c2ca55b3c012f7290e3e77c63b1a89`;
familiarity `original-ai-editorial-bee-v1`. The 98,144 accepted indexed words
include 3,636 AI editorial familiarity entries. The audit tests **every** one of
the 310 familiar-pangram letter sets with **all seven centers**: 2,170 candidates.
For each set the complete indexed matching word list is independently compared
with exhaustive filtering of the raw ENABLE dictionary, before center filtering.

Points are four letters = 1, otherwise word length, plus 7 for a pangram. The
absolute benchmark sums the best ten accepted words; the familiar benchmark sums
the best ten familiar words. Ties use descending points then ascending ASCII
word order. The tie order selects reproducible witnesses but cannot change the
benchmark points. Four raw candidates have fewer than ten accepted words; 328
have fewer than ten familiar words. Their sums use the available words and are
explicitly flagged; they cannot be mistaken for ten-word eligible boards. All
1,489 boards qualified by the current default filters have at least ten of each.

The machine report is
`tools/generation/bee/opening-benchmark-report.json`: every candidate, failures,
best-ten witnesses/points/familiarity/pangram markers, full length histograms,
long-word and pangram counts, maximum lengths, completion goals, distributions,
threshold retention and representative examples. Long means at least eight
letters in this report. Percentiles use the lower order statistic
`sorted[floor((n-1)*p)]`; comparisons use full integer benchmark totals rather
than rounded display ratios.

## Distributions

Current default qualification means >=12 familiar, 20–400 accepted, >=3 familiar
length buckets, a familiar four-letter word and a familiar pangram. It retains
1,489 boards across 283 letter sets. This audit does not change those filters.

| Metric | All 2,170: min / median / p95 / max | Current 1,489: min / median / p95 / max |
| --- | --- | --- |
| Accepted count | 4 / 152 / 504 / 1,035 | 34 / 169 / 351 / 398 |
| Familiar count | 1 / 20 / 47 / 96 | 12 / 21 / 40 / 67 |
| Completion goal | 1 / 14 / 33 / 68 | 9 / 15 / 28 / 47 |
| Absolute best-ten points | 30 / 110 / 169 / 207 | 67 / 113 / 164 / 207 |
| Familiar best-ten points | 14 / 56 / 69 / 79 | 27 / 59 / 69 / 79 |
| Familiar / absolute | 17.05% / 44.79% / 65.82% / 80.00% | 20.45% / 47.96% / 67.61% / 79.73% |
| Accepted maximum length | 7 / 10 / 14 / 17 | 7 / 10 / 14 / 17 |
| Familiar maximum length | 7 / 7 / 7 / 8 | 7 / 7 / 7 / 8 |
| Accepted words >=8 letters | 0 / 19 / 125 / 288 | 0 / 23 / 80 / 173 |
| Familiar words >=8 letters | 0 / 0 / 0 / 2 | 0 / 0 / 0 / 2 |
| Accepted pangrams | 1 / 3 / 18 / 54 | 1 / 3 / 13 / 54 |
| Familiar pangrams | 1 / 1 / 1 / 2 | 1 / 1 / 1 / 2 |
| Familiar entries in absolute top ten | 0 / 1 / 2 / 7 | 0 / 1 / 1 / 5 |

The absolute top ten are usually dominated by entries outside the editorial
shortlist. Even the largest familiar benchmark (79 points) is below the median
absolute benchmark (113). The normalizing denominator bounds mathematical
scores at 100; it does not establish that 100 is reasonable for a typical player.

Familiar pangrams are available by candidate construction, typically one, whereas
several accepted pangram variants can dominate the absolute benchmark. Familiar
long-word coverage is particularly thin: **1,439 of 1,489 qualified boards have
no familiar word of eight or more letters**. Familiar maximum length never exceeds
eight, while accepted words reach seventeen. This reflects shortlist coverage,
not measured player knowledge. Examples of ordinary-looking accepted words absent
from the familiarity shortlist include `ETIQUETTE`, `FORMAL` and `ROOMFUL`.
Absence must not be described as proof of obscurity.

## Original threshold proposal — not approved

This table applies a ratio floor **in addition to** current default qualification.
Counts are set/center identities, with distinct seven-letter sets shown separately.

| Minimum familiar / absolute | Boards | Letter sets | Current boards retained |
| --- | ---: | ---: | ---: |
| 50% | 671 | 155 | 45.06% |
| 55% | 454 | 116 | 30.49% |
| **60% — original unapproved proposal** | **266** | **77** | **17.86%** |
| 65% | 120 | 44 | 8.06% |
| 70% | 41 | 17 | 2.75% |
| 75% | 5 | 4 | 0.34% |
| 80%, 85%, 90%, 95%, 100% | 0 | 0 | 0% |

The original proposal was a **60% provisional minimum**, pinned to this exact
familiarity/source/profile version, for the initial scored pool. It would guarantee
that the best editorial-familiar ten achieve at least 60% of the absolute score,
while retaining 266 boards / 77 sets. It does **not** guarantee that arbitrary ten
familiar words reach 60%, or that the retained puzzles are human-fair. It removes
82.14% of current identities, so variety is already sharply reduced. Choosing 70%
offers a higher familiar ceiling but leaves only 41 boards / 17 sets, below a
100-start recent exclusion window; exhaustion must then remain honest. A 75%
floor leaves only five boards; 80% produces no current qualified board.

That proposed 60% compromise was **not a calibrated cutoff or a human acceptance
decision**, and the owner rejected the loss of 82% of the pool. No replacement
floor has been approved. Do not silently lower a future approved floor when
exclusions or search bounds exhaust its pool. Future frequency-backed inputs
need a new pinned familiarity version/hash and a fresh exhaustive audit; this
work does not depend on unfinished frequency tooling or alter its files.

At 60%, completion goals range 9–47, median 17 and p95 28. Four retained boards
reach their word-count goal before ten, fourteen at ten, and 248 after ten.
Across the unfiltered current pool those counts are 77 / 155 / 1,257. Therefore
goal completion and opening finalization must be modeled as separate milestones.
258 of the 266 retained boards still have no familiar >=8-letter word: the floor
does not repair the source's long-word coverage bias.

## Representative boards

| Board / center | Accepted / familiar / goal | Absolute / familiar points | Ratio | Accepted / familiar pangrams |
| --- | --- | --- | --- | --- |
| AEIMPST / I — weakest qualified | 330 / 13 / 10 | 132 / 27 | 20.45% | 6 / 1 |
| AEIJMNS / E — weak | 227 / 16 / 12 | 131 / 27 | 20.61% | 4 / 1 |
| ACDILOR / L — median | 120 / 22 / 16 | 98 / 47 | 47.96% | 2 / 1 |
| AFLMORU / R — strong | 79 / 22 / 16 | 78 / 60 | 76.92% | 1 / 1 |
| EILQTUY / I — strongest qualified | 34 / 13 / 10 | 74 / 59 | 79.73% | 1 / 1 |

AEIMPST/I's absolute top ten are `PSAMMITES` (16), `IMPASTES` (15), `PASTIMES`
(15), `PSAMMITE` (15), `IMPASTE` (14), `PASTIME` (14), `MISESTIMATES` (12),
`MISESTIMATE` (11), `EPISTASIES` (10), `METASTASIS` (10). Only `PASTIME` is
editorial-familiar. Its familiar top ten consist of `PASTIME` (14), `SPITE` (5)
and eight four-letter words (1 each): `EMIT`, `ITEM`, `MIME`, `MISS`, `MIST`,
`MITE`, `PIES`, `PIPE`. This is a large benchmark gap despite a valid familiar
pangram and attainable ten-word completion goal.

ACDILOR/L includes `CORDIAL` (14, familiar) but its absolute benchmark also uses
`CORALLOID` (16), `ACARICIDAL` (10), `CLADODIAL` (9), `COLLOIDAL` (9), and
five eight-letter words. Its familiar ten sum to 47, illustrating the median gap.

EILQTUY/I's absolute ten are `QUIETLY` (14), `ETIQUETTE` (9), `QUILLET` (7),
`TILLITE` (7), `UTILITY` (7), `EQUITY` (6), `ILLITE` (6), `LITTLE` (6),
`TITTIE` (6), `TITTLE` (6). Familiar ten sum to 59 using `QUIETLY`, `UTILITY`,
`EQUITY`, `LITTLE`, `QUIET`, `QUILL`, `QUILT`, `QUITE`, `TITLE`, `LILY`.
Even this strongest current board contains obscure/potentially sensitive accepted
entries and a missing ordinary-looking familiarity entry (`ETIQUETTE`). A high
ratio is an engineering screen, not vocabulary/sensitivity or human review.

## Provenance and remaining limits

ENABLE is the existing bundled public-domain vocabulary compiled by Alan Beale,
with M. Cooper and contributors; keep `dictionaries/ENABLE-NOTICE.txt` and existing
offline attribution. No new content source was downloaded or introduced.
Familiarity is original CLEAN Words AI editorial judgment: **zero human reviews,
zero empirical frequency evidence**. No word-frequency estimates, difficulty
calibration, phone-performance results or human fairness acceptance are claimed.
The owner's threshold decision and primary shared integration remain separate
from this completed corpus evaluation.

## Owner-directed generation rework — read-only exploration

Following the owner's response, further local calculations explored vocabulary
changes without changing the source, generator, audit script, report or scoring
implementation. The actual analysis used `exec_command` with PowerShell
here-strings piped to the installed `node` executable (`@' ... '@ | node`). The
one-off JavaScript read the existing TSV and benchmark report, enumerated the
same candidate set/centers, recomputed points and the existing quality criteria,
and printed the following scenario totals. It was not saved as a reusable tool;
no reusable command or checked-in scenario artifact is claimed. These are
exploratory calculations, separate from the reproducible pinned audit above.

### Length-capped acceptance scenarios

These scenarios apply a **new accepted-vocabulary rule**, accepting only words
at or below the cap, and intersect familiar words with that accepted set. The
benchmark still uses the best ten of **all accepted words** under that new rule;
there is no hidden larger accepted set for continued play. Current base quality
criteria are then reapplied, followed by the same unapproved 60% ratio for
comparison only. **No cap is approved, recommended for silent activation or
implemented by this exploration.**

| Accepted maximum length | Base-qualified boards / sets | Boards passing hypothetical 60% / sets | Actual old boards retained at 60% | Median base-qualified ratio |
| --- | --- | --- | --- | --- |
| 7 | 1,585 / 279 | 1,238 / 252 | 1,104 / 1,489 (74.14%) | 71.43% |
| 8 | 1,555 / 287 | 704 / 175 | 685 / 1,489 (46.00%) | 58.49% |
| 9 | 1,520 / 284 | 425 / 112 | 424 / 1,489 (28.48%) | 53.10% |
| 10 | 1,501 / 283 | 316 / 87 | 315 / 1,489 (21.16%) | 50.00% |
| Original 32 | 1,489 / 283 | 266 / 77 | 266 / 1,489 (17.86%) | 47.96% |

A cap of seven gives 1,238 surviving new-policy boards, a count equal to 83.14%
of the old pool's size. That is **not 83.14% retention of the same boards**:
1,104 old boards survive and 134 previously ineligible boards become eligible.
Among the 1,585 newly base-qualified boards, 78.11% pass the hypothetical ratio.
Forty-five old boards lose base qualification under the seven-letter cap, and
38 old-board hypothetical completion goals change when familiar eight-letter
words are removed. Existing saved puzzles would remain unchanged in any such
versioned experiment; these counts describe only hypothetical new puzzles.

Seven-letter acceptance removes an average of 30.01 accepted words per old board
and forbids every eight-or-more-letter word, including ordinary words players
may reasonably try. Caps of eight, nine and ten remove averages of 11.98, 4.90
and 1.78 words respectively, with no current familiar-count goal changes for old
boards, but they recover much less variety. A cap makes the ratio look better
by restricting vocabulary rather than establishing familiarity. Allowing those
removed words after the opening while excluding them from the benchmark would
contradict the specified best-ten-of-all-accepted benchmark.

A separate experiment allowed only the highest-scoring familiar pangram per
board while retaining all non-pangram accepted words. It retained 736 old boards
across 192 sets at the hypothetical 60% floor, removing an average of 3.83
pangram words per board. This still loses about half the old pool and would
reject legitimate pangram variants. It also needs a new explicit accepted-word
policy and owner approval; it is not a hidden scoring adjustment.

### Familiarity coverage and its real work

An optimistic mathematical exercise preserves the complete accepted vocabulary
and asks how many currently unmarked words per board would need to become
familiar to reach 60%. Adding the highest-point missing words first minimizes
the required count for each individual board. It requires 0–4 additions per
old board (median 2, p95 3, mean 1.60). The union of these greedy choices is
645 distinct words; marking the entire union familiar suffices for all 1,489
old boards at that hypothetical ratio. This union is sufficient, **not proven
globally minimal**, and is not evidence those words are familiar.

Of the 645 additions, 612 have eight or more letters: 129 length eight, 169
length nine, 164 length ten, 79 length eleven, and 71 length twelve or longer.
Examples include `MENINGOCOCCIC`, `SENSELESSNESSES`, `POSSESSEDNESSES`,
`SPLENDIDNESSES` and `SMARMINESSES`. Automatically relabeling these to hit a
score target would be dishonest. Conversely, missing ordinary-looking entries
show the present shortlist is incomplete; absence is not evidence of obscurity.
If the entire 645-word union were genuinely supported, the corresponding new
familiar-count completion goals would increase by median 2, p95 4 and maximum
7 words, another reason to review the full policy rather than relabel for score.

The current pool contains 6,638 distinct accepted words of eight or more letters
outside the familiar shortlist. Even calling every one familiar would bring
only 1,481 of 1,489 old boards to 60%; some gaps also concern shorter words and
pangram variants. This is a bound on what hypothetical reclassification could
achieve, not a proposal to mark thousands of unreviewed words familiar.

### Current direction and decision boundary

The owner selected preservation of complete ENABLE acceptance while expanding
and reviewing familiarity coverage, then repeating exhaustive evaluation before
choosing a scored-play filter. Scored generation remains gated. An alternative
owner-curated accepted bank was not selected and would directly change which
words players can submit; it would require the owner's explicit choice, a
separate pinned acceptance version, full snapshot preservation and a new audit.
No new acceptance bank or capping policy has been selected here.

The current candidate universe already includes every familiar seven-letter
pangram mask and every center. Changing the shuffle or retry mechanism alone
cannot recover more qualifying identities from this exhausted universe. More
variety requires supported additional vocabulary/candidate sets, changed
acceptance, or another owner-approved quality policy; it cannot be obtained by
claiming a new selection mechanism improves the existing ratio distribution.
Future pinned frequency-backed familiarity remains a supported input boundary,
but unfinished frequency work is not a dependency and its evidence must not be
invented. Source review, owner direction, a fresh quality decision and human
fairness acceptance remain open.
