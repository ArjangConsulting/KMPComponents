package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * A desktop-style status bar along the bottom of a window: a top rule, [start] items on the start
 * edge, and [end] items on the end edge, in label-sized text. Separate items with
 * [StatusBarSeparator]. When space is short, the end side keeps up to two thirds of the width.
 */
@Composable
public fun StatusBar(
    modifier: Modifier = Modifier,
    start: @Composable RowScope.() -> Unit = {},
    end: @Composable RowScope.() -> Unit = {},
) {
    Column(modifier.fillMaxWidth()) {
        ThemeHorizontalDivider()
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.labelMedium,
            LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = MaterialTheme.kmpSpacing.medium,
                        vertical = MaterialTheme.kmpSpacing.extraSmall,
                    ),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                    content = start,
                )
                // Both sides can shrink, so neither pushes the other off screen at large font sizes.
                Row(
                    modifier = Modifier.weight(END_WEIGHT, fill = false),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    content = end,
                )
            }
        }
    }
}

/**
 * One status bar item on a single line. When space runs out, items share it and end in an ellipsis
 * rather than dropping off the bar. [color] defaults to the bar's muted text color; pass a tone
 * for states.
 */
@Composable
public fun RowScope.StatusBarItem(text: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    Text(
        text = text,
        modifier = modifier.weight(1f, fill = false),
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/** A short vertical rule between [StatusBar] items, as tall as the label text. */
@Composable
public fun StatusBarSeparator(modifier: Modifier = Modifier) {
    Box(
        modifier
            .width(1.dp)
            .height(glyphSize(MaterialTheme.typography.labelMedium))
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

private const val END_WEIGHT = 2f
