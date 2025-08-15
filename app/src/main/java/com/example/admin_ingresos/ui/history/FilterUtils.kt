package com.example.admin_ingresos.ui.history

import java.util.Calendar

enum class DateRangePreset(val displayName: String) {
    TODAY("Hoy"),
    THIS_WEEK("Esta semana"),
    THIS_MONTH("Este mes"),
    LAST_MONTH("Mes pasado"),
    LAST_30_DAYS("Últimos 30 días"),
    LAST_90_DAYS("Últimos 90 días"),
    CUSTOM("Personalizado")
}

fun DateRange.Companion.today(): DateRange {
    val calendar = Calendar.getInstance()
    val start = calendar.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
    val end = calendar.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
    return DateRange(start, end)
}

fun DateRange.Companion.thisWeek(): DateRange {
    val calendar = Calendar.getInstance()
    calendar.firstDayOfWeek = Calendar.MONDAY
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    val start = calendar.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
    calendar.add(Calendar.DATE, 6)
    val end = calendar.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
    return DateRange(start, end)
}

fun DateRange.Companion.thisMonth(): DateRange {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val start = calendar.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
    calendar.add(Calendar.MONTH, 1)
    calendar.add(Calendar.DATE, -1)
    val end = calendar.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
    return DateRange(start, end)
}

fun DateRange.Companion.lastMonth(): DateRange {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, -1)
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val start = calendar.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
    calendar.add(Calendar.MONTH, 1)
    calendar.add(Calendar.DATE, -1)
    val end = calendar.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
    return DateRange(start, end)
}

fun DateRange.Companion.last30Days(): DateRange {
    val calendar = Calendar.getInstance()
    val end = calendar.timeInMillis
    calendar.add(Calendar.DATE, -30)
    val start = calendar.timeInMillis
    return DateRange(start, end)
}

fun DateRange.Companion.last90Days(): DateRange {
    val calendar = Calendar.getInstance()
    val end = calendar.timeInMillis
    calendar.add(Calendar.DATE, -90)
    val start = calendar.timeInMillis
    return DateRange(start, end)
}
