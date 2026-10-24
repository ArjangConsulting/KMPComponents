# KMPComponents Agent Instructions

`AGENTS.md` is the source of truth for agent guidance in this repo. `CLAUDE.md` only points here.
When this file and the checked-in build disagree, trust `settings.gradle.kts`,
`gradle/libs.versions.toml`, and the module build files, then fix this file.

## Purpose

Reusable, product-agnostic Compose Multiplatform components for Android, desktop (JVM), iOS, and web
(Wasm).
Visual values come from `io.github.maniramezan.kommon:design-system` (`ThemeTokens`); this repo
only *adapts* them to Compose/Material 3 and builds components on top.

- Do not add new design tokens here. If a component needs a value kommon lacks, derive it from
  existing tokens (see `TokenAdapters.kt`) or propose the token upstream in `maniramezan/kommon`.
- Product-specific screens, copy, persistence, networking, and DI stay in consuming apps.

## Consumer agent guidance

`skills/use-kmpcomponents/` is a portable skill for agents using this library in consuming apps.
`docs/using-with-agents.md` explains setup; root `llms.txt` routes documentation readers. These
consumer resources do not replace this file or the contributor playbooks under `.claude/skills/`.
For app-integration tasks, read the consumer skill to avoid applying contributor rules to an app.
The skill's `references/catalog.md` is generated from the README catalog and public declaration
locations. Regenerate it with `python3 scripts/update_agent_catalog.py` after catalog/source moves;
CI runs `python3 scripts/update_agent_catalog.py --check`. Keep its usage examples aligned with APIs.

## Repository map

- `components/` — the main published module (`io.github.maniramezan.kmpcomponents:components`).
  - `commonMain/.../KmpTheme.kt` — `KmpTheme`, composition locals, `MaterialTheme.kmp*` accessors
    and their data classes (`ComposeSpacing`, `KmpColors`, `KmpTypography`, `KmpMotion`).
  - `TokenAdapters.kt` — pure, non-composable token -> Compose conversions. All mapping decisions
    (derived color roles, typography expansion, shapes) live here and are unit tested.
  - Forms and settings: `ThemeMode.kt` (`ThemeMode`, `isDark`, `toggled`), `ThemeModeSelector.kt`
    (`ThemeModeSelector`, `ThemeModeDropdown`), `SectionCard.kt`, `FormTextField.kt`
    (`FormTextField`, `SecretTextField`), `ProviderConfiguration.kt` (state, validation, labels,
    form), `CheckStatus.kt` (`CheckStatus`, `CheckStatusRow`).
  - Feedback: `StatusMessage.kt` (also the shared tone accent), `Badges.kt` (`TonalBadge`,
    `CountBadge`), `EmptyState.kt`, `ConfirmDialog.kt`, `InfoTooltip.kt`.
  - Navigation and layout: `SidebarNavigation.kt` (`SidebarNavigation`, `SidebarItem`),
    `TabChip.kt` (`TabChipRow`, `TabChip`), `SelectableCard.kt`, `SplitPane.kt`
    (`HorizontalSplitPane`), `SectionHeader.kt`, `ContentRow.kt`, `KeyValueRow.kt`.
  - Inputs: `MenuPicker.kt`, `SearchField.kt`, `ToggleRows.kt` (`ThemeSwitch`, `ThemeCheckbox`,
    `ThemeTriStateCheckbox`), `InputControls.kt` (`RadioGroup`, `ThemeSlider`),
    `SelectionList.kt` (`SelectionList`, `SelectionListContent`, `SelectionListLabels`) with its
    model and filtering logic in `SelectionListNode.kt`.
  - More feedback: `NoticeCard.kt`, `ProgressIndicator.kt` (`ProgressIndicator`,
    `InlineProgressIndicator`, internal `InlineSpinner`).
  - Actions: `Buttons.kt` (`PrimaryButton`, `SecondaryButton`, `ThemeTextButton`,
    `ThemeIconButton`, `LoadingIconButton`, `FAB`, `ThemeExtendedFloatingActionButton`),
    `SegmentedControl.kt` (`SegmentedControl`, `ThemeSingleChoiceSegmentedButtonRow`,
    `SegmentDensity`, `SegmentWidthMode`), `SegmentedContent.kt` (layout choice in
    `segmentLayout`), `PillChip.kt`, `ActionPill.kt`. Icons are
    caller-supplied `Painter`s sized with `glyphSize()`.
  - Containers and feedback: `Containers.kt` (`ThemeCard`, `ThemeSurface`, `BottomSheet`,
    `ThemeDialog`, `ThemeSnackbar`), `Toast.kt` (`Toast`, `ToastHost`, `ToastHostState`),
    `StateViews.kt` (`LoadingState`, `ErrorState`), `Skeleton.kt` (`Skeleton`, `SkeletonBlock`,
    `Modifier.skeletonShimmer`), `Layout.kt` (`AdaptiveContentContainer`, `OverlayCard`,
    `DisclosureCard`, dividers, `FlowLayout`). `ConfirmDialog` delegates to `ThemeDialog`.
  - Lists and navigation: `Lists.kt` (`ThemeListItem`, `LazyList`, `LoadMoreFooter`),
    `Navigation.kt` (top app bars, `TabBar`, `TabBarItem`, `ThemeTabRow`, `NavRail`,
    `AdaptiveNavScaffold`, `TabBarDefaults`), `TabBarScrollBehavior.kt`, `PaginatedContent.kt`
    (title layout math in `titleProgress` / `titleOffset`), `Showcase.kt` (`ShowcaseRow`,
    `ShowcaseFeed`), `FlipCard.kt`, `AppText.kt`.
  - Chat and assistant: `ChatLog.kt`, `AssistantComponents.kt`, `TypingIndicator.kt`
    (`typingDotAlpha` is the pure wave function), `LoadingOverlay.kt`.
  - Desktop extras: `VerticalSplitPane.kt`, `StatusBar.kt`, `ZoomableImage.kt` (`zoomForScroll`),
    `CopyButton.kt`, `FocusTraversal.kt` (`Modifier.focusNextOnTab`, a `Modifier.Node`).
  - Accessibility: `KeyboardFocus.kt` adds token-derived focus outlines; `ReduceMotion.kt` exposes
    `LocalReduceMotionEnabled` for host overrides, `rememberReduceMotionEnabled()` for the effective value, and observes Android/iOS setting changes.
    `AccessibilityRegressionTest.kt` covers rendered focus across themes and font scales;
    `FocusScreenshotTest.kt` records focused controls.
  - Internal helpers: `ComponentMetrics.kt` (`minimumTouchTargetSize`, `CompactWindowMaxWidth`), `TooltipIconButton.kt`
    (`TooltipIconButton`, `GlyphIconButton`: icon-only buttons with a tooltip).
  - `Glyphs.kt` — internal Canvas glyphs (eye, close, info, search, check, chevron, copy) so the library needs no icon font.
  - `jvmMain`/`androidMain`/`iosMain`/`wasmJsMain` — only `resizeHorizontalPointerIcon` and
    `isReduceMotionEnabled` (`ReduceMotion.kt`; desktop always returns `false`). Decorative,
    looping animations check `decorativeAnimationsEnabled()`, which also turns them off in previews.
    Also `resizeVerticalPointerIcon` and `plainTextClipEntry` (`Clipboard.kt`), since each platform
    builds clipboard entries differently. The browser reduced-motion adapter subscribes to
    `prefers-reduced-motion` changes, and browser clipboard entries use Compose's plain-text helper.
    Web resize cursors fall back to the hand cursor because CSS resize cursors are internal.
  - `commonTest/` — pure logic tests, run on every target.
  - `jvmTest/` — Compose UI tests (`runComposeUiTest`); desktop only because they need Skiko.
    `AccessibilityAuditTest.kt` checks selection cues, keyboard actions, accessible names, and minimum targets.
    `SelectionAccessibilityScreenshotTest.kt` records selection and focus across themes, text sizes, and RTL.
    `ScreenshotTest.kt` renders a gallery of the original components into golden images in
    `jvmTest/snapshots/`; newer families get their own gallery file (`EssentialsScreenshotTest.kt`,
    `ActionsScreenshotTest.kt`, `SelectionScreenshotTest.kt`,
    `ContainersScreenshotTest.kt`, `NavigationScreenshotTest.kt`,
    `AssistantScreenshotTest.kt`, `DesktopExtrasScreenshotTest.kt`; `SegmentedControlsScreenshotTest.kt`
    covers live layout-direction changes, and `SegmentedContentTest.kt` holds both its interaction test and goldens)
    so each set of goldens stays reviewable.
  - `api/` — the checked-in public ABI dump (`checkKotlinAbi` compares against it).
- `components-image/` — the second published module (`...:components-image`, package
  `io.github.maniramezan.kmpcomponents.image`): `CachedAsyncImage` and `ImageCachePolicy`, backed by
  Coil 3 with Ktor (one engine per platform). It is separate so `components` stays free of an image
  loader; keep Coil types out of its public API (they are `implementation` dependencies). It has
  its own `api/` dump, common tests, and a desktop UI test, and shares the version with `components`.
- Root `build.gradle.kts` aggregates Dokka for both modules: `./gradlew :dokkaGenerate` writes
  `build/dokka/html`, which CI uploads for GitHub Pages.
- `sample/` — unpublished catalog showing every component. `commonMain` holds the catalog
  (`ComponentCatalog.kt`, with newer families in their own files such as `EssentialsCatalog.kt`,
  `ActionsCatalog.kt`, `ChoicesCatalog.kt`, `ContainersCatalog.kt`, `NavigationBarsCatalog.kt`, `AssistantCatalog.kt`, `DesktopCatalog.kt`, and `ImageCatalog.kt`, plus the sample's own `SampleIcons.kt`);
  `jvmMain` the desktop window (`./gradlew :sample:run`) and its PNG app icon; `icons/` contains
  desktop packaging icons. `iosMain` holds the view controller for `sample/ios` (XcodeGen spec,
  Swift host, and app icon asset catalog).
- The catalog's section picker opens each component family. `PickerAvailabilityControls.kt` lets
  testers disable a popup while it is open. `sample/src/jvmTest/.../CatalogUiTest.kt` renders all
  sections at 100%/200% text in LTR/RTL and checks section navigation.
- Catalog examples use generic copy and temporary local UI state. Do not copy consuming apps'
  workflows, business rules, storage, credentials, or backend integrations into the sample.
  Simulations must be labeled; image examples default to embedded data and load a URL only after
  an explicit action.
- `sample-android/` — Android app hosting the catalog (`./gradlew :sample-android:installDebug`),
  with an adaptive launcher icon using the sample artwork.
- `.run/` — shared IDE run configurations (desktop and Android sample catalogs, JVM tests, all-target tests, CI
  check). Keep them in sync with the CI task list.
- `.claude/skills/` — task playbooks (`add-component`, `review-component`).
- `.github/workflows/` — `ci.yml` (PRs and main), `docs.yml` (Dokka to GitHub Pages after CI on
  main), `release-please.yml` -> `release.yml` (Maven Central publish of both modules on release).

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
11. **Names follow the sibling libraries.** If ComposeUIComponents or SwiftUIComponents already
    has the component, reuse its name. If that name collides with a Material 3 (or
    `androidx.compose.ui.window`) composable, prefix it with `Theme` (`ThemeSwitch`, not
    `Switch`) so apps never pick up the unthemed Material one by accident. List new name
    mappings in the README.

### Styling and visual accessibility

- Follow `docs/design-style-guide.md` for shared color roles, contrast requirements, and verification.
- Use `MaterialTheme.kmpSpacing` for padding and gaps. Let text and icons determine control size;
  avoid fixed control widths that clip translated labels or text at 200% font scale. Segmented
  labels should request their full intrinsic width while respecting the available space.
- Use matching foreground and background roles (`onSurfaceVariant` on `surfaceVariant`,
  `onSecondaryContainer` on `secondaryContainer`). Both `outline` and `outlineVariant` must retain
  at least 3:1 contrast for enabled controls; Material outlined buttons use `outlineVariant`.
  Interactive dividers need at least 3:1 contrast against adjacent surfaces in both
  normal and focused states. Check the default and alternate themes on `surface` and
  `surfaceVariant`; custom themes must preserve those contrast relationships.
- Derive glyph dimensions from `glyphSize(style)`, keep their drawing inside the canvas bounds,
  and use consistent optical insets. Geometry fractions describe the drawing, not new tokens.
  Reserve Material's minimum interactive size independently of the visible glyph size. Separate
  neighboring controls with a spacing token and retain the container's end padding.
- Keyboard focus must be visible through a border, thickness, or shape change as well as color.
  Use theme colors and shapes for the indicator, and preserve accessible names and tooltips.
- Catalog examples use generic copy. Preview frame dimensions may be fixed, but component
  padding, typography, colors, and shapes follow the same token rules as the library.
- Verify label fit and icon controls at 100% and 200% font scales. Contrast regressions should
  measure the rendered divider in light and dark modes, including focus and alternate themes.
  Review updated light, dark, and large-text screenshot goldens before accepting visual changes.


## Adding or changing a component

Load `.claude/skills/add-component/SKILL.md`. In short: one file per component family in
`components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/`, logic tests in
`commonTest`, a UI test in `jvmTest`, a gallery entry in `ScreenshotTest.kt` (then re-record), an
updated ABI dump, an entry in `sample/.../ComponentCatalog.kt`, and a row in the README catalog
table.

Glyphs need no icon dependency: add a case to `Glyphs.kt`. Size anything that is not spacing from
typography (`glyphSize(style)`) so it follows the user's font scale.

## Build and verification

```bash
./gradlew :components:jvmTest                       # fastest: common + desktop UI tests
./gradlew :sample:jvmTest
./gradlew :components:allTests                      # all targets (iOS needs macOS + Xcode; web needs Chrome)
./gradlew ktlintCheck                               # style; ktlintFormat fixes most findings
./gradlew :components:verifyRoborazziJvm            # desktop tests + screenshot comparison
./gradlew :components:recordRoborazziJvm            # re-record goldens after an intended change
./gradlew :components:checkKotlinAbi                # public API vs components/api
./gradlew :components:updateKotlinAbi               # accept an intended API change
./gradlew :components:koverHtmlReport               # coverage (build/reports/kover)
./gradlew :components-image:allTests :components-image:checkKotlinAbi  # the image module
./gradlew :dokkaGenerate                            # API docs for both modules (build/dokka/html)
./gradlew :components:assemble :sample:compileKotlinJvm :sample-android:assembleDebug
```

CI (`ci.yml`, macOS, JDK 21) runs `ktlintCheck`, `allTests` and `checkKotlinAbi` for both modules,
`verifyRoborazziJvm`, Kover, `assemble`, the root `dokkaGenerate`, and builds the desktop, Android, and iOS
samples. It also runs `:sample:jvmTest`. A screenshot failure uploads the diff images in the
`reports` artifact. Review a changed golden before committing it; an API dump change means a public API change (see Releases).
The build needs Google Maven (`dl.google.com`) for the Android Gradle plugin; sandboxes without it
cannot configure the project, so say so rather than claiming a change was verified.

- `-PuseLocalKommon=true` substitutes a sibling `../kommon` checkout via composite build; add
  `-PkommonDir=<path>` to use another checkout, such as a git worktree.
- The `wasmJs` target runs `commonTest` in headless Chrome. It declares `binaries.executable()` only
  so webpack bundles Skiko for those tests (CMP-4906). The Node.js, Yarn and Binaryen downloads the Kotlin
  plugin needs are declared in `settings.gradle.kts`, because `FAIL_ON_PROJECT_REPOS` rejects the
  repositories the plugin would otherwise add.
- Versions live in `gradle/libs.versions.toml` only. Declare Compose libraries through catalog
  entries (`libs.compose.*`); the plugin's `compose.runtime`/`compose.material3`/... accessors are
  deprecated at error level in Compose Multiplatform 1.12. Material 3 has its own version
  (`composeMaterial3`); keep it on the release the Compose plugin pairs with `compose`.
  Dependabot groups Gradle and GitHub Actions updates into one weekly dependency PR. The ktlint
  engine version (`ktlint` in the catalog) is not tracked by Dependabot; bump it by hand.
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
