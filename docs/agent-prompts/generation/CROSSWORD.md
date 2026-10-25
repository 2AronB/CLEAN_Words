# Implement mini-crossword generation

Follow COMMON.md and docs/CROSSWORD-EXPANSION.md. This task authorizes unthemed
generated fills, varied grid layouts and a clue bank. Preserve F1 typing fixes.

OWN ONLY:
- app/src/main/java/dev/clean/words/generators/crossword/
- app/src/test/java/dev/clean/words/generators/crossword/
- app/src/main/assets/generation/crossword/
- tools/generation/crossword/
- app/src/androidTest/java/dev/clean/words/generators/crossword/ if needed.

Do not edit the live games/crossword module, its existing tests or CONTENT.md:
an input/UI owner may be changing them concurrently. Put dimension-aware models,
validators and proposed adaptation instructions in your generator folder. Primary
integrates live model/UI changes after coordinating ownership; do not race them.

The current ten 5x5 boards share one pattern; this is not a crossword constraint.
Implement seeded offline filling from vetted answers and sense-matched clue pairs.
ENABLE has spellings, not clues. Find suitable reusable clue/definition resources
with actual content rights or author original pairs. Record attribution/changes.
Definitions are candidate clues, not automatically good crossword writing.

Use answer IDs/text/length/sense/dialect/familiarity/allowedness, and multiple
distinct clue IDs per answer/sense with style/difficulty/provenance/review. Aim
first for a pilot of 200 vetted answers and two good clues where natural; report
shortfalls. This pilot is not a promise of long-term novelty. Prefer separate
recent-clue and recent-answer cooldowns, avoiding immediate exact-clue repeats.
Paraphrases of one clue are not strong novelty; common short answers will recur.

Generate or choose from at least ten structurally varied legal 5x5 grid templates,
then fill using crossing constraints. Templates/indexes are inputs; do not ship
only another preset list of finished grids. Require connected open cells, valid
word lengths, consistent numbering, accurate crossings, clue coverage and no
duplicate answer in one board. Reject poor fills instead of forcing obscure words,
false clues or arbitrary abbreviations. Theme-free does not mean quality-free.

Make dimension handling explicit and preserve legacy 25-cell snapshots as 5x5.
Get generated 5x5 working first; then support 7x7 in the generator's model if
the same quality/performance checks pass. Do not assume current game/UI index
arithmetic works for other dimensions. Hand primary a concrete dimension-aware
adaptation plan; it owns live model/UI changes and profile exposure.
Preserve rapid input/paste, auto-clue advance without Check, backward deletion,
built-in/optional phone keyboards, ordered controls and IME-aware board sizing.

Use bounded seeded fill search with cancellation, a meaningful signature including
grid/answers/clue IDs, and explicit failure/exhaustion. Freeze exact clues/fill/
dimensions and policy/source/generator versions in PuzzleRecord. Tests must
preserve old records/crossing edits/results; no storage reset or silent rejudging.

Run at least 100 seeds against real source-backed inputs. Report distinct fills,
layouts, answers/clues reused, recent-history behavior, failures and latency.
Independently verify fills, numbering and constraints; include impossible banks,
contradictory crossings, insufficient clue coverage, exhausted cooldowns and
cancellation. Run existing input/IME regressions as well as generator tests.

Deliver a working generator, dimension-safe output model/validation, pilot clue
bank/provenance, reports and integration instructions. Clearly separate
mechanical solvability from clue accuracy/fairness and identify remaining corpus
needs; do not claim a large dictionary alone prevents memorization.
