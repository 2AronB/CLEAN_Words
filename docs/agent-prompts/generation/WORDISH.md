# Implement Wordish generation

Follow COMMON.md. This task authorizes implementing this module-local generator
and tests, not the other games or shared core. Concurrent agents may be active.

OWN ONLY:
- app/src/main/java/dev/clean/words/generators/wordish/
- app/src/test/java/dev/clean/words/generators/wordish/
- app/src/main/assets/generation/wordish/
- tools/generation/wordish/
Write provenance notes in the generator folder; preserve all existing live game files.

Replace the ten-answer limitation with deterministic seeded selection from a
substantially larger vetted five-letter answer vocabulary. Keep broad accepted
guesses separate from answer eligibility. Existing ENABLE-derived 8,636 guesses
are useful validation data, not evidence that every word is a fair answer.
Do not generate random strings or select obscure entries indiscriminately.

Build a versioned answer bank with spelling, eligibility, familiarity/review
evidence, inflection/proper-name/sensitive-content policy and provenance. Aim for
at least 500 sensible distinct answers initially; report the actual reviewed vs
provisional counts and any source limitations rather than padding with bad words.
Free reusable sources may support this under standing content permission. If
familiarity data is missing, use an explicitly curated original shortlist and
mark difficulty provisional; do not substitute dictionary membership for frequency.

Implement WordishGenerator with a standard single-board/five-letter profile,
stable meaningful answer signature and recent-answer exclusions. Return a full
PuzzleRecord compatible with WordishRules.validate: answer, recognized guess
policy/version and origin, plus generator provenance/version. Preserve all old
ten-answer snapshots and exact duplicate-letter/guess-budget behavior. Expanding
guess policy requires a new pinned version with legacy support, not replacement.

Run at least 1,000 deterministic seed trials, reporting actual distinct answers,
repeat-exclusion behavior and generation latency. Test tiny/exhausted banks,
same seed/profile/input reproduction, unknown dictionary policy, every eligible
answer accepted as a guess, cancellation and immutable old snapshots. Include
fixtures involving repeated letters and strongest keyboard feedback regressions.

Six-board shared-guess play remains a later profile: document how to draw six
distinct answers without changing the standard game, but do not silently add a
new shared guess budget/UI in this task.

Deliver the real seeded selector/data pipeline and tests, source/quality report
and integration entry-point instructions. Primary wires daily and additional
attempt creation; do not replace GameModule.puzzles with a larger static schedule
and call that on-demand generation.
