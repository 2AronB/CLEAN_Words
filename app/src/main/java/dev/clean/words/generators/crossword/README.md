# Crossita crossing generator

Pure Kotlin, offline bounded seeded constraint filling. `CrosswordGenerator(bank)`
accepts `generate(seed, CrosswordProfile(), recentSignatures, cancelled)` and returns
`CrosswordGenerationResult.Success`, `NoCandidate(reason)` or `Cancelled`.
Default dimensions are 7×7; a 5×5 profile remains supported for qualification.
There is no completed-board catalog. Bundled inputs are answer/sense/clue records
and blank grid masks, filled at request time using crossing constraints.

The original pilot is AI-authored, general American English, and has zero human
reviews. `tools/generation/crossword/pilot.tsv` is the editorial source. It contains
two clue approaches for each answer. Some are two ways of describing the same
sense, not two independent pieces of trivia. Familiarity and difficulty are
uncalibrated. No external clue corpus, service, license dependency or download is
used. Original authorship is provenance, not a claim of clue quality certification.

Templates span their full dimensions and have connected open cells, both
directions, minimum three-letter runs and complete clue coverage. Single-cell
cross-runs are not entries; cells need not be checked in both directions. Grid
symmetry is not required. Larger masks use 10–12 clues of 3–5 letters, making more
work than the six-clue starter without forcing obscure long answers. The pilot
bank contains 3–5-letter answers; longer-answer content is future expansion.
Size means board size, not a promise of seven-letter answers.

Search uses Java Random under pinned versions, minimum remaining candidates,
unique answers, 250,000 recursive states and at most 32 template attempts. The
caller may lower limits; invalid/oversized profiles and sources fail explicitly.
Cancellation is checked before work and within search. No file or UI work occurs
inside this generator. The runtime must call it on the existing generation lane.

Snapshots freeze dimensions, grid, actual clue text, clue/answer/sense identities,
rule/profile/generator/selection/source versions and hashes. Existing 25-cell
records without dimensions remain 5×5. Resume uses snapshots, never this generator.

`semanticSignature` reconstructs legacy records as well as generated ones. It
contains a dimension tag, filled-grid hash, sorted answer texts and corresponding
clue-text hashes. Recent exact fills are excluded even if clues change. The
immediately preceding board's exact clues are forbidden; answers and clues from
the wider recent window are softer selection preferences. Recent `Set` iteration
must be newest-first; the existing AppSession uses ordered `toSet()` after sorting.
Daily generation passes no history, retaining deterministic daily scheduling.
Old answers can recur with other clues. A finite pilot cannot promise endless
novelty; failure/exhaustion never weakens constraints or opens a starter silently.

Qualification and limitations are in REPORT.md. Next bounded content work:
human review/correction, broader answer coverage and natural genuinely different
clues, then qualified longer-answer layouts. No numeric difficulty is advertised.
