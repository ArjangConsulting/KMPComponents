package io.github.maniramezan.kmpcomponents

import kotlinx.datetime.LocalDate
import java.text.DecimalFormatSymbols
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

internal actual fun platformDecimalSeparator(): Char =
    DecimalFormatSymbols.getInstance(Locale.getDefault(Locale.Category.FORMAT)).decimalSeparator

internal actual fun formatQuestionnaireDate(date: LocalDate): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(Locale.getDefault(Locale.Category.FORMAT))
        .format(java.time.LocalDate.of(date.year, date.month.ordinal + 1, date.day))
