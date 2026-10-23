package io.github.maniramezan.kmpcomponents

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TypingIndicatorTest {
    @Test
    fun `each dot crests one stagger step after the previous one`() {
        repeat(3) { index ->
            val crest = (index + 1) * TYPING_DOT_STAGGER_MILLIS
            assertEquals(TYPING_DOT_BRIGHT_ALPHA, typingDotAlpha(index, crest))
        }
    }

    @Test
    fun `dots rest dim outside their ramp and repeat every cycle`() {
        assertEquals(TYPING_DOT_DIM_ALPHA, typingDotAlpha(2, 0))
        assertEquals(TYPING_DOT_DIM_ALPHA, typingDotAlpha(0, TYPING_CYCLE_MILLIS - 1))
        assertEquals(typingDotAlpha(1, 200), typingDotAlpha(1, 200 + TYPING_CYCLE_MILLIS * 3))
    }

    @Test
    fun `alpha stays between dim and bright`() {
        (0 until TYPING_CYCLE_MILLIS.toInt() step 10).forEach { t ->
            val alpha = typingDotAlpha(1, t.toLong())
            assertTrue(alpha in TYPING_DOT_DIM_ALPHA..TYPING_DOT_BRIGHT_ALPHA)
        }
    }
}
