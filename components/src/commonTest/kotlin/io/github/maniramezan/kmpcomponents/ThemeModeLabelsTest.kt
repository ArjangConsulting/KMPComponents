package io.github.maniramezan.kmpcomponents

import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeModeLabelsTest {
    @Test
    fun `every mode has a label`() {
        val labels = ThemeModeLabels(system = "s", light = "l", dark = "d")
        assertEquals(listOf("s", "l", "d"), ThemeMode.entries.map(labels::labelFor))
    }
}
