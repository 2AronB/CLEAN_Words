# Familiarity supplement: one bounded editorial pass

Primary activation update (2026-10-26): owner subsequently approved ten-word
scoring with no additional ratio filter, using the original familiarity input.
This supplement remains optional and is not loaded into active generation. The
earlier gated-play decision below describes this review's original context.

Owner direction on 2026-10-26: **Preserve full ENABLE acceptance and expand/review
familiarity coverage before choosing a filter. Scored generation stays gated
meanwhile.** This delivery supplies an optional, pinned input and comparative
audit. It does not select a scored-play threshold, activate generated v3 play,
change the original index or modify existing player snapshots/results.

## What was reviewed

This original CLEAN Words AI editorial pass proposes **346 additions**, including
345 words of eight or more letters. The intent is to fill obvious general-use
vocabulary omissions: everyday compounds such as `HEADACHE`, `BOOKCASE` and
`ETIQUETTE`; ordinary long nouns such as `AGREEMENT`, `REPETITION` and
`THERMOMETER`; transparent common inflections such as `ARRANGING`, `GARDENING`
and `REMEMBERED`; and ordinary descriptive words such as `SLEEPLESS` and
`HELPLESS`. Each addition carries its group's explicit ordinary-word rationale
and individual `humanReview: pending` / `frequencyEvidence: none` markers in
the generated report. This is **AI editorial judgment, not empirical familiarity
or a human-approved word bank**. There are zero human reviews or frequency
ratings. No sensitivity or human-fairness certification is implied.

The bounded draft contains 347 candidates. `UNNECESSARY` is rejected because it
uses more than seven distinct letters and cannot occur on a seven-letter board.
Four tempting benchmark terms are explicitly excluded from the familiarity
proposal: specialist `MENINGOCOCCIC` and awkward derivatives
`SENSELESSNESSES`, `POSSESSEDNESSES`, `SMARMINESSES`. They remain accepted
where the unchanged ENABLE policy permits them. No automatic promotion was used
to reach a ratio, and no additional editorial passes were run to chase a target.

Word lengths among additions: one at length 7, 93 at 8, 158 at 9, 68 at 10,
22 at 11, two at 12 and two at 13. All are uppercase ASCII accepted entries in
the original pinned ENABLE index, have at most seven distinct letters and were
absent from the original familiarity list. The supplement is sorted, unique and
additive: 3,636 original entries become 3,982 optional augmented familiar entries.

Files:

- `tools/generation/bee/familiarity-supplement-draft.json`: original judgments
  and explicit exclusions, grouped by ordinary-use rationale.
- `tools/generation/bee/prepare-familiarity-supplement.mjs`: offline Node built-in
  validation, deterministic supplemental TSV/report and exhaustive comparison.
- `tools/generation/bee/familiarity-supplement-report.json`: metadata, individual
  judgments/rejections, distributions, threshold comparisons and all 3,066
  expanded set/center evaluations.
- `app/src/main/assets/generation/bee/familiarity-supplement-v2.tsv`: exact
  `word\n` header, sorted addition per LF-delimited line and final LF.

## Input boundary for the primary loader

| Field | Exact value |
| --- | --- |
| Supplement version / composite familiarity version | `original-ai-editorial-bee-supplement-v2` |
| Evidence label | `original-ai-editorial; human-review-pending; frequency-unknown` |
| Asset path | `generation/bee/familiarity-supplement-v2.tsv` |
| Supplemental word count | `346` |
| Original index SHA-256 (`base.sourceHash`) | `d9dcf290c0c057c0df9277bc1be855771336445991da6f09e60133829ba20143` |
| Supplement SHA-256 | `ef14949d1b720d6360d9d5e064c2199e3b85c07d014cbb8158b797592c0c6119` |
| Composite source/familiarity fingerprint | `89251ac1abf00f299e89fd76aa12aa3f6ef4c2d559e2b311ab70100f0128e127` |
| ENABLE SHA-256 | `3f16130220645692ed49c7134e24a18504c2ca55b3c012f7290e3e77c63b1a89` |

The exact composite recipe is:

```text
SHA256([base.sourceHash, supplementSha256, version, evidence].join("|"))
```

All strings are UTF-8 with no added trailing newline in that fingerprint input.
The supplement hash covers the exact TSV bytes, including its final LF. The
report also pins draft SHA-256
`ea3e1d219f3959d9fbffdc0dfcb9da5f5bb4a9e8f58c1b967d0ecb52c9c48f18`.
The Bee-local `BeeFamiliaritySupplement.prepare` implements this pinned boundary;
`BeeEditorialSupplement` exposes the exact optional asset/input/composite pins.
It verifies hashes/count, LF format, unique additions and unchanged acceptance;
mismatches fail honestly. The generated TSV is sorted and its exact hash is
verified. Seven supplement JVM tests passed in the final coordinated run, including
the real artifact and an independent raw ENABLE acceptance comparison. Shared
loading/activation remains primary-owned and gated on the owner's future choice.

Accepted vocabulary remains the complete original ENABLE matching set, including
long words and pangram variants. An optional augmented-input puzzle must freeze
its full accepted and familiar sets, augmented completion goal, input/evidence
versions and hashes in its new snapshot. Previously saved v1/v2/v3 snapshots keep
their original accepted/familiar sets, goal, state and scoring. No result is
recomputed using this supplement merely because the app updates.

## Exhaustive comparison

The script evaluates every original 310 sets / 2,170 centers, then every augmented
438 sets / 3,066 centers. The 128 additional candidate sets come from supplementary
words with exactly seven distinct letters. For every expanded set, indexed
acceptance is independently matched against exhaustive filtering of raw ENABLE.
The original base filters remain >=12 familiar words, 20–400 accepted words,
>=3 familiar length buckets, a familiar four-letter word and a familiar pangram.
Benchmark points remain four letters = 1, otherwise length, with +7 for pangrams;
all accepted words participate in the absolute best ten. Ties use points
descending and ASCII word ascending. Percentiles use lower order statistics.

The original 1,489 qualified boards remain base-qualified. Augmenting familiarity
on the original sets adds 18 base-qualified boards; new sets contribute 685 more.
The expanded base-qualified inventory is 2,192 boards / 406 distinct letter sets.
This counts prospective identities, not reviewed human-fair puzzles.

| Measure | Original 1,489, original familiarity | Same 1,489, augmented familiarity | Expanded 2,192 base-qualified boards |
| --- | --- | --- | --- |
| Accepted points min / median / p95 / max | 67 / 113 / 164 / 207 | 67 / 113 / 164 / 207 | 67 / 115 / 165 / 207 |
| Familiar points min / median / p95 / max | 27 / 59 / 69 / 79 | 27 / 63 / 87 / 112 | 27 / 64 / 87 / 112 |
| Ratio min / median / p95 / max | 20.45% / 47.96% / 67.61% / 79.73% | 20.61% / 54.48% / 76.34% / 90.54% | 20.61% / 53.72% / 74.44% / 90.54% |
| Familiar count min / median / p95 / max | 12 / 21 / 40 / 67 | 12 / 22 / 42 / 69 | 12 / 23 / 42 / 69 |
| Completion goal min / median / p95 / max | 9 / 15 / 28 / 47 | 9 / 16 / 30 / 49 | 9 / 17 / 30 / 49 |
| Boards with no familiar word >=8 letters | 1,439 | 651 | 651 |
| Familiar >=8-letter count min / median / p95 / max | 0 / 0 / 0 / 2 | 0 / 1 / 4 / 10 | 0 / 1 / 4 / 12 |

On all original 2,170 candidates, including base-filter failures, the median ratio
moves from 44.79% to 49.59%. The report supplies these full-pool distributions
separately so favorable qualified samples do not obscure the whole inventory.

The table below is comparison only: **no listed floor is approved or active**.

| Hypothetical ratio floor | Original pool with old familiarity: boards / sets | Same old pool with supplement: boards / sets | Expanded base-qualified pool with supplement: boards / sets |
| --- | --- | --- | --- |
| 50% | 671 / 155 | 928 / 194 | 1,353 / 287 |
| 60% | 266 / 77 | 531 / 126 | 701 / 166 |
| 70% | 41 / 17 | 190 / 53 | 228 / 67 |
| 80% | 0 / 0 | 43 / 13 | 44 / 14 |

The supplement improves coverage but **does not solve the rejected-pool problem**.
A hypothetical 60% floor would still reject 958 of the original 1,489 boards
(64.34%) or 1,491 of the expanded 2,192 base-qualified boards (68.02%). More
prospective candidates do not establish fairness. Scored generation remains
gated while content review and an owner-approved quality policy are unresolved.

New familiar words also increase prospective completion goals. On the original
qualified cohort, 705 boards' goals would change; the increase is median 0,
p95 3 and maximum 7 words. This is an effect on future snapshots using the new
input only; it must never alter goals in existing saved play.

## Representative effects and remaining review

- `EILQTUY` / I gains `ETIQUETTE`: absolute benchmark stays 74, familiar best-ten
  goes from 59 to 67 (79.73% to 90.54%), goal stays 10.
- `AEIMPST` / I gains ordinary plural `PASTIMES`: absolute stays 132, familiar
  goes from 27 to 41 (20.45% to 31.06%). Its benchmark gap remains large; the
  supplement does not promote specialist `PSAMMITE` or `EPISTASIES` to fix it.
- `ACELNRT` / A gains `TATTLETALE`, `CLEARANCE`, `ENTRANCE`: absolute stays 120,
  familiar goes 71 to 80 (59.17% to 66.67%); prospective goal goes 38 to 40.
- `ADEGINR` / A gains ordinary gardening/arranging inflections: absolute stays
  164, familiar goes 64 to 95 (39.02% to 57.93%); prospective goal goes 26 to 31.

Future work needs review of retained vocabulary, actual player familiarity and
goal comfort, with a fresh audit before choosing a filter. The ordinary-word
judgments themselves are reviewable and may need correction; they are not
empirical evidence. A future frequency-backed supplement must have its own
evidence, license/provenance, version/hash and frozen input boundary. Unfinished
frequency work remains optional and is not a dependency for this delivery.

## Commands actually executed

From the repository root, both succeeded:

```powershell
node tools/generation/bee/prepare-familiarity-supplement.mjs
node tools/generation/bee/prepare-familiarity-supplement.mjs --check
```

The write/check sequence was repeated after aligning the composite fingerprint
recipe with the primary loader contract. The second command recomputes all
output bytes and checks exact equality without writing. Separate Node JSON reads
confirmed metadata, rejection reasons and comparison counts. Scoped
`git diff --check` and local Markdown link validation passed. No Gradle build,
Android/device operation, dependency installation, network access, staging,
commit or push was performed for this editorial package.

ENABLE credit remains the existing public-domain source compiled by Alan Beale
with M. Cooper and contributors; retain the existing bundled notice. These
familiarity additions are original CLEAN Words AI editorial content, not a new
third-party dataset. Primary offline credits should describe the optional
3,982-entry augmented familiarity input accurately if it is integrated, retain
the AI/human-pending/frequency-unknown caveats and avoid claiming shipped closure.
