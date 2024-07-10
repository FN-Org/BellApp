package it.fnorg.bellapp.calendar_activity

import java.time.DayOfWeek
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Returns the textual representation of the YearMonth.
 *
 * @param short Whether the month name should be in short form. Default is false.
 * @return A string representing the YearMonth, formatted as "Month Year".
 */
fun YearMonth.displayText(short: Boolean = false): String {
    return "${this.month.displayText(short = short)} ${this.year}"
}

/**
 * Returns the textual representation of the Month.
 *
 * @param short Whether the month name should be in short form. Default is true.
 * @return A string representing the month name.
 */
fun Month.displayText(short: Boolean = true): String {
    val style = if (short) TextStyle.SHORT else TextStyle.FULL
    return getDisplayName(style, Locale.ENGLISH)
}

/**
 * Returns the textual representation of the DayOfWeek.
 *
 * @param uppercase Whether the day name should be in uppercase. Default is false.
 * @return A string representing the day name.
 */
fun DayOfWeek.displayText(uppercase: Boolean = false): String {
    return getDisplayName(TextStyle.SHORT, Locale.ENGLISH).let { value ->
        if (uppercase) value.uppercase(Locale.ENGLISH) else value
    }
}
