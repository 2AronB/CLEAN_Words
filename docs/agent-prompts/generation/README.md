# Generated-game agent pack

Prepared 2026-10-25. This request is for prompts only: no generator implementation
has been authorized or run in the preparing chat. Pasting an implementation
prompt as a new user task authorizes that bounded task, subject to existing
dependency/data constraints. These files do not authorize themselves.

Five module agents can work concurrently. Each reads COMMON.md and its game
prompt, then writes only its listed folders. Run INTEGRATION.md with one primary
engineer afterward (or let that engineer prepare shared wiring concurrently
without editing owned game folders). Pure generators alone are not a completed
conversion: actual daily/additional entry must be wired and tested by integration.
Current app UI/input work may continue in live game folders. Generator engines,
new data/indexes and tests have separate paths; apply changes to existing game
code only through the primary integration owner after coordinating ownership.

Current code has GameModule.puzzles and AttemptRules.create selecting from a
static list. None of these prompts should fake generation by extending that list
or building a new giant catalog of finished puzzles. Finite vocabulary/category/
theme/clue banks and preprocessing indexes are allowed as inputs; compose/select
new puzzle records locally when requested. Every finite input has a finite
possible output space; report measured variety, repeats and exhaustion honestly.

For Wordish a fresh eligible answer selected from a much larger vetted vocabulary
is legitimate generation. For Connections shuffled tiles are not a new board.
For Strands report distinct answer sets and layout variants separately. Editorial
banks still require judgment: generator validity alone does not prove fairness.

If another content agent is already following CONNECTIONS-CONTENT.md, it retains
its staging folders. The generator agent reads a reviewed, pinned snapshot or
requests a content handoff; it does not race edits or silently ingest drafts.

Do not start five shared Git mutations. In a shared checkout agents leave scoped
changes for the primary to review/commit. If separate worktrees are explicitly
used, report the branch and commit rather than switching someone else's branch.

Start each chat with: "Implement the task in docs/agent-prompts/generation/NAME.md;
read COMMON.md first. Other agents may be running concurrently." Replace NAME
with WORDISH, CONNECTIONS, BEE, STRANDS or CROSSWORD. After those deliveries,
start INTEGRATION.md. Keep limitations/manual acceptance distinct from passing tests.
