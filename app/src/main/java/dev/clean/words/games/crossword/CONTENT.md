# Minuscule Crossword S1 content

Ten distinct original 5×5 grids, assembled from an independently authored small
bank of ordinary English words; all sixty clue sentences were written for this
project on 2026-10-25. No NYT or other published puzzle/clue collection was used.
No third-party dictionary or external dataset is bundled by this module.

Each grid uses a rotationally symmetric blocked pattern with fifteen open cells,
three Across entries and three Down entries (lengths 3/5/3 in both directions).
Every open cell belongs to an answer; the grid is connected; each entry has
crossings. These are deliberately small starter crosswords, not claims of NYT
mini density or calibrated 3.5/5 difficulty. The ten grids have sixty distinct
answers; they are not copies with different labels.

Clues are direct and independently worded. NIB, NIL, EWE and ORB are familiar
crossword-style vocabulary that may need audience review. TED uses a pop-culture
proper-name clue. Whether proper names, British usage and less common vocabulary
belong in final difficulty profiles remains future product/content work.

Puzzle snapshots store grid/clue data plus origin and version, so future content
edits need not rewrite existing attempts. Guesses/selected cell/direction, check
marks and assistance are serialized through the shared state-map contract.

Structural and rule tests cover ten unique records, clues, connectivity,
crossing edits, restore, invalid edits, checks that never erase letters, false
completion and completion locking. They do not establish subjective fairness,
touch/keyboard comfort, large-text layout or process-death/file persistence.
Those need integrated Android testing and wife/device acceptance.

For future generator work: separate licensed fill vocabulary, allowed answer
policy, grid-pattern generation, clue/answer/sense records, clue difficulty and
editorial review. A filled grid without good clues is not an acceptable puzzle.
Preserve stable record IDs/revisions, source/license notices and original saved
snapshots. Difficulty should use clue indirectness, answer familiarity and
crossing support, then be calibrated with playtesting; do not infer it from grid
size alone. Test different patterns/word lengths before assuming the six-entry
starter structure represents final content coverage.
