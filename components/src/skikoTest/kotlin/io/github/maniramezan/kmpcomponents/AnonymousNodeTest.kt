package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertTrue

// iOS's native audit reported "Element has no description" next to icon buttons: VoiceOver treats a
// merging or actionable node as its own element, so one without a name is announced as unlabeled.
@OptIn(ExperimentalTestApi::class)
class AnonymousNodeTest {
    @Test
    fun `icon buttons with tooltips expose no unnamed actionable nodes`() = runComposeUiTest {
        setContent {
            KmpTheme {
                Column {
                    CopyButton(text = "value")
                    InfoTooltip(text = "Explains the field")
                    SearchField(value = "query", onValueChange = {})
                    ChatComposer(value = "Hello", onValueChange = {}, onSend = {})
                }
            }
        }
        val unnamed = onRoot().fetchSemanticsNode().descendants().filter {
            it.isAccessibilityElement() && !it.hasName()
        }
        assertTrue(unnamed.isEmpty(), "Unnamed accessibility elements: ${unnamed.map { it.config }}")
    }
}

private fun SemanticsNode.descendants(): List<SemanticsNode> = children.flatMap { listOf(it) + it.descendants() }

// Platforms expose merging nodes and nodes with actions as separate accessibility elements.
private fun SemanticsNode.isAccessibilityElement(): Boolean = config.isMergingSemanticsOfDescendants ||
    listOf(SemanticsActions.OnClick, SemanticsActions.OnLongClick, SemanticsActions.SetText)
        .any { config.getOrNull(it) != null }

private fun SemanticsNode.hasName(): Boolean =
    config.getOrNull(SemanticsProperties.ContentDescription).orEmpty().any { it.isNotBlank() } ||
        config.getOrNull(SemanticsProperties.Text).orEmpty().any { it.text.isNotBlank() } ||
        config.getOrNull(SemanticsProperties.EditableText)?.text?.isNotBlank() == true
