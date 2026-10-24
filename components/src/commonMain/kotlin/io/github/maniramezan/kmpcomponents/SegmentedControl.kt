package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.material3.SingleChoiceSegmentedButtonRow as MaterialSingleChoiceSegmentedButtonRow

/** Vertical density of a [SegmentedControl]. */
public enum class SegmentDensity {
    /** Standard padding and at least the Material minimum touch target height. */
    REGULAR,

    /**
     * Reduced padding without the minimum touch target height, so the control is shorter than other
     * controls. Use it only for dense secondary controls with generous surrounding space.
     */
    COMPACT,
}

/** How a [SegmentedControl]'s segments share the available width. */
public enum class SegmentWidthMode {
    /** Segments split the full width evenly. */
    FILL,

    /** Each segment takes its label's width; the row scrolls when the segments overflow. */
    FIT,
}

/**
 * A row of pill-shaped segments for choosing one of [options], drawn as filled buttons: the selected
 * segment uses the primary color and the others the surface variant. Segments report their selected
 * state and form one selectable group for screen readers.
 *
 * Use [ThemeSingleChoiceSegmentedButtonRow] for Material's connected, outlined style instead.
 */
@Composable
public fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    density: SegmentDensity = SegmentDensity.REGULAR,
    widthMode: SegmentWidthMode = SegmentWidthMode.FILL,
) {
    val spacing = MaterialTheme.kmpSpacing
    val colors = MaterialTheme.colorScheme
    val contentPadding = PaddingValues(
        horizontal = spacing.large,
        vertical = if (density == SegmentDensity.COMPACT) spacing.extraSmall else spacing.small,
    )
    val minHeight = if (density == SegmentDensity.COMPACT) null else minimumTouchTargetSize()
    val rowModifier = if (widthMode == SegmentWidthMode.FIT) {
        modifier.horizontalScroll(rememberScrollState())
    } else {
        modifier
    }
    Row(
        modifier = rowModifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall),
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            val sized = if (minHeight != null) Modifier.heightIn(min = minHeight) else Modifier
            val segmentModifier = if (widthMode == SegmentWidthMode.FILL) sized.weight(1f) else sized
            Button(
                onClick = { onOptionSelected(index) },
                modifier = segmentModifier
                    .keyboardFocusOutline(
                        ButtonDefaults.shape,
                        if (selected) colors.onPrimary else colors.onSurfaceVariant,
                    )
                    .semantics { this.selected = selected },
                enabled = enabled,
                border = if (selected) BorderStroke(1.dp, colors.onPrimary) else null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected) colors.primary else colors.surfaceVariant,
                    contentColor = if (selected) colors.onPrimary else colors.onSurfaceVariant,
                ),
                contentPadding = contentPadding,
            ) {
                Text(option)
            }
        }
    }
}

/**
 * Material's connected, outlined segmented buttons for choosing one of [options], with a check
 * mark on the selected segment. Each segment is at least the Material minimum touch target high.
 * Labels retain their intrinsic width; the connected row scrolls horizontally when space is tight.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun ThemeSingleChoiceSegmentedButtonRow(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    key(LocalLayoutDirection.current) {
        MaterialSingleChoiceSegmentedButtonRow(modifier = modifier.horizontalScroll(rememberScrollState())) {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = index == selectedIndex,
                    onClick = { onOptionSelected(index) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    modifier = Modifier
                        .keyboardFocusOutline(SegmentedButtonDefaults.itemShape(index = index, count = options.size))
                        .heightIn(min = minimumTouchTargetSize()),
                    enabled = enabled,
                    icon = { SegmentedButtonDefaults.Icon(active = index == selectedIndex) },
                    label = { Text(option, modifier = Modifier.width(IntrinsicSize.Max)) },
                )
            }
        }
    }
}
