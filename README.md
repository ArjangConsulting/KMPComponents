# KMPComponents

Reusable Compose Multiplatform components for Android, desktop (JVM), iOS, and web (Wasm). The library adapts
the platform-neutral tokens from [`kommon`](https://github.com/maniramezan/kommon)'s
`design-system` module; it does not define a competing design system.

## Install

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.maniramezan.kmpcomponents:components:<version>")
        }
    }
}
```

`kommon`'s `design-system`, Compose `runtime`, and `material3` are exposed as `api` dependencies,
so token types and Material components are available without extra declarations.

Targets: `android` (minSdk 26), `jvm`, `iosArm64`, `iosSimulatorArm64`, `wasmJs` (browser).

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

API reference: run `./gradlew :components:dokkaGenerate` (output in `components/build/dokka/html`),
or browse the copy that CI publishes from `main` to
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
| `SearchField` | Single-line search field with a search glyph, a clear button, and a keyboard Search action. |
| `ThemeSwitch` | Setting row with a label, optional supporting text and tooltip, and a trailing switch. The whole row toggles. |
| `ThemeCheckbox` / `ThemeTriStateCheckbox` | Checkbox rows with a label; the tri-state one is for "select all" over a group. |

### Feedback

| Component | Purpose |
| --- | --- |
| `StatusMessage` | Inline info / success / warning / error message. Warnings and errors are announced by screen readers. Sizes to its content. |
| `TonalBadge` | Short label on a tinted background (HTTP method, status code, severity), by color or `StatusTone`. |
| `CountBadge` | Pill with a count, capped as `99+`, with an optional spoken description. |
| `EmptyState` | Centered title, message, and optional action for an empty pane. |
| `ConfirmDialog` | Confirm / cancel dialog, optionally with a destructive (error-colored) confirm button. |
| `InfoTooltip` | Info glyph showing an explanation on hover, long-press, or focus; screen readers read it directly. |
| `NoticeCard` | Full-width `StatusTone` notice with an optional action ("Retry") and dismiss button. |
| `ProgressIndicator` | Centered spinner or determinate bar with an announced label, for a loading pane. |
| `InlineProgressIndicator` | Text-sized spinner and label for work in progress inside a row or toolbar. |

### Navigation and layout

| Component | Purpose |
| --- | --- |
| `SidebarNavigation` / `SidebarItem` | Desktop-style side navigation with a header and selectable destinations. |
| `TabChipRow` / `TabChip` | Scrolling row of compact tabs with optional close controls. |
| `SelectableCard` | Card in a pick-one list; the selected card is tinted and reported as selected. |
| `HorizontalSplitPane` | Two panes with a divider you can drag, move with arrow keys, or adjust with a screen reader. |
| `SectionHeader` | Title (an accessibility heading), optional subtitle, and trailing actions above a list or pane. |
| `ContentRow` | List row with title, secondary and supporting text, leading/trailing slots, and optional click and selection. |
| `KeyValueRow` | Read-only label/value pair for details panels, read as one item. |

### Names shared with the sibling libraries

Components that also exist in [ComposeUIComponents](https://github.com/maniramezan/ComposeUIComponents)
or [SwiftUIComponents](https://github.com/maniramezan/SwiftUIComponents) use the same name here (`SectionHeader`, `ContentRow`, `SearchField`,
`ProgressIndicator`, `NoticeCard`, `MenuPicker`, `EmptyState`). Where that name would collide
with a Material 3 composable, the component gets a `Theme` prefix instead (`ThemeSwitch`,
`ThemeCheckbox`), like SwiftUIComponents' `ThemeButton`. Some ComposeUIComponents names map to an
existing component here:

| ComposeUIComponents | KMPComponents |
| --- | --- |
| `Switch` | `ThemeSwitch` |
| `Checkbox` | `ThemeCheckbox` |
| `TextField` | `FormTextField` |
| `PasswordField` | `SecretTextField` |
| `Section` | `SectionCard` |

All components are stateless unless noted, take a `Modifier` as their first optional parameter,
and accept user-visible strings as parameters (or a `*Labels` data class) so apps can localize
them. Icons are drawn by the library, so there is no icon-font dependency.

### Sample app

The same catalog runs on every platform. It also switches between the default and a rebranded
token set and between 100%, 130%, and 200% font scale.

```bash
./gradlew :sample:run                          # desktop
./gradlew :sample-android:installDebug         # Android device or emulator
xcodegen generate --spec sample/ios/project.yml && open sample/ios/KmpComponentsSample.xcodeproj
```

The iOS project builds the Kotlin framework in a pre-build phase; pick an Apple Silicon simulator
and run. It needs [XcodeGen](https://github.com/yonaskolb/XcodeGen) (`brew install xcodegen`).

In IntelliJ IDEA or Android Studio, the shared run configurations in `.run/` cover the same entry
points: **Run** > *Sample catalog (desktop)*, and **Test** > *Tests (JVM)*, *Tests (all targets)*,
and *Check (CI)*.

## Theming

`KmpTheme(mode, tokens)` converts kommon tokens once per token change (results are `remember`ed)
and provides:

| Access | Source token |
| --- | --- |
| `MaterialTheme.colorScheme` | `ColorSchemeTokens`. The nine kommon roles map directly; every other Material role (containers, secondary, tertiary, surface containers, inverse, outline variant) is derived by blending kommon roles so the Material baseline palette never appears. |
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
./gradlew :components:allTests            # unit tests on every target (UI tests run on jvm)
./gradlew ktlintCheck                     # style from .editorconfig; ktlintFormat fixes most issues
./gradlew :components:verifyRoborazziJvm  # desktop tests plus screenshot comparison
./gradlew :components:recordRoborazziJvm  # re-record goldens after an intended visual change
./gradlew :components:checkKotlinAbi      # public API against components/api (updateKotlinAbi accepts changes)
./gradlew :components:koverHtmlReport     # coverage report in components/build/reports/kover
./gradlew :components:assemble :sample:compileKotlinJvm :sample-android:assembleDebug
```

iOS tests need macOS with Xcode, and web tests need Chrome. The build needs access to Google Maven
for the Android plugin.

To build against a local checkout of `kommon` next to this repo, pass `-PuseLocalKommon=true`;
Gradle substitutes `io.github.maniramezan.kommon:design-system` with the included build. Add
`-PkommonDir=<path>` when that checkout lives somewhere else.

Releases are automated by Release Please from Conventional Commit messages (`feat:`, `fix:`,
`feat!:`). Contributor and AI-agent conventions live in [`AGENTS.md`](AGENTS.md).
