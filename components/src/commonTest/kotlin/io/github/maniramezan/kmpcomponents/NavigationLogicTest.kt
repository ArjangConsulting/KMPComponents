package io.github.maniramezan.kmpcomponents

import androidx.compose.material3.Typography
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.text.font.FontWeight
import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationLogicTest {
    @Test
    fun `title progress is full on the current page and fades over one page`() {
        assertEquals(1f, titleProgress(2, 2f))
        assertEquals(0.5f, titleProgress(3, 2.5f))
        assertEquals(0f, titleProgress(4, 2f))
    }

    @Test
    fun `leading and trailing titles keep their anchor while sliding a page per page`() {
        assertEquals(0, titleOffset(0f, rowWidth = 400, titleWidth = 100, PageTitleAlignment.LEADING))
        assertEquals(400, titleOffset(1f, 400, 100, PageTitleAlignment.LEADING))
        assertEquals(300, titleOffset(0f, 400, 100, PageTitleAlignment.TRAILING))
    }

    @Test
    fun `centered titles move from the start edge to the center as they settle`() {
        assertEquals(150, titleOffset(0f, 400, 100, PageTitleAlignment.CENTER))
        assertEquals(400, titleOffset(1f, 400, 100, PageTitleAlignment.CENTER))
        assertEquals(275, titleOffset(0.5f, 400, 100, PageTitleAlignment.CENTER))
    }

    @Test
    fun `text roles resolve to Material slots with heavier weights`() {
        val typography = Typography()
        assertEquals(typography.bodyLarge, AppTextStyle.BODY.resolve(typography))
        assertEquals(FontWeight.Bold, AppTextStyle.LABEL_LARGE_BOLD.resolve(typography).fontWeight)
        assertEquals(typography.labelLarge.fontSize, AppTextStyle.LABEL_LARGE_BOLD.resolve(typography).fontSize)
    }

    @Test
    fun `tab bar scroll behavior hides within the bar height`() {
        val behavior = TabBarScrollBehavior()
        behavior.updateHeightOffsetLimit(-100f)
        val connection = behavior.nestedScrollConnection
        connection.onPostScroll(Offset(0f, -60f), Offset.Zero, NestedScrollSource.UserInput)
        assertEquals(0.6f, behavior.collapsedFraction)
        connection.onPostScroll(Offset(0f, -200f), Offset.Zero, NestedScrollSource.UserInput)
        assertEquals(1f, behavior.collapsedFraction)
        connection.onPostScroll(Offset(0f, 500f), Offset.Zero, NestedScrollSource.UserInput)
        assertEquals(0f, behavior.collapsedFraction)
    }
}
