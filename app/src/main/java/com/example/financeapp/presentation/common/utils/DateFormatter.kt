package com.example.financeapp.presentation.common.utils

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun LocalDate.formatDayMonth(locale: Locale = Locale.getDefault()): String {
    return format(DateTimeFormatter.ofPattern("d MMMM", locale))
}

fun LocalTime.formatHourMinute(): String {
    return format(HourMinuteFormatter)
}

private val HourMinuteFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
