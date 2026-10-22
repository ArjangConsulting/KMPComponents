package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role

/**
 * A full-height side navigation column for desktop-style layouts (app sections, preference panes).
 * Fill it with [SidebarItem]s; [header] (an app name, a pane title) sits above them.
 *
 * It has no intrinsic width; pass one, e.g. `Modifier.width(220.dp)`. The items form one selection
 * group for accessibility services.
 */
@Composable
public fun SidebarNavigation(
    modifier: Modifier = Modifier,
    header: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(MaterialTheme.kmpSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.extraSmall),
    ) {
        if (header != null) {
            header()
        }
        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.extraSmall),
            content = content,
        )
    }
}

/**
 * One destination in a [SidebarNavigation]. The selected item uses the secondary container color
 * and is exposed as a selected tab.
 */
@Composable
public fun SidebarItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val container = if (selected) colors.secondaryContainer else Color.Transparent
    val content = if (selected) colors.onSecondaryContainer else colors.onSurfaceVariant
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(container)
            .selectable(selected = selected, enabled = enabled, role = Role.Tab, onClick = onClick)
            .padding(horizontal = MaterialTheme.kmpSpacing.medium, vertical = MaterialTheme.kmpSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
    ) {
        CompositionLocalProvider(LocalContentColor provides content) {
            icon?.invoke()
            Text(label, style = MaterialTheme.typography.labelLarge, color = content)
        }
    }
}
