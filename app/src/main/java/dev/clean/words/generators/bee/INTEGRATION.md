# Bee integration contract

1. On a worker, read the bundled `BeeBundledSource.indexPath`, then call
   `BeeVocabulary.fromBundledIndex(text, isCancelled)`. Cache the immutable
   prepared vocabulary for subsequent requests. Source failure must preserve
   saved play and show an honest retry state. Preparation cancellation throws
   `BeePreparationCancelled`; generation cancellation returns `Cancelled`.
2. Call `BeeGenerator(vocabulary).generate(BeeRequest(seed, BeeProfile(), recent),
   isCancelled)`. The API is package-local in purpose, without a shared universal
   dictionary. Its Kotlin declarations are public for the primary adapter.
3. Map `Success.puzzle` directly into the existing attempt/save snapshot. Validate
   through `BeeGame.validate`, use existing `BeeGame.result`, and finish durable
   saving before opening successful new play. The generator does not write saves.
4. Map `Exhausted`, `SearchLimit`, `Cancelled` and `InvalidInput` honestly. No
   repeated starter or weakened profile is returned as generation success.
5. Use the canonical `Success.signature` for additional-play exclusions. For an
   old starter snapshot derive the same signature from its `letters`/`center`;
   never derive novelty from `puzzle.id` or shuffled outer letters. Daily seed
   identity uses pinned public source/generator/profile policy constants and date,
   independent of user extra history. Existing saved daily identity wins.

The minimum pinned daily policy includes `generatorVersion`, `selectionPolicy`,
`indexVersion`/`indexSha256`, `dictionaryPolicy`, `familiarityVersion`, and the
full default profile version. If default thresholds change, bump profile/version
and preserve existing puzzle snapshots; do not recalculate old goals. The source
is 1,764,601 bytes of plain TSV and no first-run download is required.

No changes are requested to BeePuzzle, BeeRules, honeycomb, immediate input,
animations, keyboard, controls, or legacy schema. Additions to the `data` map are
diagnostic/provenance; the complete `goalWords` and `validWords` in the recognized
v2 schema freeze gameplay. Independent corpus/rule/legacy tests are supplied.

Offline credits must retain the existing ENABLE notice and attribute Alan Beale
with M. Cooper/contributors; source is the already bundled public-domain ENABLE
enable1.txt. Add an entry for original CLEAN Words AI editorial familiarity
shortlists: 3,636 words, zero human-reviewed, empirical familiarity unknown,
provisional difficulty. The longer/four/six-letter original shortlist is in
`tools/generation/bee/familiar-draft.json`; five-letter entries were copied during
preprocessing from the original Wordish shortlist at its pinned TSV hash. No new
external source, SDK, service, permission or runtime network access is introduced.

Primary owns updating live game CONTENT.md and PRODUCT/DECISIONS/PLAN/CONTINUE/
TODO/VERIFICATION, installing only on an explicitly selected disposable emulator,
and the actual app conversion. This package alone is integration-ready module
work; it does not establish that daily/additional UI, cancellation/save recovery,
Android packaging or human fairness have been accepted.
