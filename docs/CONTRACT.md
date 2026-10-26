# S1 game integration contract v1

## Connektions generation extension C2 — 2026-10-26

OfflinePuzzleGeneration now adapts the module-local ConnectionsGenerator from
its compiled pinned bank. Policy includes generator/selection/bank versions,
JSON and canonical typed hashes, two-decoy target and search limits. Preparation
and generation stay on the existing worker; no game/UI/storage schema changes.
Current PuzzleRecord group fields retain stable tile indices. Snapshots add
source/profile/review pins, intended senses, two exact-three decoy contexts and
tested quartets. Rules remain four distinct wrong sets. Existing daily snapshots
win; additional exclusions derive the same semantic group/member partition for
legacy and generated boards. Failure/cancellation uses existing coordinator/save
rollback behavior. No implicit starter fallback. Stranded/Crossita remain starters.
Module API/limits/qualification: generators/connections/README and REPORT.

## Bee opening integration — 2026-10-26

Owner approved activation without an additional familiarity-ratio filter. New
Bee snapshots use bee-opening10-v3, full accepted vocabulary, existing familiar
inputs and pinned ratio 0 / owner-no-ratio-filter-2026-10-26-v1. The optional AI
supplement stays review-only. Prior saved daily content always wins.

Shared AttemptPresentation derives opening progress and resume need from frozen
BeeOpening APIs. GameResult.status/completedAt mean word-goal completion only;
score/maxScore stay null for v3. No new timestamp/schema is required. Both opening
recorded and word goal reached are needed to remove v3 from prior-puzzle resume.
History/calendar/home/hub show the milestones separately. Invalid v3 content/state
is visible for recovery and cannot replace saved state/results. Legacy semantics
and snapshots are unchanged. Primary owns this adapter, shared UI and app tests.

## Generated-start extension G1 — 2026-10-25

Wordish/Bee profiles/results remain module-local. Core OfflinePuzzleGeneration
explicitly adapts GenerationRequest to Ready(full PuzzleRecord), Unavailable or
Cancelled. No universal dictionary/runtime plugin system. Module API/input pins:
generators/<game>/INTEGRATION.md. Primary owns core/shared UI; live game rules/UI
need no generator changes. Stranded/Crossita retain labeled ten-record starters;
Connections is extended by C2 above.

GenerationCoordinator prepares/generates on a separate worker and suppresses
canceled/superseded callbacks. AppSession owns existing-daily lookup, pinned
game/profile/source/date seed identity, extra UUIDs and latest 100 semantic-content
exclusions. Daily generation has no history inputs. Resume always uses snapshots.
Generated starts use AttemptRules.createSnapshot, bypassing create's legacy
starter selector (retained for fixtures/explicit starter use).

Staged starts block new starts/edits. OrderedLibraryWriter saves full content
before opening, rechecks token/date on acknowledgement and compensates canceled
writes with baseline twice (active/recovery copies). Failure retains baseline
memory and blocks starts until retry, also purging both copies. Never reset storage.
Incomplete/failed rollback follows keep-open/retry constraints; no process-death
or power-loss journal guarantee. Future versions can alter new outputs only;
never reinterpret old accepted lists, targets, answers, scores or completion.

Wordish remains single-five/six guesses/enable-five-v1. Bee remains frozen
bee-wordcount-v2, complete accepted/familiar sets and equal word-count credit.
No new difficulty/variant UI. Generation types do not replace GameModule below.

F1 refinement: parent wraps Play in key(attempt.id). Games with fast input keep an
immediate local reducer/buffer before emitting full replacement state, so stale
parent recomposition cannot drop key events. Never key such buffers only by the
reused puzzle record across attempts. Shared saving is ordered off-main; no game
may perform disk IO. New rule policies get new puzzle revisions and retain legacy
snapshot dispatch. See FEEDBACK-01 for active UI/control/compatibility requirements.

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
