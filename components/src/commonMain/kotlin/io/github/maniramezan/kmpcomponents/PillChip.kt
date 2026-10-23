package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight

/**
 * A capsule-shaped chip with a selected and an unselected look, for filter rows and option
 * toggles. With [onClick] it is a selectable chip with a minimum touch target that reports its
 * selected state; with `null` [onClick] it is a static label.
 *
 * Override the colors in matching pairs (`onX` on `X`) to keep the label's contrast.
 */
@Composable
public fun PillChip(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    selectedBackground: Color = MaterialTheme.colorScheme.primary,
    unselectedBackground: Color = MaterialTheme.colorScheme.primaryContainer,
    selectedLabel: Color = MaterialTheme.colorScheme.onPrimary,
    unselectedLabel: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    val interactive = if (onClick != null) {
        Modifier
            .heightIn(min = minimumTouchTargetSize())
            .selectable(selected = isSelected, role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (isSelected) selectedBackground else unselectedBackground)
            .then(interactive)
            .padding(horizontal = MaterialTheme.kmpSpacing.medium, vertical = MaterialTheme.kmpSpacing.small),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            ),
            color = if (isSelected) selectedLabel else unselectedLabel,
        )
    }
}
