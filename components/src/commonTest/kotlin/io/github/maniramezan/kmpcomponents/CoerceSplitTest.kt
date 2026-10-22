package io.github.maniramezan.kmpcomponents

import kotlin.test.Test
import kotlin.test.assertEquals

class CoerceSplitTest {
    @Test
    fun `values inside the bounds are kept`() {
        assertEquals(0.5f, coerceSplit(0.5f, 0.2f, 0.8f))
    }

    @Test
    fun `values outside the bounds are clamped`() {
        assertEquals(0.2f, coerceSplit(-1f, 0.2f, 0.8f))
        assertEquals(0.8f, coerceSplit(0.95f, 0.2f, 0.8f))
    }

    @Test
    fun `nan becomes the midpoint`() {
        assertEquals(0.5f, coerceSplit(Float.NaN, 0.2f, 0.8f))
    }
}
