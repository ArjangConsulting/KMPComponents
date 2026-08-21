package io.github.maniramezan.kmpcomponents

import androidx.compose.ui.graphics.Color
import io.github.maniramezan.kommon.designsystem.ColorToken
import kotlin.test.Test
import kotlin.test.assertEquals

class ColorTokenAdapterTest {
    @Test
    fun convertsArgbWithoutTreatingItAsPackedColorSpaceData() {
        assertEquals(
            Color(0xFF_3F_51_B5.toInt()),
            ColorToken(0xFF_3F_51_B5).toComposeColor(),
        )
    }
}
