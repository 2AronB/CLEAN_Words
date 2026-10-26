# Seven-letter Bee generator

`BeeGenerator(vocabulary).generate(BeeRequest(seed, profile, recentSignatures),
isCancelled)` is a pure Kotlin worker entry point. It returns `Success` containing
the complete existing `PuzzleRecord`, meaningful signature and diagnostics, or
`Exhausted`, `SearchLimit`, `Cancelled`, or `InvalidInput`. It performs no file,
network, UI, or save operations and adds no dependencies or permissions.

Prepare the source once on a generation worker with
`BeeVocabulary.fromBundledIndex(readText(BeeBundledSource.indexPath), isCancelled)`.
Preparation verifies the exact packaged SHA-256, parses and checks the bounded
word/mask/familiarity index, copies its inputs, and checks cancellation per line
and per validated word. `BeePreparationCancelled` is explicit; other source
failures must reach the shared retry/error flow. Do not load the index in a
composable or on the input/save thread. Public `BeeBundledSource` constants allow
daily policy identity without loading the vocabulary.

The index contains 98,144 unique words with 37,313 letter masks. It is a word
index, **not a list of finished puzzles**. Candidate letter sets come from
original familiarity-shortlist words with exactly seven distinct letters. There
are 310 such candidate sets. Every set is considered with all seven possible
centers. A seeded Java Random Fisher-Yates permutation of sorted mask/center
pairs defines the pinned `sorted-mask-center-shuffle-v1` policy. A request tests
candidates in that order; accepted words are enumerated by all submasks containing
the center. Letters can repeat arbitrarily in a word.

`BeeProfile` contains typed provisional filters: at least 12 familiar words,
20–400 accepted words, three familiar word lengths, a familiar four-letter word,
and a familiar pangram. At most 256 non-excluded candidates are evaluated per
default request. Profile bounds are validated and all thresholds are frozen in
the snapshot. If the bounded search stops early it returns `SearchLimit`; only a
complete scan of the remaining pool can return `Exhausted`. Neither silently
relaxes filters. Cooperative cancellation is checked during shuffle, candidate
iteration, submask enumeration and word enumeration.

These defaults admit 1,489 set/center identities across 283 distinct sets. This
is finite provisional variety, not mathematical infinity or calibrated medium
difficulty. A different center is a meaningful change; letter display order is
not. The signature is `bee:letters:<sorted letters>:center:<center>` and excludes
any matching recent identity independent of seed, timestamp or permutation.

Success freezes the existing `bee-wordcount-v2` schema. `goalWords` stores the
matched original AI familiarity shortlist including a pangram; its size fixes
the visible target at ceil(70%). **Every accepted word earns identical word-count
completion credit**, whether or not it occurs in `goalWords`. Points remain
length-four=1, otherwise length, with +7 for a pangram. `validWords` is the entire
accepted set under the pinned inclusion policy, and extra frozen diagnostics
include target, pangrams and per-length totals. Existing rules derive those
values from the snapshot; they do not consult an updated dictionary. No v1 or
v2 old snapshot or result is modified.

Source/quality details and limits are in [REPORT.md](REPORT.md); shared wiring
and offline attribution are in [INTEGRATION.md](INTEGRATION.md).

Future bounded work: human-review the familiarity/pangram shortlist and goals,
evaluate comfortable goals for the owner, and record any agreed quality changes
as new source/profile versions. Four-letter triangle and five-letter square
modes need their own accepted-word policies, profiles, pangram definitions,
completion and fairness review; generic polygon geometry does not authorize
changing this seven-letter profile.
