# Crossita runtime adapter

Primary owns live game/shared integration; generator files do not edit game UI.

1. Add crossword to the existing generation adapter and policy pins. Construct
   `CrosswordGenerator(BundledCrosswordBank.bank)` on the generation worker. Use
   compiled constants in policy construction so UI-thread policy reads do not load
   or hash the bank. The source JSON is a provenance artifact, not a runtime
   download or prerequisite.
2. Adapt Success.puzzle, NoCandidate reasons and Cancelled to existing coordinator
   outcomes. Do not silently substitute starters or reset storage on failure.
3. Use `CrosswordGenerator.semanticSignature(puzzle)` for old and new history.
   Recent signatures must stay newest-first. AppSession's existing latest-100
   order and ordered Kotlin set satisfy this contract. Daily uses no exclusions.
4. `grid` and `clues` retain legacy encodings: flattened letters/# and lines
   `ANSWER|text`. Require explicit matching width/height for new snapshots; absent
   dimensions are legacy 5×5 only. Replace all fixed-five arithmetic in board
   parsing, numbering, neighbor traversal, cells/check indices and UI geometry.
5. Retain buffered input, rapid paste, automatic unfinished-clue advance,
   backward deletion, explicit Check only and both keyboard modes. Test last-row
   7×7 crossings and checked indices >24; old 5×5 results must remain unchanged.
6. Disclose original finite AI pilot and unreviewed clue fairness in offline
   credits/home/hub; larger size does not imply calibrated difficulty. Preserve
   existing daily snapshots, ordered durable save-before-open and update identity.

Fields additionally frozen: entryMetadata lines `number|A/D|answerId|clueId|sense`,
templateId, rulePolicy, generator/generatorVersion, selectionPolicy, sourceVersion,
sourceSha256, bankSha256, profile, seed, origin, review and difficulty. Human review,
physical-phone large text/input, fairness and actual overnight behavior remain
separate from mechanical qualification.
