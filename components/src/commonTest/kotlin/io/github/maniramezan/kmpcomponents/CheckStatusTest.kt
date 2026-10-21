package io.github.maniramezan.kmpcomponents

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckStatusTest {
    private val labels = CheckStatusLabels(idle = "idle", checking = "checking")

    @Test
    fun `messages come from labels until there is a result`() {
        assertEquals("idle", CheckStatus.Idle.message(labels))
        assertEquals("checking", CheckStatus.Checking.message(labels))
        assertEquals("Connected", CheckStatus.Success("Connected").message(labels))
        assertEquals("Timed out", CheckStatus.Failure("Timed out").message(labels))
    }

    @Test
    fun `only checking reports isChecking`() {
        assertTrue(CheckStatus.Checking.isChecking)
        assertFalse(CheckStatus.Idle.isChecking)
        assertFalse(CheckStatus.Success("ok").isChecking)
        assertFalse(CheckStatus.Failure("no").isChecking)
    }
}
