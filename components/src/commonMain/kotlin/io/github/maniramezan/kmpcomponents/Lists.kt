package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics

/**
 * A Material-style list row: a [headline] in body text, optional [supportingText], and leading and
 * trailing slots, read by screen readers as one item. That item is at least the Material minimum
 * touch target high, so a one-line row still meets target-size audits. For clickable or selectable
 * rows with titles, use [ContentRow].
 */
@Composable
public fun ThemeListItem(
    headline: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.kmpSpacing.medium),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingContent?.invoke()
        Column(
            modifier = Modifier
                .weight(1f)
                .semantics(mergeDescendants = true) {}
                .heightIn(min = minimumTouchTargetSize())
                .padding(vertical = MaterialTheme.kmpSpacing.small),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(headline, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        trailingContent?.invoke()
    }
}

/** A full-width `LazyColumn` with the standard vertical padding. */
@Composable
public fun LazyList(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(vertical = MaterialTheme.kmpSpacing.small),
    content: LazyListScope.() -> Unit,
) {
    LazyColumn(modifier = modifier.fillMaxWidth(), state = state, contentPadding = contentPadding, content = content)
}

/**
 * The last item of an infinite-scrolling lazy list. Each time it enters composition (scrolls into
 * view) it calls [onLoadMore]; while [isLoadingMore] it shows a spinner named [loadingLabel] and
 * announced politely. Guard against duplicate requests in [onLoadMore], and stop emitting the
 * footer when there is nothing left to load.
 */
@Composable
public fun LoadMoreFooter(
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    loadingLabel: String = "Loading more",
) {
    LaunchedEffect(Unit) { onLoadMore() }
    if (isLoadingMore) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(MaterialTheme.kmpSpacing.medium)
                .semantics {
                    contentDescription = loadingLabel
                    liveRegion = LiveRegionMode.Polite
                },
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}
