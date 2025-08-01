package com.example.admin_ingresos.data

import java.util.*

data class TransactionFilter(
    val dateRange: DateRange? = null,
    val categories: List<Int> = emptyList(),
    val paymentMethods: List<Int> = emptyList(),
    val transactionTypes: List<String> = emptyList(),
    val amountRange: AmountRange? = null,
    val searchQuery: String = ""
) {
    fun isEmpty(): Boolean {
        return dateRange == null &&
                categories.isEmpty() &&
                paymentMethods.isEmpty() &&
                transactionTypes.isEmpty() &&
                amountRange == null &&
                searchQuery.isBlank()
    }
    
    fun getActiveFilterCount(): Int {
        var count = 0
        if (dateRange != null) count++
        if (categories.isNotEmpty()) count++
        if (paymentMethods.isNotEmpty()) count++
        if (transactionTypes.isNotEmpty()) count++
        if (amountRange != null) count++
        if (searchQuery.isNotBlank()) count++
        return count
    }
}

data class DateRange(
    val startDate: Long,
    val endDate: Long
) {
    companion object {
        fun today(): DateRange {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endOfDay = calendar.timeInMillis
            
            return DateRange(startOfDay, endOfDay)
        }
        
        fun thisWeek(): DateRange {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfWeek = calendar.timeInMillis
            
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            calendar.add(Calendar.MILLISECOND, -1)
            val endOfWeek = calendar.timeInMillis
            
            return DateRange(startOfWeek, endOfWeek)
        }
        
        fun thisMonth(): DateRange {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfMonth = calendar.timeInMillis
            
            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.MILLISECOND, -1)
            val endOfMonth = calendar.timeInMillis
            
            return DateRange(startOfMonth, endOfMonth)
        }
        
        fun lastMonth(): DateRange {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfLastMonth = calendar.timeInMillis
            
            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.MILLISECOND, -1)
            val endOfLastMonth = calendar.timeInMillis
            
            return DateRange(startOfLastMonth, endOfLastMonth)
        }
        
        fun last30Days(): DateRange {
            val endDate = System.currentTimeMillis()
            val startDate = endDate - (30 * 24 * 60 * 60 * 1000L)
            return DateRange(startDate, endDate)
        }
        
        fun last90Days(): DateRange {
            val endDate = System.currentTimeMillis()
            val startDate = endDate - (90 * 24 * 60 * 60 * 1000L)
            return DateRange(startDate, endDate)
        }
    }
}

data class AmountRange(
    val minAmount: Double,
    val maxAmount: Double
)

enum class DateRangePreset(val displayName: String) {
    TODAY("Hoy"),
    THIS_WEEK("Esta semana"),
    THIS_MONTH("Este mes"),
    LAST_MONTH("Mes pasado"),
    LAST_30_DAYS("Últimos 30 días"),
    LAST_90_DAYS("Últimos 90 días"),
    CUSTOM("Personalizado")
}

enum class SortOption(val displayName: String) {
    DATE_DESC("Fecha (más reciente)"),
    DATE_ASC("Fecha (más antigua)"),
    AMOUNT_DESC("Monto (mayor)"),
    AMOUNT_ASC("Monto (menor)"),
    CATEGORY("Categoría"),
    DESCRIPTION("Descripción")
}

data class FilterPreset(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val filter: TransactionFilter,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun getDefaultPresets(): List<FilterPreset> {
            return listOf(
                FilterPreset(
                    id = "today",
                    name = "Hoy",
                    filter = TransactionFilter(dateRange = DateRange.today())
                ),
                FilterPreset(
                    id = "this_week",
                    name = "Esta semana",
                    filter = TransactionFilter(dateRange = DateRange.thisWeek())
                ),
                FilterPreset(
                    id = "this_month",
                    name = "Este mes",
                    filter = TransactionFilter(dateRange = DateRange.thisMonth())
                ),
                FilterPreset(
                    id = "expenses_only",
                    name = "Solo gastos",
                    filter = TransactionFilter(transactionTypes = listOf("Gasto"))
                ),
                FilterPreset(
                    id = "income_only",
                    name = "Solo ingresos",
                    filter = TransactionFilter(transactionTypes = listOf("Ingreso"))
                ),
                FilterPreset(
                    id = "high_amounts",
                    name = "Montos altos (>$100)",
                    filter = TransactionFilter(amountRange = AmountRange(100.0, Double.MAX_VALUE))
                )
            )
        }
    }
}