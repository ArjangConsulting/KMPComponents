package io.github.maniramezan.kmpcomponents

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier

/** Localizable labels for [ThemeModeSelector]. */
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun ThemeModeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    labels: ThemeModeLabels = ThemeModeLabels(),
) {
    val modes = ThemeMode.entries
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        modes.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = mode == selected,
                onClick = { onSelect(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                enabled = enabled,
                label = { Text(labels.labelFor(mode)) },
            )
        }
    }
}
