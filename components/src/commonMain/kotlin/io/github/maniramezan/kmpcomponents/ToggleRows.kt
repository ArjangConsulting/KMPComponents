package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.material3.Checkbox as MaterialCheckbox
import androidx.compose.material3.Switch as MaterialSwitch

// Labeled toggle rows. The whole row is the toggle target, so the label is clickable and screen
// readers read label, tooltip, and state as one control.

/**
 * A setting row with [label] (and optional [supportingText]) on the leading edge and a switch on
 * the trailing edge. Tapping anywhere on the row toggles [checked].
 *
 * @param tooltip optional explanation shown by an [InfoTooltip] after the label.
 */
@Composable
public fun ThemeSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    supportingText: String? = null,
    tooltip: String? = null,
) {
    Row(
        modifier = modifier
            .heightIn(min = minimumTouchTargetSize())
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ToggleLabel(label, supportingText, tooltip, enabled, Modifier.weight(1f))
        MaterialSwitch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}

/**
 * A checkbox followed by [label] (and optional [supportingText]). Tapping anywhere on the row
 * toggles [checked].
 *
 * @param tooltip optional explanation shown by an [InfoTooltip] after the label.
 */
@Composable
public fun ThemeCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    supportingText: String? = null,
    tooltip: String? = null,
) {
    CheckboxRow(
        modifier = modifier.toggleable(
            value = checked,
            enabled = enabled,
            role = Role.Checkbox,
            onValueChange = onCheckedChange,
        ),
    ) {
        MaterialCheckbox(checked = checked, onCheckedChange = null, enabled = enabled)
        ToggleLabel(label, supportingText, tooltip, enabled, Modifier.weight(1f, fill = false))
    }
}

/**
 * A three-state checkbox row for "select all" over a group, where [state] is
 * [ToggleableState.Indeterminate] when only some items are selected. Tapping the row calls
 * [onClick]; the caller decides the next state (usually all on, or all off when everything is on).
 *
 * @param tooltip optional explanation shown by an [InfoTooltip] after the label.
 */
@Composable
public fun ThemeTriStateCheckbox(
    state: ToggleableState,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    supportingText: String? = null,
    tooltip: String? = null,
) {
    CheckboxRow(
        modifier = modifier.triStateToggleable(
            state = state,
            enabled = enabled,
            role = Role.Checkbox,
            onClick = onClick,
        ),
    ) {
        TriStateCheckbox(state = state, onClick = null, enabled = enabled)
        ToggleLabel(label, supportingText, tooltip, enabled, Modifier.weight(1f, fill = false))
    }
}

@Composable
private fun CheckboxRow(modifier: Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = modifier.heightIn(min = minimumTouchTargetSize()),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun ToggleLabel(
    label: String,
    supportingText: String?,
    tooltip: String?,
    enabled: Boolean,
    modifier: Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.extraSmall),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) colors.onSurface else colors.onSurface.copy(alpha = DISABLED_ALPHA),
            )
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) colors.onSurfaceVariant else colors.onSurface.copy(alpha = DISABLED_ALPHA),
                )
            }
        }
        if (tooltip != null) InfoTooltip(tooltip)
    }
}

// Material 3's disabled content opacity.
private const val DISABLED_ALPHA = 0.38f
