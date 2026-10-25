# Crossword expansion after the input fixes

2026-10-25. Concrete proposal, not a claim that a new generator/corpus is built.
All ten original S1 boards use one connected 5x5 pattern as a starter shortcut.
This is not a constraint of 5x5 crosswords. Existing snapshots retain their grid.

An unthemed offline generator can combine a grid template, answer vocabulary and
clue/answer bank. Theme-free filling removes a major constraint; it does not
remove crossing, clue accuracy, familiarity or duplicate-answer requirements.
ENABLE supplies spellings, not clues or word senses. A separate original/licensed
clue bank is required; never turn arbitrary definitions into claimed reviewed clues.

## Content records and repeat prevention

Keep separate stable answer ID/text/display spelling/length/word-sense/dialect/
allowedness/familiarity and clue ID/text/intended sense/style/difficulty/source/
license/review. One answer can have several genuinely different clue IDs.
Board snapshots freeze grid, clue IDs/text, answers, rules and generator version.

Track recently used clue IDs and answer IDs separately. Prefer a new clue for a
recently seen answer, and reduce recent-answer selection as well. Rewording the
same clue is not strong novelty. Some repeat answers are normal and can support
learning; repeated exact clues should have a longer cooldown. Cooldown lengths
need play feedback and corpus size, not an invented promise of no repetition.

Keep multiple clues per common answer, instead of either one fixed hint per word
or endless synonyms presented as novel. Small grids share many short answers;
more clues help, but cannot rescue a narrow fill vocabulary. Count unique answers,
unique clue/sense combinations and recent overlap in reports, not just theoretical
permutations of the same board. No specific minimum bank size is established yet.

## Bounded future agent package

Entry: approved sizes (recommend 5x5 first, then 7x7), grid aesthetics, allowed
short words/proper names/dialect, approved clue sources and repetition policy.
Only then assign isolated content-staging/crossword/ and tools/crossword-staging/.
Primary retains live game/input/save code ownership.

Pilot output: ten varied legal 5x5 patterns, at least 200 vetted answer entries
and two original/licensed sense-matched clues for each where natural. Use the
bank to fill templates by crossing constraints; reject impossible/poor fills.
Do not force rare abbreviations or false clues to meet counts. Report shortfalls.

Closure: deterministic fixed-seed fills, connected-grid/word-length/clue-coverage/
crossing checks, no repeated answer in one board, no invented words, matching
provenance, bounded search with validated fallback and explicit failure. Include
seeded recent-history tests preventing immediate exact-clue repeats, with a clear
exhaustion policy. Independently review a representative fill/clue sample before
shipping. Variant sizes require dimension-aware model/UI migration; current5x5
index arithmetic must not be silently reused for7x7.

Dictionary-pair generation alone cannot prove all clues are accurate or fair.
Maintain accepted/rejected review states and reasons. More content can be built
concurrently, but production selection changes require reviewed integration.
