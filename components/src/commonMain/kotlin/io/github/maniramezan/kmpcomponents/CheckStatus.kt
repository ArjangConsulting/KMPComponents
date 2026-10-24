package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics

/**
 * The state of an on-demand check such as "Test connection": not run yet, running, or finished
 * with a message. Messages come from the app, so they can be specific ("Connected to llama3").
 */
@Immutable
public sealed interface CheckStatus {
    /** The check has not run, or its result was discarded. */
    public data object Idle : CheckStatus

    /** The check is running. Disable the control that starts it (see [isChecking]). */
    public data object Checking : CheckStatus

    /** The check passed. */
    public data class Success(public val message: String) : CheckStatus

    /** The check failed. Do not put secrets in [message]; it is shown and announced. */
    public data class Failure(public val message: String) : CheckStatus
}

/** True while the check is running, e.g. `enabled = !status.isChecking` on its button. */
public val CheckStatus.isChecking: Boolean get() = this == CheckStatus.Checking

/** Localizable strings for [CheckStatusRow]. Set [idle] to an empty string to show nothing. */
@Immutable
public data class CheckStatusLabels(public val idle: String = "Not tested", public val checking: String = "Checking…")

/** The text [CheckStatusRow] shows for this status. */
public fun CheckStatus.message(labels: CheckStatusLabels = CheckStatusLabels()): String = when (this) {
    CheckStatus.Idle -> labels.idle
    CheckStatus.Checking -> labels.checking
    is CheckStatus.Success -> message
    is CheckStatus.Failure -> message
}

/**
 * One line describing a [CheckStatus]: muted text when idle, a small spinner while checking, and the
 * result in the success or error color when readable on page and card surfaces, otherwise the
 * on-surface color. Results are announced through a polite live region.
 */
@Composable
public fun CheckStatusRow(
    status: CheckStatus,
    modifier: Modifier = Modifier,
    labels: CheckStatusLabels = CheckStatusLabels(),
) {
    val text = status.message(labels)
    val accent: Color = when (status) {
        CheckStatus.Idle, CheckStatus.Checking -> MaterialTheme.colorScheme.onSurfaceVariant
        is CheckStatus.Success -> MaterialTheme.kmpColors.success
        is CheckStatus.Failure -> MaterialTheme.colorScheme.error
    }
    val color = readableAccent(
        accent,
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.onSurface,
    )
    val announce = status is CheckStatus.Success || status is CheckStatus.Failure
    Row(
        modifier = modifier.semantics(mergeDescendants = true) {
            if (announce) liveRegion = LiveRegionMode.Polite
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
    ) {
        if (status.isChecking) {
            InlineSpinner(color = MaterialTheme.colorScheme.primary)
        }
        if (text.isNotEmpty()) {
            Text(text, style = MaterialTheme.typography.bodyMedium, color = color)
        }
    }
}
