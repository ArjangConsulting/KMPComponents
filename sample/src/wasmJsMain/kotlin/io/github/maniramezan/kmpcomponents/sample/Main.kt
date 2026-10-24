package io.github.maniramezan.kmpcomponents.sample

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

/** The catalog in the browser, mounted on `index.html`'s body. */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport { ComponentCatalog() }
}
