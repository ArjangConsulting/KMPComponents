package io.github.maniramezan.kmpcomponents

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class SkeletonShimmerTest {
    @Test
    fun `sweep ramps from start to end then rests`() {
        assertEquals(0f, skeletonShimmerSweep(0))
        val endOfSweep = (SHIMMER_PERIOD_MILLIS * SHIMMER_SWEEP_FRACTION).toLong()
        assertEquals(1f, skeletonShimmerSweep(endOfSweep), absoluteTolerance = 0.01f)
        assertEquals(1f, skeletonShimmerSweep(SHIMMER_PERIOD_MILLIS - 1))
    }

    @Test
    fun `sweep repeats every period`() {
        assertEquals(skeletonShimmerSweep(300), skeletonShimmerSweep(300 + SHIMMER_PERIOD_MILLIS * 5))
    }

    @Test
    fun `highlight is the lighter of surface and on surface`() {
        val light = lightColorScheme(surface = Color.White, onSurface = Color.Black)
        val dark = darkColorScheme(surface = Color.Black, onSurface = Color.White)
        assertEquals(Color.White, light.shimmerHighlight())
        assertEquals(Color.White, dark.shimmerHighlight())
    }
}
