# Crossita X1 generated content — 2026-10-26

New daily/additional starts integrate the original bundled crossword clue pilot
and seeded crossing search in generators/crossword. Runtime selects varied 7×7
layouts, with exact dimensions/grid/clue text/answer and clue IDs frozen in each
record. Module README/REPORT and bundled provenance identify actual bank size,
source pins, qualification and limits. No external clue corpus or runtime service.
All pilot clues are AI-authored drafts pending human accuracy/fairness review.

The live decoder now handles explicit 5/7 dimensions; absent dimensions remain
legacy 5×5 only. Old starters and saved content/results are unchanged. Check marks
cover all 49 possible cells. F1 immediate buffering, paste, auto-clue advance,
backward Delete and built-in/phone keyboards keep their existing behavior.
Older sections below describe historical starter/feedback/proposal scope.

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

## Initial-play feedback update

The ten S1 grids intentionally use the same blocked template; that is a starter
content shortcut, not a restriction of 5x5 crosswords. Existing distributed
records remain unchanged to preserve saves. More varied 5x5 patterns and other
sizes need dimension-aware grid/navigation/validation rather than relabeling the
same template. No new sizes or generated content are claimed in this update.

Input is now a pure cursor/edit reducer plus immediate game-local state. Every
character in an IME commit or paste is applied, and cursor/clue advancement does
not compare the word with its answer. A filled word advances to the next clue
with blanks, including when the word is wrong. Enter advances without checking.
Only explicit Check marks incorrect letters and records assistance. Completion
still validates the full grid silently, as an ordinary solved-state rule.

Delete clears a filled selected cell and selects its predecessor; on a blank it
steps back and clears that previous cell. At clue start it goes to the previous
clue's last cell. Clear erases the current clue, including shared crossing cells,
without resetting other progress or assistance history. A built-in keyboard is
the default to keep the board above the controls; optional phone keyboard
supports text commits/paste, with a smaller grid and a local append-only input
buffer. The app must key game composition by saved attempt ID to avoid sharing
local state between repeated puzzle records. Large-text/keyboard comfort still
requires actual phone acceptance.

## Proposed generator/content package (not implemented)

1. Generalize width/height and grid runs while retaining the legacy 5x5 decoder.
   Preserve old content, IDs, clue snapshots and scoring rather than regenerating
   saved attempts from current pool ordering.
2. Keep a source-licensed answer bank with multiple sense-matched original or
   reusable clues per answer: answer ID, normalized/display forms, sense ID,
   clue ID/text/style/difficulty, provenance/license and review status. Plain
   dictionary definitions need editing before they become good crossword clues.
3. Author multiple legal grid templates per size; choose dictionary entries by
   crossing constraints and reject fills that require unsuitable obscure words.
   Validate connectivity, minimum run length, unique numbering, all crossings,
   clue coverage and originality/provenance before accepting a generated record.
4. Assign a clue variant per entry and freeze that exact grid/clue assignment in
   the attempt. Reusing a familiar answer with different accurate clues and
   different crossings can remain thoughtful; repeating an identical clue/answer
   pair too soon encourages memorization. Track recent pairs independently of
   answers and templates, with an honest repeat/exhaustion policy.
5. Start with deterministic build-time generation and solver/corpus reports;
   runtime generation must later have bounded search and validated offline
   fallback. No cloud generator or runtime dictionary request is allowed.

Entry for a separate content agent: fixed answer/clue schema, source terms,
allowed answer policy, chosen sizes and repeat targets. Closure: versioned clue
bank plus generated sample grids, source manifest, rejection reasons, structural
checks and actual representative fairness review. A larger raw corpus alone does
not prove either playable grids or sustained novelty.
