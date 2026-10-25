# Implement Spelling Bee generation

Follow COMMON.md. This task authorizes new seven-letter generation; triangle/
square modes and new completion policies are not automatically added.

OWN ONLY:
- app/src/main/java/dev/clean/words/generators/bee/
- app/src/test/java/dev/clean/words/generators/bee/
- app/src/main/assets/generation/bee/
- tools/generation/bee/
Write provenance notes in the generator folder; preserve honeycomb/UI/legacy rules.

Replace the ten letter seeds with a real seeded seven-letter/required-center
generator using the bundled licensed vocabulary. Choose candidate letter sets
from eligible words with seven distinct letters, then choose the center and
enumerate EVERY accepted word under the pinned inclusion policy. Letters may
repeat in words. Require at least one eligible pangram and a useful mix/count of
familiar words; do not require players to know obscure words to finish the goal.

Pre-index words by letter mask/length/center eligibility for offline performance
if useful. Indexes are inputs, not preset finished puzzles. Validate the index
against an independent exhaustive dictionary filter, not just its own builder.
Keep the broad accepted bank distinct from familiarity evidence. Document source,
policy, normalization and unknown familiarity; no invented frequency ratings.

Output BeeGenerator PuzzleRecords accepted by the existing module, with complete
accepted words, per-length totals, center/letters, pangrams, source hashes and
frozen bee-wordcount-v2 policy. Every accepted word advances the visible count
goal equally. Choose the initial goal from a documented attainable familiar-word
count (current default ceil(70%)); snapshot it through the recognized schema or
coordinate a versioned extension with the primary. Do not reintroduce goal/bonus
membership into credit. Do not reinterpret old v1 or v2 saved attempts.

Use typed profile thresholds for minimum familiar options, manageable answer
counts and word-length mix; record these as provisional quality filters, not
calibrated Easy/Hard. Canonical letter-set plus center identifies meaningful
novelty. Changing letter order is not a new puzzle. Handle exhausted eligible
sets and bounded search explicitly; no UI thread dictionary scanning.

Run at least 1,000 seeds and report distinct sets/centers, answer-count/familiarity
distributions, goal attainability, failures and latency. Tests independently
recompute accepted words, pangrams, scores, word-length bars, duplicate handling,
equal-credit completion, exclusions, cancellation and old snapshot results.

Deliver generator/preprocessing, real source-backed data and reports. Keep current
hexagon interaction, Clear/Enter/Delete, immediate input buffering and animations.
Document the future outer-polygon+center extension separately rather than assuming
the current seven-letter rules work unchanged for four or five letters.
