# Connections decoy-first generation

2026-10-26. Pure Kotlin composer and explicit shared adapter are implemented.
Owner authorized slices 2–5 and integration. New daily/extra starts compose from
the pinned original pilot; old starters and saved snapshots remain unchanged.
Development inputs: [pilot.json](../../../../../../../../../tools/generation/connections/pilot.json).
No prior Connections staging handoff existed. Review limits and measured variety
are in [REPORT.md](REPORT.md). This does not complete all-five generation.

## Owner intent and scope

Current boards have too little overlap. Prefer constructing boards from decoys:
select tempting partial relationships first, then fill four intended groups
around their words. Initial target: two distinct relationships with exactly
three visible members each, spanning at least two intended groups. Two-member
relationships may support a board, but do not satisfy that initial target.
Do not count the same relationship twice with different labels. Do not silently
reduce this target when inputs are insufficient. Content remains provisional.

## Pure Kotlin API

Module-local `ConnectionsGenerator(bank).generate(seed: Long,
profile: ConnectionsProfile, recentSignatures: Set<String>,
cancelled: () -> Boolean): ConnectionsGenerationResult`.

Profile pins canonical typed bank SHA-256, generator and selection-policy versions,
two-three-member-decoy target and candidate/search budgets. Bank schema is v1.
Result distinguishes
`Success(puzzle: PuzzleRecord, diagnostics)`, `NoCandidate(reason, diagnostics)`
and `Cancelled`. Reasons distinguish invalid source, impossible constraints,
recent-pool exhaustion and budget exhaustion; a bounded failed search cannot
prove the entire pool exhausted. Diagnostics include attempts/search steps,
rejections, chosen category/member IDs, decoy evidence and tested candidate
groups. `NoCandidate` reasons are INVALID_SOURCE, INVALID_PROFILE,
IMPOSSIBLE_CONSTRAINTS, RECENT_POOL_EXHAUSTED and BUDGET_EXHAUSTED. Only a complete
modeled traversal can report impossible constraints/recent exhaustion. Runtime
timing is measured by the qualification harness, never part of deterministic content.
Check cancellation within selection and partition loops. Use stable sorted
iteration and `java.util.Random`/Collections.shuffle under POLICY
`two-triples-java-random-dfs-v1`; never wall clock or hash-set order.

Bounds: 256 intended categories, 128 decoy relations, 4–16 members per relation,
64-character tiles and 1024-character metadata/senses. Larger sources are rejected.
Generator copies accepted caller-owned lists. Default limits: 250,000 counted
search steps and 500 evaluated boards (maximum configurable 2,000,000/10,000).
Cancellation is checked before work and throughout indexing, selection, filling
and partition traversal. Source validation/canonical hashing are separately
bounded by the source format; caller prepares/runs the module on the worker.

## Input contract v1

The development JSON has schema/version/provenance/review metadata, intended
`categories`, separate `decoyRelations`, and board examples. Every category has
stable ID/revision, label, relation type, explanation, prerequisites, wordplay
flag, known overlaps, review status and member records with tile/sense. Member
identity is category ID plus normalized tile; cross-category senses are distinct.
All material inherits the file's source and authorship declaration. Changes to
members, senses or explanations require revision and a new pinned bank hash.
Six members are preferred where natural; four-member exceptions need a reason.

Tiles are uppercase ASCII letters with single internal spaces, no `|` delimiters.
American English is the default. Decoy relation members explain actual senses or
phrase expansions, including absent members. Compute visible intersection from
the whole recorded membership, rather than storing a hand-picked visible triple.
Known overlaps are discoverable by shared tile text; explicit relation links
make intended traps auditable. The bank is not exhaustive human semantics.

## Board checking and composition plan

Board fixtures specify four category IDs and four selected members each. They
are test examples, never an array for the runtime to cycle. Build all modeled
candidate four-member subsets from EVERY intended category and decoy relation's
intersection with the sixteen tiles. Deduplicate identical member sets. Count
disjoint complete partitions of all sixteen cells, stopping once a second is
found. Require precisely the intended partition. An isolated extra valid quartet
is not by itself a second full solution. Broad relationships can still make
a puzzle feel unfair; human review remains necessary even with one partition.

Implementation: index member-to-category memberships; select decoy pairs; assign their
visible triples to candidate solution categories; backtrack to complete four
disjoint groups; recompute all modeled intersections and partitions; reject
nonqualifying boards. Bound every phase and report honest failures. Store tested
alternatives. No runtime AI, external service or new dependency is needed.

## Novelty

Semantic board signature canonicalizes four groups' sorted normalized member
sets and sorts those groups; tile/group order never creates novelty. Keep a
separate category-reuse diagnostic. Pin policy/source separately; do not make
the same word partition fresh merely by changing a version or label. Integration
must also derive equivalent signatures for legacy starter snapshots.

## Review and provenance

Original AI-authored vocabulary selections, relationships and explanations for
CLEAN Words, 2026-10-26; no external dataset or published puzzle collection was
consulted or copied. No third-party dataset notice is introduced. This is an
authorship declaration, not a claim that common relationships are novel or a
legal license grant. All entries are `ai-draft-needs-human-review`, with zero
human/frequency/fairness review. Structural validation cannot approve content.
No calibrated difficulty, abundance or guaranteed 200-seed success is claimed.

## Next checkpoint

The actual bank passed 200 seeds and a separate 200-start rolling exclusion
sequence, with independently enumerated quartets/partitions for every success.
Only nine intended categories/four decoy pairings appeared. Broader bank expansion
and human review are still needed; 198/200 seed variety does not imply unlimited
categories or novelty. See [integration mapping](INTEGRATION.md) and REPORT.
