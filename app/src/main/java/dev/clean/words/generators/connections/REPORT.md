# Connections composer qualification — 2026-10-26

Input: 20 original AI-authored intended categories/121 member records and five
decoy relations/30 members, all unreviewed by a human. No external dataset,
published puzzle collection or new SDK/service was used. Runtime conversion
excludes the two authored board examples. They are validator fixtures only.

Pins:

- Generator: `connections-decoy-composer-v1`.
- Selection: `two-triples-java-random-dfs-v1`.
- Bank: `connections-pilot-2026-10-26-v1`.
- JSON SHA-256: `6278aa8d8c77eb9612276fa8d0c621d8cb3674fce40d9d0d46490aa678c7a2bb`.
- Typed bank SHA-256: `4910c764db422f9b7aa81ede9e87ad3199f1826eb628b40f795a5891427a8c63`.

Run from repository root with process-local JAVA_HOME pointing to Android Studio
jbr, using existing approved dependencies only:

```text
node tools/generation/connections/validate.mjs --self-test
node tools/generation/connections/prepare-test-bank.mjs --check
.\gradlew.bat --offline :app:testDebugUnitTest
.\gradlew.bat --offline :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
```

| Real-source evaluation | Independent seeds 0–199 | Sequential seeds 0–199, latest 100 exclusions |
| --- | --- | --- |
| Successful starts | 200 | 200 |
| Distinct semantic boards | 198 | 200 |
| Failures | 0 | 0 |
| Minimum three-member decoys | 2 | 2 |
| Worst counted search steps | 548 | 548 |
| Worst evaluated boards/request | 1 | 2 |
| Host p50 / p95 / maximum latency | 800 / 3169 / 50786 microseconds | 614 / 3214 / 13314 microseconds |

Timing includes generation, not independent verification. Every successful board
also passes separate set-based enumeration of all modeled quartets and complete
partitions in the test harness. Raw metrics: [qualification.txt](../../../../../../../../../tools/generation/connections/qualification.txt).

Each evaluation uses categories: body 148, book 97, cleaning 51, containers 148,
cookware 51, fire 104, instruments 47, paper 104, tooth 50. Eleven other input
categories were unused. Decoy pairs: ball+cake 51, ball+ear 45, ball+house 52,
ear+house 52. Rejects: 170 assignments with a decoy inside only one intended group;
the rolling run additionally rejected two recent boards. More bank categories
alone do not establish useful overlap or category variety. The unused eat decoy
does not form a qualifying pair under these selections. No calibrated difficulty,
category-reuse cooldown or unlimited novelty is claimed.

Fifteen focused composer tests cover canonical source ordering, reproducibility,
six diverse seeds, genuine composition beyond two fixtures, source/profile errors,
exact-three enforcement, competing-partition rejection, tiny-pool exhaustion,
budget-versus-exhaustion distinction, cancellation before/during work, semantic
exclusions, caller-list detachment and generated/legacy codec round-trips.
The full JVM suite currently passes 171 tests, zero failures. Offline APK builds
and lint pass; lint retains the prior target SDK warning. Full offline Android
update suite passes 14/14 in 175.357 seconds; generated-board screenshot inspected.
Exact APK/hash/signer and remaining device/manual gaps: docs/VERIFICATION/CONTINUE.

Frozen content includes intended words/senses/explanations, category/review and
source/policy pins, both decoy triples/senses/context and every tested modeled
quartet. Extra novelty ignores tile/group order, labels, IDs and versions; legacy
snapshot signatures use the same canonical member partition. Daily history is
independent of extra play under the existing shared coordinator/save behavior.

Human ambiguity, vocabulary/prerequisite comfort, phone layout/input and desired
challenge remain unverified. The checker cannot find relationships missing from
its bank. Expansion toward the separate prompt's 120 categories/five pilot boards
is still unmet and is a content follow-up, not a prerequisite silently waived by
claiming this small provisional bank is reviewed or abundant.
