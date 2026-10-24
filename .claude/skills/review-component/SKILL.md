---
name: review-component
description: Review a change to KMPComponents' public components or theme adapters against the repo's API, accessibility, theming, performance, and test conventions. Use when asked to review a PR, diff, or component in this repo.
---

# Review a component change

Read the diff, then each touched file in full. For API-shape, build-graph, and verification-evidence
rules that apply beyond this repo, consult `kmp-library-conventions`. Report findings ranked by severity with
`file:line`, a concrete failure scenario, and the fix. Skip anything you cannot tie to a rule or a
real failure.

## Correctness and security

- State updates: every `onXChange` receives the full new state; no lost edits from stale captures.
- Secrets: no secret in `toString()`, logs, `rememberSaveable`, or semantics text of a masked field.
- Validation lives in pure functions and matches the documented rules; tests cover edge cases.
- Keyboard: correct `KeyboardType`, `imeAction` chaining (Next ... Done), autocorrect off for
  URLs, identifiers, and secrets; overriding `KeyboardActions` must not drop default behavior.

## Theming (see `TokenAdapters.kt`)

- No literal `Color(...)`, `dp` spacing, or `sp` sizes in components; spacing via
  `MaterialTheme.kmpSpacing`, extra colors via `MaterialTheme.kmpColors`.
- New Material role mappings are derived from kommon roles, never left at the baseline palette,
  and have a test in `TokenAdaptersTest`.
- Contrast: foreground/background pairs come from matching roles (`onX` on `X`).

## API shape

- Parameter order: required, `modifier`, optional, trailing content. `modifier` on the root only.
- User-visible strings are parameters or `@Immutable` `XxxLabels` defaults.
- Public models are `@Immutable data class` with `val`s; enums use SCREAMING_CASE entries.
- New public API has KDoc; breaking changes are called out for a `feat!:` commit. The
  `components/api/` dump diff is the API diff: removed or changed signatures are breaking.
- Reusable sub-parts are exposed as their own components rather than duplicated.

## Accessibility

- Titles are headings; dynamic warnings/errors use a polite live region; icon-only controls have a
  meaningful name. Focused items expose their associated name/role/state without hiding independent actions.
- Selection and keyboard focus have a shape/border/check cue as well as color. Meaningful drawn or
  bitmap graphics are reviewed separately from a provider's contrast verdict.
- Distinguish visible/clipped bounds from full targets. Missing exporter properties are unavailable
  evidence, not proof of an unnamed app control. A Compose semantics assertion is not a native speech test.

## Performance

- Derived objects are `remember`ed with the right keys; no work in composition that could be
  hoisted to a pure function computed once.
- Theme accessors are `@ReadOnlyComposable`; static composition locals for rarely changing values.

## Coverage and docs

- `commonTest` for logic, `jvmTest` UI test for interaction, a `ScreenshotTest` gallery entry with
  reviewed goldens, sample catalog entry, README row, `AGENTS.md` repository map updated.

## Platform contracts and evidence

Use [UI verification](../../../skills/kmp-library-conventions/references/ui-verification.md)
for relevant failure cases and verification scope. Review actual text fit and reachable actions at
constrained widths, live RTL changes, gesture ownership inside scrolling parents, and host effects
such as resolved-theme system bars or launch icons when touched. Check that direction-dependent
cache invalidation does not discard caller-owned state.

Name the tested target/build and state. Separate source/JVM checks, native observations, heuristic
versus native audits, and assistive-technology journeys. Report unsupported or blocked checks as
coverage gaps; do not require unrelated platform runs for a change with no cross-platform effect.
