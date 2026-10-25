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
Gameplay accepts alternate routes only when a covering solution still exists
with all already-found routes fixed. Authored remaining paths give an immediate
proof for ordinary play; bounded backtracking also finds legitimate alternatives
that require rerouting other words. This does not force every answer onto its
authored route. A route that blocks the remainder is not committed and receives
an explicit explanation without losing selection/found progress. The real
spanning answer must span the actual chosen path, including alternate paths.
The search has a 100,000-step budget; exhaustion reports that checking could not
finish rather than claiming the word is invalid, and does not commit an unchecked
route. Larger future layouts need profiling/possibly worker execution.

Undo last found word remains available before completion, including for older
saved attempts whose already-accepted alternate route blocks other answers.
Puzzle IDs/content revisions and stored found routes remain unchanged. Clear
clears the current selection; Delete removes its final cell; Enter submits.

Board rendering now connects letter centers with animated pastel lines rather
than numbering route steps. The spanning answer is gold; ordinary found paths
cycle distinct pastel colors and the active route is blue. Coordinates share
the same cell/gap geometry as touch targets, so resize does not detach lines.
Letters remain high-contrast; selection step order remains in accessibility
descriptions but is no longer drawn. Found-route reveal is 420ms, selected-route
extension 130ms and cell-color transition 220ms, without delaying input/save.

Before larger production pools: independent theme/clue review, route ambiguity
checks/solver assistance, spangram quality and size/difficulty calibration. A
generator should produce candidate boards plus proof-of-cover paths and quality
metadata, then pass the same validator. Automated structural validity does not
establish fairness or enjoyment. No user telemetry or online service is needed.
