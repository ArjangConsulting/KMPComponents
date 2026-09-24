# KMPComponents Agent Instructions

`AGENTS.md` is the source of truth for agent guidance in this repo. `CLAUDE.md` only points here.
When this file and the checked-in build disagree, trust `settings.gradle.kts`,
`gradle/libs.versions.toml`, and the module build files, then fix this file.

## Purpose

Reusable, product-agnostic Compose Multiplatform components for Android, desktop (JVM), and iOS.
Visual values come from `io.github.maniramezan.kommon:design-system` (`ThemeTokens`); this repo
only *adapts* them to Compose/Material 3 and builds components on top.

- Do not add new design tokens here. If a component needs a value kommon lacks, derive it from
  existing tokens (see `TokenAdapters.kt`) or propose the token upstream in `maniramezan/kommon`.
- Product-specific screens, copy, persistence, networking, and DI stay in consuming apps.

## Repository map

- `components/` — the only published module (`io.github.maniramezan.kmpcomponents:components`).
  - `commonMain/.../KmpTheme.kt` — `KmpTheme`, composition locals, `MaterialTheme.kmp*` accessors
    and their data classes (`ComposeSpacing`, `KmpColors`, `KmpTypography`, `KmpMotion`).
  - `TokenAdapters.kt` — pure, non-composable token -> Compose conversions. All mapping decisions
    (derived color roles, typography expansion, shapes) live here and are unit tested.
  - `ThemeMode.kt`, `ThemeModeSelector.kt`, `SectionCard.kt`, `FormTextField.kt`
    (`FormTextField`, `SecretTextField`), `StatusMessage.kt`, `ProviderConfiguration.kt`
    (state, validation, labels, form).
  - `commonTest/` — pure logic tests, run on every target.
  - `jvmTest/` — Compose UI tests (`runComposeUiTest`); desktop only because they need Skiko.
- `sample/` — unpublished desktop catalog showing every component (`./gradlew :sample:run`).
- `.claude/skills/` — task playbooks (`add-component`, `review-component`).
- `.github/workflows/` — `ci.yml` (PRs and main), `release-please.yml` -> `release.yml` (Maven
  Central publish on release).

## Component API rules

Follow these for every public composable; reviewers check them.

1. **Stateless first.** Take `value` + `onValueChange` (or `state` + `onStateChange`). Keep only
   ephemeral UI state internal (e.g. `SecretTextField`'s reveal toggle) and document it.
2. **Parameter order:** required parameters, then `modifier: Modifier = Modifier`, then optional
   parameters, then a trailing `content` lambda if any. Apply `modifier` to the root only.
3. **No hardcoded user-visible strings without a parameter.** Defaults in English are fine, but
   expose them as parameters or a `@Immutable data class XxxLabels` for multi-string components.
4. **Theme only via tokens.** Use `MaterialTheme.colorScheme/typography/shapes` and
   `MaterialTheme.kmpSpacing/kmpColors/kmpTypography/kmpMotion`. No literal colors, `dp` spacing,
   or `sp` sizes (a 1.dp border is the accepted exception).
5. **Accessibility.** Headings get `semantics { heading() }`; dynamic warnings/errors use a polite
   `liveRegion`; interactive elements have a text label or `contentDescription`.
6. **Compose-stable models.** Public state/label classes are `@Immutable data class`es with `val`s.
   Classes holding secrets override `toString()` to redact them.
7. **Composable decomposition.** Build larger components from smaller public ones
   (`ProviderConfigurationForm` = `FormTextField` x3 + `SecretTextField`) so apps can reuse parts.
8. **Pure logic out of composables.** Validation, mapping, and formatting are plain functions in
   `commonMain` with `commonTest` coverage.
9. **KDoc** on every public declaration that isn't self-explanatory, including non-obvious
   behavior (masking, when errors show, what is announced).
10. `explicitApi()` is on: every public declaration needs `public` and an explicit type.

## Adding or changing a component

Load `.claude/skills/add-component/SKILL.md`. In short: one file per component family in
`components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/`, logic tests in
`commonTest`, a UI test in `jvmTest`, an entry in `sample/.../ComponentCatalog.kt`, and a row in the
README catalog table.

## Build and verification

```bash
./gradlew :components:jvmTest                       # fastest: common + desktop UI tests
./gradlew :components:allTests                      # all targets (iOS needs macOS + Xcode)
./gradlew :components:assemble :sample:compileKotlinJvm
```

CI runs `:components:allTests :components:assemble :sample:compileKotlinJvm` on macOS with JDK 21.
The build needs Google Maven (`dl.google.com`) for the Android Gradle plugin; sandboxes without it
cannot configure the project, so say so rather than claiming a change was verified.

- `-PuseLocalKommon=true` substitutes a sibling `../kommon` checkout via composite build.
- Versions live in `gradle/libs.versions.toml` only. Dependabot groups Kotlin, Compose, AGP,
  publishing, and kommon updates.
- Keep Kotlin/JVM bytecode at `JvmTarget.JVM_17` to match kommon.
- `.editorconfig` holds the house style (ktlint `intellij_idea` style, 120 columns, expression bodies
  may stay on the signature line, `@Composable` functions are PascalCase).

## Releases

- Conventional Commits drive Release Please (`feat:` minor pre-1.0, `fix:` patch, `!` breaking).
  Call out breaking public API changes in the commit body; the library is pre-1.0, so they are
  allowed but must be explicit.
- Never hand-edit versions. `VERSION_NAME` is injected by `release.yml` from the tag, which must
  equal `.release-please-manifest.json`.
- Publishing needs `MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_PASSWORD`, `SIGNING_IN_MEMORY_KEY`,
  and `SIGNING_IN_MEMORY_KEY_PASSWORD` in the `secrets` environment.
