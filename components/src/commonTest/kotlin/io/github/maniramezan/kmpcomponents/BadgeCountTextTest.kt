package io.github.maniramezan.kmpcomponents

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BadgeCountTextTest {
    @Test
    fun `counts up to the cap are shown as is`() {
        assertEquals("0", badgeCountText(0))
        assertEquals("7", badgeCountText(7))
        assertEquals("99", badgeCountText(99))
    }

    @Test
    fun `counts above the cap collapse to cap plus`() {
        assertEquals("99+", badgeCountText(100))
        assertEquals("9+", badgeCountText(12, maxCount = 9))
    }

    @Test
    fun `negative counts show zero`() {
        assertEquals("0", badgeCountText(-3))
    }

    @Test
    fun `cap must be positive`() {
        assertFailsWith<IllegalArgumentException> { badgeCountText(1, maxCount = 0) }
    }
}
