package io.github.maniramezan.kmpcomponents

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ReduceMotionTest {
    private fun observe(override: Boolean?): Boolean {
        var observed: Boolean? = null
        runComposeUiTest {
            setContent {
                CompositionLocalProvider(LocalReduceMotionEnabled provides override) {
                    observed = rememberReduceMotionEnabled()
                }
            }
            waitForIdle()
        }
        return checkNotNull(observed)
    }

    @Test
    fun `an override of true is returned as is`() {
        assertEquals(true, observe(true))
    }

    @Test
    fun `an override of false is returned as is`() {
        assertEquals(false, observe(false))
    }

    @Test
    fun `without an override the desktop system preference is used`() {
        assertEquals(false, observe(null))
    }
}
