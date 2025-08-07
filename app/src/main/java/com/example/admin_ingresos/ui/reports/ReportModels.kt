package com.example.admin_ingresos.ui.reports

import java.util.Date

/**
 * Enum para representar diferentes períodos de reporte
 */
enum class ReportPeriod(val label: String) {
    THIS_WEEK("Esta semana"),
    THIS_MONTH("Este mes"),
    LAST_MONTH("Mes pasado"),
    THIS_YEAR("Este año"),
    LAST_YEAR("Año pasado"),
    CUSTOM("Personalizado")
}

/**
 * Enum para representar diferentes formatos de exportación
 */
enum class ExportFormat(val label: String, val extension: String) {
    PDF("PDF", "pdf"),
    CSV("CSV", "csv"),
    EXCEL("Excel", "xlsx")
}

/**
 * Clase para representar un rango de fechas
 */
data class DateRange(
    val startDate: Date,
    val endDate: Date
)

/**
 * Configuración de filtros para reportes
 */
data class ReportFilter(
    val period: ReportPeriod = ReportPeriod.THIS_MONTH,
    val dateRange: DateRange? = null,
    val categories: List<String> = emptyList(),
    val includeIncome: Boolean = true,
    val includeExpenses: Boolean = true
)

/**
 * Datos para gráficos de categorías
 */
data class CategoryChartData(
    val name: String,
    val value: Double,
    val percentage: Float,
    val color: androidx.compose.ui.graphics.Color
)

/**
 * Datos para gráficos de tendencias temporales
 */
data class TimeSeriesData(
    val date: Date,
    val income: Double,
    val expenses: Double,
    val balance: Double
)

/**
 * Resumen de métricas financieras
 */
data class FinancialSummary(
    val totalIncome: Double,
    val totalExpenses: Double,
    val netBalance: Double,
    val savingsRate: Double,
    val averageDailyExpenses: Double,
    val largestExpense: Double,
    val mostUsedCategory: String
)
