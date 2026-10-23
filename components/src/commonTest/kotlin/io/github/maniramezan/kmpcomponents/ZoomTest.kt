package io.github.maniramezan.kmpcomponents

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoomTest {
    @Test
    fun `scrolling up zooms in and down zooms out by the same step`() {
        val zoomedIn = zoomForScroll(1f, scrollDeltaY = -1f)
        assertTrue(zoomedIn > 1f)
        assertEquals(1f, zoomForScroll(zoomedIn, scrollDeltaY = 1f), absoluteTolerance = 0.0001f)
    }

    @Test
    fun `no scroll keeps the zoom`() {
        assertEquals(2f, zoomForScroll(2f, 0f))
    }
}
