# Continue CLEAN Words

Updated: 2026-10-25. Phase: S1 implementation and verification.

## Authority — do not request again

Owner explicitly approved S1 baseline/tooling in D-017: "You're good to proceed."
TEN starter puzzles per game supersedes three. Native Kotlin/Compose, approved
standard build/test dependencies/downloads, local save/calendar and bounded game
agents are authorized. Owner directs completing consequential rule/content/save
checks and documenting future generators, difficulty and parallel work. Routine
fixes in S1 need no repeated permission. No public app release, global settings
changes or automatic implementation of the future backlog.

Pushes as needed to https://github.com/2AronB/CLEAN_Words are authorized until
release. Implementation commit 83ea2a2 is pushed to origin/main; local source
checkpoint and remote matched after push. Source snapshots are in starter-reference. Recommend and
propagate justified reusable starter improvements to
C:\Users\Kayli\Desktop\CLEAN Development\development subject to filesystem
permissions. Three improvements were returned to PROJECT-AGENTS-TEMPLATE.md;
see STARTER-IMPROVEMENTS. Original snapshots retained; no global settings changed.

## Implemented scope

Five real playable modules with ten unique records each: Wordish, Connektions,
Spelling Meh, Strand-ish and Minuscule Crossword. Primary owns core/storage/app;
bounded agents delivered isolated engines, content, UI/tests and future planning.
Read CONTRACT before parallel game changes. PRODUCT is requirements source;
DECISIONS records actual owner approvals versus engineering refinements.

All runtime features offline with no accounts, analytics, ads or internet
permission. Local 6 a.m. daily boundary; daily/extra states independent; calendar
retains started older dailies; resume option below daily. No unstarted archive,
streak algorithm, deletion, export/import or backup promise. App snapshots puzzle
data and version, moves and results; checksum/atomic files/last-good local copy
protect tested failure cases. Future schemas block rather than resetting files.
Failed writes retain pending memory in a ViewModel for retry across rotation.

Public-domain ENABLE acquired; source notice/credits bundled. Wordish accepts
8,636 five-letter entries and has ten curated answers. Bee accepts matching
ENABLE words, but familiar goal words determine its 70% completion threshold;
other words are bonus. Full dictionaries/provenance pinned in Bee snapshots.
Spanning layouts and crosswords validated mechanically. Fairness/difficulty is
unverified until user review; target remains medium-plus, about 3.5/5 daily.

## Verified S1 checkpoint

Installed Android Studio JBR25, Gradle9.3.1, AGP9.1.1, Kotlin/Compose plugin2.2.10,
Compose BOM2026.02.01, Activity1.12.4, SDK36; minimum API26, target36. Existing
cache works with normal Gradle permissions; no global settings/tool installs.
Debug APK built and 54 JVM tests passed. Three Android integration tests pass:
all 50 puzzles solved/persisted, all five actual boards opened, Help/recreation/
Back and Wordish keyboard/disk restore checked. No Internet or dangerous runtime
permission; dataset notice bundled. Lint passes with zero errors/one target36
warning. apksigner verification succeeds. Content generation reproduces hashes.
Initial emulator System UI stall and test-helper wait/selection issues fixed;
final integrated suite green. Evidence/manual gaps: VERIFICATION.
Installable artifact: artifacts/CLEAN-Words-S1-debug.apk (debug signing, ~12 MB).

Headless emulator uses existing Medium_Phone_API_37.0 AVD in read-only,
no-window/no-snapshot-save mode, serial emulator-5554. Wi-Fi/data disabled for
tests. Test instance shut down at handoff with `adb -s emulator-5554 emu kill`;
no physical phone or saved emulator image altered. Test files were isolated;
no production data reset. ADB server may remain available for normal development.

## Next actions

Available S1 engine/content/persistence/build checks pass. Next: user/wife runs
MANUAL-ACCEPTANCE on her phone, especially input comfort, calendar rollover,
large text, fairness and Bee goal/bonus understanding. Fix observed S1 bugs
autonomously within existing authority. Future slices need scope agreement,
not repeated S1 permission. T-001–008 mechanically closed; T-009 awaits human
acceptance. No claim of full MVP completion.

FUTURE-DEVELOPMENT specifies content schemas/versioning, expansion and solver
checks, difficulty proxies/calibration, six-board variant rules to settle, agent
ownership and closure. Do not claim near-infinite puzzles, selectable difficulty/
variants, final polish or tested backup/recovery yet.
