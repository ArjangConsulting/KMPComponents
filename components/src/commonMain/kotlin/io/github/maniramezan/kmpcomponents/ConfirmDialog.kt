package io.github.maniramezan.kmpcomponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A two-button confirmation dialog ("Delete all variants?", "Allow Amoo to reinstall the app?").
 *
 * With [destructive], the confirm button uses the error color. Dismissing (outside tap, back, Esc)
 * calls [onDismiss], never [onConfirm]. Show it conditionally; it has no visibility parameter.
 * Built on [ThemeDialog].
 */
@Composable
public fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmLabel: String = "Confirm",
    dismissLabel: String = "Cancel",
    destructive: Boolean = false,
) {
    ThemeDialog(
        title = title,
        text = message,
        confirmText = confirmLabel,
        onConfirm = onConfirm,
        onDismissRequest = onDismiss,
        modifier = modifier,
        dismissText = dismissLabel,
        destructive = destructive,
    )
}
