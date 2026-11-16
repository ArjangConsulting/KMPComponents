package io.github.maniramezan.kmpcomponents

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ThumbsFeedbackRowUiTest {
    private var rating by mutableStateOf(ThumbsRating.NONE)
    private val reported = mutableListOf<ThumbsRating>()

    private fun ComposeUiTest.setRow(
        enabled: Boolean = true,
        allowClearing: Boolean = true,
        selectedStateDescription: String? = null,
    ) {
        setContent {
            KmpTheme {
                ThumbsFeedbackRow(
                    rating = rating,
                    onRatingChange = {
                        reported += it
                        rating = it
                    },
                    positiveContentDescription = "Good response",
                    negativeContentDescription = "Bad response",
                    enabled = enabled,
                    allowClearing = allowClearing,
                    selectedStateDescription = selectedStateDescription,
                )
            }
        }
    }

    @Test
    fun `tapping selects switches and clears the rating`() = runComposeUiTest {
        setRow()
        onNodeWithContentDescription("Good response").assertIsNotSelected()
        onNodeWithContentDescription("Good response").performClick()
        onNodeWithContentDescription("Good response").assertIsSelected()
        onNodeWithContentDescription("Bad response").performClick()
        onNodeWithContentDescription("Good response").assertIsNotSelected()
        onNodeWithContentDescription("Bad response").assertIsSelected()
        onNodeWithContentDescription("Bad response").performClick()
        onNodeWithContentDescription("Bad response").assertIsNotSelected()
        assertEquals(listOf(ThumbsRating.POSITIVE, ThumbsRating.NEGATIVE, ThumbsRating.NONE), reported)
    }

    @Test
    fun `the selected choice stays when clearing is not allowed`() = runComposeUiTest {
        rating = ThumbsRating.POSITIVE
        setRow(allowClearing = false)
        onNodeWithContentDescription("Good response").performClick()
        onNodeWithContentDescription("Good response").assertIsSelected()
        assertEquals(emptyList(), reported)
    }

    @Test
    fun `a disabled row keeps its rating and ignores taps`() = runComposeUiTest {
        rating = ThumbsRating.NEGATIVE
        setRow(enabled = false)
        onNodeWithContentDescription("Bad response").assertIsNotEnabled().assertIsSelected()
        onNodeWithContentDescription("Good response").performClick()
        assertEquals(emptyList(), reported)
    }

    @Test
    fun `buttons are named buttons with a full touch target and an optional state`() = runComposeUiTest {
        rating = ThumbsRating.POSITIVE
        setRow(selectedStateDescription = "Selected")
        onNodeWithContentDescription("Good response")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Selected"))
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
        onNodeWithContentDescription("Bad response")
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.StateDescription))
    }
}
