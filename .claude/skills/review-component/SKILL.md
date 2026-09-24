---
name: review-component
description: Review a change to KMPComponents' public components or theme adapters against the repo's API, accessibility, theming, performance, and test conventions. Use when asked to review a PR, diff, or component in this repo.
---

# Review a component change

Read the diff, then each touched file in full. Report findings ranked by severity with
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
- New public API has KDoc; breaking changes are called out for a `feat!:` commit.
- Reusable sub-parts are exposed as their own components rather than duplicated.

## Accessibility

- Titles are headings; dynamic warnings/errors use a polite live region; icon-only controls have a
  `contentDescription`; touch targets are not shrunk below Material minimums.

## Performance

- Derived objects are `remember`ed with the right keys; no work in composition that could be
  hoisted to a pure function computed once.
- Theme accessors are `@ReadOnlyComposable`; static composition locals for rarely changing values.

## Coverage and docs

- `commonTest` for logic, `jvmTest` UI test for interaction, sample catalog entry, README row,
  `AGENTS.md` repository map updated.
