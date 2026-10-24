# Multiplatform UI verification with KMPComponents

The reusable contracts live in the `kmp-library-conventions` skill:
[ui-verification.md](../../kmp-library-conventions/references/ui-verification.md). It covers platform
ownership, constraint and gesture contracts, visual versus interactive versus semantic geometry, early
evidence, and what each kind of check does and does not establish. Install that skill next to this one
(see [Use KMPComponents with a coding agent](../../../docs/using-with-agents.md)); the build-graph advice
for keeping the IDE import working is in its
[build-and-versions.md](../../kmp-library-conventions/references/build-and-versions.md).

Apply the relevant sections to the requested change; a copy edit does not need a full device audit.
Consumers keep app changes in their app; contributing to this library follows its own repository
instructions.

## What is specific to KMPComponents

- The sample catalog renders every component family at 100% and 200% text in LTR and RTL. Use it for
  composition examples and quick visual checks. A catalog text multiplier does not establish full
  Android nonlinear font scaling or iOS Dynamic Type coverage; test the platform setting when that is
  the requirement.
- Components with child-driven pan, resize, or drag (zoomable images, split panes, the segmented
  control) leave gestures to a scrolling parent when their interaction is inactive. Check that in the
  consuming screen, with the interaction both inactive and active.
- Selection components expose a shape cue (check, border) and a semantic selected state in addition to
  tint. Keep that when theming; do not replace them with color-only states.
- Internal popup, reveal, and expansion state is managed by the components. Group expansion near a
  viewport edge reveals the new content when user initiated and preserves position for initial state and
  search.
- For this repository's own mobile verification, use an Amoo subagent when available, discover its
  installed schema and audit capabilities rather than assuming support from a tool's name, and record
  missing or blocked coverage explicitly. Consumer projects choose their own device workflow.
