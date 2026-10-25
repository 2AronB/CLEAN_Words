# CLEAN Words

An offline Android word-game suite. F1 adds initial-playtest input/visual fixes to
the ten-starter-puzzles-per-game baseline; it is not the completed MVP or a release.

## Run

Open this folder in Android Studio, let the approved Android/Compose dependencies
resolve, and run the `app` configuration. Minimum Android 8 (API 26), compile and
target API 36. No runtime Internet permission, accounts, ads or analytics.

Windows command-line example (process-local environment only):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME = 'C:\Users\Kayli\AppData\Local\Android\Sdk'
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

Configure your own SDK path through Android Studio or ignored `local.properties`;
do not commit personal paths. The wrapper is Gradle 9.3.1, Android Gradle Plugin
9.1.1 with built-in Kotlin, Compose plugin 2.2.10 and Compose BOM 2026.02.01.
The installed Android Studio JBR 25 successfully builds this checkout. Build-time
dependencies may need downloading; the installed app works without a connection.

With a disposable emulator running, use `tools/test-emulator.ps1 -Serial emulator-5554`
for Android checks. It rejects physical-device serials and explicitly targets
the emulator, even with a phone connected. Tests use isolated cache files and
test app attempts; do not run against player data. Never clear player storage
to resolve a bug. Avoid unfiltered connectedAndroidTest with personal devices
connected. Actual checks/results: [VERIFICATION](docs/VERIFICATION.md).

## What is included

Wordish, Connektions, Spelling Meh, Strand-ish and Minuscule Crossword each have
ten distinct starter records, rules, help and local saved progress. Daily puzzles
change at 6 a.m. local phone time; old started puzzles remain in a calendar and
additional attempts are independent. Starter content repeats after cycling the
small pool; difficulty has not been calibrated and variants are not implemented.

New Bee puzzles give every valid word equal progress toward a visible word-count
goal and show per-length found/total bars. Old saved puzzles keep their original
score policy. A real honeycomb replaces the old rows of letter buttons.
Crossword Check records assistance. Personal history preserves original puzzle
snapshots. There is no streak system, export/import or promised device-loss backup.

## Content and contributions

ENABLE is public domain, compiled by Alan Beale, M. Cooper and contributors.
Exact file, source README and public-domain notice are bundled under
`app/src/main/assets/dictionaries/`; source SHA-256 is
`3f16130220645692ed49c7134e24a18504c2ca55b3c012f7290e3e77c63b1a89`.
Offline credits identify its use. See each game's `CONTENT.md` for transformations,
authorship and quality limits. Do not assume a license for third-party content
merely because it is downloadable or its surrounding code is open source.

Run `node tools/prepare-wordish-dictionary.mjs` and
`node tools/prepare-bee-content.mjs` to reproduce the derived vocabulary/content.
Strands' original board authoring helper is in its game folder. Do not overwrite
existing distributed puzzle revisions or silently rejudge saved results.

Start future work at [CONTINUE](docs/CONTINUE.md), [CONTRACT](docs/CONTRACT.md),
[TODO](docs/TODO.md) and [FUTURE-DEVELOPMENT](docs/FUTURE-DEVELOPMENT.md). Agent
packages have isolated ownership and closure checks; primary owns integration.
PRODUCT contains approved intent; future TODOs do not authorize their own scope.
