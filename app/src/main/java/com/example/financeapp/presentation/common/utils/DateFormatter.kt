package com.example.financeapp.presentation.common.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun LocalDate.formatDayMonth(locale: Locale = Locale.getDefault()): String {
    return format(DateTimeFormatter.ofPattern("d MMMM", locale))
}

