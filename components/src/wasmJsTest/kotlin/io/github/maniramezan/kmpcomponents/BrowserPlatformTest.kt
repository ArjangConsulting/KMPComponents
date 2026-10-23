@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package io.github.maniramezan.kmpcomponents

import kotlin.js.JsAny
import kotlin.js.js
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BrowserPlatformTest {
    @Test
    fun `reduced motion tracks browser preference changes and releases the listener`() {
        val fake = installMatchMedia()
        try {
            assertFalse(prefersReducedMotion())
            var latest = false
            val observer = observeReducedMotion { latest = it }
            assertEquals(1, listenerCount(fake))
            setReducedMotion(fake, true)
            assertTrue(prefersReducedMotion())
            assertTrue(latest)
            stopObservingReducedMotion(observer)
            assertEquals(0, listenerCount(fake))
            setReducedMotion(fake, false)
            assertTrue(latest)
        } finally {
            restoreMatchMedia(fake)
        }
    }
}

private fun installMatchMedia(): JsAny = js(
    """{
        const original = window.matchMedia;
        const listeners = new Set();
        const query = {
            matches: false,
            addEventListener: (type, listener) => listeners.add(listener),
            removeEventListener: (type, listener) => listeners.delete(listener)
        };
        window.matchMedia = () => query;
        return { original, query, listeners };
    }""",
)

private fun setReducedMotion(fake: JsAny, enabled: Boolean): Unit = js(
    """{
        fake.query.matches = enabled;
        fake.listeners.forEach(listener => listener());
    }""",
)

private fun listenerCount(fake: JsAny): Int = js("fake.listeners.size")

private fun restoreMatchMedia(fake: JsAny): Unit = js("{ window.matchMedia = fake.original; }")
