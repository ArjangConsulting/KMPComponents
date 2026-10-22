package io.github.maniramezan.kmpcomponents

import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ToastDurationTest {
    @Test
    fun `long toasts outlast short ones and indefinite ones never time out`() {
        val short = ToastDuration.SHORT.timeoutMillis ?: error("short has a timeout")
        val long = ToastDuration.LONG.timeoutMillis ?: error("long has a timeout")
        assertTrue(long > short)
        assertNull(ToastDuration.INDEFINITE.timeoutMillis)
    }
}
