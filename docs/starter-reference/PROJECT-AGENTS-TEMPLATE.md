# Project-local AGENTS.md starter

Copy the fenced content to the new app's root AGENTS.md during discovery. Fill it from agreed decisions before coding. Remove irrelevant sections and unresolved placeholders; do not use this workout app's AGENTS.md unchanged for another app.

```markdown
# [App name] — engineering instructions

## Entry point
Read docs/CONTINUE.md, then the relevant requirement in docs/PRODUCT.md.
Read docs/PLAN.md for the approved implementation slice and docs/DECISIONS.md
for lasting choices. Read only files needed for the task.
Current phase: discovery; application code is not yet authorized.

## Product contract
- Purpose/user: [one sentence].
- First-release success: [one observable end-to-end outcome].
- Approved scope/non-goals: docs/PRODUCT.md [baseline status/date].
- Privacy/connectivity/accounts: [confirmed app-specific rules].
- Platform/design/accessibility: [confirmed rules].
- Data that must be preserved: [records, snapshots, units, drafts as relevant].

## Working agreements
The owner is learning app development. Give concise explanations and exact manual
steps. Inspect existing code before editing. Make one complete small slice at a
time; preserve working behavior and user changes. Do not implement the backlog.
Proceed with routine choices within authorized scope. Resolve consequential
ambiguity before dependent implementation. Document material requirement changes
and affected data/workflows/tests; keep approval distinct from implementation.

No new/upgraded dependencies, SDKs, permissions, services or network functionality
without explicit approval unless already approved in the product contract.
Do not rewrite large areas for preference or hide failures to get a green check.

## Implementation and data rules
- Stack: [chosen after requirements]; versions live in build/configuration files.
- State/persistence ownership: [simple agreed approach].
- Enforce validation below the UI; keep errors recoverable and drafts intact.
- Historical edits/deletion/derived results: [agreed policy].
- Migrations/imports must preserve existing data with meaningful tests.
- Never clear production data, uninstall or reset storage as a shortcut.
- No secrets/private exports in source control. No remote publication implied.

## Verification
- Build: [exact command once setup exists].
- Automated tests: [exact command, relevant test categories].
- Lint/static checks: [exact command].
- Device/integration checks: [prerequisites, safe isolated data, command].
- Manual acceptance: [specific flow checklist or file].
Commands remain unverified until actually executed. Do not invent results.
For documentation-only changes check consistency/links rather than rebuilding.

## Handoff
Update docs/CONTINUE.md after each completed slice: date, behavior, exact checks
and results, data/dependency impact, outstanding manual acceptance and next step.
Keep it short; archive history rather than appending conflicting “latest” notes.
Update affected requirements and decisions. Report manual/phone checks separately.
```
