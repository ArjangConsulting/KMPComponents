package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.material3.Slider as MaterialSlider

/**
 * A vertical list of radio buttons for choosing one of [options]. Each row's full width is the tap
 * target, and the rows form one selectable group for screen readers.
 */
@Composable
public fun RadioGroup(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(modifier = modifier.selectableGroup()) {
        options.forEachIndexed { index, option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minimumTouchTargetSize())
                    .selectable(
                        selected = index == selectedIndex,
                        enabled = enabled,
                        role = Role.RadioButton,
                        onClick = { onOptionSelected(index) },
                    ),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = index == selectedIndex, onClick = null, enabled = enabled)
                Text(option, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

/**
 * A full-width slider for one value in [valueRange].
 *
 * A bare slider only announces a percentage. Pass [label] to say what it controls and
 * [valueDescription] to announce a meaningful value ("Timeout, 30 seconds"); both should be
 * localized by the caller.
 *
 * @param steps the number of discrete stops between the range's ends; `0` for a continuous slider.
 */
@Composable
public fun ThemeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
    label: String? = null,
    valueDescription: String? = null,
) {
    MaterialSlider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                label?.let { contentDescription = it }
                valueDescription?.let { stateDescription = it }
            },
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
    )
}
