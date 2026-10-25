# Common instructions for every generator agent

You are implementing one bounded generated-game conversion for CLEAN Words.
Read AGENTS.md, docs/CONTINUE.md, docs/PRODUCT.md, docs/CONTRACT.md,
docs/FEEDBACK-01.md and the current game source/tests/CONTENT.md. Inspect actual
code before editing; newer human instructions take precedence over this snapshot.

The owner wants offline generated games, fun/fair puzzles, medium-plus daily
challenge usually winnable with effort, and more play on demand. No accounts,
runtime internet, ads, tracking, new permissions or paid services. No new SDKs/
dependencies/global settings without approval. Useful freely reusable/adaptable
content is allowed, including attribution-bearing data; record exact source,
license, version/hash, transformation and required notices for offline credits.
Do not copy published NYT puzzles/clues or assume downloadable data is licensed.

Your game prompt defines file ownership. Do not change core/, shared ui/,
MainActivity, Gradle, manifest, general product docs, other games or another
agent's content-staging folders. Do not stage/commit/push/switch branches in a
shared checkout. Primary owns actual app integration and shared changes. Existing
untracked user Gradle JVM configuration must be preserved. Requests for shared
changes go in your integration handoff, not competing edits.

## Required module-local contract

Create a pure Kotlin generator in your owned generators/<game>/ package. Expose
a documented entry point accepting seed:Long, typed game profile, recent content
signatures:Set<String>, and cooperative cancellation callback. Return explicit
Success with a complete existing core.PuzzleRecord plus diagnostics, Exhausted/
NoCandidate, or Cancelled. Use your own package-local request/result/profile
types so all five agents compile independently before shared integration exists.
Do not invent or edit a common core interface from several chats.

Pin generator version, source/index versions and selection policy. Equal inputs
must reproduce equal output under those pinned versions. Bound candidate attempts,
search steps and memory; check cancellation inside long loops. No generation/file
IO in composables or on the input thread. Prebuilt vocabulary/relationship/search
indexes are welcome; a larger array of completed puzzles is not the solution.

PuzzleRecord must freeze enough content and rule/policy version to resume and
interpret an attempt after source/generator updates. A seed/version string alone
is insufficient if the old inputs/implementation will disappear. Never mutate
saved puzzles or rejudge old scores/history. Keep F1 buffered typing, ordered
controls, colors/animations and compatibility tests. Variants are separate named
profiles, not silently changed standard rules. Difficulty proxies stay internal
until human calibration; do not advertise invented 3.5/5 accuracy.

Recent-content handling must use meaningful signatures, not random IDs, shuffled
display order or timestamps. Report pool exhaustion instead of looping forever,
silently weakening quality constraints or presenting a replay as novel. The
primary chooses user-facing fallback; existing validated starters remain an
explicitly labeled fallback, not a silent successful generation result.

## Verification and closure

Add meaningful deterministic tests for reproduction, validity, cancellation,
exhaustion, malformed/small banks, repeat exclusion, old snapshot compatibility
and differing seeds. Run a reproducible multi-seed evaluation using the real
bundled inputs, reporting unique outputs, semantic content variety, failures,
rejections, latency distribution, worst search effort and missing quality data.
Seeds are not a claim of unique puzzles. Include rejected candidates in reports.

Use existing approved Gradle/test tooling. Coordinate builds to avoid competing
daemons; do not clear caches/player data or hide failures. On Android target an
explicit disposable emulator via tools/test-emulator.ps1, never an unfiltered
connected test runner when a physical phone may be connected.

Deliver within your generator folder: README of design/API/format and limitations,
INTEGRATION.md describing wiring/data/credits and any shared changes needed,
benchmark/report with exact commands/results, and next bounded TODOs. Keep new
provenance notes in your owned folder; primary updates existing game CONTENT.md
at integration. Mark Android/manual/fairness checks accurately.
Do not stop at a schema or sample fixture if independent generator work can
continue. If licensing/content access blocks progress, implement/test the real
pipeline against transparent original pilot inputs, document the missing bank
contract and report the shortfall; do not claim the full game is converted.

Module closure means integration-ready generation, not a shipped generated app.
The primary must complete INTEGRATION.md across all five before claiming the
conversion works from daily/additional play. Do not request repeated permission
for routine work within this pasted task; resolve consequential ambiguity only.
