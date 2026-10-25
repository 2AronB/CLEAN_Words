# Playtest feedback slice F1 — authorized 2026-10-25

Owner requested actual improvements after initial phone play. Existing S1 bug-fix
authority applies; this message authorizes the described visual/input/rule refinements.
No new dependencies, runtime permissions, online services or global settings.
Existing saved attempts/history remain protected. New corpus/generator work is
separate, with a concurrent Connections prompt and explicit staging ownership.

## Observable requirements

- R-017: Strong pastel color throughout, with contrasting readable labels in
  light/dark themes. Per-game accents; Wordish correctness uses green/yellow/gray.
- R-018: Modest meaningful transitions/reveals and selection/found animations.
  No artificial delay on every keystroke or forced waiting to make an app slow.
  Reduced/system-disabled animations must remain usable; animation never owns
  durable results or required game state.
- R-019: Where all three actions apply, use Clear, Enter, Delete in that order.
  Clear removes the current draft/selection, Enter submits, Delete removes the
  last input. Do not erase accepted moves/history via Clear. Check stays separate.
- R-020: Wordish tiles and keyboard reflect correct-place green, present yellow,
  absent gray. Strongest accumulated evidence wins; used keys remain selectable.
- R-021: Bee center plus six surrounding hexagons is essential. New games replace
  hidden familiar-goal/bonus membership with equal word-count goal credit for
  all accepted words and per-length found/total progress bars. Single found list.
  The initial new goal is ceil(70% of seed familiar-word count), but membership
  does not determine credit. This is an engineering default to keep completion
  attainable while the full dictionary bars remain optional completion work.
  New snapshots/version freeze this policy; v1 records retain old scoring/results.
  Triangle=three outer+center/four letters and square=five are future mode ideas,
  not silently added content/difficulty variants in F1.
- R-022: Strands shows connecting paths instead of numeric path labels. Alternate
  word routes must not strand the remaining answers: accept only if the remaining
  theme words have a full-cover completion with existing found paths fixed; actual
  spanning-answer path must span. A rejected route explains why without losing
  accepted words. Solver work is bounded; do not freeze the UI.
- R-023: Crossword checks only on explicit request; filling a word advances to
  the next clue without judging correctness. Backspace clears/moves backward,
  including sensible behavior on a blank next cell. Burst typing/paste must
  preserve all characters, and the board must stay usable around the keyboard.
  Prefer a built-in keyboard with optional phone keyboard for compactness.

## Consequential effects

Wordish feedback/input and global colors/motion change presentation, not history.
Bee changes completion rules only for new versioned puzzles. Strands adds a rule
that protects solvability; previously saved accepted paths must be preserved and
still support undo/recovery rather than reset. Crossword cursor/input does not
silently mark answers or clear existing saved letters/check assistance.

Primary owns theme, shared controls, app integration/save queue and verification.
Agents own isolated crossword, Bee and Strands code/tests. Wrap game composition
in attempt ID, not only puzzle ID, so local input buffers never leak into another
attempt reusing the same puzzle. Preserve untracked user Gradle JVM properties.

## Acceptance and next work

Regression examples: repeated-letter keyboard evidence never downgrades; fast
input retained; Clear never discards accepted guesses; Bee word-length buckets
and equal-credit count; legacy result unchanged; valid alternate Strands route
accepted, dead-end route rejected without damage; all starter boards still solve;
crossword whole-word paste/burst/backspace/clue cycling and no implicit Check.
Run available JVM/Android tests/build/lint; manual phone keyboard/layout/motion and
subjective difficulty remain separate. Source collections/generator/repetition
discussion and Connections prompt are in agent-prompts/CONNECTIONS-CONTENT.md.
