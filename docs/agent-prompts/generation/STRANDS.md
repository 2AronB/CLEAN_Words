# Implement Strands generation

Follow COMMON.md. This task authorizes theme-based board composition and layout
generation while preserving F1 path safety and existing saved puzzles.

OWN ONLY:
- app/src/main/java/dev/clean/words/generators/strands/
- app/src/test/java/dev/clean/words/generators/strands/
- app/src/main/assets/generation/strands/
- tools/generation/strands/
Write provenance notes in the generator folder; preserve all existing path UI/rules.

A word list alone is insufficient. Build/consume original or properly licensed
theme records containing a coherent clue/explanation, theme vocabulary with
intended senses, eligible spanning answers, familiarity/review evidence and
provenance. Do not label unrelated random words a theme or copy published puzzles.
Start with a 20-theme pilot, expand where quality permits, and report actual
approved/provisional counts instead of claiming unlimited thematic variety.

Implement StrandsGenerator: select a coherent answer set whose lengths match a
supported board area, then construct disjoint neighboring paths covering every
cell exactly once. Include diagonals under existing rules; actual spanning-answer
path must touch opposing edges. Respect current solver/bit-mask board-size limits.
Use supported small dimensions initially; do not silently enlarge beyond safety
limits. Freeze letters/clue/answers/paths and versions into a compatible record.

Vary geometry meaningfully, including turns/interlocking paths; do not generate
only a repeated obvious row/snake arrangement with different labels. Report unique
theme/answer sets separately from layout variants of those same answers. Novelty
and recent exclusion must not count shuffled metadata as a new puzzle.

Use bounded, cancellable search, structural validation and a full-cover solver
independent where practical from the placement builder. Test that intended paths
solve the board and F1 alternate-route checks still accept viable rerouting and
reject routes that strand remaining words. Preserve Undo and old snapshots; no
silent replacement with a new board after interruption or a source update.

Run at least 100 seeds from the actual bank. Report answer-set/layout uniqueness,
theme reuse, generation success/rejection rates, worst search and latency. Include
impossible length totals, repeated-letter traps, invalid spanning routes, tight
budgets, cancellation and source-bank exhaustion. The theme clue's fairness
requires separate human review even when exact-cover checks pass.

Deliver the working composer/solver pipeline, real pilot data, source/report and
integration API. If varied good themes are the limiting input, state that clearly
after completing available algorithm work; do not present generated word soup as
equivalent to authored theme content or claim near-infinite reviewed puzzles.
