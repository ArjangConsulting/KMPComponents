# Design style guide

Wrap app content in `KmpTheme` and brand it through kommon `ThemeTokens`. Components use
semantic Material roles and the `MaterialTheme.kmp*` accessors. Keep color mapping in
`TokenAdapters.kt` so a theme correction reaches every Material control and consuming app.

## Color roles

| Purpose | Background | Foreground or boundary |
| --- | --- | --- |
| App content | `surface` | `onSurface` |
| Grouped content, including `SectionCard` | `surfaceVariant` | `onSurfaceVariant` |
| Filled primary action | `primary` | `onPrimary` |
| Selected or tonal content | `secondaryContainer` | `onSecondaryContainer` |
| Destructive filled action | `error` | `onError` |
| Enabled outlined controls | The surrounding surface | `outline` or `outlineVariant` |

`outline` maps directly from kommon. `outlineVariant` blends that token 16% toward `onSurface`
to give antialiased button borders contrast headroom. Material 3 uses `outlineVariant` for
enabled outlined buttons, so fading that role toward a surface makes interactive boundaries
disappear. Treat both roles as control boundaries; do not assume `outlineVariant` is decorative.
Choose decorative separators separately without weakening either shared role.

Use matching `onX` and `X` roles. A darker or lighter theme needs its own token values; changing
opacity at a call site does not establish a readable pair. Sample actions use Material defaults
under `KmpTheme`, without local color or border overrides.

## Contrast requirements

Enabled control boundaries and meaningful graphics need at least 3:1 contrast against adjacent
surfaces. Body text needs at least 4.5:1. Measure against the actual container: a border that works
on `surface` can disappear on a `surfaceVariant` card. Disabled controls may use Material's
reduced emphasis and are excluded from the enabled contrast requirement.

Custom themes must preserve these relationships in both light and dark modes. In particular,
check the kommon `outline` token against `surface`, `surfaceVariant`, and derived Material
surface containers. Adjust the source theme tokens rather than overriding individual buttons.

## Shape, spacing, and focus

Use `MaterialTheme.shapes`, `MaterialTheme.typography`, and `MaterialTheme.kmpSpacing`.
Derive glyph dimensions from typography and preserve Material's minimum interactive size.
Allow translated labels and 200% text to determine control size.

Keyboard focus needs a visible border, thickness, or shape change as well as a color change.
Keep text labels, content descriptions, and tooltips available in every interaction state.

## Verification

`TokenAdaptersTest` checks outline contrast on all standard surfaces and text contrast on the
two main content surfaces. `ControlContrastTest` measures rendered button borders and both
split-pane orientations on page and card surfaces, with normal and focused states, default and alternate themes, and
100% and 200% font scales. `ComponentUiTest` checks long segmented labels, neighboring icon
controls, minimum targets, and label fit at 100% and 200% text. `AccessibilityRegressionTest`
checks visible keyboard focus, including the information tooltip.

The screenshot gallery includes enabled and disabled outlined actions. For visual changes,
record and review the light, dark, and large-text goldens before running screenshot verification.
Add regression coverage at the theme adapter when a shared role changes, and at the rendered
control when Material defaults or drawing behavior caused the failure.
