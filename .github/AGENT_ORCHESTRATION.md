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

Copilot's custom agents (`.github/agents/*.agent.md`) are picked up by **both**:

1. **Copilot CLI / VS Code chat**, where the runtime intent-matches your prompt against each
   agent's `description` and delegates to it as a sub-agent in an isolated context, streaming
   results back to the parent session — the same automatic-delegation model as Claude Code's
   subagents, just running in a different product.
2. **The Copilot coding agent** (the cloud agent that opens PRs on its own), which reads
   `.github/agents/` from the repository as "how we do things here" context for asynchronous
   work. This is where Copilot's *reactive triggering* actually lives — the coding agent starts
   a session on its own when:
   - an issue is assigned to Copilot (`@copilot`, or the "assign Copilot" UI/API),
   - a review is requested from Copilot on a PR,
   - or a workflow explicitly hands off a task to it (e.g. via `/delegate` in Copilot CLI, or a
     GitHub Actions step that calls the coding-agent API).

   None of that is wired up by this change — the four `.agent.md` files just make sure that
   *when* the coding agent is triggered (by a human assigning an issue, today), it has
   role-specific context available. Turning that into e.g. "label an issue `security` to
   auto-assign it to Copilot with the Security Manager persona" would be a further, separate
   step (a GitHub Actions workflow), not something the agent files alone provide.

`target: vscode` / `target: github-copilot` can restrict an agent to one surface; these four
are left unrestricted so they're available in both.

## Why four separate roles instead of one generic agent

Each role is deliberately narrow so its `tools` allowlist matches what it should be trusted to
do without asking: Developer and QA can edit and execute; Architect can research and write
design notes; Security Manager is **read-only** — it reports findings rather than patching
them, which keeps a security review honest and matches the repo's `/security-review` skill
convention on the Claude side.
