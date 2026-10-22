package io.github.maniramezan.kmpcomponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

/** Localizable labels for [SelectionList] and [SelectionListContent]. */
@Immutable
public data class SelectionListLabels(
    /** Placeholder of the search field. */
    public val searchPlaceholder: String = "Search",
    /** Accessible name of the search field's clear button. */
    public val clearSearch: String = "Clear search",
    /** Shown when a search matches nothing. */
    public val noResults: String = "No results",
    /** State announced for an expanded parent row. */
    public val expanded: String = "Expanded",
    /** State announced for a collapsed parent row. */
    public val collapsed: String = "Collapsed",
)

/**
 * A single-choice [SelectionListContent] in a modal bottom sheet.
 *
 * Without [confirmButton], tapping an item calls [onSelect] and then [onDismissRequest]. With
 * [confirmButton], taps only call [onSelect] and the sheet stays open until the confirm control
 * (which the caller wires to [onDismissRequest]) is used.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun <ID : Any> SelectionList(
    title: String,
    nodes: List<SelectionListNode<ID>>,
    selectedId: ID?,
    onSelect: (ID) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    isSearchable: Boolean = false,
    labels: SelectionListLabels = SelectionListLabels(),
    confirmButton: (@Composable () -> Unit)? = null,
) {
    SelectionList(
        title = title,
        nodes = nodes,
        selectedIds = selectedId?.let { setOf(it) } ?: emptySet(),
        onSelect = if (confirmButton == null) {
            { id ->
                onSelect(id)
                onDismissRequest()
            }
        } else {
            onSelect
        },
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        isSearchable = isSearchable,
        labels = labels,
        confirmButton = confirmButton,
    )
}

/**
 * A multiple-choice [SelectionListContent] in a modal bottom sheet. Taps only call [onSelect];
 * toggle membership in [selectedIds] yourself and give the sheet a [confirmButton] to close it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun <ID : Any> SelectionList(
    title: String,
    nodes: List<SelectionListNode<ID>>,
    selectedIds: Set<ID>,
    onSelect: (ID) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    isSearchable: Boolean = false,
    labels: SelectionListLabels = SelectionListLabels(),
    confirmButton: (@Composable () -> Unit)? = null,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        SelectionListContent(
            title = title,
            nodes = nodes,
            selectedIds = selectedIds,
            onSelect = onSelect,
            modifier = Modifier.weight(1f, fill = false),
            isSearchable = isSearchable,
            labels = labels,
            confirmButton = confirmButton,
        )
    }
}

/**
 * A searchable one- or two-level selection list to place in your own screen or pane; [SelectionList]
 * shows the same list in a bottom sheet.
 *
 * Leaves are selectable and show a check when their id is in [selectedIds]. Parents expand inline;
 * a collapsed parent lists its selected children as its subtitle, and parents containing a
 * selection start expanded. While searching, every matching parent is expanded. The search query
 * and expansion are internal UI state. A blank [title] without [confirmButton] draws no header.
 */
@Composable
public fun <ID : Any> SelectionListContent(
    title: String,
    nodes: List<SelectionListNode<ID>>,
    selectedIds: Set<ID>,
    onSelect: (ID) -> Unit,
    modifier: Modifier = Modifier,
    isSearchable: Boolean = false,
    labels: SelectionListLabels = SelectionListLabels(),
    confirmButton: (@Composable () -> Unit)? = null,
) {
    var query by remember { mutableStateOf("") }
    var expandedIds by remember { mutableStateOf(parentsWithSelection(nodes, selectedIds)) }
    val isSearching = isSearchable && query.isNotBlank()
    val visibleNodes = remember(nodes, query, isSearchable) {
        filterSelectionListNodes(nodes, if (isSearchable) query else "")
    }
    val spacing = MaterialTheme.kmpSpacing

    Column(modifier = modifier.fillMaxWidth()) {
        if (confirmButton != null || title.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.medium, vertical = spacing.small),
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f).semantics { heading() },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                confirmButton?.invoke()
            }
        }
        if (isSearchable) {
            SearchField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.medium, vertical = spacing.small),
                labels = SearchFieldLabels(placeholder = labels.searchPlaceholder, clear = labels.clearSearch),
            )
        }
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
            if (visibleNodes.isEmpty() && isSearching) {
                item {
                    Text(
                        text = labels.noResults,
                        modifier = Modifier.fillMaxWidth().padding(spacing.large),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(visibleNodes, key = { it.id }) { node ->
                if (node.isLeaf) {
                    SelectionListRow(
                        node = node,
                        subtitle = node.subtitle,
                        isSelected = node.id in selectedIds,
                        isIndented = false,
                        expanded = null,
                        labels = labels,
                        onClick = { onSelect(node.id) },
                    )
                } else {
                    val expanded = isSearching || node.id in expandedIds
                    val summary = remember(node, selectedIds) { selectedChildrenSummary(node, selectedIds) }
                    SelectionListRow(
                        node = node,
                        subtitle = if (expanded) node.subtitle else summary ?: node.subtitle,
                        isSelected = false,
                        isIndented = false,
                        expanded = expanded,
                        labels = labels,
                        onClick = {
                            expandedIds = if (node.id in expandedIds) expandedIds - node.id else expandedIds + node.id
                        },
                    )
                    AnimatedVisibility(
                        visible = expanded,
                        enter = expandVertically(tween(MaterialTheme.kmpMotion.standardMillis)),
                        exit = shrinkVertically(tween(MaterialTheme.kmpMotion.standardMillis)),
                    ) {
                        Column {
                            node.children.forEach { child ->
                                SelectionListRow(
                                    node = child,
                                    subtitle = child.subtitle,
                                    isSelected = child.id in selectedIds,
                                    isIndented = true,
                                    expanded = null,
                                    labels = labels,
                                    onClick = { onSelect(child.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * One row of a [SelectionListContent]. A non-null [expanded] makes it an expandable parent with a
 * chevron and an expanded/collapsed state; otherwise it is a selectable leaf with a check mark.
 */
@Composable
private fun SelectionListRow(
    node: SelectionListNode<*>,
    subtitle: String?,
    isSelected: Boolean,
    isIndented: Boolean,
    expanded: Boolean?,
    labels: SelectionListLabels,
    onClick: () -> Unit,
) {
    val spacing = MaterialTheme.kmpSpacing
    val colors = MaterialTheme.colorScheme
    val rotation by animateFloatAsState(
        targetValue = if (expanded == true) HALF_TURN else 0f,
        animationSpec = tween(MaterialTheme.kmpMotion.standardMillis),
        label = "chevron",
    )
    val interaction = if (expanded == null) {
        Modifier.selectable(selected = isSelected, role = Role.Button, onClick = onClick)
    } else {
        Modifier.clickable(role = Role.Button, onClick = onClick)
            .semantics { stateDescription = if (expanded) labels.expanded else labels.collapsed }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minimumTouchTargetSize())
            .then(interaction)
            .padding(
                start = if (isIndented) spacing.extraLarge else spacing.medium,
                end = spacing.medium,
                top = spacing.small,
                bottom = spacing.small,
            ),
        horizontalArrangement = Arrangement.spacedBy(spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        node.leadingGlyph?.let { Text(it, style = MaterialTheme.typography.titleLarge) }
        Column(Modifier.weight(1f)) {
            Text(
                text = node.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else null,
                ),
                color = if (isSelected) colors.primary else colors.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        when {
            expanded != null -> GlyphIcon(
                glyph = Glyph.CHEVRON_DOWN,
                // Rotate in the draw layer so the animation does not recompose the row.
                modifier = Modifier.graphicsLayer { rotationZ = rotation },
                color = colors.onSurfaceVariant,
            )

            isSelected -> GlyphIcon(glyph = Glyph.CHECK, color = colors.primary)
        }
    }
}

private const val HALF_TURN = 180f
