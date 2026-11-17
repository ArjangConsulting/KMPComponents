package io.github.maniramezan.kmpcomponents

import kotlinx.datetime.LocalDate
import kotlin.math.abs
import kotlin.math.floor

/**
 * Locale-aware text editing for a questionnaire number field: no grouping, no exponent notation,
 * and no non-finite values. The decimal separator comes from [platformDecimalSeparator].
 */
internal object QuestionnaireNumbers {
    private const val MINUS_SIGN = '−'
    private const val MAX_PLAIN_INTEGER = 1e15

    /** Keeps decimal digits, the first [separator], and one leading minus sign when [allowsNegative]. */
    fun sanitize(text: String, separator: Char, allowsNegative: Boolean): String {
        var hasSeparator = false
        return buildString {
            text.forEach { character ->
                when {
                    character.digitToIntOrNull() != null -> append(character)

                    character == separator && !hasSeparator -> {
                        append(character)
                        hasSeparator = true
                    }

                    (character == '-' || character == MINUS_SIGN) && isEmpty() && allowsNegative -> append('-')
                }
            }
        }
    }

    /**
     * Parses field text written with [separator], including non-Latin decimal digits. Blank,
     * incomplete ("-", "."), or non-finite text is `null`.
     */
    fun parse(text: String, separator: Char): Double? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null
        val canonical = buildString {
            trimmed.forEachIndexed { index, character ->
                val digit = character.digitToIntOrNull()
                when {
                    digit != null -> append(digit)
                    character == separator -> append('.')
                    (character == '-' || character == MINUS_SIGN) && index == 0 -> append('-')
                    else -> return null
                }
            }
        }
        if (canonical.none { it.isDigit() }) return null
        return canonical.toDoubleOrNull()?.takeIf { it.isFinite() }
    }

    /** Formats [value] in plain decimal notation with [separator], so [parse] reads it back. */
    fun format(value: Double, separator: Char): String = plainDecimal(value).replace('.', separator)

    /** [value] without exponent notation or grouping, "-" for negatives, and no trailing ".0". */
    fun plainDecimal(value: Double): String {
        if (value == 0.0) return "0"
        if (value == floor(value) && abs(value) < MAX_PLAIN_INTEGER) return value.toLong().toString()
        val raw = value.toString()
        val exponentAt = raw.indexOfFirst { it == 'E' || it == 'e' }
        if (exponentAt < 0) return raw.removeSuffix(".0")
        val negative = raw.startsWith('-')
        val mantissa = raw.substring(if (negative) 1 else 0, exponentAt)
        val exponent = raw.substring(exponentAt + 1).removePrefix("+").toInt()
        val pointAt = mantissa.indexOf('.').takeIf { it >= 0 } ?: mantissa.length
        val digits = mantissa.replace(".", "")
        val newPointAt = pointAt + exponent
        val expanded = when {
            newPointAt <= 0 -> "0." + "0".repeat(-newPointAt) + digits
            newPointAt >= digits.length -> digits + "0".repeat(newPointAt - digits.length)
            else -> digits.substring(0, newPointAt) + "." + digits.substring(newPointAt)
        }
        val trimmed = if ('.' in expanded) expanded.trimEnd('0').removeSuffix(".") else expanded
        val normalized = trimmed.trimStart('0').let { if (it.isEmpty() || it.startsWith('.')) "0$it" else it }
        return if (negative) "-$normalized" else normalized
    }
}

/** The decimal separator of the current number-format locale. */
internal expect fun platformDecimalSeparator(): Char

/** [date] as a medium-length date in the current locale, for example "Nov 17, 2026". */
internal expect fun formatQuestionnaireDate(date: LocalDate): String
