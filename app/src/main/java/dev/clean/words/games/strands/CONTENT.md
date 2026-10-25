# Strand-ish S1 content

Ten original compact boards authored for CLEAN Words on 2026-10-25. No external
dataset, NYT puzzle, licensed clue collection or dictionary was copied. Themes,
word sets and clues are original development fixtures using ordinary English
words. The original authoring helper GenerateContent.cjs deterministically packs
the selected words onto full-cover orthogonal paths; the runtime accepts diagonal
paths too. StrandsContent.kt contains the resulting static routes and snapshots.

Topics: garden, breakfast, weather, music, sports, colors, kitchen, ocean, forest
and space. The theme-label spanning answer plus three/four related answers fill
every cell. Each is a distinct word set/board. Boards are deliberately small
(4x5 or 4x6) to validate input, path geometry, snapshots and completion. These are
not claimed to be medium-plus, editorially unique or wife-approved puzzles.
The broad forest/ocean/theme classifications are intentional straightforward
fixtures. No licensed dictionary or accepted incidental-word hint system exists.

Content validation verifies dimensions, spelling, adjacency, no reused cells,
one edge-to-edge spanning answer and a complete non-overlapping partition.
Gameplay accepts any valid route spelling a theme word, not just the authored
route. Because an alternate route can block another answer, Undo last found word
returns letters to the board; this is available before completion. Found-word
path order and current selection are persisted through the shared state map.

Before larger production pools: independent theme/clue review, route ambiguity
checks/solver assistance, spangram quality and size/difficulty calibration. A
generator should produce candidate boards plus proof-of-cover paths and quality
metadata, then pass the same validator. Automated structural validity does not
establish fairness or enjoyment. No user telemetry or online service is needed.
