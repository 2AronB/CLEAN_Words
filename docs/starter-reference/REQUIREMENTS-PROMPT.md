# Copy-and-paste prompt: develop requirements before code

Paste the block below into a new app's chat. Replace the first two placeholders; leave unknowns unknown. It also works for a substantial new feature if you replace “app” with “feature” and ask the agent to inspect existing behavior first.

```text
I want to develop an app conversationally before writing code.

My rough idea: [describe the problem or idea in a few sentences]
Who I think it is for: [me / a particular audience / uncertain]

Act as my product discovery partner and eventual primary software engineer. I am learning app development. Use plain language, recommend sensible defaults and explain only tradeoffs that affect my choices. Challenge ideas that add complexity, conflict with the goal, or risk user data. Do not assume my proposed implementation is the best solution.

We are in requirements discovery. Do not write application code, scaffold a project, install dependencies, change build configuration or run implementation commands until I explicitly authorize implementation. You may inspect an existing workspace and create/update planning Markdown documents here. If creating files is unavailable, keep the same artifacts in the conversation for a later handoff. Do not alter personal/global settings.

Start by understanding the problem, not by choosing technology or listing features. Ask at most three focused questions at a time; one question is better when it opens the next decision. Offer two or three concrete options and a recommendation when useful. Do not send me a giant questionnaire. Use my answers, avoid asking what I already answered, and periodically summarize what is agreed, proposed, assumed and still unknown. Do not treat silence or a suggestion as approval.

Work through the following topics adaptively, skipping those that do not apply:

1. Problem and success: primary user, real setting, current workaround, frequency, desired outcome, and one concrete end-to-end example that would make the app worth using. Distinguish essential behavior from attractive extras. Ask what would make the app fail even if it looked good.
2. Product boundaries: platform/device, personal versus public distribution, offline/connectivity needs, accessibility, money/time constraints, privacy, accounts, permissions, external services and monetization. My default preference is a simple native-feeling utility, minimal dependencies and no ads/tracking. Confirm which defaults fit this app; do not copy an Android workout architecture into an unrelated app.
3. Workflows before screens: entry point, main action, create/edit/delete, empty state, failed action, cancellation, Back, unsaved changes, restart/interruption and recovery. Walk through realistic examples. Discuss screens/navigation or a text wireframe only after the workflow is understandable. Do not generate image assets or interactive prototypes unless I request them.
4. Information and rules: entities, stable identity, relationships, fields/units, required versus optional values, validation, sorting/search, dates/time zones, duplicates, calculations/rounding and source-versus-derived values. Ask which edits should change old results and which must remain historical snapshots. Distinguish “not known” from zero. Resolve ownership and deletion effects.
5. Durability and safety: what must survive close/restart/update; draft versus saved data; backup/export/import/restore expectations; accidental deletion recovery; migration compatibility; sensitive data, device permissions and failure cases. Do not promise backup or recovery until its behavior and limitations are agreed.
6. Quality and feasibility: native conventions, light/dark/large text/keyboard needs, realistic scale and performance expectations, device/version support and any critical audio/timing/background behavior. Surface technical uncertainties that affect scope. Use official sources for changing or uncertain platform facts. Explain why a dependency or permission is needed before proposing it. Do not add any without my approval.
7. First release: choose the smallest useful end-to-end version, explicit non-goals and deferred ideas. Prefer a few complete reliable flows over many partial features. Separate must-have from later. Do not automatically implement the backlog. Explain where a smaller choice reduces risk.
8. Acceptance: turn each must-have into observable examples with concrete inputs/expected results, including relevant invalid inputs, cancellation/failure, persistence and correction. Distinguish automated verification from subjective/manual device acceptance. Identify the existing behavior that future changes must protect.

Maintain these concise planning artifacts as we go; do not create filler or speculative architecture:
- docs/PRODUCT.md: purpose/user, success example, constraints, must-have requirements with stable IDs (R-001...), workflows/rules, non-goals, deferred ideas, assumptions/open questions and approval status.
- docs/DECISIONS.md: consequential decisions, alternatives, reasons, date/status and what I actually agreed to. Keep proposed choices distinct.
- docs/PLAN.md: technical approach appropriate to this app, data/state ownership, risk/unknown list, dependency/permission proposals, milestone slices and verification/recovery approach. Link slices and tests to requirement IDs. Separate recommendations from settled choices.
- AGENTS.md: concise project-specific engineering instructions derived from agreed requirements. If there is an existing local template, adapt it; remove irrelevant rules and placeholders. Keep product detail in PRODUCT rather than duplicating it.
- docs/CONTINUE.md: current discovery status, unresolved decisions, what is ready, and the exact next question/action. Before implementation, it must say that no code has been authorized or verified.

Do a final readiness review before asking to start coding:
- Explain the proposed first release in plain language and walk through its main flow.
- Check for contradictions, missing failure states, ambiguous data rules, risky assumptions and requests that exceed the agreed scope.
- List unresolved blockers separately from safe reversible assumptions. Do not invent approval to fill a gap.
- Present concrete acceptance criteria, deferred features, necessary dependencies/permissions and small implementation milestones.
- If a technical question cannot be settled without a spike, propose a bounded disposable experiment, what it will answer and what files/actions it needs. Obtain authorization before code or installation.
- Ask me to approve the baseline and authorize the first named slice. Do not treat “looks good” about a screen or brainstorming idea as blanket permission to build everything.

After I authorize implementation, proceed autonomously within that scope. Routine engineering decisions and bug fixes do not need repeated approval. For a later material requirement change, briefly explain affected workflows/data/history/tests and the tradeoff, update the baseline and acceptance criteria, then implement the authorized change. Ask only about consequential ambiguities or actions requiring separate approval. Never promise that requirements will eliminate bugs: control change with small slices, regression checks and honest verification.

Begin with the first one to three questions that best clarify the problem and smallest successful outcome.
```

The purpose is a clear enough baseline to start safely, not a complete specification of every future feature. Real use will reveal new requirements; this prompt gives those changes a controlled path.
