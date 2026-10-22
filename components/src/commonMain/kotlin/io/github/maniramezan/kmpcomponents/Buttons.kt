package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.material3.ExtendedFloatingActionButton as MaterialExtendedFloatingActionButton
import androidx.compose.material3.TextButton as MaterialTextButton

// Themed buttons. Each one fixes the color roles, padding, and minimum height so every app's
// primary, secondary, and tertiary actions look the same. Icons are caller-supplied painters
// (for an ImageVector, use `rememberVectorPainter`), sized from the body text so they follow the
// user's font scale.

/** A filled, high-emphasis button for a screen's or dialog's primary action. */
@Composable
public fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = minimumTouchTargetSize()),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        contentPadding = buttonPadding(),
    ) {
        Text(text)
    }
}

/** An outlined, medium-emphasis button for a secondary action next to a [PrimaryButton]. */
@Composable
public fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = minimumTouchTargetSize()),
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        contentPadding = buttonPadding(),
    ) {
        Text(text)
    }
}

/**
 * A low-emphasis text button for tertiary or dismissive actions ("Cancel", "Learn more").
 *
 * @param destructive uses the error color, for actions such as "Delete all".
 */
@Composable
public fun ThemeTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    destructive: Boolean = false,
) {
    MaterialTextButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = minimumTouchTargetSize()),
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        ),
        contentPadding = PaddingValues(
            horizontal = MaterialTheme.kmpSpacing.medium,
            vertical = MaterialTheme.kmpSpacing.small,
        ),
    ) {
        Text(text)
    }
}

/**
 * An icon-only button tinted with the primary color. [contentDescription] names the action for
 * screen readers and is also shown as a tooltip on hover and focus.
 */
@Composable
public fun ThemeIconButton(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val iconSize = glyphSize()
    TooltipIconButton(
        label = contentDescription,
        onClick = onClick,
        iconSize = iconSize,
        modifier = modifier,
        enabled = enabled,
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * A [ThemeIconButton] that swaps its icon for a spinner and ignores clicks while [loading], for
 * actions such as "Refresh providers". Screen readers hear [loadingLabel] as its state.
 */
@Composable
public fun LoadingIconButton(
    loading: Boolean,
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loadingLabel: String = "Loading",
) {
    val iconSize = glyphSize()
    TooltipIconButton(
        label = contentDescription,
        onClick = { if (!loading) onClick() },
        iconSize = iconSize,
        modifier = modifier,
        enabled = enabled,
        extraSemantics = { if (loading) stateDescription = loadingLabel },
    ) {
        if (loading) {
            InlineSpinner(style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
        } else {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** An icon-only floating action button for a screen's primary action. */
@Composable
public fun FAB(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Icon(painter = icon, contentDescription = contentDescription, modifier = Modifier.size(glyphSize()))
    }
}

/**
 * A floating action button with an icon and a visible [text] label. The icon is decorative because
 * [text] already names the action. [text] is also set as the button's accessible name, because
 * Material's extended FAB hides its label from screen readers.
 */
@Composable
public fun ThemeExtendedFloatingActionButton(
    text: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MaterialExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier.semantics { contentDescription = text },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        icon = { Icon(painter = icon, contentDescription = null, modifier = Modifier.size(glyphSize())) },
        text = { Text(text) },
    )
}

@Composable
@ReadOnlyComposable
private fun buttonPadding(): PaddingValues = PaddingValues(
    horizontal = MaterialTheme.kmpSpacing.large,
    vertical = MaterialTheme.kmpSpacing.small,
)
