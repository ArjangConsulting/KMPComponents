package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Avatar, PageIndicator, and rememberTypewriterReveal.
@OptIn(ExperimentalTestApi::class)
class IdentityAndPagingUiTest {
    private val politeLiveRegion = SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite)
    private val imageRole = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Image)

    @Test
    fun `avatar shows initials and is read as one named image`() = runComposeUiTest {
        setContent { KmpTheme { Avatar(name = "Ada Lovelace") } }
        onNodeWithContentDescription("Ada Lovelace").assert(imageRole).assertWidthIsEqualTo(40.dp)
        onNodeWithText("AL", useUnmergedTree = true).assertExists()
        onAllNodesWithText("AL").assertCountEquals(0)
    }

    @Test
    fun `avatar image replaces the initials and a blank name is decorative`() = runComposeUiTest {
        setContent {
            KmpTheme {
                Avatar(name = "", modifier = Modifier.testTag("avatar")) {
                    Box(Modifier.fillMaxSize().background(Color.Red))
                }
            }
        }
        onNodeWithTag("avatar").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
        onNodeWithTag("avatar").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Role))
    }

    @Test
    fun `a decorative avatar keeps its initials but has no spoken name`() = runComposeUiTest {
        setContent {
            KmpTheme { Avatar(name = "Ada Lovelace", modifier = Modifier.testTag("avatar"), contentDescription = null) }
        }
        onNodeWithTag("avatar").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
        onNodeWithText("AL", useUnmergedTree = true).assertExists()
    }

    @Test
    fun `avatar grows with the font scale so initials fit`() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = 2f)) {
                KmpTheme { Avatar(name = "Wren Wu", size = AvatarSize.LARGE) }
            }
        }
        val avatar = onNodeWithContentDescription("Wren Wu").assertHeightIsAtLeast(112.dp)
        val initials = onNodeWithText("WW", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val bounds = avatar.getUnclippedBoundsInRoot()
        assertTrue(initials.left >= bounds.left && initials.right <= bounds.right, "Initials overflow the avatar")
    }

    @Test
    fun `page indicator announces the clamped position and updates it`() = runComposeUiTest {
        var page by mutableIntStateOf(1)
        setContent {
            KmpTheme {
                PageIndicator(
                    pageCount = 3,
                    currentPage = page,
                    pagePositionDescription = { index, count -> "Page ${index + 1} of $count" },
                )
            }
        }
        // A spoken indicator is its own accessibility element, so it gets a full-size hit region.
        onNodeWithContentDescription("Page 2 of 3").assert(politeLiveRegion).assertHeightIsAtLeast(48.dp)
        page = 9
        onNodeWithContentDescription("Page 3 of 3").assert(politeLiveRegion)
    }

    @Test
    fun `page indicator marks the current page by width as well as color`() = runComposeUiTest {
        setContent {
            KmpTheme {
                CompositionLocalProvider(LocalReduceMotionEnabled provides true) {
                    Surface { PageIndicator(pageCount = 3, currentPage = 0, modifier = Modifier.testTag("dots")) }
                }
            }
        }
        val image = onNodeWithTag("dots").captureToImage().toPixelMap()
        val background = image[0, 0]
        val runs = mutableListOf<Int>()
        var run = 0
        for (x in 0 until image.width) {
            if (image[x, image.height / 2] != background) {
                run++
            } else if (run > 0) {
                runs += run
                run = 0
            }
        }
        if (run > 0) runs += run
        assertEquals(3, runs.size, "Expected three dots, measured $runs")
        assertTrue(runs[0] > runs[1] * 2, "The current dot is not wider than the others: $runs")
        assertEquals(runs[1], runs[2])
    }

    @Test
    fun `page indicator without a description stays out of the accessibility tree`() = runComposeUiTest {
        setContent { KmpTheme { PageIndicator(pageCount = 3, currentPage = 0, modifier = Modifier.testTag("dots")) } }
        onNodeWithTag("dots").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
    }

    @Test
    fun `typewriter reveal continues when the text grows and restarts when it changes`() = runComposeUiTest {
        mainClock.autoAdvance = false
        var text by mutableStateOf("Hello")
        setContent {
            KmpTheme {
                CompositionLocalProvider(LocalReduceMotionEnabled provides false) {
                    val revealed by rememberTypewriterReveal(text, charsPerSecond = 10)
                    Text(revealed, Modifier.testTag("reveal"))
                }
            }
        }
        mainClock.advanceTimeBy(250)
        onNodeWithTag("reveal").assertTextEquals("He")
        text = "Hello there"
        mainClock.advanceTimeBy(550)
        onNodeWithTag("reveal").assertTextEquals("Hello t")
        mainClock.advanceTimeBy(1_000)
        onNodeWithTag("reveal").assertTextEquals("Hello there")
        text = "Bye"
        mainClock.advanceTimeBy(150)
        onNodeWithTag("reveal").assertTextEquals("B")
    }

    @Test
    fun `typewriter reveal shows the whole text when motion is reduced`() = runComposeUiTest {
        setContent {
            KmpTheme {
                CompositionLocalProvider(LocalReduceMotionEnabled provides true) {
                    val revealed by rememberTypewriterReveal("Already here", charsPerSecond = 1)
                    Text(revealed, Modifier.testTag("reveal"))
                }
            }
        }
        onNodeWithTag("reveal").assertTextEquals("Already here")
    }
}
