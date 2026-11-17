package io.github.maniramezan.kmpcomponents

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class QuestionnaireNumbersTest {
    @Test
    fun `sanitizing keeps digits one separator and a leading minus only when negatives are allowed`() {
        assertEquals("12.50", QuestionnaireNumbers.sanitize("1a2.5.0", '.', allowsNegative = false))
        assertEquals("1250", QuestionnaireNumbers.sanitize("1,250", '.', allowsNegative = false))
        assertEquals("12,5", QuestionnaireNumbers.sanitize("1.2,5", ',', allowsNegative = false))
        assertEquals("-3", QuestionnaireNumbers.sanitize("-3", '.', allowsNegative = true))
        assertEquals("-3", QuestionnaireNumbers.sanitize("−3", '.', allowsNegative = true))
        assertEquals("3", QuestionnaireNumbers.sanitize("-3", '.', allowsNegative = false))
        assertEquals("34", QuestionnaireNumbers.sanitize("3-4", '.', allowsNegative = true))
        assertEquals("15", QuestionnaireNumbers.sanitize("1e5", '.', allowsNegative = false))
        assertEquals("١٢", QuestionnaireNumbers.sanitize("١٢", '.', allowsNegative = false))
    }

    @Test
    fun `parsing reads the locale separator and non-Latin digits and rejects incomplete text`() {
        assertEquals(12.5, QuestionnaireNumbers.parse("12.5", '.'))
        assertEquals(12.5, QuestionnaireNumbers.parse("12,5", ','))
        assertEquals(5.0, QuestionnaireNumbers.parse("5.", '.'))
        assertEquals(0.5, QuestionnaireNumbers.parse(".5", '.'))
        assertEquals(-2.0, QuestionnaireNumbers.parse("-2", '.'))
        assertEquals(12.0, QuestionnaireNumbers.parse("١٢", '.'))
        assertNull(QuestionnaireNumbers.parse("", '.'))
        assertNull(QuestionnaireNumbers.parse("-", '.'))
        assertNull(QuestionnaireNumbers.parse(".", '.'))
        assertNull(QuestionnaireNumbers.parse("1.5", ','))
        assertNull(QuestionnaireNumbers.parse("1-5", '.'))
    }

    @Test
    fun `formatting uses plain decimal notation that parses back`() {
        val values =
            listOf(0.0, -0.0, 5.0, -2.0, 0.1, 12.25, 1e7, 1.5e-7, 123456789.125, -0.000042, 1e21, Double.MAX_VALUE)
        values.forEach { value ->
            val text = QuestionnaireNumbers.format(value, ',')
            assertEquals(false, text.any { it == 'E' || it == 'e' }, text)
            assertEquals(if (value == 0.0) 0.0 else value, QuestionnaireNumbers.parse(text, ','), text)
        }
        assertEquals("0", QuestionnaireNumbers.plainDecimal(-0.0))
        assertEquals("5", QuestionnaireNumbers.plainDecimal(5.0))
        assertEquals("12,25", QuestionnaireNumbers.format(12.25, ','))
        assertEquals("10000000", QuestionnaireNumbers.plainDecimal(1e7))
        assertEquals("0.00000015", QuestionnaireNumbers.plainDecimal(1.5e-7))
        assertEquals("-0.000042", QuestionnaireNumbers.plainDecimal(-0.000042))
        assertEquals("1000000000000000000000", QuestionnaireNumbers.plainDecimal(1e21))
    }
}
