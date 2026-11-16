# Module components

Reusable Compose Multiplatform components for Android, desktop (JVM), iOS, and web (Wasm), themed
from kommon's `design-system` tokens.

Wrap your UI in `KmpTheme` once. It applies the tokens as a Material 3 theme and adds the values
Material has no slot for: `MaterialTheme.kmpSpacing`, `kmpColors`, `kmpTypography`, and
`kmpMotion`. Every component reads its spacing, colors, shapes, and type from the theme.

Components are stateless: they take a value and report changes through a callback. User-visible
strings are parameters, or a `*Labels` class for components with several, so apps can localize
them. Names follow the sibling ComposeUIComponents and SwiftUIComponents libraries; a component
whose name would collide with Material 3 gets a `Theme` prefix (`ThemeSwitch`, `ThemeCard`).

The README's component catalog lists every family, and the web catalog shows them live.

# Package io.github.maniramezan.kmpcomponents

The theme (`KmpTheme` and its token adapters) and every component family: forms and settings,
feedback, navigation and layout, inputs, actions, containers, lists, chat and assistant views, and
desktop extras such as split panes and a status bar.
