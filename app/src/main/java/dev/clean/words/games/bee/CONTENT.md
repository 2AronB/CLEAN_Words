# Spelling Meh content and score policy

Ten distinct original seven-letter sets/goals prepared 2026-10-25: CENTRAL,
READING, KITCHEN, PICTURE, JOURNEY, CAPTURE, ANOTHER, SUBJECT, BLANKET, COUNTRY.
Each seed uses all seven distinct letters and is a dictionary-listed goal pangram.
These are development fixtures, not copied published Spelling Bee puzzles or
calibrated daily difficulty. Centers and familiar goal subsets are original
editorial selections; some goals are more familiar than others and need wife
review. Proper-name-only entries were not intentionally included in the goals.

Accepted vocabulary comes from the public-domain ENABLE1 dictionary bundled at
assets/dictionaries/enable1.txt, with its source/notice managed by the parent
project. tools/prepare-bee-content.mjs deterministically validates every selected
goal against that file, enumerates all matching accepted words, and generates
BeeContent.kt. Every puzzle snapshot pins the source file's SHA-256. It also
contains complete goal/accepted word sets so future dictionary changes cannot
silently change an old saved puzzle. There are no runtime network lookups.

Generation result: target/accepted counts are 22/384 CENTRAL, 20/373 READING,
18/88 KITCHEN, 22/202 PICTURE, 10/28 JOURNEY, 20/234 CAPTURE, 23/205 ANOTHER,
16/107 SUBJECT, 24/174 BLANKET and 23/89 COUNTRY. All accepted words contain the
required center, use only the seven available letters and have length >=4.

## Feedback revision 2 — all words count

Owner playtesting found the target/bonus split confusing, particularly when many
ordinary accepted words counted only as bonus. New records now use new IDs
`bee-<seed>-v2`, revision 2 and scoring version `bee-wordcount-v2`.

Every accepted word counts equally toward the visible daily word-count goal.
For this starter revision, the goal is ceil(70% of the former familiar seed-word
count), producing modest goals of 7–17 words. Membership in that seed list has
no effect on a word's credit: a player can complete using entirely other valid
words. This threshold is an engineering default to keep a winnable short session;
it is not calibrated 3.5/5 difficulty or a new owner decision about final scoring.

All accepted found words appear in one list. Points remain four letters = 1,
longer words = their length, and a pangram +7. There is no separate bonus score
in v2. Result score/maxScore store words found / word-count goal, and summary
also records total points. Continued play may exceed the word goal; completed
status and the original completion timestamp remain stable.

Per-length bars show found/total from the **complete frozen accepted set**, such
as 1/10 four-letter words, not just the seed list. Found words are distinct and
invalid state words do not inflate bars or points. Finishing every bar is optional.
Some accepted dictionary words are obscure; they are not all needed to complete.

The required seven-letter visual layout is now an actual honeycomb: a mandatory
center hexagon surrounded by six individually selectable outer hexagons. Strong
pastels, press scaling, found-word transitions and animated progress provide
feedback without delaying acceptance/saving. Controls are Clear, Enter, Delete
from left to right. Pure geometry supports center plus polygon corners (triangle
4 letters, square 5, hexagon 7); only the existing seven-letter mode/content is
implemented. Other shapes still need distinct rules/content/acceptance.

Tap/typing actions accumulate immediately in a local BeeInputBuffer before
emitting full state to the parent. A stale parent acknowledgement cannot replace
newer unacknowledged letters or undo a just-submitted found word. The app keys
the game by attempt identity; switching attempts starts a separate buffer.

## Existing saved revision-1 puzzles

Snapshots with `bee-goal70-bonus-v1` retain their exact original familiar-goal
score, bonus calculation, completion threshold and result summary. Their
revision and original IDs/data remain reconstructible. They are not silently
converted into word-count games, and archived history is not rejudged. A small
legacy explanation appears for a resumed older puzzle while the new per-length
bars and single found list remain usable. Original state messages may retain
legacy scoring language; new submitted-word notifications say simply points.

Both rule versions remain covered by unit fixtures. New dictionary changes must
not overwrite a distributed version or mutate accepted words in saved snapshots.

Future work: validate vocabulary familiarity with a licensed frequency/familiarity
source, define plural/inflection policy, review potentially offensive accepted
bonus words, produce a broader audited goal bank, measure solve rates/time/hint
use locally during manual testing, and calibrate 3.5/5 dailies. Difficulty must
not be inferred from the target percentage alone. Keep all inclusion/scoring
changes versioned; do not silently recalculate historical results. Unit tests
cover v1 result preservation, v2 count credit, duplicates, bucket denominators,
target attainability, normalized cell positions and rapid callback/parent
acknowledgement ordering are covered by pure tests.
Kotlin/Compose build and
physical-device honeycomb sizing, animations and fairness need primary/user
verification; no device acceptance is claimed by content generation alone.
