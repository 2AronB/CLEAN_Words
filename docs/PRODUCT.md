# CLEAN Words product discovery

Status: S1 implementation authorized 2026-10-25. Owner approved the proposed S1
baseline/tooling and increased starter content to TEN puzzles per game. Full MVP
acceptance remains pending, including abundant content, variants and difficulty.

## Purpose and user

A fun, attractive Android word-game suite for the owner's wife, who enjoys the
NYT games but avoids their subscription cost. Broader audience and distribution
are undecided. She typically plays each favorite game once before bed. A session
after midnight can consume the daily puzzle she wants the following evening;
she finds the current reset behavior frustrating.

## Owner requirements

- R-001: Support Android. Other platforms are not required.
- R-002: All games are free; no monthly fee or ads.
- R-003: No trackers or excess permissions. Specific necessary permissions, if
  any, require a proposal and approval.
- R-004: Feel polished, fun and native, like a stock phone app.
- R-005: Each included game offers near-infinite generated puzzles or a large
  known puzzle set if generation is impractical. Quality criteria, repetition
  limits and content sourcing remain unresolved.
- R-006: Target suite includes games corresponding to Wordle, Connections,
  Spelling Bee, Strands and the mini-crossword. Other common word games remain
  optional. Owner explicitly requires all five in the MVP; a one-game MVP was
  declined. Development may use smaller slices, but the MVP is the full five.
  Scope selection does not authorize implementation.
- R-007: Development names should be humorous and excessively similar to the
  reference names, as requested by the owner. TODO before final release: review
  and replace these names. Names are not selected yet.
- R-008: Provide a daily puzzle and additional puzzles on demand for each
  included game, so completing a daily puzzle does not prevent further play.
  Daily puzzles reset at 6 a.m., selected by the owner as the time she is least
  likely to be awake. Local device time is the proposed interpretation; travel,
  clock changes and separation of results remain open.
- R-009: Puzzles must feel fair. Concrete standards for accepted words, clues,
  difficulty and ambiguous answers remain to be defined per game. Free play
  without ads and fairness are the owner's stated success conditions.
- R-010: Preserve personal play history. Which results and statistics are kept,
  daily versus additional-play treatment, retention and recovery remain open.
- R-011: Each core game should offer variety beyond the standard daily format.
  Owner gave "6-wordle" as an example. Proposed interpretation: six simultaneous
  answer boards using shared guesses; this is not yet confirmed. Specific modes
  per game and minimum variant coverage for MVP remain to be agreed.
- R-012: Reuse and adapt useful third-party material when its terms permit the
  intended use. Attribution requirements are acceptable to the owner. This is
  standing permission to select suitable reusable content within implementation
  scope, not permission to copy material with unclear or incompatible rights.
- R-013: Once any dataset is included, provide an in-app attribution screen
  identifying used datasets and their sources, licenses and required credits.
  Preserve any additional notices required by the selected licenses.
- R-014: Provide a difficulty option. Its meaning and available levels may differ
  by game. Owner's daily target is medium-plus, about 3.5/5: difficult, but she
  expects to win with effort. This is a subjective playtesting target, not a
  measured score or guaranteed win. Daily override behavior remains open; the
  proposed default is a fixed daily challenge and selectable on-demand levels.
  Do not claim levels are calibrated before evaluation. Candidate on-demand
  levels: Easy, Standard, Hard.
- R-015: Nothing in the MVP requires internet access or an account to function.
  All five games, daily/additional puzzles, variants, difficulty, progress,
  history, help and dataset credits must operate offline, including first launch
  after installation. Bundle needed content and/or generate locally; no remote
  content fetch, sign-in or server response may gate play. Installation/update
  delivery is separate from runtime functionality.
- R-016: Preserve unfinished daily puzzles in a calendar under their original
  puzzle date. Show a "Finish previous puzzle?" option below the current daily
  when unfinished earlier play exists. New daily availability must not replace
  or erase old progress. Multiple unfinished days and late-completion statistics
  follow the proposed first-slice rules below unless changed by the owner.

The owner's view about protection of game rules/names is recorded as motivation,
not a verified legal conclusion. Content and naming review remain future work.

## Success, workflows and rules

Success example: she opens the app before bed, plays the daily puzzles available
for her favorite games, and can start another puzzle when a daily one is already
complete, including after the previous night's 1 a.m. session. Unfinished daily
puzzles survive the reset and can be resumed from the calendar or the option
below today's daily. Reset time-zone/clock behavior remains to be finalized.

Personal history is required. She enjoys passively seeing friends' activity,
although many friends drop off. She does not need or want active sharing.
An in-app friend system is exploratory, not an approved requirement; the owner
has asked about the effort to operate one.
Detailed scoring and persistence rules are proposed for S1 below, not approved.

## Boundaries and deferred ideas

No implementation, assets or prototypes during discovery. No other-platform
requirement. Technology is undecided. Offline/no-account operation is required.
Backup, accessibility, device support and release channel remain open.
The MVP includes all five core games. Variant scope and other feature deferrals
remain open. Active result sharing is excluded from current scope. Additional
game types are research candidates, not approved additions to the MVP.
Passive friend activity is outside the offline MVP. Any later optional online
feature needs separate scope agreement and must not gate offline functionality.

## Acceptance

R-001 through R-016 need concrete acceptance examples as discovery resolves the
first slice. Candidate example for R-008: after completing a daily puzzle,
requesting another starts a playable additional puzzle. Whether that changes
daily stats is unresolved. No behavior has been implemented or tested.
Subjective fun, puzzle fairness and visual quality need real user acceptance.
For R-014 daily acceptance, ask the wife whether representative puzzles feel
challenging but usually achievable with effort; no numeric win-rate target has
been agreed. Bee-style "win"/completion target especially needs definition.

R-013 acceptance: every bundled dataset has a matching attribution entry,
required credit/license notices and a recorded source/version. Recommended:
credits readable offline; external links supplement rather than replace required
notices. No dataset has been included or checked against this criterion yet.

R-014 candidate acceptance: difficulty is chosen before starting an on-demand
puzzle and is recorded with its result; changing the selection does not silently
replace an unfinished puzzle or rewrite historical results. These behavior rules
are recommendations awaiting workflow agreement.

R-015 acceptance: on a fresh installation with connectivity disabled before
first launch, open each game, start/finish daily and additional puzzles, select
available variants/difficulty, read help/credits and view saved history. Close
and reopen offline and confirm persisted progress/results under the agreed
persistence rules. At the next 6 a.m. boundary, new daily puzzles are available
without downloading content. Verify runtime behavior on a device as well as
checking that game logic has no required network service. Not yet executed.

## Next open questions

1. What variants are required per game for the MVP?
2. What must history show, and how should additional puzzles affect it?
3. Is passive friend activity worth a later online feature? No service approved.
4. Confirm local time and resolve reset during unfinished play, travel/clock
   changes and missed days before finalizing daily identity rules.
5. Can editorial games use a large bundled reviewed pool to protect fairness,
   while more mechanical games generate new puzzles? This fits the initial
   fallback allowance, but a concrete content plan is still unapproved.
6. Daily target is medium-plus, about 3.5/5. Resolve whether daily difficulty can
   be overridden and how assistance/completion are recorded, especially for Bee.
7. Complete offline/no-account MVP is agreed. Resolve content exhaustion and
   repetition policy for bundled pools, plus progress/history durability.

## Proposed S1 baseline — Five-game offline playable baseline

Prepared and APPROVED 2026-10-25 by "You're good to proceed," with TEN starter
puzzles per game, future-development/difficulty planning and meaningful boundary
checks. This is an internal playable milestone across all five games, not full
MVP completion. Named S1 and its standard dependencies/build downloads are
authorized. Larger future feature slices still need their own scope approval.

### Flow and common rules

- Open a five-game home list; choose a game and its daily or additional puzzle.
  Use consistent help, Back, result and history conventions with game-specific
  boards/input. Temporary development names: Wordish, Connektions, Spelling Meh,
  Strand-ish and Minuscule Crossword; R-007 release rename remains mandatory.
- Use local device date with a 6 a.m. boundary: before 6 a.m. belongs to the prior
  calendar date. Reopening the same game/date resumes the same puzzle. Changing
  the phone clock/time zone recalculates the current date but never deletes or
  replaces saved attempts. No anti-cheating clock service.
- Calendar shows saved daily attempts and their not-finished/completed/failed
  status. Missed dates have no attempt; S1 does not supply an unplayed archive.
  Below each current daily, the resume option opens the most recent unfinished
  prior daily; calendar reaches the others. Old completion keeps the original
  puzzle date and records its actual finish time. Streaks are deferred.
- Save game progress automatically, including guesses, selected/entered letters
  and found answers. Back and normal interruption preserve progress; reopen
  restores it. Completed/failed attempts are viewable without resubmitting or
  overwriting results. Additional attempts have separate identities/history.
- Requesting another puzzle creates an additional attempt. Preserve any previous
  unfinished attempt rather than silently replacing it. S1 history permits resume
  of unfinished additional attempts. No delete/reset feature in this slice.
- Daily content selection is stable by game/date. Bundle at least ten original,
  mechanically checked puzzle records per game; extra play cycles through this
  sample pool with a visible development-content/repetition notice. Daily and
  extra puzzles may repeat content but never share mutable progress. No claim
  of abundant unique content or calibrated 3.5/5 difficulty for S1.
- Rules explain accepted words and completion. Include offline credits for any
  reused material. Invalid input gives a reason without losing prior progress.
  No accounts, ads, tracking, internet permission, online features or paid SDKs.
- History records game, daily/additional mode, puzzle identity/version/date,
  start/finish times, current status and game-specific result. Preserve puzzle
  content and rules needed to resume or interpret old attempts after updates.
  No export/import, cloud/device backup promise or cross-device recovery in S1.

### Minimum playable game rules (approved S1 defaults)

| Game | S1 behavior |
| --- | --- |
| Wordish | One five-letter answer, six valid guesses, exact/present/absent feedback with duplicate letters counted correctly. Invalid/short guesses consume no turn; win on exact answer, fail and reveal after six incorrect valid guesses. Separate accepted-guess and answer lists. |
| Connektions | Sixteen words, four groups of four. Select/deselect words and submit exactly four. Correct groups lock with explanation; four distinct incorrect submissions end the attempt and reveal groups. Repeating an incorrect group gives feedback without another penalty. |
| Spelling Meh | Seven distinct letters, required center, words of four or more letters; letters reusable. Four-letter word scores 1, longer word its length, pangram adds 7. Duplicate/invalid words score nothing. S1 completion at 70% of curated familiar goal points rounded up, with other valid ENABLE words earning separate bonus points; obscure words never inflate the completion target. Each sample has a goal pangram. Goal/bonus distinction is engineering refinement D-018; full MVP completion semantics need feedback. |
| Strand-ish | Themed board with adjacent horizontal/vertical/diagonal letter paths, no cell reused within a word. Accept bundled theme answers including a spanning answer; found theme paths stay marked and partition the whole board. Complete when all are found. Non-theme words do not consume attempts; no hint system or timer in S1. |
| Minuscule Crossword | Valid 5x5 blocked/unblocked grid, Across/Down clues, editable crossing letters and clue selection. Complete only when every answer is correct. Optional explicit Check marks incorrect entries without deleting them; corrections remain possible. No timer/penalty in S1. |

### Observable S1 acceptance

- A-S1-01 (R-001/R-006/R-015): install/open offline and complete a sample of
  each of the five actual games. Buildable screens alone do not satisfy this.
- A-S1-02 (R-008/R-016): with a partially played daily dated Oct 24 at 5:59 a.m.
  Oct 25, crossing 6 a.m. offers Oct 25 daily. Resume option/calendar restores
  the Oct 24 inputs exactly; neither attempt overwrites the other.
- A-S1-03 (R-010/R-016): exit/reopen during each game; saved guesses/entries and
  status return. Completing an old daily retains its old date. Multiple unfinished
  dates are individually resumable; completed attempts cannot create duplicate
  result records by reopening them.
- A-S1-04 (R-005/R-008): additional play remains available after daily completion;
  cycling the small sample pool is clearly disclosed and progress is separate.
- A-S1-05 (R-009): duplicate-letter Wordish scoring, invalid words, repeated Bee
  words, incorrect/repeated Connections groups, illegal Strands paths and wrong
  crossword crossings obey the stated rules without corrupting progress.
- A-S1-06 (R-012/R-013/R-015): bundled external datasets, if used, have verified
  reuse terms and matching offline credits/notices. No network permission or
  runtime service required. Original samples have provenance recorded too.
- A-S1-07 (R-004): phone acceptance covers touch input, readable feedback beyond
  color alone, Back/resume and light/dark appearance. Larger-text layout reviewed
  on device; no assertion of polished/fair final quality until the wife tries it.

Automated checks cover rules, saved-state round trips, date boundary selection,
sample structural validity and duplicate completion. Manual Android acceptance
covers installation, interruption, input, calendar and visual usability. No tests
or builds have run; no device acceptance is claimed.

### S1 blockers versus later decisions

Starting approval received, including S1 scope and dependency/tooling proposal.
Missing build packages or incompatible tools must not trigger unapproved global
installs. Actual implementation/verification status is in CONTINUE.

Approved bounded S1 defaults: local 6 a.m. date, calendar of
started puzzles only, no streaks yet, small clearly labeled repeating content,
Bee target above (with D-018 goal/bonus refinement), and fixed standard rules per
game. Final difficulty, large corpus, selected variants,
backup/recovery and release/device coverage can wait beyond S1; they remain
required work or open scope before claiming the full MVP is complete.

## Additional game candidates (research only)

Dictionary generation appears suitable for Boggle-style adjacent-letter hunts,
Wordscapes-style anagram crosswords, ordinary word searches and word ladders.
Letter Boxed-style word chains are another candidate with solver validation.
Scrabble-style solo tile play is possible but adds board scoring and opponent
design; not recommended as an early addition. These are feasibility judgments,
not selected features or verified generators. Sources and detail: GAME-OPTIONS.
