package io.github.maniramezan.kmpcomponents

import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.KeyInputModifierNode
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalFocusManager

/**
 * Moves focus to the next control on Tab and the previous one on Shift+Tab, instead of letting the
 * element (typically a multi-line text field on desktop) consume the key. Apply it to fields in
 * forms that keyboard users move through.
 */
public fun Modifier.focusNextOnTab(): Modifier = this then FocusNextOnTabElement

private data object FocusNextOnTabElement : ModifierNodeElement<FocusNextOnTabNode>() {
    override fun create(): FocusNextOnTabNode = FocusNextOnTabNode()

    override fun update(node: FocusNextOnTabNode) = Unit

    override fun InspectorInfo.inspectableProperties() {
        name = "focusNextOnTab"
    }
}

private class FocusNextOnTabNode :
    Modifier.Node(),
    KeyInputModifierNode,
    CompositionLocalConsumerModifierNode {
    override fun onPreKeyEvent(event: KeyEvent): Boolean {
        if (event.type != KeyEventType.KeyDown || event.key != Key.Tab) return false
        currentValueOf(
            LocalFocusManager,
        ).moveFocus(if (event.isShiftPressed) FocusDirection.Previous else FocusDirection.Next)
        return true
    }

    override fun onKeyEvent(event: KeyEvent): Boolean = false
}
