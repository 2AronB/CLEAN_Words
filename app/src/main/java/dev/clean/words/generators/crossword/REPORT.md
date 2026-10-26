# Crossword qualification — 2026-10-26

Final source is 394 original AI-authored answers / 788 clues: 211 three-letter,
50 four-letter and 133 five-letter records. There are 16 structurally varied
fillable masks per size (32 total). Production profile is 7×7, with 10–12 entries
instead of the six-entry original starter. No finished-grid catalog is shipped.
The 5×5 profile works and was qualified first; it remains an internal supported
profile, with no extra user-facing size selector.

Source JSON SHA-256:
`5d0771002642a4fcd841da1e1c6ef415b6e2469fb9e8cc9e3e5f31d2a568d690`.
Canonical typed SHA-256:
`1a93c3ac57d2db83686e7f79f35577edeb09b36007cb237335dd55ec3ae95aad`.
Generator `crossword-crossing-dfs-v1`, policy
`original-pilot-mrv-java-random-7x7-v1`. The hash equality regression passes.

## Final actual-source qualification

Each row is 100 fixed seeds 0–99 using the real compiled pilot. No request failed
or was cancelled. Timing is host JVM microseconds divided by 1,000 below, not a
physical-phone performance claim. Exact raw output, including every reused
answer/clue count, is `tools/generation/crossword/qualification.txt`.

| Profile | Distinct fills | Distinct clue snapshots | Layouts used | Answers used | Clues used | Max search states |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| 5×5, no history | 81 | 100 | 15 | 194 | 281 | 1,083 |
| 7×7, no history | 90 | 100 | 15 | 241 | 380 | 7,781 |
| 7×7, rolling latest 100 | 100 | 100 | 15 | 293 | 455 | 6,508 |

The final diagnostics rerun also passed all 300 seeds, recording these host JVM
latencies and actual internal candidate rejections:

| Profile | Median ms | p95 ms | Max ms | Crossing dead ends | Recent fills rejected | Unfillable templates |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| 5×5, no history | 6.481 | 40.172 | 75.140 | 9,943 | 0 | 0 |
| 7×7, no history | 4.220 | 25.360 | 78.258 | 28,410 | 0 | 0 |
| 7×7, rolling latest 100 | 7.384 | 108.011 | 391.087 | 37,979 | 18 | 2 |

These rejected partial fills/templates never became started attempts. Budgets
stayed at 250,000 states / 32 template attempts; no quality constraints were
weakened. Runtime diagnostics distinguish cancellation, invalid source/profile,
impossible constraints, recent-pool exhaustion and exceeded search budgets.

Without history, multiple seeds can produce one fill with different clues. Those
are counted as repeated fills in this report. Rolling history hard-excludes the
filled grid regardless of changed clues, and forbids the immediately previous
board's exact clue text. Every generated board independently passed numbering,
crossing, connected-cell coverage, source clue/sense matching and answer uniqueness
checks. Minimum entry counts were six for 5×5 and ten for 7×7. All 32 masks have
an independently found fill; 15 per size happened to be selected by these seeds.

Answer/clue repeats are real: the rolling run used 293 of 394 answers and 455 of
788 clues, so it reuses vocabulary/clues outside the immediate-clue constraint.
Two clues often describe the same fact in different words; this is a finite pilot,
not a promise of long-term novelty or calibrated medium-plus difficulty.

## Commands and verification

Passed source preparation/check and independent JavaScript smoke:

```
node tools/generation/crossword/prepare-bank.mjs
node tools/generation/crossword/prepare-bank.mjs --check
node tools/generation/crossword/check-fill.mjs
```

The authoring tool generates up to 512 legal candidate masks per size, tries
bounded actual-source fills and retains the first 16 qualifying masks. Invalid
geometry and unfillable masks are rejected rather than forcing invented answers.
No filled-grid output is stored in runtime inputs. Independent JavaScript smoke
passed all 32 retained masks, with worst 8,644 states in that deterministic scan.

Primary coordinated Gradle checks: focused generator tests, live crossword/input
regressions and adapter checks passed; final full JVM suite passed 182 tests.
`CrosswordGeneratorTest` includes deterministic output, malformed/duplicate source,
missing and contradictory clue senses, impossible crossings, strict candidate/step
budgets, cancellation, exhausted clue cooldowns, no repeated fill, and reconstructed
legacy signatures. `CrosswordQualificationTest` independently scans snapshots and
tests the 300-seed matrix above. Shared/live tests retain old 25-cell behavior and
verify 49-cell editing/checking/result compatibility. Final Android/build/lint and
delivered artifact evidence are maintained by primary in `docs/VERIFICATION.md`.

## Editorial and remaining limits

Original content was checked mechanically and by a separate AI reviewer, including
all 394 pairs across the original and supplemental inputs; AGO/ACORN/ALBUM wording was corrected in the final source.
Zero human editorial reviews or calibrated difficulty observations are available.
The 4-letter vocabulary is still alphabetically narrow (mostly A–C), with only
50 entries. No 6–7-letter clue bank is supplied. The present 7×7 size adds connected
clues and letters using 3–5-letter words; not every letter is checked in both
directions and masks need not be symmetric. These are explicit pilot choices.

Mechanical solvability does not establish clue fairness, enjoyment, accessibility
or physical-phone comfort. Human review of clues/solve length, both keyboards,
large text and actual-phone layouts remains open. Existing saved snapshots and
daily precedence must stay unchanged on update; no storage reset is permitted.
