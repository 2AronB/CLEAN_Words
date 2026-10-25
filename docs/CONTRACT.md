# S1 game integration contract v1

Implementation authorized 2026-10-25 with TEN distinct original puzzles per game.
All packages use Kotlin dev.clean.words. Shared code lives in core/ and ui/;
game agents own only games/<game>/ and corresponding test package. Primary owns
Gradle, manifest, core, app navigation/history/storage and docs integration.

Shared types are defined in core/GameModule.kt. Do not modify them without
coordination. PuzzleRecord contains stable id, revision, display title and a
game-specific Map<String,String> data snapshot. Each game owns how it encodes
its records; no shared universal dictionary. Persist the whole snapshot.

GameModule exposes id/title/description, puzzles, result(puzzle,state),
validate(puzzle), and composable Play(puzzle,state,enabled,onChange). Play receives
an immutable Map<String,String> and must emit a complete replacement map on
each meaningful edit. No file/network access inside games. Result is pure and
deterministic and returns IN_PROGRESS/COMPLETED/FAILED plus summary, optional
score/maxScore and assisted flag. COMPLETED must not revert just because Bee
continues earning points. App retains completion timestamp after first completion.
enabled=false is read-only history; do not emit mutations. App owns save failures,
history and common controls. Game input uses full-width responsive controls;
include text/symbol feedback beyond color. Import Compose RowScope/ColumnScope
layout extensions as required. Avoid dependencies beyond approved Compose set.

Each game has object <Name>Game : GameModule. Agent file naming must avoid shared
top-level helper conflicts (use private helpers). Public pure engine functions
and content validation should be unit-tested without an Android runtime. UI
can use Compose/Foundation/Material3; no Android JSON in pure engines.

Validation returns a list of errors, empty when valid. Validate all ten records
and provenance; fixtures may be original authored content, not licensed corpus
claims. Store content origins/review caveats in your game folder's CONTENT.md.
Expose no fake difficulty score or nonexistent hint action. S1 standard only;
future difficulty/variants documented in FUTURE-DEVELOPMENT.md.

Shared UI helpers in ui/GameUi.kt: GameMessage(text), LetterKey(label,onClick,
enabled=true,selected=false), GamePanel { ... }. Their use is optional. Agents
can create their own game-local controls. Core files exist before assignment.

Assignments close with ten actual structurally checked puzzles, playable board,
rules/invalid-input tests, map-state restore test and a report of tests actually
run. Primary integration validates whole app/build/repository and offline state.
No screen appearance, difficulty/fairness or process-death acceptance is claimed
from engine tests alone. Ask parent to change shared files rather than racing.
