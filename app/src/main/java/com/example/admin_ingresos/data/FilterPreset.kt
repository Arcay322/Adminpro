package com.example.admin_ingresos.data

data class FilterPreset(
    val id: String,
    val name: String,
    val filter: TransactionFilter
)

// Extension function to create default presets
fun List<FilterPreset>.getDefaultPresets(): List<FilterPreset> {
    return listOf(
        FilterPreset(
            id = "all",
            name = "Todas",
            filter = TransactionFilter()
        ),
        FilterPreset(
            id = "income",
            name = "Ingresos",
            filter = TransactionFilter(type = "Ingreso")
        ),
        FilterPreset(
            id = "expenses",
            name = "Gastos",
            filter = TransactionFilter(type = "Gasto")
        ),
        FilterPreset(
            id = "this_month",
            name = "Este mes",
            filter = TransactionFilter(
                startDate = getStartOfMonth(),
                endDate = getEndOfMonth()
            )
        )
    )
}

private fun getStartOfMonth(): Long {
    val calendar = java.util.Calendar.getInstance()
    calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
    calendar.set(java.util.Calendar.MINUTE, 0)
    calendar.set(java.util.Calendar.SECOND, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

private fun getEndOfMonth(): Long {
    val calendar = java.util.Calendar.getInstance()
    calendar.set(java.util.Calendar.DAY_OF_MONTH, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
    calendar.set(java.util.Calendar.MINUTE, 59)
    calendar.set(java.util.Calendar.SECOND, 59)
    calendar.set(java.util.Calendar.MILLISECOND, 999)
    return calendar.timeInMillis
}