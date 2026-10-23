package io.github.maniramezan.kmpcomponents

import androidx.compose.runtime.Immutable

/**
 * A node in a one- or two-level [SelectionList] tree.
 *
 * A node without [children] is a leaf that is selected when tapped. A node with children is an
 * expandable parent: tapping it shows its children inline, and selecting one reports that child's
 * [id]. Identifiers must be unique across the whole tree; they are list keys and the values the
 * list reports.
 *
 * @property title primary row text.
 * @property subtitle optional secondary text under the title.
 * @property leadingGlyph optional leading text glyph, typically an emoji or flag.
 */
@Immutable
public data class SelectionListNode<ID : Any>(
    public val id: ID,
    public val title: String,
    public val subtitle: String? = null,
    public val leadingGlyph: String? = null,
    public val children: List<SelectionListNode<ID>> = emptyList(),
) {
    /** Whether the node has no children and is therefore directly selectable. */
    public val isLeaf: Boolean get() = children.isEmpty()
}

/** Whether the node's title or subtitle contains [query], ignoring case. */
internal fun SelectionListNode<*>.matches(query: String): Boolean =
    title.contains(query, ignoreCase = true) || subtitle?.contains(query, ignoreCase = true) == true

/**
 * Filters [nodes] by [query], ignoring case and keeping order. A leaf stays when it matches. A
 * parent stays whole when it matches itself; otherwise it stays with only its matching children,
 * or is dropped when none match. A blank query returns [nodes] unchanged.
 */
internal fun <ID : Any> filterSelectionListNodes(
    nodes: List<SelectionListNode<ID>>,
    query: String,
): List<SelectionListNode<ID>> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return nodes
    return nodes.mapNotNull { node ->
        when {
            node.isLeaf -> node.takeIf { it.matches(trimmed) }

            node.matches(trimmed) -> node

            else -> {
                val matching = node.children.filter { it.matches(trimmed) }
                if (matching.isEmpty()) null else node.copy(children = matching)
            }
        }
    }
}

/** The comma-joined titles of [node]'s selected children, or `null` when none is selected. */
internal fun <ID : Any> selectedChildrenSummary(node: SelectionListNode<ID>, selectedIds: Set<ID>): String? =
    node.children.filter { it.id in selectedIds }.map { it.title }.takeIf { it.isNotEmpty() }?.joinToString(", ")

/** Ids of the parents in [nodes] that contain a selected child, so they can start expanded. */
internal fun <ID : Any> parentsWithSelection(nodes: List<SelectionListNode<ID>>, selectedIds: Set<ID>): Set<ID> =
    nodes.filter { parent -> parent.children.any { it.id in selectedIds } }.mapTo(mutableSetOf()) { it.id }
