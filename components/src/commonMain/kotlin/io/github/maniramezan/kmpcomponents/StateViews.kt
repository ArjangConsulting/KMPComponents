package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign

/**
 * A full-width, padded loading placeholder for a pane: a spinner and [label], announced politely.
 * The pane-level counterpart of [ProgressIndicator], with [EmptyState] and [ErrorState] as its
 * siblings.
 */
@Composable
public fun LoadingState(label: String, modifier: Modifier = Modifier) {
    ProgressIndicator(
        label = label,
        modifier = modifier.fillMaxWidth().padding(MaterialTheme.kmpSpacing.extraLarge),
    )
}

/**
 * A full-width, padded error placeholder for a pane that failed to load, with an optional
 * [action] such as a Retry button and optional [leadingContent] such as an illustration. The
 * title is an accessibility heading, and the error is announced politely when it appears.
 */
@Composable
public fun ErrorState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(MaterialTheme.kmpSpacing.extraLarge),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        leadingContent?.invoke()
        Column(
            modifier = Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        action?.invoke()
    }
}
