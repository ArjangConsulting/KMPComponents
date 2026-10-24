# Using the components

Use [catalog.md](catalog.md) to discover names and source files. The examples here describe this
checkout's APIs; confirm compatibility with the application's resolved library version first.

## Dependencies and targets

Add `io.github.maniramezan.kmpcomponents:components` at the app's chosen version using its existing
Gradle/version-catalog conventions. The core targets Android (minSdk 26), JVM, iOS arm64/simulator
arm64, and browser Wasm. It exposes kommon design-system, Compose runtime, and Material 3 as API
dependencies; do not add a second design-token system to make the examples work.

`io.github.maniramezan.kmpcomponents:components-image` is optional and shares the core version.
It targets Android, JVM, and iOS, not Wasm. `CachedAsyncImage` accepts a nullable description:
use `null` for decorative images and meaningful text otherwise. Android network images require
the host app's INTERNET permission. Coil types are implementation details, not its public API.

The checked-in manifest tracks releases, while an app resolves its own dependency version.
Use that resolved version; the repository's `0.0.0-SNAPSHOT` build default is not a Maven version
to recommend to consumers. Building or reading this checkout does not establish release availability.

## Component decisions

| Need | Choose | Important distinction |
| --- | --- | --- |
| Titled group, generic card, selectable option | `SectionCard`, `ThemeCard`, `SelectableCard` | Selection is app-owned; a generic clickable card does not provide selected semantics. |
| Form field, secret, filtering query | `FormTextField`, `SecretTextField`, `SearchField` | Search has search/clear behavior; secrets mask their value and disable keyboard autocorrect. |
| Settings toggle, multiple choices, single choice | `ThemeSwitch`, `ThemeCheckbox`, `RadioGroup` | Match the control's semantics to the decision. |
| Short one-of-many choice | `SegmentedControl` or `ThemeSingleChoiceSegmentedButtonRow` | Filled independent pills versus connected outlined segments; both take option labels and an index. |
| Segments that switch a content pane | `SegmentedContent` | Uses `SegmentedItem` models; inspect the controlled and initial-selection overloads. |
| Searchable grouped choices | `SelectionListContent` or `SelectionList` | Inline bounded content versus a modal sheet; the caller owns selected IDs and dismissal. |
| Clickable/selectable row, display-only row | `ContentRow` or `ThemeListItem` | Keep secondary actions independently focusable. |
| Section navigation, compact tabs, window-adaptive navigation | `SidebarNavigation`, `TabChipRow`, `AdaptiveNavScaffold` | These compose destinations; the app still owns routing and back-stack behavior. |
| Confirmation, custom dialog, sheet | `ConfirmDialog`, `ThemeDialog`, `BottomSheet` | The app owns visibility and confirmed work. |
| Inline status, notice with actions, transient feedback | `StatusMessage`, `NoticeCard`, `ToastHost`/`ThemeSnackbar` | Toasts use `ToastHostState`; standalone snackbars do not manage a queue. |
| Initial loading, pagination, blocked operation | `LoadingState`/`Skeleton`, `LoadMoreFooter`, `LoadingOverlay` | Do not use an interaction-blocking overlay for ordinary background work. |
| Resizable panes, width cap, wrapping controls | `HorizontalSplitPane`/`VerticalSplitPane`, `AdaptiveContentContainer`, `FlowLayout` | Pane fractions are controlled; width caps do not supply app navigation. |
| Conversation rendering | `ChatLog` | The input composer, message IDs, sending, retries, and backend integration belong to the app. |
| Informative image, image inspection | `CachedAsyncImage`, `ZoomableImage` | Loading/caching and zoom interaction are separate responsibilities. |

## A stateless panel

This is a complete common-source example. The caller owns the values and save operation and places
the panel under its app-level `KmpTheme`. Strings are parameters so the app can localize them.

```kotlin
import androidx.compose.runtime.Composable
import io.github.maniramezan.kmpcomponents.FormTextField
import io.github.maniramezan.kmpcomponents.PrimaryButton
import io.github.maniramezan.kmpcomponents.SectionCard
import io.github.maniramezan.kmpcomponents.ThemeSwitch

@Composable
fun PreferencesPanel(
    title: String,
    name: String,
    onNameChange: (String) -> Unit,
    nameLabel: String,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    enabledLabel: String,
    saveLabel: String,
    onSave: () -> Unit,
) {
    SectionCard(title = title) {
        FormTextField(value = name, onValueChange = onNameChange, label = nameLabel)
        ThemeSwitch(checked = enabled, onCheckedChange = onEnabledChange, label = enabledLabel)
        PrimaryButton(text = saveLabel, onClick = onSave)
    }
}
```

## Theme and layout integration

`KmpTheme(mode = ..., tokens = ...)` adapts kommon tokens into Material roles and the `kmpSpacing`,
`kmpColors`, `kmpTypography`, and `kmpMotion` accessors. Import the accessors from the core package.
Use `rememberVectorPainter` for an `ImageVector` passed to a `Painter` parameter.

Avoid fixed widths for controls. Use spacing tokens between fields and neighboring actions,
including app-owned chat composers. Prefer `SegmentWidthMode.FIT` for naturally sized pill segments
that should scroll rather than split the available width. Reserve bounded space for `ChatLog` and
`SelectionListContent`, such as an appropriately weighted pane in a bounded `Column`.

A selection list with a confirm button reports choices without dismissing itself. In the
single-choice sheet overload, omitting the confirm button selects and dismisses on tap.
`SelectionListContent` expands matching search parents and reveals manually expanded groups;
the application still updates its selected-ID set.

## Accessibility and sensitive state

Components supply built-in semantics, selection cues, focus outlines, and minimum targets. Custom
slot content still needs meaningful text/descriptions and an appropriate focus order. Keep matching
foreground/background roles, and check customized themes on the actual surrounding surface.

`ProviderConfigurationState.toString()` redacts its key, but persistence and secure credential storage
remain app responsibilities. `SecretTextField`'s reveal state is ephemeral and is reset when cleared.
Do not display backend error payloads containing secrets in status messages or retry bubbles.

Decorative animations observe reduced-motion settings. A host can override them through
`LocalReduceMotionEnabled`; `rememberReduceMotionEnabled()` exposes the effective setting to app UI.
An automated desktop check is not evidence of VoiceOver, TalkBack, or browser screen-reader behavior.

## Finding exact APIs

With a source checkout, search the declaration file linked from the catalog and its family example
in `sample/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/sample/`. Public ABI dumps live
in `components/api/` and `components-image/api/`. The hosted API reference is at
https://arjangconsulting.github.io/KMPComponents/ and normally describes `main`, not every release.
For a published dependency, prefer matching source artifacts or a matching repository tag.

The root `AGENTS.md` and `.claude/skills/add-component` / `review-component` govern contributing to
KMPComponents itself. Do not copy those maintenance rules into an application just to consume it.
