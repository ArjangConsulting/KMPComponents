# Use KMPComponents with a coding agent

The portable consumer skill is [use-kmpcomponents](../skills/use-kmpcomponents/SKILL.md). It helps an
agent choose from the component catalog, inspect the API for your dependency version, apply the
theme, and connect components to your application's state. Its supporting files include a generated
[catalog with source links](../skills/use-kmpcomponents/references/catalog.md), a
[usage guide with a complete example](../skills/use-kmpcomponents/references/usage.md) and
[KMPComponents-specific UI verification notes](../skills/use-kmpcomponents/references/multiplatform-ui.md).

The companion skill [kmp-library-conventions](../skills/kmp-library-conventions/SKILL.md) holds the
reusable layout, gesture, native-host, and accessibility contracts, and distinguishes builds,
screenshots, native audits, and screen-reader evidence. It also covers the rules for building or
releasing a Compose Multiplatform library. Install it alongside `use-kmpcomponents`; the verification
notes link to it.

Both skills are checked in under `skills/` for distribution. That location does not automatically
register them with every agent. Install each entire folder, including `references/`, into your
application's supported skill location, or point the agent at its `SKILL.md` directly. The commands
below show `use-kmpcomponents`; repeat them for `kmp-library-conventions`.

## Codex

From your application's repository root, copy the folder from your downloaded checkout:

```bash
mkdir -p .agents/skills
cp -R "/path/to/KMPComponents/skills/use-kmpcomponents" .agents/skills/
```

Then ask Codex to use `$use-kmpcomponents` for the UI task. Codex's repository skill locations are
documented in [Build skills](https://learn.chatgpt.com/docs/build-skills).

## Claude Code

From your application's repository root:

```bash
mkdir -p .claude/skills
cp -R "/path/to/KMPComponents/skills/use-kmpcomponents" .claude/skills/
```

Invoke `/use-kmpcomponents` with the task, or let Claude Code select the skill when relevant.
See [Claude Code skills](https://code.claude.com/docs/en/skills) for its discovery rules.

## Other agents and direct file access

The folder uses the [Agent Skills format](https://agentskills.io/specification). Tools with compatible
skill support can install it using their documented method. Other tools can read the same files
without a skill registry. For example:

```text
Use the KMPComponents consumer guide at /path/to/KMPComponents/skills/use-kmpcomponents/SKILL.md.
Find existing components for this settings screen before writing custom controls. Check our
resolved dependency version before copying signatures. Keep values and callbacks in our app.
```

You can add a short pointer to your application's existing `AGENTS.md` or equivalent instructions.
Preserve the instructions already there; do not replace them with this repository's contributor
`AGENTS.md`. The root [llms.txt](../llms.txt) provides a compact documentation index that an agent
can read explicitly. Automatic `llms.txt` discovery depends on the tool.

## Useful requests

- “Build a settings panel using existing KMPComponents fields, toggles, and validation feedback.”
- “Choose the right inline or bottom-sheet component for searchable grouped selection.”
- “Replace these custom cards and buttons with equivalent KMPComponents controls while preserving
  state and callbacks.”
- “Check this screen's component imports, token usage, labels, focus behavior, and 200% text layout.”

## Version and platform checks

Match the skill and source checkout to the library version you use when possible. The generated
catalog and hosted API site can describe APIs newer than your Maven dependency. The skill instructs
agents to inspect the consumer's resolved version, a matching tag, or matching source artifacts
instead of inventing signatures or upgrading dependencies without authorization.

Core components support Android, desktop, iOS, and Wasm. The optional image artifact supports
Android, desktop, and iOS. Application networking, persistence, routing, credential storage, and
business logic remain in the consuming app.

## Maintain the consumer guidance

The generated index takes its component names and descriptions from the README and resolves their
public declaration files from source. Update those authoritative sources, then run from the
KMPComponents checkout:

```bash
python3 scripts/update_agent_catalog.py
python3 scripts/update_agent_catalog.py --check
```

CI checks freshness and unambiguous source resolution for every catalog entry. The skill entry
point and usage examples remain hand-authored and should be reviewed when an API changes.

`AGENTS.md`, `CLAUDE.md`, and `.claude/skills/add-component` / `review-component` describe work on
KMPComponents itself. The distributed consumer skill describes using the library in another app.
