# Reusable app development kit

Use [REQUIREMENTS-PROMPT.md](REQUIREMENTS-PROMPT.md) to start a conversational discovery session before code. [PROJECT-AGENTS-TEMPLATE.md](PROJECT-AGENTS-TEMPLATE.md) provides project-specific engineering rules; [GLOBAL-AGENTS-TEMPLATE.md](GLOBAL-AGENTS-TEMPLATE.md) provides optional shared working preferences. These are ordinary document templates, not installed skills or configuration.

## Recommended organization

Use one Codex project, one workspace folder and preferably one local Git repository per independent app. Several chats can work within that app project. A new chat alone does not create a separate filesystem boundary. Share a repository only when apps intentionally share code and release/maintenance decisions.

Use a short global AGENTS.md for stable personal preferences, plus a self-contained AGENTS.md in each app root for its stack, data/privacy rules, commands and document entry points. Keep the discovery prompt and templates in a reusable library; copy/adapt them when starting an app. A central template update should not silently change every existing app's engineering contract.

Example (suggested future organization; no folders outside this project were created):

```text
C:\Users\Kayli\.codex\AGENTS.md        optional global preferences
C:\Users\Kayli\Documents\App Starters\  reusable prompt/templates
C:\Users\Kayli\Desktop\My Apps\App A\
    AGENTS.md
    docs\PRODUCT.md
    docs\DECISIONS.md
    docs\PLAN.md
    docs\CONTINUE.md
C:\Users\Kayli\Desktop\My Apps\App B\  independent app and contract
```

The current CLEAN Apps folder is already the workout app project. Do not create new apps inside it or move this working Android project as part of this setup.

## How discovery works in Codex

Official guidance documents global instructions under Codex home (normally `~/.codex`) and project instructions along the root-to-working-directory path. More local instructions override earlier guidance; an AGENTS.override.md can replace the normal AGENTS.md at its level. Without a detected project root, discovery only checks the current directory for project instructions. The default combined instruction limit is 32 KiB. Keep automatically loaded rules compact and point to relevant docs. [Official AGENTS.md guide](https://learn.chatgpt.com/docs/agent-configuration/agents-md).

A file named Master AGENTS.md elsewhere is not automatically loaded just because it exists. A Markdown link is a reference for the agent to read, not a guaranteed automatic include. If you intentionally use an external master, explicitly instruct the agent to read its full path and handle it being unavailable. A project-local snapshot is more portable and easier to review; the global file plus a local contract is the preferred default here.

On this Windows account the usual global path is `C:\Users\Kayli\.codex\AGENTS.md`, unless CODEX_HOME is customized. This kit has not changed that file, config.toml, overrides or any app settings. Merge shared guidance deliberately; keep workout-specific rules local. Verify effective instructions in a fresh chat after setup rather than assuming discovery worked.

## Start the next app

1. Create an empty folder for the new app outside CLEAN Apps, using File Explorer > New > Folder.
2. Add/open that folder as its own project in Codex. The folder is the app's workspace.
3. Paste the block from REQUIREMENTS-PROMPT into its first chat with a rough idea. Optionally copy the starter templates there first; never copy private workout data or build outputs.
4. Answer a few questions per round. The agent maintains the product baseline, decisions, plan, local rules and continuation status. Review proposed assumptions and the smallest useful version.
5. Once satisfied, say: “I approve the baseline in docs/PRODUCT.md dated [date]. Implement only [first slice] from docs/PLAN.md.” Mention any separately approved dependency/permission decisions. This is a suggested user message, not authorization supplied by this file.
6. After the first slice, review verification and perform its manual checklist. Continue in small complete slices. Establish local Git file-history checkpoints before substantial coding; no public hosting is required.

In a fresh chat, verify with:

```text
Before changing anything, list the global and project instruction files you
actually received or read. Read docs/CONTINUE.md and summarize the current phase,
approved scope, data/privacy constraints and next task. State missing instructions
or contradictions. Do not write application code yet.
```

## Documents to grow only when needed

Start with PRODUCT, DECISIONS, PLAN and CONTINUE; local AGENTS routes to them. Add ARCHITECTURE once there is real code, and feature notes when rules/file ownership become too large for the product overview. Put measurable requirement IDs and acceptance examples in PRODUCT, then link milestones/tests to those IDs. Avoid maintaining several competing copies of the requirements.

For an existing prototype, reconstruct a baseline from actual behavior and prior decisions before designing the next feature. Label evidence accurately; do not claim the owner approved a newly reconstructed specification. The current workout app follows this approach in [PRODUCT](../PRODUCT.md), [DEVELOPMENT](../DEVELOPMENT.md) and [CONTINUE](../CONTINUE.md).

Requirements will change. Early discovery catches expensive decisions about identity, history, persistence, navigation and privacy. Small changes, local source control, focused regression checks and honest acceptance status handle what discovery cannot predict.
