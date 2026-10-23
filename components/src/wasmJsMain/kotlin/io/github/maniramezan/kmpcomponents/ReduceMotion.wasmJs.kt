@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package io.github.maniramezan.kmpcomponents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.js.JsAny
import kotlin.js.js

@Composable
internal actual fun isReduceMotionEnabled(): Boolean {
    var enabled by remember { mutableStateOf(prefersReducedMotion()) }
    DisposableEffect(Unit) {
        val observer = observeReducedMotion { enabled = it }
        onDispose { stopObservingReducedMotion(observer) }
    }
    return enabled
}

internal fun prefersReducedMotion(): Boolean = js("window.matchMedia('(prefers-reduced-motion: reduce)').matches")

internal fun observeReducedMotion(onChange: (Boolean) -> Unit): JsAny = js(
    """{
        const query = window.matchMedia('(prefers-reduced-motion: reduce)');
        const listener = () => onChange(query.matches);
        query.addEventListener('change', listener);
        onChange(query.matches);
        return { query, listener };
    }""",
)

internal fun stopObservingReducedMotion(observer: JsAny): Unit = js(
    "observer.query.removeEventListener('change', observer.listener)",
)
