package io.github.maniramezan.kmpcomponents

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ThumbsFeedbackRowTest {
    @Test
    fun `tapping an unselected choice reports it`() {
        assertEquals(ThumbsRating.POSITIVE, resolveThumbsRatingTap(ThumbsRating.NONE, ThumbsRating.POSITIVE, true))
        assertEquals(ThumbsRating.NEGATIVE, resolveThumbsRatingTap(ThumbsRating.POSITIVE, ThumbsRating.NEGATIVE, false))
    }

    @Test
    fun `tapping the selected choice clears it only when allowed`() {
        assertEquals(ThumbsRating.NONE, resolveThumbsRatingTap(ThumbsRating.NEGATIVE, ThumbsRating.NEGATIVE, true))
        assertNull(resolveThumbsRatingTap(ThumbsRating.NEGATIVE, ThumbsRating.NEGATIVE, false))
    }

    @Test
    fun `a tap on none is never reported`() {
        assertNull(resolveThumbsRatingTap(ThumbsRating.POSITIVE, ThumbsRating.NONE, true))
    }
}
