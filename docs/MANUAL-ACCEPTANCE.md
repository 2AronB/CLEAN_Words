# S1 phone acceptance — not yet performed

G1 generated-play update: install artifacts/CLEAN-Words-generation-debug.apk
(0.3.0-generation/versionCode 3) as an update, preserving data. Batch checks:
old saved dailies retain content/progress; new tWordle/Bee daily and additional
play generate offline; additional play does not replace daily; Back/cancel during
generation stays put and creates no finished start; source/save failure has an
honest retry; calendar/history/restart retain the full generated snapshot.
Review representative unfamiliar words/spelling alternatives and Bee goals 9–47
for comfort/fairness. Other three games still disclose 10 repeating starters.
Exercise touch/fast typing, motion, light/dark/larger text and interruption on
the actual phone. No power-loss/incomplete-rollback or device-loss guarantee.

Initial phone playtest was received and drove F1 (FEEDBACK-01). Re-test the new
APK on the same phone without uninstalling/clearing data. In F1 new Bee puzzles
use a unified word-count goal and per-length bars; old snapshots retain rules.
Crossword now defaults to its own keyboard, with optional phone keyboard.
Extra focused checks: tWordle keyboard colors stay selectable; reveal timing
feels pleasant; rapid crossword typing/paste/backspace; auto-clue advance with
Check untouched; honeycomb targets; Strands alternate route rejection/recovery
and line visibility. Full subjective visual/keyboard acceptance remains pending.

Use the debug APK on a test device or retain existing app data when updating it.
Do not uninstall/clear storage as part of this checklist. No device-loss or
reinstall recovery is promised. The five deliberately similar names are temporary.

Batch these checks in one sitting; report game, puzzle/date and observed issue.

1. Disable connectivity before first app launch. Start each game, read Help and
   try a puzzle. Confirm controls/feedback are understandable and comfortable.
2. tWordle: enter a real five-letter guess; inspect repeated-letter feedback.
   Invalid words should explain the rejection without consuming a turn.
3. Connektions: select/deselect four words and submit; try a wrong grouping.
   Read explanations after solving. Note any equally plausible grouping that
   the game unfairly rejects, or any category requiring unexpected knowledge.
4. Speling Bee: find several words, including a repeat and a missing-center
   word. Every accepted word should count equally toward the visible count goal;
   per-length bars must update found/total. Note surprising accepted/rejected
   vocabulary. Old saved puzzles should clearly state original completion rules.
5. Stranded: select paths by tapping neighboring letters; submit, clear and
   undo. Note awkward touch targets or a valid route that appears impossible to
   recover from. Check theme clue fairness and spanning-answer understanding.
6. Mini: enter/correct letters at crossings, switch Across/Down and Check.
   Check must mark errors without deleting them. Try keyboard deletion/pasting;
   note unexpected focus movement or clipped content.
7. Leave every game unfinished. Use Back, switch apps, rotate and close/reopen.
   Confirm saved moves/entries and resumed puzzle identity. Complete a puzzle;
   opening its history should not submit it a second time.
8. Start an extra puzzle after a daily; open extra history to resume one left
   incomplete. Starting another must not overwrite existing progress. Repeats
   are explicitly expected with the ten-record development pool.
9. At a real 6 a.m. rollover (or on a disposable emulator), confirm the new daily
   appears, old daily stays open if currently playing, and "Finish previous
   puzzle?"/calendar resume original inputs under the original date. Do not change
   a personal phone's clock merely to run this test. Unstarted dates stay empty.
10. Open credits offline. Try system light/dark mode and your usual large-text
    setting; inspect board, keyboard, clues, calendar and buttons. Larger-text/
    screen-reader and older-device coverage remain incomplete until reviewed.

Ask the wife per game: too easy, effortful but satisfying, or frustrating/unfair?
Do not equate completion with enjoyment. These starter puzzles are not calibrated
3.5/5 difficulty and do not establish a near-infinite content supply.

Known S1 limits: one standard mode per game, small cycling pool, mostly direct
editorial categories/clues, simple history, no streak calculation, no backup or
export. The remaining MVP work is in TODO/FUTURE-DEVELOPMENT; this checklist
does not waive it.
