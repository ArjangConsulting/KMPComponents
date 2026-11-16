package io.github.maniramezan.kmpcomponents

import kotlin.test.Test
import kotlin.test.assertEquals

class TypewriterRevealTest {
    @Test
    fun `reveals one character at a time`() {
        assertEquals(1, nextRevealLength("abc", 0))
        assertEquals(3, nextRevealLength("abc", 2))
    }

    @Test
    fun `keeps surrogate pairs together`() {
        val text = "a😀b"
        assertEquals(1, nextRevealLength(text, 0))
        assertEquals(3, nextRevealLength(text, 1))
        assertEquals(4, nextRevealLength(text, 3))
    }

    @Test
    fun `never passes the end of the text`() {
        assertEquals(3, nextRevealLength("abc", 3))
        assertEquals(3, nextRevealLength("abc", 7))
    }

    @Test
    fun `reveals several characters in one step without splitting a pair`() {
        assertEquals("a😀", revealedPrefix("a😀b", "", 2))
        assertEquals("a😀b", revealedPrefix("a😀b", "a", 5))
    }

    @Test
    fun `restarts when the shown text is not a prefix`() {
        assertEquals("B", revealedPrefix("Bye", "Hel", 1))
    }
}
