# Implement Connections generation

Follow COMMON.md. This task authorizes this generator and its own content bank;
production integration/other agents' staging remain separately owned.

OWN ONLY:
- app/src/main/java/dev/clean/words/generators/connections/
- app/src/test/java/dev/clean/words/generators/connections/
- app/src/main/assets/generation/connections/
- tools/generation/connections/
Write provenance notes in the generator folder; preserve all existing live game files.

Build a seeded board composer that chooses four compatible category groups and
four words per group from a structured, versioned relationship bank. The result
must be sixteen distinct displayed tiles, four explanations and a frozen snapshot
compatible with ConnectionsRules.validate. Shuffling the old ten boards is not
generation. More obscure trivia or ambiguous answers is not the difficulty goal.

First inspect whether the separate CONNECTIONS-CONTENT.md content task has a
delivery. Do not edit its content-staging/connections/ or tools/connections-staging/
folders. Consume a reviewed, pinned handoff; if absent, build an original pilot
bank in your own folders and document what expansion is needed. Preserve review
status honestly; a schema-valid draft is not human-approved content.

Categories need member senses/phrase expansions, explanations, familiarity/
prerequisites, wordplay type, modeled cross-category memberships, provenance and
review status. Prefer at least 120 usable categories after a 20-category pilot,
with at least six members where natural. Never pad intrinsically four-member
categories with false members to meet a count. Report deficits transparently.

Candidate boards should include meaningful partial decoys across intended groups
(initial target: two plausible three-of-four relationships), but reject duplicate
tiles, nonsensical combinations and second full partitions among ALL recorded
candidate four-member groups. Store which alternatives were tested. Uniqueness
under the model does not prove no other human interpretation exists; keep manual
ambiguity/play review as a quality gate. Do not maximize overlap blindly.

Use bounded seeded search with cancellation and explicit failure if no board
meets constraints. Novelty signature uses sorted intended groups/member sets,
not tile order. Exclude recently used boards and track category reuse separately.
Freeze board membership/explanations in PuzzleRecord; never reroll old attempts.

Run at least 200 seeds using the actual bank. Report distinct semantic boards,
category reuse, modeled decoy counts, rejection reasons, search/latency and how
many records still lack human review. Include intentionally ambiguous fixtures,
insufficient-overlap banks and exhausted pools. Existing four-mistake, repeated
wrong-set, Delete and saved-selection behavior must remain unchanged.

Deliver a working composer, independently checked fixtures/tests and honest bank
quality/scale limits. If the bank is the blocker, finish the algorithm and bounded
pilot, then state precisely what the content agent must supply. Do not claim an
infinite category dictionary or silently ship unreviewed generated drafts as final.
