package io.github.maniramezan.kmpcomponents

import kotlin.test.Test
import kotlin.test.assertEquals

class SegmentLayoutTest {
    @Test
    fun `a full-width bar shares the width evenly when every title fits its share`() {
        assertEquals(
            SegmentLayout.EVEN,
            segmentLayout(SegmentFitMode.EVEN_WHEN_FITS, fillWidth = true, fitsEvenly = true, fits = true),
        )
    }

    @Test
    fun `a title wider than its equal share makes the bar scroll instead of truncating`() {
        assertEquals(
            SegmentLayout.SCROLLABLE,
            segmentLayout(SegmentFitMode.EVEN_WHEN_FITS, fillWidth = true, fitsEvenly = false, fits = true),
        )
    }

    @Test
    fun `overflowing titles scroll`() {
        assertEquals(
            SegmentLayout.SCROLLABLE,
            segmentLayout(SegmentFitMode.INTRINSIC, fillWidth = false, fitsEvenly = false, fits = false),
        )
    }

    @Test
    fun `intrinsic full-width bars scroll and content-width bars that fit hug their titles`() {
        assertEquals(
            SegmentLayout.SCROLLABLE,
            segmentLayout(SegmentFitMode.INTRINSIC, fillWidth = true, fitsEvenly = true, fits = true),
        )
        assertEquals(
            SegmentLayout.INTRINSIC,
            segmentLayout(SegmentFitMode.EVEN_WHEN_FITS, fillWidth = false, fitsEvenly = true, fits = true),
        )
    }
}
