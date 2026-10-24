---
name: add-component
description: Add a new reusable Compose Multiplatform component (or split an existing one) to KMPComponents with the repo's API conventions, tests, sample entry, and docs. Use when asked to create, extract, or break down a component in this repo.
---

# Add a component

Read `AGENTS.md` ("Component API rules") first. For the rules that transfer to any KMP library
(API shape, ABI dumps, version graph, evidence reporting), load `kmp-library-conventions`. Then:

## 1. Decide scope

- Is it product-agnostic? Product copy, persistence, networking, and DI belong in the app.
- Does it need a visual value kommon lacks? Derive it from existing tokens in `TokenAdapters.kt`
  or `MaterialTheme.kmp*`; do not add tokens here.
- Can it be composed from existing public pieces (`FormTextField`, `SecretTextField`,
  `SectionCard`, `StatusMessage`, `TonalBadge`, `CheckStatusRow`, `SelectableCard`, ...)? Prefer that. If an existing component contains a reusable
  piece, extract that piece as its own public composable first.

## 2. Implement

File: `components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/<ComponentName>.kt`,
package `io.github.maniramezan.kmpcomponents`. Put the composable, its `@Immutable` state/labels
classes, and any enum in that file; put pure helpers (validation, mapping) in the same file as
top-level `public`/`internal` functions.

Template:

```kotlin
/** One-line purpose. Non-obvious behavior (what is announced, what is saved, when errors show). */
@Composable
public fun Thing(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    labels: ThingLabels = ThingLabels(),
) {}
```

Checklist while writing:

- `modifier` applied to the root only; child modifiers start from `Modifier`.
- Strings come from parameters / `XxxLabels`; no literal colors, `dp` spacing, or `sp` sizes.
  Icons come from `Glyphs.kt`, sized with `glyphSize(style)` so they scale with the font.
- Semantics: `heading()` for titles, polite `liveRegion` for dynamic warnings/errors, labels for
  icon-only controls.
- `remember` derived values keyed on their inputs; avoid allocations that break skipping.
- Material APIs marked experimental need `@OptIn(ExperimentalMaterial3Api::class)` on the function.

## 3. Test

Use the relevant contracts and evidence levels in
[UI verification](../../../skills/kmp-library-conventions/references/ui-verification.md).
Start with a small behavioral reproducer and an early affected-platform probe; a desktop gallery
render is not proof of mobile label fit, native semantics, keyboard reachability, or host effects.

- Pure logic -> `components/src/commonTest/.../<Name>Test.kt` using `kotlin.test`, backtick names
  describing the invariant.
- Interaction -> add a case to the family's UI test in `components/src/skikoTest/...` (for example
  `EssentialsUiTest.kt`, or a new file) with `runComposeUiTest { setContent { KmpTheme { ... } } }`.
  Assert callbacks fire and visible text/state changes. These run on desktop, the iOS simulator,
  and headless Chrome. Use `nonWebTest` for hover or fling gestures and `jvmTest` only for desktop
  window sizes or desktop APIs.
- Visuals -> add the component to its family's gallery (`Gallery()` in `ScreenshotTest.kt` for the
  original components, `EssentialsGallery()` in `EssentialsScreenshotTest.kt`, or a new
  `<Family>ScreenshotTest.kt` for a new family), run
  `./gradlew :components:recordRoborazziJvm`, and look at the updated images in
  `jvmTest/snapshots/` (light, dark, 200% font) before committing them.

## 4. Showcase and document

- Add the component to the sample catalog in `sample/src/commonMain/.../` (`ComponentCatalog.kt`,
  or the family's own file such as `EssentialsCatalog.kt`).
- Add a row to the README "Catalog" table; update "Theming" if you added a theme accessor.
- Update the repository map in `AGENTS.md` if you added a file.

## 5. Verify and commit

For an intended public API change, update the ABI dump and review the diff:

```bash
./gradlew :components:updateKotlinAbi
```

Otherwise leave the dump unchanged. Run the required checks:

```bash
./gradlew ktlintFormat ktlintCheck :components:verifyRoborazziJvm :components:checkKotlinAbi \
    :components:assemble :sample:compileKotlinJvm
```

Commit an intended `components/api/` change with the implementation; reviewers read it as the API diff.

If the environment cannot reach Google Maven the build cannot configure; say that explicitly
instead of reporting success. Commit with a Conventional Commit (`feat: add Thing component`);
breaking API changes use `feat!:` and describe the migration in the body.
