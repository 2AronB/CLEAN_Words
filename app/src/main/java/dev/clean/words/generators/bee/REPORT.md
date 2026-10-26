# Bee source and quality report

Current activation (2026-10-26): new scored daily/additional play is now integrated
under the owner's explicit no-additional-ratio-filter choice, with original
inputs/full ENABLE acceptance. The optional supplement is not loaded; score
fairness remains provisional. Earlier gated-generation statements below record
the original side-task delivery. Primary checks: docs/VERIFICATION.md.

This report records the preserved v2 source and original generator. The
2026-10-26 ten-word opening implementation and exhaustive benchmark are documented
in [OPENING-EVALUATION.md](OPENING-EVALUATION.md) and
[INTEGRATION.md](INTEGRATION.md). The owner chose full ENABLE acceptance with
expanded/reviewed familiarity before selecting a filter; scored generation stays
gated. [FAMILIARITY-REVIEW.md](FAMILIARITY-REVIEW.md) describes the separate optional
346-word AI editorial supplement, its pins and limitations. It does not replace
this original source, rewrite old attempts or establish human familiarity.

Prepared 2026-10-25. Source pipeline command:

```powershell
node tools/generation/bee/prepare-index.mjs
node tools/generation/bee/prepare-index.mjs --check
```

Both commands ran successfully. The second independently regenerates all output
bytes in memory and verifies the checked-in index, metadata, policy and report.
No network access, new dependency or download was used. Runtime/JVM/Android
results must be recorded from the primary's coordinated build, not inferred from
preprocessing.

## Provenance and normalization

Existing source: [ENABLE enable1.txt](https://github.com/dolph/dictionary/blob/master/enable1.txt),
previously acquired by the project on 2026-10-25. Public-domain status and credit
are in the existing bundled `dictionaries/ENABLE-NOTICE.txt`: Alan Beale, with
M. Cooper and contributors. Source SHA-256:
`3f16130220645692ed49c7134e24a18504c2ca55b3c012f7290e3e77c63b1a89`.

Pinned policy `enable-ascii-fourplus-v1`: lowercase ASCII source entries, lengths
4–32, uppercase, deduplicate, sort. This gives 171,755 dictionary entries. Words
with more than seven distinct letters cannot match seven-letter boards and are
omitted from the index without changing acceptance on any seven-letter puzzle.
The remaining 98,144 words span 37,313 masks. Inflections, plurals, obscure,
archaic and potentially sensitive ENABLE entries remain accepted; there is no
unstated frequency/proper-name filter. This matches broad existing Bee play and
does not require those uncommon words for the goal. Narrowing acceptance later
requires a new inclusion/source policy; existing snapshots keep their words.

Familiarity is a **separate original AI editorial shortlist**, not evidence from
dictionary membership. Four/six/longer entries were authored in
`tools/generation/bee/familiar-draft.json`; five-letter entries were copied from
the original Wordish answer shortlist, not a published puzzle sequence. The
source hashes and explicit rejected candidate reasons are in
`tools/generation/bee/validation-report.json` and bundled `generation/bee/policy.json`.
Obvious doubtful, sensitive or proper-name-only familiarity candidates were
excluded explicitly. Shortlist entries include ordinary inflections and
common-word senses, without claiming usage frequencies. Every retained familiar
entry is accepted by the pinned dictionary; that test establishes validation,
not its familiarity. **3,636 AI editorial provisional words, zero human-reviewed,
zero empirical familiarity/frequency ratings**. The 33 rejected draft occurrences
include 16 words impossible in the seven-letter profile; all are recorded.

Index version `bee-mask-index-v1`; SHA-256:
`d9dcf290c0c057c0df9277bc1be855771336445991da6f09e60133829ba20143`.
Bundled TSV records mask/word/familiarity marker, not finished puzzle records.

## Exhaustive default inventory

The original shortlist supplies 310 seven-distinct letter sets. Testing every
center gives 2,170 meaningful candidates. The documented provisional default
filters admit **1,489 set/center identities across 283 sets**, rejecting 681
candidates with explicit reasons in the report. Some rejected candidates have
multiple failing criteria. Accepted counts range 34–398, familiar counts 12–67,
and frozen ceil(70%) goals 9–47. Every admitted candidate has a familiar pangram,
a familiar four-letter word, and at least three familiar word lengths. Goals are
attainable using only familiar options by construction, but human solve comfort
and word familiarity remain unverified. Goals at the high end may need a later
versioned profile adjustment after feedback.

## Test and evaluation entry point

Primary coordinates the existing Gradle process:

```powershell
.\gradlew.bat --offline :app:testDebugUnitTest --tests dev.clean.words.generators.bee.BeeGeneratorTest
```

Tests cover packaged index equality against an independent exhaustive raw
dictionary filter, reproduction/canonical signatures, malformed/empty/tiny
banks, bounded-search status, cancellation during preparation/generation,
recent exclusions, independent pangrams/points/length bars, duplicates, completion
using entirely non-shortlist accepted words, copied inputs, full snapshot codec
and old v1/v2 results. The benchmark runs 1,000 real seeds, independently filters
the raw dictionary for every distinct output, and prints distinct sets/centers,
failures, goal attainability, accepted/familiar/goal distributions, rejection
reasons, preparation time, p50/p95/max generation latency and worst candidate
inspection. Wall-time results belong to the actual run and are not promised phone
performance. Android/UI and human fairness results remain primary/user work.

## Recorded JVM evaluation — 2026-10-25

Primary's coordinated offline Gradle unit/build/lint/Android-test compilation run
passed all seven `BeeGeneratorTest` tests. The actual JUnit XML benchmark reports:

| Measure | Result |
| --- | --- |
| Trials / failures / unattainable goals | 1,000 / 0 / 0 |
| Distinct set/center identities / distinct letter sets | 719 / 266 |
| Source words / candidate sets | 98,144 / 310 |
| Index preparation in this JVM run | 917,690 microseconds |
| Generation p50 / p95 / maximum | 672 / 2,714 / 83,434 microseconds |
| Worst candidates inspected | 6 |
| Accepted words minimum / p50 / p95 / maximum | 37 / 173 / 351 / 397 |
| Familiar options minimum / p50 / p95 / maximum | 12 / 21 / 38 / 61 |
| Visible goal minimum / p50 / p95 / maximum | 9 / 15 / 27 / 43 |

The 1,000 trials encountered rejected candidates as well as successes:
accepted-count 171, familiar-length-mix 43, no-familiar-four 18, and
too-few-familiar 278. A candidate may fail several criteria. No criterion was
relaxed. An independent exhaustive source filter matched every distinct output's
entire accepted set. These are development JVM timings and semantic variety,
not phone-performance or human-fairness acceptance. Module Android behavior is
verified only through the primary's separately recorded integration checks.
