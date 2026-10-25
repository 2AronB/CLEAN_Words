# Spelling Meh S1 content and score policy

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

Fairness refinement agreed by primary integration: familiar target words supply
the maximum **goal score**, and completion is ceil(70% of that goal score).
Other dictionary-matching words are accepted and scored as **bonus**, without
inflating the goal target or being needed to complete. UI explicitly explains
this. Four-letter words are worth 1, longer words their length, and pangrams +7.
No repeated word scores again. History score/maxScore represent goal points and
available goal points; summary/state preserve bonus progress. Play may continue
after completion, and completion never reverts. No claim is made that 70% of the
entire ENABLE set is the target.

Future work: validate vocabulary familiarity with a licensed frequency/familiarity
source, define plural/inflection policy, review potentially offensive accepted
bonus words, produce a broader audited goal bank, measure solve rates/time/hint
use locally during manual testing, and calibrate 3.5/5 dailies. Difficulty must
not be inferred from the target percentage alone. Keep all inclusion/scoring
changes versioned; do not silently recalculate historical results.
