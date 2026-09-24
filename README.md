# KMPComponents

Reusable Compose Multiplatform components for Android, desktop (JVM), and iOS. The library adapts
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

Targets: `android` (minSdk 26), `jvm`, `iosArm64`, `iosSimulatorArm64`.

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

| Component | Purpose |
| --- | --- |
| `KmpTheme` | Applies kommon `ThemeTokens` as a Material 3 theme for a `ThemeMode`. |
| `ThemeModeSelector` | Segmented System / Light / Dark control. |
| `SectionCard` | Titled group of content with optional description and header action. Title is an accessibility heading. |
| `FormTextField` | Outlined text field with label, placeholder, and supporting/error text. |
| `SecretTextField` | Masked field with a Show/Hide toggle and a password keyboard without autocorrect. |
| `StatusMessage` | Inline info / success / warning / error message. Warnings and errors are announced by screen readers. |
| `ProviderConfigurationForm` | Provider name, base URL, model, and API key fields with validation, built from the fields above. |

All components are stateless unless noted, take a `Modifier` as their first optional parameter,
and accept user-visible strings as parameters (or a `*Labels` data class) so apps can localize
them.

Run the interactive catalog on desktop:

```bash
./gradlew :sample:run
```

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

## Development

```bash
./gradlew :components:allTests            # unit tests on every target (UI tests run on jvm)
./gradlew :components:jvmTest             # fastest loop: common + desktop UI tests
./gradlew :components:assemble :sample:compileKotlinJvm
```

iOS tests need macOS with Xcode. The build needs access to Google Maven for the Android plugin.

To build against a local checkout of `kommon` next to this repo, pass `-PuseLocalKommon=true`;
Gradle substitutes `io.github.maniramezan.kommon:design-system` with the included build.

Releases are automated by Release Please from Conventional Commit messages (`feat:`, `fix:`,
`feat!:`). Contributor and AI-agent conventions live in [`AGENTS.md`](AGENTS.md).
