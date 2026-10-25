# Decisions

## D-001 — Discovery before implementation

2026-10-25 — Owner instructed. Maintain planning Markdown and inspect workspace;
do not code, scaffold, install or run implementation commands until explicit
baseline and named-slice authorization. No technology decision is settled.

## D-002 — Product direction

2026-10-25 — Owner requested. Android, wife as primary user, free word games,
no ads/trackers/excess permissions, polished native feel, abundant puzzles.
Exact game set, play model and first release remain open; see PRODUCT.

## D-003 — Repository and reusable starters

2026-10-25 — Owner authorized pushes as needed to
https://github.com/2AronB/CLEAN_Words until app release. Owner requested copies
of relevant development guidance and future reusable improvements returned to
the source library. Four Markdown source files copied to starter-reference;
project AGENTS adapted locally. No global configuration changes. No push needed
yet, and remote/local Git configuration has not been verified.

## D-004 — Temporary game names

2026-10-25 — Owner requested deliberately similar punny development names and
replacement before final release. Names and any release review are unresolved.

## Recommendations awaiting discussion

The original recommendation for a smaller game subset was declined in D-007.
Abundant puzzles must also be fair and fun;
generation feasibility and word/content sources need evaluation before coding.

## D-005 — Bedtime daily play plus additional puzzles

2026-10-25 — Owner stated favorite games: Wordle, Connections, Spelling Bee,
Strands and mini-crossword; typically one of each before bed. Owner requests
daily puzzles plus additional games on demand. Fairness and free play without
ads define success. Daily reset boundary is not chosen. Personal history and
friends' activity are valued, but neither a social system nor accounts have
been authorized or specified.

Initial proposals were a 4 a.m. local reset and history with result sharing.
The owner superseded both in D-006. Resolve scope before choosing technology.

## D-006 — 6 a.m. reset, required history, no active sharing

2026-10-25 — Owner selected 6 a.m. as the time she is least likely to be awake.
Local device time remains a proposed interpretation. Owner requires history and
explicitly says active sharing is neither needed nor wanted. Her social interest
is passive visibility of others' activity. Owner asked for an effort estimate
for a scoreboard/friend service; this is exploration, not approval to add
accounts, dependencies, connectivity or a server.

## D-007 — Five-game MVP and variations

2026-10-25 — Owner explicitly requires all five named core games for MVP and
variety per game, citing "6-wordle." Exact variants are unresolved. One-game MVP
recommendation is superseded. Other popular dictionary-driven games are being
researched, not added to MVP. Owner expects a relatively short day; this is a
hope, not a verified schedule or acceptance waiver. A playable prototype and
wife-accepted puzzle quality must be reported separately.

## D-008 — Bounded parallel work

2026-10-25 — Owner requests identifying parallel-agent opportunities with clear
entry and closure, including content work and isolated game development. During
discovery a research-only content-feasibility task was delegated. No code or
dataset construction was authorized. Recommended later workflow: define shared
contracts, then allocate isolated games/content packages; primary agent owns
integration. Skeleton-first implementation remains a proposal, not permission.

## D-009 — Content-source clarification

2026-10-25 — Owner asked whether proposed collections are free/open source.
Research has not verified a large suitable redistributable collection for the
editorial games. Prior bundled-pool suggestion was an approach, not an existing
source commitment. Open English WordNet is a CC BY 4.0 building-block candidate;
it is not a finished puzzle bank. Original reviewed content remains a proposal.
No third-party content or dependency is approved or added.

## D-010 — Reusable material and attribution

2026-10-25 — Owner authorizes using/modifying useful material when permitted,
including sources requiring attribution. An attribution screen is required once
any dataset is used. This supersedes the need to re-ask permission solely for
an acceptable dataset attribution requirement. Record source/license/version,
modifications and required notices. No specific dataset selected or imported;
implementation and SDK/service approvals remain separate.

## D-011 — Difficulty selection

2026-10-25 — Owner requests difficulty options and recognizes game-specific
feasibility. Proposed Easy/Standard/Hard levels, their actual rules, daily
coverage and history treatment are not yet agreed. Preserve fairness; obscure
vocabulary or arbitrary ambiguity is not an assumed definition of difficulty.

## D-012 — Daily challenge target

2026-10-25 — Owner's target is medium-plus, about 3.5/5: hard enough to be
difficult, with a win expected after effort. Record as qualitative acceptance
intent; no measured difficulty algorithm, win percentage or guarantee is agreed.
The daily-difficulty override policy and Bee completion target remain open.

## D-013 — Shared foundation and game ownership proposal

2026-10-25 — Owner asks whether a consistent interface, per-game agents and
unique dictionaries are best. Recommendation: shared player conventions and
small integration contract; separate game rules, controls and content profiles.
Share suitable base vocabulary/tools rather than duplicate data; use separate
curated resources for accepted guesses, answers, Bee words, relationship banks,
theme sets and clue/answer records. Agent ownership is an execution arrangement,
not a permanent app requirement. This approach remains proposed; no framework,
dependency, code interface or scaffolding has been selected or authorized.

## D-014 — Fully offline MVP

2026-10-25 — Owner answered yes to all five games/dailies/history working offline
without accounts and stated: "Nothing should require the internet to function."
All included runtime functionality must work offline; no sign-in, remote puzzle
fetch or server may gate play. Needed content must ship with the app or be
generated locally. Development-time tools/content creation do not imply a
runtime service. Passive friends remain outside MVP; any future optional online
scope must preserve offline operation. No network dependency/permission approved.

## D-015 — Calendar and old-daily continuation

2026-10-25 — Owner approves preserving prior unfinished daily puzzles in a
calendar with a "finish previous puzzle?" option below the daily. Current daily
remains separately available. Multi-attempt selection and late-completion stats
are proposed in S1, not individually approved yet.

## D-016 — First playable readiness proposal

2026-10-25 — Owner asks if ready to start minimal versions of all games and
prepare TODOs for future agents. This is a readiness question, not explicit
implementation authorization. PRODUCT now contains a proposed bounded S1
baseline; PLAN proposes native Kotlin/Compose and tooling/dependencies; TODO
defines agent packages. All five stay in MVP. S1 deliberately uses disclosed
small sample content and defers calibrated difficulty, variants and corpus scale.
This proposal was subsequently authorized in D-017.

## D-017 — Implementation authorization and ten starters

2026-10-25 — Owner: "You're good to proceed. Make the starting contain 10 games
each." Authorizes proposed S1 baseline, Kotlin/Compose tooling and standard
dependencies/build downloads, with ten distinct puzzles per game superseding
three. Owner also directs building/testing through consequential rework boundaries
and documenting generator/version/difficulty/parallel work. If a free dictionary
is inaccessible, structure assumptions are permitted and must be recorded.
Offline scope and constraints remain. No global settings, public app release or
automatic full future backlog authorization.

## D-018 — ENABLE and Bee goal/bonus refinement

2026-10-25 — Engineering within approved S1: acquired public-domain ENABLE
word list and author notice. Version/hash/provenance recorded; offline credits
included. Wordish accepted guesses derive from five-letter entries; answer
selections stay separate. Bee accepts matching dictionary words, but completion
uses 70% of explicitly labeled familiar goal points; other words earn bonus.
This avoids requiring obscure dictionary words to win. Goal/bonus distinction
is an implementation refinement, not an owner decision about final difficulty.
The curated sets still need wife fairness review.

## D-019 — Initial phone playtest / feedback slice F1

2026-10-25 — Owner requests stronger pastels, animations and ordered input actions;
Wordish board/keyboard colors; Bee hex layout and per-length progress; Strands
lines/alternate-route safety; reliable crossword typing, navigation, deletion and
keyboard fit. F1 scope/acceptance in FEEDBACK-01. These are authorized refinements,
not a reason to re-request S1 permission. Existing history protected. New Bee
policy has new snapshots; legacy results not rejudged. Connections expansion is
an isolated staging prompt requested by owner, not production corpus ingestion.

## D-020 — F1 input/persistence integration and version compatibility

2026-10-25 — Engineering within requested fixes: immediate local game input
reducers prevent dropped fast events; ordered background persistence replaces
per-keystroke UI-thread fsync. Save failures preserve latest memory and stop later
writes until retry; onStop drains with a bounded wait. New Bee v2 snapshots count
all words equally; legacy v1 rules/results remain. No new dependency/permission.
Untracked user gradle/gradle-daemon-jvm.properties was preserved, not adopted into
the commit. All physical-device testing stays with the owner; emulator targeted.
