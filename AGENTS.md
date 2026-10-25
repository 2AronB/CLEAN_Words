# CLEAN Words engineering instructions

Read docs/CONTINUE.md first, then relevant requirements in docs/PRODUCT.md,
docs/DECISIONS.md and docs/PLAN.md. Product details belong in PRODUCT.

Current phase: S1 implementation authorized 2026-10-25. Owner approved the
proposed baseline/tooling and requested TEN starter puzzles per game (supersedes
three). Implement each game through meaningful rule/content/save checks before
human device/fairness review. Standard approved build dependencies/downloads,
bounded agents and routine fixes are authorized. No global/personal settings
changes. Do not generate image assets or unrelated prototypes.
S1 is in docs/PRODUCT.md and docs/PLAN.md; agent packages in docs/TODO.md.
Current authorized feedback slice F1: docs/FEEDBACK-01.md. Preserve prior saves.
Read docs/CONTRACT.md before editing game modules or shared integration.

Use plain language. Ask at most three focused questions per round. Distinguish
owner requirements, recommendations, assumptions, approvals and verified behavior.
Keep decisions and existing authorization in the handoff; do not ask repeatedly.
Perform available local checks and report exactly what remains unverified.
Owner requests parallel agents when a task has a clear entry, bounded ownership
and observable closure. During discovery delegate research/planning only. After
implementation authorization, agree shared integration contracts first; agents
may own separate games/content tasks while the primary agent integrates shared
navigation, history and daily behavior. Do not delegate speculative scaffolding.
After implementation authorization, complete small approved slices autonomously;
do not implement the backlog merely because it is documented.

No dependencies, SDKs, permissions or external services without approval.
Owner permits useful third-party content reuse/adaptation when its terms permit
the intended use, including attribution-bearing datasets. Do not re-ask solely
for attribution. Track provenance/license/version and required notices; provide
the dataset attribution screen required by PRODUCT. This does not approve
SDKs/services bundled with a content source.
Protect user data; never reset storage or hide failures to pass checks.
All MVP runtime functionality must work offline without accounts (PRODUCT
R-015). Do not introduce mandatory downloads, remote daily puzzles, online
validation or server-dependent generation. No network permission is approved.
No secrets or private data in source control. Repository pushes are authorized
as needed until release: https://github.com/2AronB/CLEAN_Words . This does not
authorize application implementation or public app release.

Starter snapshots are in docs/starter-reference; their examples and relative
links are historical reference, not this project's requirements or configuration.
Source library: C:\Users\Kayli\Desktop\CLEAN Development\development .
Recommend reusable starter improvements when evidence supports them; the owner
has requested propagating improvements to that library. Record changes and
preserve project-specific requirements locally. External writes remain subject
to workspace permissions. Three reusable template improvements were propagated
on 2026-10-25; see docs/STARTER-IMPROVEMENTS.md. Original snapshots retained.

Before every handoff update CONTINUE with current scope, checks, unresolved
decisions and the exact next action. Before coding, it must explicitly say no
code has been authorized or verified. Documentation changes need consistency
and link checks, not application builds.
