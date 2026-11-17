package io.github.maniramezan.kmpcomponents

import kotlinx.datetime.LocalDate
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarIdentifierGregorian
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSLocale
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSTimeZone
import platform.Foundation.currentLocale
import platform.Foundation.timeZoneForSecondsFromGMT

internal actual fun platformDecimalSeparator(): Char =
    NSNumberFormatter().apply { locale = NSLocale.currentLocale }.decimalSeparator.firstOrNull() ?: '.'

internal actual fun formatQuestionnaireDate(date: LocalDate): String {
    val utc = NSTimeZone.timeZoneForSecondsFromGMT(0)
    val calendar = NSCalendar(calendarIdentifier = NSCalendarIdentifierGregorian).apply { timeZone = utc }
    val components = NSDateComponents().apply {
        year = date.year.toLong()
        month = (date.month.ordinal + 1).toLong()
        day = date.day.toLong()
    }
    val instant = calendar.dateFromComponents(components) ?: return date.toString()
    return NSDateFormatter().apply {
        locale = NSLocale.currentLocale
        timeZone = utc
        dateStyle = NSDateFormatterMediumStyle
        timeStyle = NSDateFormatterNoStyle
    }.stringFromDate(instant)
}
