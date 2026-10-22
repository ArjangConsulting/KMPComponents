package io.github.maniramezan.kmpcomponents

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

/**
 * A labeled dropdown for choosing a [ThemeMode], for settings forms where a segmented control does
 * not fit. The menu's open state is internal UI state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun ThemeModeDropdown(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    labels: ThemeModeLabels = ThemeModeLabels(),
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = labels.labelFor(selected),
            onValueChange = {},
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled),
            enabled = enabled,
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ThemeMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(labels.labelFor(mode)) },
                    onClick = {
                        expanded = false
                        onSelect(mode)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}
