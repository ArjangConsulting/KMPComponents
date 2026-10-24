package io.github.maniramezan.kmpcomponents

import kotlin.test.Test
import kotlin.test.assertEquals

class AvatarInitialsTest {
    @Test
    fun `first and last words give two uppercase initials`() {
        assertEquals("AL", avatarInitials("ada lovelace"))
        assertEquals("GH", avatarInitials("Grace Brewster Murray Hopper"))
    }

    @Test
    fun `a single word gives one initial`() {
        assertEquals("C", avatarInitials("cher"))
    }

    @Test
    fun `punctuation and digits are skipped`() {
        assertEquals("JN", avatarInitials("  jean-luc, o'neil  "))
        assertEquals("R", avatarInitials("2024 report"))
    }

    @Test
    fun `non-latin letters and combining marks are kept`() {
        assertEquals("ÉZ", avatarInitials("élodie zoë"))
        assertEquals("مر", avatarInitials("مانی رمضان"))
    }

    @Test
    fun `names without letters give no initials`() {
        assertEquals("", avatarInitials(""))
        assertEquals("", avatarInitials("  123 !! "))
    }
}
