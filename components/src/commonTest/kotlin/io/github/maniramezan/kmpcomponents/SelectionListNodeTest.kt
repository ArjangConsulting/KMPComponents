package io.github.maniramezan.kmpcomponents

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class SelectionListNodeTest {
    private val english = SelectionListNode(
        id = "en",
        title = "English",
        children = listOf(
            SelectionListNode("en-US", "United States", subtitle = "en-US"),
            SelectionListNode("en-GB", "United Kingdom", subtitle = "en-GB"),
        ),
    )
    private val german = SelectionListNode("de", "Deutsch", subtitle = "de")
    private val nodes = listOf(english, german)

    @Test
    fun `a blank query returns the nodes unchanged`() {
        assertSame(nodes, filterSelectionListNodes(nodes, "  "))
    }

    @Test
    fun `leaves match on title or subtitle ignoring case`() {
        assertEquals(listOf(german), filterSelectionListNodes(nodes, "DEUT"))
        assertEquals(listOf(german), filterSelectionListNodes(nodes, " de "))
    }

    @Test
    fun `a matching parent keeps all its children`() {
        assertEquals(listOf(english), filterSelectionListNodes(nodes, "english"))
    }

    @Test
    fun `a parent is pruned to its matching children`() {
        val result = filterSelectionListNodes(nodes, "kingdom")
        assertEquals(listOf("en-GB"), result.single().children.map { it.id })
    }

    @Test
    fun `nodes without matches are dropped`() {
        assertEquals(emptyList(), filterSelectionListNodes(nodes, "français"))
    }

    @Test
    fun `summary joins selected child titles in order`() {
        assertEquals("United States, United Kingdom", selectedChildrenSummary(english, setOf("en-GB", "en-US")))
        assertNull(selectedChildrenSummary(english, setOf("de")))
    }

    @Test
    fun `parents with a selected child start expanded`() {
        assertEquals(setOf("en"), parentsWithSelection(nodes, setOf("en-US", "de")))
        assertEquals(emptySet(), parentsWithSelection(nodes, setOf("de")))
    }
}
