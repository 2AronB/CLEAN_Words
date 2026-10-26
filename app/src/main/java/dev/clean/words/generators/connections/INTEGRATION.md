# Connections integration handoff — contract only

Slice 1 has no executable module yet. Do not wire the fixtures into daily/extra
play. Later integration follows docs/agent-prompts/generation/INTEGRATION.md,
limited to Connections; retain already integrated Wordish/Bee behavior and the
honest Stranded/Crossita starter flows. Check current shared APIs before coding.

`ConnectionsRules` accepts `PuzzleRecord(id, revision, title, data)` with:

- `origin`: nonblank pinned source/authorship summary.
- `group.0.label` through `group.3.label`.
- `group.i.words`: exactly four uppercase tiles separated by `|`.
- `group.i.explanation`: frozen explanation including intended senses.

Group/member ordering defines tile IDs (`group * 4 + member`), so freeze order
with the record. UI already shuffles presentation from puzzle ID. Do not shuffle
underlying groups during resume. Preserve old selection/solved/mistakes state and
four-distinct-mistake behavior. Extra metadata fields may preserve bank/profile/
generator pins, semantic signature and decoy/alternative evidence; the current
rules ignore them. Do not change core schema or saved score policies for this.

Primary later adapts module-local results into the existing offline generation
worker. Daily identity must stay independent of extra history; existing daily
snapshots win. Extra requests use meaningful recent exclusions. Full generated
snapshots persist before opening; cancellation/source/search/save failures use
existing visible recovery flows. Never replace a saved puzzle or clear data.

Pilot inputs remain under tools/generation/connections, outside runtime assets.
Slice 2/3 determines the validated runtime bank/index format and packages pinned
inputs only when ready for integration. No runtime file IO in rules/composables.
Credits must honestly identify original provisional content; add source notices
if future bank expansion introduces a licensed dataset.

Future checks: actual source multi-seed qualification, frozen snapshot validation,
legacy semantic exclusions, daily/extra/resume/calendar/update, double taps,
cancellation/failure/exhaustion, offline emulator and prior save compatibility.
Human phone/ambiguity/fairness review remains separate.
