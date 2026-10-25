# Concurrent Connections content agent prompt

Paste the following into a separate coding chat opened on this project. It can
run alongside primary UI/input work because its writes are isolated. It does
not require a live server, paid service or new dependency.

---

You are the CLEAN Words Connections content engineer. Read AGENTS.md,
docs/PRODUCT.md, docs/CONTRACT.md, docs/FEEDBACK-01.md and the existing Connections
CONTENT.md/rules/content as reference. Primary development continues concurrently.

Goal: expand original category material and build genuinely interesting boards
with plausible overlap/decoys, while keeping intended answers defensible and
unique under the recorded relationship model. Existing starters are too trivial.
More obscure trivia or ambiguous answers is not the desired difficulty increase.
Target wife/player is English-speaking, seeking medium-plus, usually solvable
with effort. Default American English; label cultural/proper-name prerequisites.

WRITE ONLY content-staging/connections/ and tools/connections-staging/.
Do not edit live game code/tests, shared core/UI, build files or product docs.
Do not switch branches, stage/commit/push shared work or overwrite others' files.
Primary reviews and integrates approved staged records later. Use built-in Node
APIs; no package installs. No runtime AI/server. Never copy published NYT boards,
category collections or unclear-license content. Permitted licensed vocabulary
can support original writing; record actual rights/provenance and adaptations.

First deliver a pilot of 20 original categories and five complete 16-tile boards.
Then expand to at least 120 usable categories and 30 candidate boards if the pilot
validates cleanly. Keep the pilot separate for coordinator review; do not label
unreviewed drafts as approved or claim human fairness review that did not occur.
Do not trade quality for a count; report any shortfall with a reason.

Category schema (versioned JSON): id, revision, label, relationType, members
(at least six where natural), exact word sense/phrase expansion, explanation,
familiarity/reference prerequisites, wordplay flag, known overlapping categories,
source/provenance/license, reviewStatus and notes. Multiword tiles allowed only
with clear normalization. A category with only four natural members is allowed
when documented, not padded with wrong members to hit six.

Board schema: id, revision, four frozen groups of four distinct displayed tiles,
group explanations, intended difficulty rationale, every meaningful modeled
decoy/alternate group, overlap rationale, rejected interpretations, provenance
and review status. Keep separate candidate-group data for the validator.

Each board should contain at least two meaningful decoy relationships spanning
intended groups (e.g. plausible three-of-four). Across the collection vary direct
semantic groups, phrases/compounds, senses, letter/wordplay and familiar references.
Avoid repeating one template with substituted labels. No duplicate display tile
within a board. Reusing words across boards is allowed with documented senses.

Write node tools/connections-staging/validate.mjs. Validate schemas/IDs/16 unique
tiles/four groups/coverage/nonempty explanations/provenance and duplicate or near
duplicate boards. Enumerate complete partitions among all recorded candidate
four-member groups; reject boards with a second complete partition. This proves
only uniqueness under recorded relations, not all plausible human interpretations.
Produce a report with counts, failures, decoys, category reuse and unknown review.
Run it and provide the exact command/results. Do not omit failures to get green.

Closure: pilot JSON, expansion JSON when feasible, machine-readable report,
README of format/provenance/quality limits, validation tests (including an
intentionally ambiguous fixture), and a short integration note mapping each
group to existing game snapshot fields. No production integration. Clearly list
which boards need independent play/ambiguity review. Coordinator will accept or
reject boards before adding them to the app; drafts are never silently shipped.

---

For a shorter assignment, stop after the 20-category/five-board pilot and report
the same validation/review evidence. Scale only once the approach is useful.
