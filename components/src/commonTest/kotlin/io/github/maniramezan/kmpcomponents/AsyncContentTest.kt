package io.github.maniramezan.kmpcomponents

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AsyncContentTest {
    @Test
    fun `content and error keep their kind when their payload changes`() {
        assertEquals(asyncContentKind(AsyncContentState.Content("a")), asyncContentKind(AsyncContentState.Content("b")))
        assertEquals(asyncContentKind(AsyncContentState.Error()), asyncContentKind(AsyncContentState.Error("Offline")))
    }

    @Test
    fun `every state has a distinct kind`() {
        val kinds = listOf(
            AsyncContentState.Loading,
            AsyncContentState.Empty,
            AsyncContentState.Error(),
            AsyncContentState.Content(Unit),
        ).map(::asyncContentKind)
        assertEquals(kinds.size, kinds.toSet().size)
        assertNotEquals(asyncContentKind(AsyncContentState.Loading), asyncContentKind(AsyncContentState.Empty))
    }

    @Test
    fun `the error's own message wins unless it is blank`() {
        assertEquals("Offline", asyncErrorMessage(AsyncContentState.Error("Offline"), "Failed"))
        assertEquals("Failed", asyncErrorMessage(AsyncContentState.Error("  "), "Failed"))
        assertEquals("Failed", asyncErrorMessage(AsyncContentState.Error(), "Failed"))
    }
}
