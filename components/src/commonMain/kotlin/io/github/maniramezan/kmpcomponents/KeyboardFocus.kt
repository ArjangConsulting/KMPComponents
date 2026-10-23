package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

/** Adds a visible outline when the following focus target gains keyboard focus. */
@Composable
internal fun Modifier.keyboardFocusOutline(
    shape: Shape = MaterialTheme.shapes.small,
    color: Color = MaterialTheme.colorScheme.onSurface,
): Modifier {
    var focused by remember { mutableStateOf(false) }
    val outline = if (focused) {
        Modifier.border(MaterialTheme.kmpSpacing.extraSmall / 2, color, shape)
    } else {
        Modifier
    }
    return onFocusChanged { focused = it.isFocused }.then(outline)
}
