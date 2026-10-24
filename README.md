# KMPComponents

[![Maven Central](https://img.shields.io/maven-central/v/io.github.maniramezan.kmpcomponents/components)](https://central.sonatype.com/artifact/io.github.maniramezan.kmpcomponents/components)
[![CI](https://github.com/ArjangConsulting/KMPComponents/actions/workflows/ci.yml/badge.svg)](https://github.com/ArjangConsulting/KMPComponents/actions/workflows/ci.yml)

Reusable Compose Multiplatform components for Android, desktop (JVM), iOS, and web (Wasm). The library adapts
the platform-neutral tokens from [`kommon`](https://github.com/maniramezan/kommon)'s
`design-system` module; it does not define a competing design system.

## Using coding agents

Give your agent the portable [use-kmpcomponents skill](skills/use-kmpcomponents/SKILL.md) to find
existing components, check the API against your installed version, and wire theme, state, labels,
and callbacks. It includes a [component index with source links](skills/use-kmpcomponents/references/catalog.md)
and a [usage guide](skills/use-kmpcomponents/references/usage.md).

[Setup instructions](docs/using-with-agents.md) cover copying the skill into Codex or Claude Code
projects and using the same guide with other agents. [llms.txt](llms.txt) is a compact discovery
index. Contributor instructions for changing this library remain in `AGENTS.md`.

## Install

The latest release is on [Maven Central](https://central.sonatype.com/namespace/io.github.maniramezan.kmpcomponents).
Release Please updates the versions below when a release is cut.

<!-- x-release-please-start-version -->
```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.maniramezan.kmpcomponents:components:0.2.0")
        }
    }
}
```
<!-- x-release-please-end -->

`kommon`'s `design-system`, Compose `runtime`, and `material3` are exposed as `api` dependencies,
so token types and Material components are available without extra declarations.

Targets: `android` (minSdk 26), `jvm`, `iosArm64`, `iosSimulatorArm64`, `wasmJs` (browser).

Wasm builds use the `wasmJs` variant published by kommon `0.3.0` or later. The default build
resolves it from Maven Central; no local dependency substitution is required.

### Optional: async images

`CachedAsyncImage` lives in a separate artifact so the main library does not pull in an image
loader. It is backed by [Coil 3](https://coil-kt.github.io/coil/) with Ktor networking and
brings each platform's Ktor engine (Android, Darwin, Java, JS); no Coil types appear in its API.

<!-- x-release-please-start-version -->
```kotlin
implementation("io.github.maniramezan.kmpcomponents:components-image:0.2.0")
```
<!-- x-release-please-end -->

The image artifact has the same targets as the core library. On the web there is no disk cache, so
`diskCachePolicy` has no effect and the browser's HTTP cache handles repeat loads.

On Android, network images need `<uses-permission android:name="android.permission.INTERNET" />`
in the app manifest. Both artifacts share one version.

## Quick start

```kotlin
var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }
var provider by remember { mutableStateOf(ProviderConfigurationState()) }
var submitted by remember { mutableStateOf(false) }

KmpTheme(mode = themeMode) {
    SectionCard(title = "Appearance") {
        ThemeModeSelector(selected = themeMode, onSelect = { themeMode = it })
    }
    SectionCard(title = "Provider", description = "Where requests are sent") {
        ProviderConfigurationForm(
            state = provider,
            onStateChange = { provider = it },
            showValidationErrors = submitted,
            onDone = { submitted = true },
        )
        if (submitted && provider.isValid()) {
            StatusMessage("Saved", tone = StatusTone.SUCCESS)
        }
    }
}
```

## Catalog

API reference for both artifacts: run `./gradlew :dokkaGenerate` (output in `build/dokka/html`),
or browse the copy for the latest release on
[GitHub Pages](https://arjangconsulting.github.io/KMPComponents/).

### Theme and forms

| Component | Purpose |
| --- | --- |
| `KmpTheme` | Applies kommon `ThemeTokens` as a Material 3 theme for a `ThemeMode`. |
| `ThemeModeSelector` | Segmented System / Light / Dark control. |
| `ThemeModeDropdown` | Labeled dropdown for the same choice, for settings forms. Built on `MenuPicker`. |
| `ThemeMode.toggled` | The opposite explicit mode, for a single light/dark toggle button. |
| `SectionCard` | Titled group of content with optional description and header action. Title is an accessibility heading. |
| `FormTextField` | Outlined text field with label, placeholder, and supporting/error text. |
| `SecretTextField` | Masked field with a Show/Hide text or eye-icon toggle and a password keyboard without autocorrect. |
| `ProviderConfigurationForm` | Provider name, base URL, model, and API key fields with validation, built from the fields above. |
| `CheckStatus` / `CheckStatusRow` | State and one-line display of an on-demand check such as "Test connection" (idle, spinner, success, failure). Results are announced. |
| `MenuPicker` | Labeled dropdown field for picking one item of any type, for forms where a segmented control does not fit. |
| `SearchField` | Single-line search field with a search glyph, a clear button, and a keyboard Search action. A blank placeholder draws none. |
| `ThemeSwitch` | Setting row with a label, optional supporting text and tooltip, and a trailing switch. The whole row toggles. |
| `ThemeCheckbox` / `ThemeTriStateCheckbox` | Checkbox rows with a label; the tri-state one is for "select all" over a group. |
| `RadioGroup` | Vertical radio list; each full-width row is the tap target. |
| `ThemeSlider` | Full-width slider with an optional spoken label, value description, and steps. |
| `SelectionList` / `SelectionListContent` / `SelectionListNode` | Searchable one- or two-level picker, single or multiple choice, in a bottom sheet or inline. Parents expand inline, scroll into view, and summarize their selected children when collapsed. |

### Feedback

| Component | Purpose |
| --- | --- |
| `StatusMessage` | Inline info / success / warning / error message. Warnings and errors are announced by screen readers. Sizes to its content. |
| `TonalBadge` | Short label on a tinted background (HTTP method, status code, severity), by color or `StatusTone`. |
| `CountBadge` | Pill with a count, capped as `99+`, with an optional spoken description. |
| `EmptyState` | Centered title, message, and optional action for an empty pane. |
| `ConfirmDialog` | Confirm / cancel dialog, optionally with a destructive (error-colored) confirm button. |
| `InfoTooltip` | Info glyph showing an explanation on hover, long-press, or focus; configurable hover duration (`displayDurationMillis = null` keeps it visible while hovered); screen readers read it directly. |
| `NoticeCard` | Full-width `StatusTone` notice with an optional action ("Retry") and dismiss button. |
| `ProgressIndicator` | Centered spinner or determinate bar with an announced label, for a loading pane. |
| `LocalReduceMotionEnabled` | Optional override for decorative animations and page hints; null follows live system settings. |
| `rememberReduceMotionEnabled` | Returns the effective reduced-motion preference (the override if set, else the system), so app animations can follow it. |
| `InlineProgressIndicator` | Text-sized spinner and label for work in progress inside a row or toolbar. |

### Actions

Icons are `Painter`s you supply (for an `ImageVector`, `rememberVectorPainter(icon)`); they are
sized from the body text so they follow the user's font scale.

| Component | Purpose |
| --- | --- |
| `PrimaryButton` / `SecondaryButton` | Filled and outlined text buttons for the primary and secondary action. |
| `ThemeTextButton` | Low-emphasis text button, optionally `destructive` (error-colored). |
| `ThemeIconButton` | Icon-only button; its description is also shown as a tooltip. |
| `LoadingIconButton` | Icon button that shows a spinner and ignores clicks while loading. |
| `FAB` / `ThemeExtendedFloatingActionButton` | Floating action buttons; the extended one is named by its visible label. |
| `SegmentedControl` | Pill-shaped segments for one choice, with `SegmentDensity` and `SegmentWidthMode`. |
| `ThemeSingleChoiceSegmentedButtonRow` | Material's connected, outlined segmented buttons with a check mark; scrolls to preserve full labels in tight layouts. |
| `SegmentedContent` | Segment bar (pill, underline, or bare) above content that crossfades; scrolls with edge fades instead of truncating titles. |
| `PillChip` | Capsule chip with selected and unselected looks; selectable when clickable, static otherwise. |
| `ActionPill` | Capsule command with arbitrary content and no selected state. |

### Containers, feedback, and layout

| Component | Purpose |
| --- | --- |
| `ThemeCard` / `ThemeSurface` | Elevated (optionally clickable) and flat full-width containers that stack their content. |
| `DisclosureCard` | Card with an always-visible summary that reveals detail; announces expanded or collapsed. |
| `OverlayCard` | Translucent rounded container for labels over images or video. |
| `ThemeDialog` | Alert dialog with a confirm action and an optional dismiss action; `ConfirmDialog` is built on it. |
| `BottomSheet` | Modal bottom sheet that always opens fully expanded. |
| `Toast` / `ToastHost` / `ToastHostState` | Transient messages with optional icon and action, shown one at a time like snackbars. `showToast` suspends and returns a `ToastResult`. |
| `ThemeSnackbar` | Snackbar with an optional action, announced when shown standalone. |
| `LoadingState` / `ErrorState` | Pane placeholders for loading and failure, announced by screen readers; siblings of `EmptyState`. |
| `Skeleton` / `SkeletonBlock` / `Modifier.skeletonShimmer` | Decorative loading placeholders and a sweep that stays still when the system reduces motion. |
| `ThemeHorizontalDivider` / `ThemeVerticalDivider` | Rules in the outline variant color. |
| `FlowLayout` | Wrapping row with the standard gaps, for chips and button groups. |
| `AdaptiveContentContainer` | Centers content and caps its width on wide windows. |

### Chat and assistant

| Component | Purpose |
| --- | --- |
| `ChatLog` / `ChatMessage` / `ChatMessageState` | Conversation with user and assistant bubbles, typing and retryable error states; follows new messages only while the reader is at the bottom. |
| `ChatComposer` / `ChatComposerVoiceInput` / `ChatComposerVoiceState` | Growing message field with a send button (Enter sends, Shift+Enter adds a line) and optional caller-driven voice input: microphone, stop with a level ring, and processing. |
| `TypingIndicator` | Three-dot "reply is being written" wave; still when the system reduces motion. |
| `rememberTypewriterReveal` | Reveals streamed text a character at a time, continuing as it grows; shows it at once when the system reduces motion. |
| `LoadingOverlay` | Scrim that blocks interaction with its content during work the user must wait for, and announces it. |
| `AssistantContextCard` | Title, highlighted value, optional quote, and footnote describing what the assistant sees. |
| `AssistantQuickActionChips` | Scrolling filter chips for quick actions, with caller-owned state. |
| `AssistantStatusBanner` | Announced error banner for an unavailable provider or blocking failure. |
| `AssistantLimitPromptCard` | Quota or upgrade prompt with primary and secondary actions. |
| `AssistantDisclaimerFooter` | Low-emphasis disclaimer line. |

### Images (`components-image`)

| Component | Purpose |
| --- | --- |
| `CachedAsyncImage` / `ImageCachePolicy` | Loads a URL, file, or bytes with explicit memory, disk, and network cache policies, plus placeholder, error, and fallback painters. Package `io.github.maniramezan.kmpcomponents.image`. |

### Navigation and layout

| Component | Purpose |
| --- | --- |
| `SidebarNavigation` / `SidebarItem` | Desktop-style side navigation with a header and selectable destinations. |
| `TabChipRow` / `TabChip` | Scrolling row of compact tabs with optional close controls. |
| `SelectableCard` | Outlined card with a selection box, checked state, and visible keyboard focus; reports selection to accessibility services. |
| `HorizontalSplitPane` | Two panes with a divider you can drag, move with arrow keys, or adjust with a screen reader. |
| `VerticalSplitPane` | The same divider between a top and a bottom pane, moved with the Up/Down keys. |
| `StatusBar` / `StatusBarItem` / `StatusBarSeparator` | Desktop status bar; items share space and ellipsize rather than dropping off. |
| `ZoomableImage` | Fitted image with wheel, keyboard, and drag zoom/pan and an optional marker ring; announces the zoom level. |
| `CopyButton` | Copies text to the clipboard, then shows a check and announces "Copied". |
| `Modifier.focusNextOnTab` | Makes Tab and Shift+Tab move focus out of fields that would otherwise consume the key. |
| `SectionHeader` | Title (an accessibility heading), optional subtitle, and trailing actions above a list or pane. |
| `ThemeListItem` / `LazyList` | Material-style list row read as one item, and a padded full-width `LazyColumn`. |
| `LoadMoreFooter` | Last item of an infinite list; asks for the next page when it scrolls into view and announces loading. |
| `ThemeTopAppBar` / `ThemeMediumTopAppBar` / `ThemeLargeTopAppBar` | Top app bars on the surface color with primary-tinted icons. |
| `ThemeTabRow` | Material primary tab row for peers at one level. |
| `TabBar` / `TabBarItem` / `TabBarScrollBehavior` | Bottom navigation with badges, a selected pill, and optional hide-on-scroll. |
| `NavRail` | Side rail sharing `TabBarItemData` and colors with `TabBar`. |
| `AdaptiveNavScaffold` | `TabBar` in compact windows, `NavRail` from 600 dp up. |
| `PaginatedContent` | Swipeable pages with a synced title row, dots or progress footer, and an optional forward-only mode. |
| `PageIndicator` | Dots for the current page of a pager or carousel; the current dot is a wider pill, and an optional description is announced. |
| `ShowcaseRow` / `ShowcaseFeed` / `ShowcaseSnapping` | Peeking horizontal carousels that settle on an item edge (or scroll freely), and an app-store style feed of them. SwiftUIComponents calls these `CarouselRow` and `CarouselBoard`. |
| `FlipCard` | Two-sided card that turns in 3D; only the visible face is read by screen readers. |
| `AppText` / `AppTextStyle` | Text in a semantic role, including heavier weight variants of Material slots. |
| `ContentRow` | List row with title, secondary and supporting text, leading/trailing slots, and optional click and selection. |
| `KeyValueRow` | Read-only label/value pair for details panels; flows onto separate lines when space is tight and is read as one item. |
| `Avatar` / `AvatarSize` | Circular image or initials for a person, read as one named image; sizes follow the text size. |

### Names shared with the sibling libraries

Components that also exist in [ComposeUIComponents](https://github.com/maniramezan/ComposeUIComponents)
or [SwiftUIComponents](https://github.com/maniramezan/SwiftUIComponents) use the same name here,
for example `SectionHeader`, `ContentRow`, `PrimaryButton`, `PillChip`, `SelectionList`,
`BottomSheet`, `Toast`, `DisclosureCard`, `LoadingState`, `Skeleton`, `TabBar`, `NavRail`,
`PaginatedContent`, `PageIndicator`, `SegmentedContent`, `ShowcaseFeed`, `FlipCard`, `Avatar`, `ChatLog`, `ChatComposer`,
`TypingIndicator`, `rememberTypewriterReveal`, `LoadingOverlay`,
and the `Assistant*` components. Where that name would
collide with a Material 3 composable, the component gets a `Theme` prefix instead (`ThemeSwitch`,
`ThemeCard`), like SwiftUIComponents' `ThemeButton`. Enum entries use SCREAMING_CASE
(`ToastDuration.SHORT`, not `Short`). Some ComposeUIComponents names map to a different or
existing component here:

| ComposeUIComponents | KMPComponents |
| --- | --- |
| `Switch` | `ThemeSwitch` |
| `Checkbox` | `ThemeCheckbox` |
| `Card` | `ThemeCard` |
| `Surface` | `ThemeSurface` |
| `Dialog` | `ThemeDialog` |
| `Snackbar` | `ThemeSnackbar` |
| `HorizontalDivider` / `VerticalDivider` | `ThemeHorizontalDivider` / `ThemeVerticalDivider` |
| `ListItem` | `ThemeListItem` |
| `TopAppBar` / `MediumTopAppBar` / `LargeTopAppBar` | `ThemeTopAppBar` / `ThemeMediumTopAppBar` / `ThemeLargeTopAppBar` |
| `TabRow` | `ThemeTabRow` |
| `Slider` | `ThemeSlider` |
| `TextButton` | `ThemeTextButton` |
| `IconButton` | `ThemeIconButton` |
| `ExtendedFloatingActionButton` | `ThemeExtendedFloatingActionButton` |
| `SingleChoiceSegmentedButtonRow` | `ThemeSingleChoiceSegmentedButtonRow` |
| `TextField` | `FormTextField` |
| `PasswordField` | `SecretTextField` |
| `Section` | `SectionCard` |

All components are stateless unless noted, take a `Modifier` as their first optional parameter,
and accept user-visible strings as parameters (or a `*Labels` data class) so apps can localize
them. Built-in glyphs (search, close, check, chevron) are drawn by the library and other icons
are `Painter`s you pass in, so there is no icon-font dependency.

### Sample app

The shared catalog runs on desktop, Android, iOS, and the web. Each release publishes the web build
at [arjangconsulting.github.io/KMPComponents/catalog/](https://arjangconsulting.github.io/KMPComponents/catalog/),
so you can try every component in a browser. Use **Catalog section** to jump between
component families. Every public component has an example, including standalone list and
navigation primitives. Examples use generic copy and temporary local state; switching sections
resets their state. Forms keep local state; chat replies, progress, and loading are UI simulations.
Image examples use embedded illustrations by default; only **Load URL** requests a remote image.

Expand **Appearance and accessibility** to switch the light/dark theme, token set, and
100%, 130%, or 200% font scale.
Use **Right-to-left layout** to test RTL rendering and splitter movement. To test a dropdown being
disabled while open, press **Disable picker in 3 seconds**, then open that picker. Re-enable it
with **Picker enabled**. The horizontal split-pane example also shows its fraction and a reset
button.

```bash
./gradlew :sample:run                          # desktop
./gradlew :sample:wasmJsBrowserDevelopmentRun  # web, served on localhost
./gradlew :sample-android:installDebug         # Android device or emulator
xcodegen generate --spec sample/ios/project.yml && open sample/ios/KmpComponentsSample.xcodeproj
```

The iOS project builds the Kotlin framework in a pre-build phase; pick an Apple Silicon simulator
and run. It needs [XcodeGen](https://github.com/yonaskolb/XcodeGen) (`brew install xcodegen`).

The Android build uses AGP 9.1.1 and SDK 37. Use Android Studio Panda 3 (2025.3.3) Patch 1
or newer, which supports both according to the
[Android compatibility table](https://developer.android.com/build/releases/about-agp#api-level-support).
AGP stays in the 9.1 series so the catalog does not require an IDE that supports AGP 9.4.

In Android Studio, reload/sync the root Gradle project, select **Run** > *Sample catalog (Android)*,
choose an emulator or connected device running Android 8.0/API 26 or later, then Run or Debug.
The application module is `sample-android`; the `sample` module supplies the shared catalog UI.
If Android is missing from an existing checkout's project view, reload all Gradle projects so
`settings.gradle.kts` is imported again. The Android App configuration requires Android IDE support;
Android Studio includes it. The `installDebug` command above also installs the catalog from a terminal.

The desktop catalog uses the Arjang artwork for its window, Dock/taskbar, and packaged launcher.
Stop and rerun an already-open catalog after updating the icon; the existing JVM keeps its loaded resources.

The shared configurations in `.run/` also include **Run** > *Sample catalog (desktop)* and
**Test** > *Tests (JVM)*, *Tests (all targets)*, and *Check (CI)*.

## Theming

Component styling follows the [design style guide](docs/design-style-guide.md):
token-based spacing and colors, typography-scaled glyphs, Material minimum interactive sizes,
visible keyboard focus, and contrast checks across themes and font scales.

`KmpTheme(mode, tokens)` converts kommon tokens once per token change (results are `remember`ed)
and provides:

| Access | Source token |
| --- | --- |
| `MaterialTheme.colorScheme` | `ColorSchemeTokens`. The nine kommon roles map directly; additional roles derive from them so the Material baseline palette never appears. `outlineVariant` strengthens kommon's `outline` toward `onSurface` for visible button boundaries. |
| `MaterialTheme.typography` | `TypographyTokens`. display, title, body, and label expand to the full Material scale. |
| `MaterialTheme.shapes` | `ShapeTokens` radii. |
| `MaterialTheme.kmpSpacing` | `SpacingTokens` as `Dp`. |
| `MaterialTheme.kmpColors` | `success` and `warning`, which have no Material role. |
| `MaterialTheme.kmpTypography.code` | Monospace `code` style. It is intentionally not mapped to `bodySmall`, which Material uses for supporting text. |
| `MaterialTheme.kmpMotion` | Animation durations in milliseconds. |

To brand an app, pass a copy of `KommonDesignTokens.default` with your own values:

```kotlin
val brand = KommonDesignTokens.default.copy(
    lightColors = KommonDesignTokens.default.lightColors.copy(primary = ColorToken(0xFF_00_6C_4F)),
)
KmpTheme(tokens = brand) { /* ... */ }
```

Keyboard focus uses a visible outline in addition to Material's state colors. Outlined control
boundaries use contrast-preserving theme roles on page and card surfaces. Components keep the
same size when focus moves.

Decorative animations follow live Android and iOS reduced-motion settings. Desktop hosts can
provide their own setting with `CompositionLocalProvider(LocalReduceMotionEnabled provides true)`;
`null` follows the platform preference. Read the effective value with `rememberReduceMotionEnabled()`
to make your own animations follow it. The sample's Appearance controls include an override.

## Security notes

- `ProviderConfigurationState.toString()` redacts `apiKey`, so the state can be logged safely.
  Persisting the key securely (Keychain / Keystore) is the app's job.
- `SecretTextField` does not restore its revealed state after recreation and hides the value again
  when it is cleared.
- `CheckStatus.Failure` messages are displayed and announced; do not put secrets or raw server
  responses containing credentials in them.

## Development

```bash
./gradlew :components:jvmTest             # fastest loop: common + desktop UI tests
./gradlew :components:allTests            # unit tests on every target; UI tests on desktop, iOS, and web
./gradlew ktlintCheck                     # style from .editorconfig; ktlintFormat fixes most issues
./gradlew :components:lint :components-image:lint  # Android and JVM lint for the libraries
./gradlew :components:verifyRoborazziJvm  # desktop tests plus screenshot comparison
./gradlew :components:recordRoborazziJvm  # re-record goldens after an intended visual change
./gradlew :components:checkKotlinAbi      # public API against components/api (updateKotlinAbi accepts changes)
./gradlew :components:koverHtmlReport     # coverage report in components/build/reports/kover
./gradlew :components:koverVerify         # fails below the coverage floor in components/build.gradle.kts
./gradlew :components:assemble :sample:compileKotlinJvm :sample-android:assembleDebug
```

Contribution steps are in [CONTRIBUTING.md](CONTRIBUTING.md); report vulnerabilities as described in
[SECURITY.md](SECURITY.md). iOS tests need macOS with Xcode, and web tests need Chrome. The build needs access to Google Maven
for the Android plugin.

To build against a local checkout of `kommon` next to this repo, pass `-PuseLocalKommon=true`;
Gradle substitutes `io.github.maniramezan.kommon:design-system` with the included build. Add
`-PkommonDir=<path>` when that checkout lives somewhere else.

Releases are automated by Release Please from Conventional Commit messages (`feat:`, `fix:`,
`feat!:`). Contributor and AI-agent conventions live in [`AGENTS.md`](AGENTS.md).

The shared-component [accessibility audit](docs/accessibility-audit.md) records verified behavior,
regression coverage, and the platform checks still needed in consuming apps.
