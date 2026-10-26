# Wordish seeded generation v1

`WordishGenerator().generate(WordishRequest(seed, profile, recentSignatures),
isCancelled)` is pure bounded CPU work. Run it on the generation worker. Success
contains a complete PuzzleRecord and diagnostics; Exhausted, Cancelled and
InvalidInput are explicit. The standard profile is one five-letter answer/six
valid guesses. There is no shared-guess six-board variant in this implementation.

Sorted reservoir selection uses java.util.Random with the supplied Long seed,
with at most 20,000 bank entries (1,387 bundled). Every eligible answer is in the
unchanged `enable-five-v1` accepted-guess policy. Unknown policies, malformed or
duplicate entries fail instead of silently weakening answer eligibility.
`wordish:answer:APPLE` is the meaningful signature, independent of seed, ID or
display. Exclusions remove answers before selection; a drained bank is exhausted.

Snapshots freeze answer, guess policy, source version/hash, generator/selection
policy, seed, profile and review caveat. Existing starter and saved snapshots
are untouched. Keep legacy guess-policy implementations if a future version
expands acceptance; a new bank cannot change historical answers or six-guess rules.

## Source and quality

Original AI editorial shortlist, authored 2026-10-25; 1,387 AI-selected entries,
zero human-reviewed and zero with measured frequency evidence. All difficulty is
provisional. This is an initial everyday-word shortlist, not a frequency ranking.
Spelling, inflection, names and sensitive-content policy are in
`assets/generation/wordish/policy.json`. Rejections and exact input hashes are in
`tools/generation/wordish/validation-report.json`; 64 draft occurrences rejected.
No proprietary puzzle sequence or new external answer corpus was used.

ENABLE is the existing public-domain guess validator, not evidence of answer
familiarity. Preserve `assets/dictionaries/ENABLE-NOTICE.txt` and app credits.
Pipeline: `node tools/generation/wordish/prepare-bank.mjs --check`.

## Next bounded work

Human review for ordinary vocabulary, spelling alternatives and subjective
challenge; any revised bank gets a new version. For a future named six-board
profile, draw six answers by adding each answer signature to a local exclusion
set, freeze all six, and fail if fewer than six qualify. Shared budgets, UI and
scoring require separate rules/authorization; the current profile stays intact.
