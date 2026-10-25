# Primary integration prompt — make all five actually generated

Read COMMON.md, all five generator handoffs, AGENTS/CONTINUE/CONTRACT and actual
current code. This task authorizes shared generation integration and routine
fixes within offline/privacy/data constraints. Confirm ownership with concurrent
game agents; do not overwrite their work. You own core/, shared UI, MainActivity,
generation adapters, credits and coordinated build/tests/docs/commits. Do not
add new dependencies/permissions/services without separate approval.
Coordinate ownership before applying any live game/model/UI adaptations,
particularly dimension-aware crossword changes; generator agents deliberately
leave those files untouched so ongoing input/presentation work can continue.

The module agents expose pure generators using module-local profile/result types.
Inspect those real APIs and implement the smallest shared request/result adapter
contract needed. Do not build a runtime plugin framework or universal dictionary.
Keep content loading/index preparation and generation off UI/input/save threads.
Do not treat a module helper or large precomputed puzzle catalog as app conversion.

Replace AttemptRules.create's static game.puzzles selection for new attempts with
generation through a separate, cancellable coordinator. Existing attempts always
open their frozen snapshot; do not rerun the generator on resume/restart/update.
Persist final generated content before presenting successful start, using the
existing ordered save/error machinery. Preserve all S1/F1/legacy Bee histories.

Daily seed derives from game, profile, pinned generator/source policy and the
local puzzle date at the agreed 6 a.m. boundary; no required network clock. Existing
daily ID wins over every new request. Daily generation must not depend on user's
extra-play history; extra play must not change today's daily. Seed scheduling
and generation may be deterministic under a pinned version without promising
identical outputs across future source/algorithm versions. Snapshot solves that.

Additional play gets independent attempt/seed identity and meaningful recent
content exclusions. Do not call tile shuffles novel boards. Keep failed/cancelled
requests out of started/completed history. Coalesce duplicate taps/in-flight
daily requests and prevent late callbacks opening a puzzle after cancellation,
navigation, a newer request or a 6 a.m. rollover. Back cancels pending generation
without deleting prior attempts. Source/index failure preserves saved play.

Add honest local loading/cancel/retry/exhaustion states. Bound work per request;
do not silently relax quality or fall back to a repeated starter as success.
An explicit labeled replay/starter fallback can remain available when no fresh
candidate qualifies; define and document it. Report actual content variety and
bank limits instead of claiming mathematical infinity or calibrated difficulty.

Complete offline credits for every included source and required notice. Validate
packaged indexes/content and test first-launch offline, not first-run downloads.
Keep original signature/permissions and install updates without clearing data.

Closure requires all five games starting newly generated daily/additional puzzles
through the real UI, cancellation/failure recovery, saving/restart/resume/calendar
and update compatibility; no game silently still cycles ten records. Test double
taps, clock boundaries, source-version changes, deterministic pinned fixtures,
rapid typing and queue failure, new profiles and exhausted banks. Run rule/corpus/
multi-seed reports, build/lint, explicit disposable-emulator Android checks and
manual checklist. Never target a connected personal phone with an unfiltered
test runner. Record human fairness/phone acceptance still needed.

Update PRODUCT/DECISIONS/PLAN/CONTINUE/TODO/VERIFICATION and game contracts with
actual behavior/authority/results/limitations. Stage only owned coordinated work;
preserve unrelated/untracked user files. Push verified source under existing repo
authorization, provide an updated debug APK, and state exactly which generators
and content tiers are functional versus blocked/provisional. If a module is not
ready, keep its honest existing flow and report partial integration; do not fake
success or rewrite approved game rules to hide a missing generator.
