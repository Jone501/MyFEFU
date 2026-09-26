package ru.jone501.myfefu.utils

import android.content.Context
import java.util.Locale

fun swapIfRu(first: Any, second: Any, locale: Locale?, delimiter: String = " "): String {
    return when (locale?.country?.lowercase()) {
        "ru" -> "$second$delimiter$first"
        else -> "$first$delimiter$second"
    }
}

fun Context.getMainLocale(): Locale? = resources.configuration.getLocales().get(0)