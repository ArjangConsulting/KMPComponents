package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.material3.Card as MaterialCard
import androidx.compose.material3.Snackbar as MaterialSnackbar
import androidx.compose.material3.Surface as MaterialSurface
import androidx.compose.material3.TextButton as MaterialTextButton

/**
 * An elevated, full-width card that stacks [content] with the standard gap. With [onClick] the
 * whole card is one click target at least the Material minimum height; buttons inside [content]
 * stay separately focusable. For a selectable card use [SelectableCard]; for a titled group use
 * [SectionCard].
 */
@Composable
public fun ThemeCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(MaterialTheme.kmpSpacing.medium),
    content: @Composable ColumnScope.() -> Unit,
) {
    val column: @Composable ColumnScope.() -> Unit = {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
            content = content,
        )
    }
    if (onClick != null) {
        MaterialCard(
            onClick = onClick,
            modifier = modifier.fillMaxWidth().heightIn(min = minimumTouchTargetSize()),
            content = column,
        )
    } else {
        MaterialCard(modifier = modifier.fillMaxWidth(), content = column)
    }
}

/** A flat, full-width surface-colored container that stacks [content] with the standard gap. */
@Composable
public fun ThemeSurface(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(MaterialTheme.kmpSpacing.medium),
    content: @Composable ColumnScope.() -> Unit,
) {
    MaterialSurface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
            content = content,
        )
    }
}

/**
 * A modal bottom sheet that always opens fully expanded and pads and stacks [content]. Show it
 * conditionally and hide it in [onDismissRequest] (scrim tap, drag down, back).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun BottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.kmpSpacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
            content = content,
        )
    }
}

/**
 * An alert dialog with a confirm action and an optional dismiss action. Pass `null` [dismissText]
 * for an acknowledge-only dialog. Dismissing it (outside tap, back, Esc) calls [onDismissRequest].
 * For the common two-button case, [ConfirmDialog] has English defaults.
 *
 * @param destructive uses the error color for the confirm button.
 */
@Composable
public fun ThemeDialog(
    title: String,
    text: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String? = null,
    destructive: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = { Text(title, modifier = Modifier.semantics { heading() }) },
        text = { Text(text) },
        confirmButton = { ThemeTextButton(text = confirmText, onClick = onConfirm, destructive = destructive) },
        dismissButton = dismissText?.let { label -> { ThemeTextButton(text = label, onClick = onDismissRequest) } },
    )
}

/**
 * A snackbar message with an optional action, usable on its own or as a `SnackbarHost`'s
 * `snackbar` slot. Standalone it announces itself politely. The action shows when both
 * [actionLabel] and [onAction] are set.
 */
@Composable
public fun ThemeSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    MaterialSnackbar(
        modifier = modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        action = if (actionLabel != null && onAction != null) {
            {
                MaterialTextButton(onClick = onAction) {
                    Text(actionLabel, color = MaterialTheme.colorScheme.inversePrimary)
                }
            }
        } else {
            null
        },
    ) {
        Text(message)
    }
}
