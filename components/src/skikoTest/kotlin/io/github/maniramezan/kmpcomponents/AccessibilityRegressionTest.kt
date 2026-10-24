package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.maniramezan.kommon.designsystem.ColorToken
import io.github.maniramezan.kommon.designsystem.KommonDesignTokens
import io.github.maniramezan.kommon.designsystem.ThemeTokens
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AccessibilityRegressionTest {
    @Test
    fun `keyboard focus adds a border across themes and font scales`() {
        val themes = listOf(
            KommonDesignTokens.default,
            KommonDesignTokens.default.copy(
                lightColors = KommonDesignTokens.default.lightColors.copy(primary = ColorToken(0xFF_00_6C_4F)),
                darkColors = KommonDesignTokens.default.darkColors.copy(primary = ColorToken(0xFF_7F_D8_B5)),
            ),
        )
        themes.forEach { tokens ->
            listOf(ThemeMode.LIGHT, ThemeMode.DARK).forEach { mode ->
                listOf(1f, 2f).forEach { fontScale ->
                    FocusCase.entries.forEach { control -> verifyFocus(control, mode, fontScale, tokens) }
                }
            }
        }
    }

    private fun verifyFocus(
        control: FocusCase,
        mode: ThemeMode,
        fontScale: Float,
        tokens: ThemeTokens,
    ) = runComposeUiTest {
        var focusColor = Color.Unspecified
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                KmpTheme(mode = mode, tokens = tokens) {
                    focusColor = when (control) {
                        FocusCase.TOOLTIP -> MaterialTheme.colorScheme.primary

                        FocusCase.PRIMARY, FocusCase.SELECTED_PILL, FocusCase.SEGMENT ->
                            MaterialTheme.colorScheme.onPrimary

                        FocusCase.UNSELECTED_PILL -> MaterialTheme.colorScheme.onPrimaryContainer

                        FocusCase.ACTION_PILL, FocusCase.DISCLOSURE, FocusCase.SIDEBAR ->
                            MaterialTheme.colorScheme.onSurfaceVariant

                        FocusCase.TAB, FocusCase.TAB_CLOSE -> MaterialTheme.colorScheme.onPrimaryContainer

                        FocusCase.SELECTED_CARD -> MaterialTheme.colorScheme.onSecondaryContainer

                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    Surface {
                        FocusControl(control, Modifier.testTag("control"))
                    }
                }
            }
        }
        val target = when (control) {
            FocusCase.ICON, FocusCase.TOOLTIP -> onNodeWithContentDescription("Focus")
            FocusCase.ZOOM -> onNodeWithTag("control")
            FocusCase.TAB_CLOSE -> onNodeWithContentDescription("Close tab")
            FocusCase.SECRET_ICON -> onNodeWithContentDescription("Show")
            FocusCase.SECRET_TEXT -> onNodeWithText("Show")
            else -> onNodeWithText("Focus")
        }
        val before = target.captureToImage()
        target.requestFocus()
        val after = target.captureToImage()
        assertEquals(before.width, after.width)
        assertEquals(before.height, after.height)
        val borderY = if (control == FocusCase.TOOLTIP) 0 else 1
        val top = after.toPixelMap()[after.width / 2, borderY]
        assertTrue(
            colorsMatch(focusColor, top),
            "$control $mode $fontScale expected a $focusColor focus border, got $top",
        )
        assertTrue(!colorsMatch(focusColor, before.toPixelMap()[before.width / 2, borderY]))
    }

    @Test
    fun `reduced motion override changes while the subtree remains composed`() = runComposeUiTest {
        var reduceMotion by mutableStateOf(false)
        var animationsEnabled = false
        setContent {
            CompositionLocalProvider(LocalReduceMotionEnabled provides reduceMotion) {
                KmpTheme {
                    animationsEnabled = decorativeAnimationsEnabled()
                    TypingIndicator(modifier = Modifier.testTag("typing"))
                }
            }
        }
        runOnIdle { assertTrue(animationsEnabled) }
        runOnIdle { reduceMotion = true }
        runOnIdle { assertTrue(!animationsEnabled) }
        mainClock.autoAdvance = false
        val before = onNodeWithTag("typing").captureToImage().toPixelMap()
        mainClock.advanceTimeBy(450L)
        val after = onNodeWithTag("typing").captureToImage().toPixelMap()
        repeat(before.width) { x -> repeat(before.height) { y -> assertEquals(before[x, y], after[x, y]) } }
        runOnIdle { reduceMotion = false }
        mainClock.autoAdvance = true
        runOnIdle { assertTrue(animationsEnabled) }
    }
}

@Composable
private fun FocusControl(control: FocusCase, modifier: Modifier) {
    when (control) {
        FocusCase.PRIMARY -> PrimaryButton("Focus", {}, modifier)

        FocusCase.SECONDARY -> SecondaryButton("Focus", {}, modifier)

        FocusCase.TEXT -> ThemeTextButton("Focus", {}, modifier)

        FocusCase.SELECTED_PILL -> PillChip("Focus", true, modifier, onClick = {})

        FocusCase.UNSELECTED_PILL -> PillChip("Focus", false, modifier, onClick = {})

        FocusCase.ACTION_PILL -> ActionPill({}, modifier) { Text("Focus") }

        FocusCase.SEGMENT -> SegmentedContent(listOf(SegmentedItem("Focus")), modifier) { _, _ -> }

        FocusCase.TOOLTIP -> InfoTooltip("Focus", modifier)

        FocusCase.ICON -> ThemeIconButton(ColorPainter(MaterialTheme.colorScheme.primary), "Focus", {}, modifier)

        FocusCase.ZOOM -> ZoomableImage(ImageBitmap(100, 50), "Screenshot", modifier.size(200.dp))

        FocusCase.DISCLOSURE -> DisclosureCard(false, {}, modifier, summary = { Text("Focus") }, detail = {})

        FocusCase.SELECTED_CARD -> SelectableCard(true, {}, modifier) { Text("Focus") }

        FocusCase.UNSELECTED_CARD -> SelectableCard(false, {}, modifier) { Text("Focus") }

        FocusCase.CARD -> ThemeCard(modifier, onClick = {}) { Text("Focus") }

        FocusCase.SIDEBAR -> SidebarItem("Focus", false, {}, modifier)

        FocusCase.TAB -> TabChip("Focus", true, {}, modifier)

        FocusCase.TAB_CLOSE -> TabChip("Tab", true, {}, modifier, onClose = {}, closeDescription = "Close tab")

        FocusCase.CONTENT_ROW -> ContentRow("Focus", modifier, onClick = {})

        FocusCase.SWITCH -> ThemeSwitch(false, {}, "Focus", modifier)

        FocusCase.CHECKBOX -> ThemeCheckbox(false, {}, "Focus", modifier)

        FocusCase.TRI_CHECKBOX -> ThemeTriStateCheckbox(ToggleableState.Indeterminate, {}, "Focus", modifier)

        FocusCase.HEADER_ACTION -> SectionHeader("Header", modifier, actionLabel = "Focus", onAction = {})

        FocusCase.NOTICE_ACTION -> NoticeCard("Notice", modifier, actionLabel = "Focus", onAction = {})

        FocusCase.SECRET_ICON -> SecretTextField("secret", {
        }, "Secret", modifier, revealToggle = SecretRevealToggle.ICON)

        FocusCase.SECRET_TEXT -> SecretTextField("secret", {}, "Secret", modifier)

        FocusCase.RADIO -> RadioGroup(listOf("Focus"), 0, {}, modifier)

        FocusCase.SELECTION -> Box(Modifier.size(300.dp)) {
            SelectionListContent("", listOf(SelectionListNode("id", "Focus")), emptySet(), {}, modifier)
        }
    }
}

private enum class FocusCase {
    SELECTED_CARD,
    UNSELECTED_CARD,
    CARD,
    SIDEBAR,
    TAB,
    TAB_CLOSE,
    CONTENT_ROW,
    SWITCH,
    CHECKBOX,
    TRI_CHECKBOX,
    HEADER_ACTION,
    NOTICE_ACTION,
    SECRET_ICON,
    SECRET_TEXT,
    PRIMARY,
    SECONDARY,
    TEXT,
    SELECTED_PILL,
    UNSELECTED_PILL,
    ACTION_PILL,
    SEGMENT,
    ICON,
    TOOLTIP,
    ZOOM,
    DISCLOSURE,
    RADIO,
    SELECTION,
}

private fun colorsMatch(first: Color, second: Color): Boolean =
    kotlin.math.abs(first.red - second.red) < 0.01f &&
        kotlin.math.abs(first.green - second.green) < 0.01f &&
        kotlin.math.abs(first.blue - second.blue) < 0.01f &&
        kotlin.math.abs(first.alpha - second.alpha) < 0.01f
