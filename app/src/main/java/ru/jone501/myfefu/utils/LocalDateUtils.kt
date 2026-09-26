package ru.jone501.myfefu.utils

import android.content.Context
import ru.jone501.myfefu.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.util.Locale


fun LocalDate.academicYearStart(): Int {
    return if (month < Month.SEPTEMBER)
        year - 1
    else year
}

fun LocalDate.academicWeekNumber(): Int? {
    var current = this
    var number = 0
    var weekDays: List<LocalDate>
    do {
        weekDays = current.getWeekDays()
        if (++number >= 48)
            return null
        current = current.minusWeeks(1L)
    } while (weekDays.count {
            it.month == Month.SEPTEMBER && it.dayOfMonth == 14
        } == 0)
    return number
}

fun LocalDate.getWeekDays(): List<LocalDate> {
    val startOfWeek = this.getStartOfWeek()
    return (0L..6L).map {
        startOfWeek.plusDays(it)
    }
}

fun LocalDate.getStartOfWeek(): LocalDate {
    return this.minusDays(this.dayOfWeek.value - 1L)
}

fun LocalDate.toStringWithMonth(context: Context): String {
    val locale: Locale? = context.resources.configuration.getLocales().get(0)
    return swapIfRu(month.toLocalRodPadejString(context), dayOfMonth, locale)
}

fun Month.toLocalString(context: Context): String {
    return when (this.value) {
        1 -> context.getString(R.string.january)
        2 -> context.getString(R.string.february)
        3 -> context.getString(R.string.march)
        4 -> context.getString(R.string.april)
        5 -> context.getString(R.string.may)
        6 -> context.getString(R.string.june)
        7 -> context.getString(R.string.july)
        8 -> context.getString(R.string.august)
        9 -> context.getString(R.string.september)
        10 -> context.getString(R.string.october)
        11 -> context.getString(R.string.november)
        else -> context.getString(R.string.december)
    }
}

fun Month.toLocalRodPadejString(context: Context): String {
    if (context.getMainLocale()?.country?.lowercase() == "ru") {
        return when (this.value) {
            1 -> context.getString(R.string.january).replace("ь", "я")
            2 -> context.getString(R.string.february).replace("ь", "я")
            3 -> context.getString(R.string.march) + "а"
            4 -> context.getString(R.string.april).replace("ь", "я")
            5 -> context.getString(R.string.may).replace("й", "я")
            6 -> context.getString(R.string.june).replace("ь", "я")
            7 -> context.getString(R.string.july).replace("ь", "я")
            8 -> context.getString(R.string.august) + "а"
            9 -> context.getString(R.string.september).replace("ь", "я")
            10 -> context.getString(R.string.october).replace("ь", "я")
            11 -> context.getString(R.string.november).replace("ь", "я")
            else -> context.getString(R.string.december).replace("ь", "я")
        }
    }
    return toLocalString(context)
}

fun LocalDate.isNationalHoliday(): Boolean {
    return listOf(
        1 to 1, 2 to 1, 3 to 1, 4 to 1,
        5 to 1, 6 to 1, 7 to 1, 8 to 1,
        23 to 2, 8 to 3, 1 to 5, 9 to 5,
        12 to 6, 4 to 11, 31 to 12,
    ).contains(dayOfMonth to month.value)
}

fun DayOfWeek.abbreviated(context: Context): String {
    return when (value) {
        1 -> context.getString(R.string.monday_abbreviated)
        2 -> context.getString(R.string.tuesday_abbreviated)
        3 -> context.getString(R.string.wednesday_abbreviated)
        4 -> context.getString(R.string.thursday_abbreviated)
        5 -> context.getString(R.string.friday_abbreviated)
        6 -> context.getString(R.string.saturday_abbreviated)
        else -> context.getString(R.string.sunday_abbreviated)
    }
}