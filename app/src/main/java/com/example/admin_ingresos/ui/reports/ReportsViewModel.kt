package com.example.admin_ingresos.ui.reports

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.admin_ingresos.data.AppDatabase
import com.example.admin_ingresos.data.Budget
import com.example.admin_ingresos.data.Category
import com.example.admin_ingresos.data.Transaction
import com.example.admin_ingresos.ui.history.DateRange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

// --- Data Classes for UI State ---

data class ReportsUiState(
    val isLoading: Boolean = true,
    val selectedDateRange: DateRange? = null,
    val dateRangePreset: DateRangePreset = DateRangePreset.THIS_MONTH,
    val reportData: ReportData = ReportData(),
    val error: String? = null
)

data class ReportData(
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netSavings: Double = 0.0,
    val expenseByCategory: List<CategoryExpenseShare> = emptyList(),
    val incomeVsExpenseTrend: List<TrendDataPoint> = emptyList(),
    val budgetVsActual: List<BudgetComparison> = emptyList()
)

data class CategoryExpenseShare(
    val category: Category,
    val amount: Double,
    val percentage: Float,
    val color: Color
)

data class TrendDataPoint(
    val timestamp: Long,
    val income: Double,
    val expense: Double
)

data class BudgetComparison(
    val budget: Budget,
    val category: Category,
    val actualAmount: Double,
    val progress: Float
)

enum class DateRangePreset(val displayName: String) {
    TODAY("Hoy"),
    THIS_WEEK("Esta Semana"),
    THIS_MONTH("Este Mes"),
    LAST_3_MONTHS("Últimos 3 Meses"),
    THIS_YEAR("Este Año"),
    CUSTOM("Personalizado")
}

class ReportsViewModel(private val db: AppDatabase) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private val transactionDao = db.transactionDao()
    private val budgetDao = db.budgetDao()
    private val categoryDao = db.categoryDao()

    init {
        setDateRange(DateRangePreset.THIS_MONTH)
    }

    fun setDateRange(preset: DateRangePreset, customRange: DateRange? = null) {
        val range = when (preset) {
            DateRangePreset.TODAY -> getTodayRange()
            DateRangePreset.THIS_WEEK -> getThisWeekRange()
            DateRangePreset.THIS_MONTH -> getThisMonthRange()
            DateRangePreset.LAST_3_MONTHS -> getLastNMonthsRange(3)
            DateRangePreset.THIS_YEAR -> getThisYearRange()
            DateRangePreset.CUSTOM -> customRange
        }
        _uiState.update { it.copy(dateRangePreset = preset, selectedDateRange = range) }
        loadReportData()
    }

    private fun loadReportData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val range = _uiState.value.selectedDateRange
            if (range == null) {
                _uiState.update { it.copy(isLoading = false, error = "Rango de fechas no seleccionado") }
                return@launch
            }
            try {
                val transactions = transactionDao.getTransactionsByDateRange(range.startDate, range.endDate)
                val budgets = budgetDao.getAllBudgets().first()

                val totalIncome = transactions.filter { it.type == "Ingreso" }.sumOf { it.amount }
                val totalExpenses = transactions.filter { it.type == "Gasto" }.sumOf { it.amount }

                val expenseByCategory = calculateExpenseByCategory(transactions.filter { it.type == "Gasto" })
                val incomeVsExpenseTrend = calculateIncomeVsExpenseTrend(transactions)
                val budgetVsActual = calculateBudgetVsActual(transactions.filter { it.type == "Gasto" }, budgets)

                val newReportData = ReportData(
                    totalIncome = totalIncome,
                    totalExpenses = totalExpenses,
                    netSavings = totalIncome - totalExpenses,
                    expenseByCategory = expenseByCategory,
                    incomeVsExpenseTrend = incomeVsExpenseTrend,
                    budgetVsActual = budgetVsActual
                )
                _uiState.update { it.copy(isLoading = false, reportData = newReportData) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error al cargar los datos del reporte: ${e.message}") }
            }
        }
    }

    private suspend fun calculateExpenseByCategory(expenses: List<Transaction>): List<CategoryExpenseShare> {
        val totalExpenses = expenses.sumOf { it.amount }
        if (totalExpenses == 0.0) return emptyList()

        return expenses
            .groupBy { it.categoryId }
            .map { (categoryId, transactions) ->
                val category = categoryDao.getCategoryById(categoryId) ?: Category.uncategorized()
                val categoryTotal = transactions.sumOf { it.amount }
                CategoryExpenseShare(
                    category = category,
                    amount = categoryTotal,
                    percentage = (categoryTotal / totalExpenses).toFloat(),
                    color = Color(android.graphics.Color.parseColor(category.color))
                )
            }
            .sortedByDescending { it.amount }
    }

    private fun calculateIncomeVsExpenseTrend(transactions: List<Transaction>): List<TrendDataPoint> {
        if (transactions.isEmpty()) return emptyList()

        return transactions
            .groupBy { getStartOfDay(it.date) } // Group by day
            .map { (day, dayTransactions) ->
                val income = dayTransactions.filter { it.type == "Ingreso" }.sumOf { it.amount }
                val expense = dayTransactions.filter { it.type == "Gasto" }.sumOf { it.amount }
                TrendDataPoint(timestamp = day, income = income, expense = expense)
            }
            .sortedBy { it.timestamp }
    }

    private suspend fun calculateBudgetVsActual(expenses: List<Transaction>, budgets: List<Budget>): List<BudgetComparison> {
        return budgets.map { budget ->
            val categoryExpenses = expenses.filter { it.categoryId == budget.categoryId }.sumOf { it.amount }
            val progress = if (budget.amount > 0) (categoryExpenses / budget.amount).toFloat() else 0f
            val category = categoryDao.getCategoryById(budget.categoryId) ?: Category.uncategorized()
            BudgetComparison(
                budget = budget,
                category = category,
                actualAmount = categoryExpenses,
                progress = progress
            )
        }
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    // --- Date Range Helpers ---
    private fun getTodayRange(): DateRange {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val end = cal.timeInMillis
        return DateRange(start, end)
    }

    private fun getThisWeekRange(): DateRange {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        return DateRange(getStartOfDay(cal), getEndOfDay(Calendar.getInstance()))
    }

    private fun getThisMonthRange(): DateRange {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return DateRange(getStartOfDay(cal), getEndOfDay(Calendar.getInstance()))
    }

    private fun getLastNMonthsRange(months: Int): DateRange {
        val endCal = Calendar.getInstance()
        val startCal = Calendar.getInstance()
        startCal.add(Calendar.MONTH, -months)
        return DateRange(getStartOfDay(startCal), getEndOfDay(endCal))
    }

    private fun getThisYearRange(): DateRange {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_YEAR, 1)
        return DateRange(getStartOfDay(cal), getEndOfDay(Calendar.getInstance()))
    }

    private fun getStartOfDay(cal: Calendar): Long {
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getEndOfDay(cal: Calendar): Long {
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }
}