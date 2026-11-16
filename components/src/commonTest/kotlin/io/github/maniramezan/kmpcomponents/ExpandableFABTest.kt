package io.github.maniramezan.kmpcomponents

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExpandableFABTest {
    private fun action(
        id: String,
    ) = FABAction(id = id, label = "Action $id", icon = ColorPainter(Color.Black), onClick = {})

    @Test
    fun `duplicate action ids are detected`() {
        assertTrue(hasUniqueFABActionIds(listOf(action("a"), action("b"))))
        assertFalse(hasUniqueFABActionIds(listOf(action("a"), action("b"), action("a"))))
        assertTrue(hasUniqueFABActionIds(emptyList()))
    }

    @Test
    fun `a blank action label is rejected`() {
        assertFailsWith<IllegalArgumentException> { FABAction("a", " ", ColorPainter(Color.Black), {}) }
    }

    @Test
    fun `up and start place actions before the trigger`() {
        assertTrue(FABExpansionDirection.UP.placesActionsFirst)
        assertTrue(FABExpansionDirection.START.placesActionsFirst)
        assertFalse(FABExpansionDirection.DOWN.placesActionsFirst)
        assertFalse(FABExpansionDirection.END.placesActionsFirst)
    }

    @Test
    fun `start and end open horizontally`() {
        assertTrue(FABExpansionDirection.START.isHorizontal)
        assertTrue(FABExpansionDirection.END.isHorizontal)
        assertFalse(FABExpansionDirection.UP.isHorizontal)
        assertFalse(FABExpansionDirection.DOWN.isHorizontal)
    }
}
