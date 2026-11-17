@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package io.github.maniramezan.kmpcomponents

import kotlinx.datetime.LocalDate
import kotlin.js.js

internal actual fun platformDecimalSeparator(): Char = browserDecimalSeparator().firstOrNull() ?: '.'

internal actual fun formatQuestionnaireDate(date: LocalDate): String =
    browserMediumDate(date.year, date.month.ordinal, date.day)

private fun browserDecimalSeparator(): String = js(
    """(new Intl.NumberFormat().formatToParts(1.5).find((part) => part.type === 'decimal') || { value: '.' }).value""",
)

// The day is built and formatted in UTC so the browser's own time zone cannot shift it.
private fun browserMediumDate(year: Int, monthIndex: Int, day: Int): String = js(
    """{
        const instant = new Date(0);
        instant.setUTCFullYear(year, monthIndex, day);
        return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeZone: 'UTC' }).format(instant);
    }""",
)
