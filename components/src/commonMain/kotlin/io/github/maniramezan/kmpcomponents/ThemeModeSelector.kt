package io.github.maniramezan.kmpcomponents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier

/** Localizable labels for [ThemeModeSelector] and [ThemeModeDropdown]. */
@Immutable
public data class ThemeModeLabels(
    public val system: String = "System",
    public val light: String = "Light",
    public val dark: String = "Dark",
) {
    public fun labelFor(mode: ThemeMode): String = when (mode) {
        ThemeMode.SYSTEM -> system
        ThemeMode.LIGHT -> light
        ThemeMode.DARK -> dark
    }
}

/** Segmented control for choosing a [ThemeMode]; pair it with [KmpTheme]'s `mode` parameter. */
@Composable
public fun ThemeModeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    labels: ThemeModeLabels = ThemeModeLabels(),
) {
    val modes = ThemeMode.entries
    ThemeSingleChoiceSegmentedButtonRow(
        options = modes.map(labels::labelFor),
        selectedIndex = modes.indexOf(selected),
        onOptionSelected = { onSelect(modes[it]) },
        modifier = modifier,
        enabled = enabled,
    )
}

/**
 * A labeled dropdown for choosing a [ThemeMode], for settings forms where a segmented control does
 * not fit. Built on [MenuPicker]; the menu's open state is internal UI state.
 */
@Composable
public fun ThemeModeDropdown(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    labels: ThemeModeLabels = ThemeModeLabels(),
) {
    MenuPicker(
        items = ThemeMode.entries,
        selected = selected,
        onSelect = onSelect,
        label = label,
        itemLabel = labels::labelFor,
        modifier = modifier,
        enabled = enabled,
    )
}
