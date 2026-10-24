---
name: use-kmpcomponents
description: Build or update Compose Multiplatform app UI using KMPComponents, choose existing components, and wire their theme, state, labels, and callbacks. Use in apps that consume or explicitly want this library. Library maintenance follows the checkout's contributor instructions instead.
---

# Use KMPComponents in an app

Prefer the library's existing components when they fit the requested UI. Preserve the app's
architecture, dependency conventions, and the user's explicit design choices. This skill helps
consume the library; it does not authorize a dependency upgrade or changes to the library itself.

## Find the right component

Read the relevant family in [references/catalog.md](references/catalog.md). It covers the catalog
with declaration-file links. For ambiguous choices, state ownership, theme setup, and examples,
read [references/usage.md](references/usage.md).

The core package is `io.github.maniramezan.kmpcomponents`. Async images use
`io.github.maniramezan.kmpcomponents.image` from the separate `components-image` artifact.

## Check the API available to this app

Inspect the consumer's Gradle declarations or version catalog and determine the resolved
KMPComponents version and target platforms. An included build or project dependency may supply
local source instead of the published artifact.

Use source/KDoc from that exact version, a matching repository tag, dependency source JAR, or
that checkout's `components/api/` dumps to confirm names, parameter names, overloads, defaults,
and opt-ins. Catalog links (main) and the hosted API site (latest release) can be newer than the installed
dependency.
If a desired API is unavailable, use an available component or explain the version requirement;
do not invent a similar-looking Material signature or silently upgrade the dependency.

## Compose the requested UI

- Wrap the app's themed content in `KmpTheme`; brand it with kommon `ThemeTokens` when needed.
  Use `MaterialTheme.kmpSpacing`, typography, shapes, and matching color roles for surrounding UI.
- Keep values, selected IDs, asynchronous work, persistence, and validation decisions in the app.
  Wire the component's actual callbacks to that state. Internal popup, reveal, and expansion state
  is intentionally managed by the relevant components.
- Prefer the `Theme*` wrapper when it provides the requested Material-style control. Import it
  explicitly to avoid resolving an unthemed Material function with a similar name.
- Pass localized visible text, `*Labels` models, icon descriptions, and retry/dismiss labels.
  Supply `Painter` icons; internal library glyphs are not public extension points.
- Preserve selection checks, visible focus, minimum targets, and readable foreground/background
  pairs. Let translated labels wrap or scroll at large text sizes. Bound lazy content in the
  scrolling axis instead of nesting it in an unbounded same-axis scroll container.
- The sample catalog demonstrates component behavior with temporary state and simulations. Use
  it for composition examples, not as a source of production persistence, networking, or app flows.

## Verify in the consuming project

For layout, gesture, accessibility, or native-host changes, read the relevant sections of
[references/multiplatform-ui.md](references/multiplatform-ui.md), which links to the companion
`kmp-library-conventions` skill for ownership, constraint and gesture contracts, selecting platform
checks, and interpreting audit evidence.

Compile the affected target using the app's existing Gradle tasks. Exercise the callbacks and
state transitions changed by the task, and inspect affected UI in the relevant themes, large text,
and RTL where applicable. Distinguish source/build verification from device or screen-reader checks
actually performed. Follow the consumer's own instructions for validation and release actions.
