# Wordish answer-bank build

Run from the repository root using the existing Node runtime:

```
node tools/generation/wordish/prepare-bank.mjs
node tools/generation/wordish/prepare-bank.mjs --check
```

No packages, network, SDK changes or runtime file loading are needed. The build
reads the original shortlist draft, explicit editorial exclusions, policy and
the existing bundled ENABLE source. It checks the pinned ENABLE SHA-256 and
8,636-word guess count, rejects duplicates/malformed/unaccepted/excluded words,
sorts eligible words and writes answers.tsv, Kotlin and a rejection report.
`--check` compares every generated output byte for byte without writing.

Canonical inputs are shortlist-draft.json, exclusions.json and asset policy.json.
Both files under generated/ and the runtime BundledWordishAnswers.kt are derived;
do not edit their word arrays independently. The generator bank's sourceSha256
is SHA-256 of the exact UTF-8 answers.tsv bytes. JSON input hashes are retained
in validation-report.json. New content changes require a new bank version and
must not change previously saved PuzzleRecord answers or dictionary policy.

## Actual checks on 2026-10-25

Both commands above pass. Final inventory: 1,451 draft occurrences, 1,449 distinct
draft candidates, 1,387 eligible entries, 64 rejected occurrences (including two
duplicates). Every eligible entry is five uppercase ASCII letters, distinct,
sorted and present in enable-five-v1. The rejection report gives actual words
and reasons; nothing was replaced by random strings or dictionary padding.

TSV SHA-256:
390a36fde39a5264d51032eb7dea2187dd2934c940f1ad2f9dda2d6a9bf15414

All 1,387 received AI editorial selection/screening; **0 have human fairness
review, 0 have measured frequency evidence, and all 1,387 have provisional
difficulty**. AI familiarity judgments are a starting shortlist, not a frequency
corpus or validated daily difficulty. Player fairness/device testing and generator
seed/latency results belong to the primary module report. This tooling did not
run Gradle or Android tests and does not wire daily/additional game creation.

## Editorial boundaries

The policy file records spelling, inflection, proper-name and sensitive-content
rules. Ordinary lowercase senses permit some words that are also names, e.g.
AMBER resin and ROBIN bird. Familiar irregular forms are retained; regular plural
and routine -ed variants are not included merely to reach a count. Sensitive or
ambiguous draft entries are excluded explicitly, including GIPSY, SPUNK and
VIXEN. This is conservative editorial selection, not a comprehensive moderation
lexicon. US/UK alternatives (e.g. MOLDY/MOULD) remain possible and require player
review; shared anagram/inflection families are not automatically excluded by
answer-level recent signatures.

## Provenance and next work

The shortlist was originally authored for CLEAN Words by an AI assistant on
2026-10-25. It was not copied from NYT or any published answer sequence. It uses
no external familiarity data. ENABLE is used only to validate guess compatibility;
its source/notice remains unchanged. Retain the app's existing offline ENABLE
credits/notice at integration and add the original editorial bank description.
Individual English words are factual vocabulary; no additional third-party answer
corpus license is introduced. Existing bundled ENABLE has its own public-domain
notice and attribution, documented in assets policy.json and the game's CONTENT.

Next bounded work: human review of a representative sample and flagged spelling
alternatives, then empirical familiarity/difficulty calibration under a new bank
version where appropriate. Do not relabel this list as human-reviewed meanwhile.
