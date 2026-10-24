package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
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
    fun `a described paging footer exposes no unnamed progress node`() = runComposeUiTest {
        setContent {
            KmpTheme {
                PaginatedContent(
                    pages = listOf(PaginationPage("One"), PaginationPage("Two"), PaginationPage("Three")),
                    footerStyle = PageFooterStyle.PROGRESS,
                    showScrollHint = false,
                    pagePositionDescription = { index, count -> "Step ${index + 1} of $count" },
                ) { _, page -> Text(page.title) }
            }
        }
        val unnamed = onRoot().fetchSemanticsNode().descendants().filter {
            it.isAccessibilityElement() && !it.hasName()
        }
        assertTrue(unnamed.isEmpty(), "Unnamed accessibility elements: ${unnamed.map { it.config }}")
    }

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

// Platforms expose merging nodes, nodes with actions, and progress indicators as separate
// accessibility elements.
private fun SemanticsNode.isAccessibilityElement(): Boolean = config.isMergingSemanticsOfDescendants ||
    config.getOrNull(SemanticsProperties.ProgressBarRangeInfo) != null ||
    listOf(SemanticsActions.OnClick, SemanticsActions.OnLongClick, SemanticsActions.SetText)
        .any { config.getOrNull(it) != null }

private fun SemanticsNode.hasName(): Boolean =
    config.getOrNull(SemanticsProperties.ContentDescription).orEmpty().any { it.isNotBlank() } ||
        config.getOrNull(SemanticsProperties.Text).orEmpty().any { it.text.isNotBlank() } ||
        config.getOrNull(SemanticsProperties.EditableText)?.text?.isNotBlank() == true
