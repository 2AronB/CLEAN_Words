# Integration entry point

Use `WordishGenerator.runtimePolicy` in daily seed identity. This pins generator,
selection policy, bank version/TSV hash, standard profile and accepted guesses.
Initialize the source and generate on the separate generation lane; no file or
dictionary scanning belongs in Compose/input/save callbacks. The generated
Kotlin bank is reproducibly compiled from the checked TSV; runtime needs no
download or asset parsing for Wordish.

Daily requests have no history-dependent exclusions. Additional requests pass
recent meaningful answer signatures (include answers in legacy starter snapshots).
Save the full Success.puzzle before opening it. Resume a saved daily by identity
before generating, regardless of future policy/source changes. Empty/fully
excluded banks must show exhaustion/retry, never a silent starter as success.

Credits: retain ENABLE attribution/notice and describe original AI editorial
answer selection, zero human/frequency review, provisional difficulty. See README
and REPORT. There are no new dependencies, permissions or services.
