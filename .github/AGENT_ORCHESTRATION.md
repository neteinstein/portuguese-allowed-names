# Agent orchestration

This repo defines four specialist roles — **Developer**, **QA**, **Architect**, and
**Security Manager** — for both Claude Code and GitHub Copilot. The role definitions are kept
in separate directories because the two platforms use different frontmatter schemas, but the
prompt content is intentionally kept in sync:

```
.claude/agents/*.md            Claude Code subagents
.github/agents/*.agent.md      Copilot custom agents
```

If you update the responsibilities or codebase context for a role, update both files.

## Claude Code

Subagent files live in `.claude/agents/` (YAML frontmatter: `name`, `description`, `tools`, ...).
Delegation happens **within a single Claude Code session**:

- Claude reads each subagent's `description` and automatically routes matching work to it (the
  `PROACTIVELY` wording in each description here is a hint to delegate without being asked).
- You can also invoke one explicitly: `Use the qa agent to add tests for this change`, or
  `@agent-qa`.
- A subagent can hand work to another subagent — that's why each role's prompt tells it who to
  flag work to (Developer → QA/Architect/Security Manager, etc.) instead of overstepping into
  another role's job.

There's no automatic reactive triggering here (nothing fires off a Claude Code subagent from a
GitHub event) — the entry point is always a Claude Code session, interactive or headless.

## GitHub Copilot

Copilot's custom agents (`.github/agents/*.agent.md`) are picked up in **three** places:

1. **Copilot CLI**, which infers the right agent from your prompt against each agent's
   `description` (same idea as Claude Code's auto-delegation — "I want a security-focused
   review" routes to `security-manager`), or you invoke one explicitly with `/agent`, by asking
   for it by name, or with `copilot --agent <name>`.
2. **VS Code / JetBrains / Eclipse / Xcode chat**, same profile files, agent picker in the chat UI.
3. **The Copilot coding agent** (the cloud agent that opens PRs on its own), which reads
   `.github/agents/` from the repository as "how we do things here" context for asynchronous
   work, available from the agents tab, an issue assignment, or a PR review request. This is
   where Copilot's *reactive triggering* actually lives — the coding agent starts a session on
   its own when:
   - an issue is assigned to Copilot (`@copilot`, or the "assign Copilot" UI/API),
   - a review is requested from Copilot on a PR,
   - or a workflow explicitly hands off a task to it (e.g. via `/delegate` in Copilot CLI, or a
     GitHub Actions step that calls the coding-agent API).

   None of that is wired up by this change — the four `.agent.md` files just make sure that
   *when* the coding agent is triggered (by a human assigning an issue, today), it has
   role-specific context available. Turning that into e.g. "label an issue `security` to
   auto-assign it to Copilot with the Security Manager persona" would be a further, separate
   step (a GitHub Actions workflow), not something the agent files alone provide.

Two other frontmatter fields are worth knowing about even though these four agents don't set
them: `disable-model-invocation: true` opts an agent out of automatic selection (explicit
invocation only), and `user-invocable: false` hides it from manual pickers (automatic-only,
e.g. an internal helper agent). `target: vscode` / `target: github-copilot` restricts an agent
to one surface; these four are left unrestricted so they're available everywhere.

There's a separate, SDK-level "custom agents" concept (`CopilotClient.createSession()` with a
programmatic `customAgents` option, for apps built on the Copilot SDK) that also does
description-based delegation into an isolated sub-agent session. That's a different mechanism
from the `.github/agents/*.agent.md` repository profiles this repo uses — relevant only if
someone builds a Copilot SDK app against this repo, not to CLI/VS Code/cloud agent usage.

## Why four separate roles instead of one generic agent

Each role is deliberately narrow so its `tools` allowlist matches what it should be trusted to
do without asking: Developer and QA can edit and execute; Architect can research and write
design notes; Security Manager is **read-only** — it reports findings rather than patching
them, which keeps a security review honest and matches the repo's `/security-review` skill
convention on the Claude side.
