package io.github.maniramezan.kmpcomponents

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ThemeModeTest {
    @Test
    fun `system mode follows system`() {
        assertTrue(ThemeMode.SYSTEM.isDark(systemInDarkTheme = true))
        assertFalse(ThemeMode.SYSTEM.isDark(systemInDarkTheme = false))
    }

    @Test
    fun `explicit modes ignore system`() {
        assertTrue(ThemeMode.DARK.isDark(systemInDarkTheme = false))
        assertFalse(ThemeMode.LIGHT.isDark(systemInDarkTheme = true))
    }

    @Test
    fun `toggling flips what is shown and never returns system`() {
        assertEquals(ThemeMode.LIGHT, ThemeMode.SYSTEM.toggled(systemInDarkTheme = true))
        assertEquals(ThemeMode.DARK, ThemeMode.SYSTEM.toggled(systemInDarkTheme = false))
        assertEquals(ThemeMode.DARK, ThemeMode.LIGHT.toggled(systemInDarkTheme = true))
        assertEquals(ThemeMode.LIGHT, ThemeMode.DARK.toggled(systemInDarkTheme = false))
    }
}
