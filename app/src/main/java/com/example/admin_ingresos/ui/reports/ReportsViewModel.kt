package com.example.admin_ingresos.ui.reports

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.net.Uri
import com.example.admin_ingresos.data.AppDatabase
// Budget types removed from this ViewModel per cleanup
import com.example.admin_ingresos.data.Category
import com.example.admin_ingresos.data.Transaction
import com.example.admin_ingresos.ui.history.DateRange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    val totalTransfers: Double = 0.0,
    val netSavings: Double = 0.0,
    val expenseByCategory: List<CategoryExpenseShare> = emptyList(),
    val incomeByCategory: List<CategoryExpenseShare> = emptyList(),
    val savingsByCategory: List<CategoryExpenseShare> = emptyList(),
    val incomeVsExpenseTrend: List<TrendDataPoint> = emptyList(),
    val transfersGrowth: List<SavingsPoint> = emptyList(),
    // budgetVsActual removed per cleanup request
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
    val expense: Double,
    val transfers: Double = 0.0
)

data class SavingsPoint(
    val timestamp: Long,
    val amount: Double
)

// BudgetComparison and related budget types removed per request

enum class DateRangePreset(val displayName: String) {
    TODAY("Hoy"),
    THIS_WEEK("Esta Semana"),
    THIS_MONTH("Este Mes"),
    LAST_3_MONTHS("Últimos 3 Meses"),
    THIS_YEAR("Este Año"),
    CUSTOM("Personalizado")
}

// Estado simple para exportación
sealed interface ExportStatus {
    object Idle : ExportStatus
    object Loading : ExportStatus
    data class Success(val uri: Uri) : ExportStatus
    data class Error(val message: String) : ExportStatus
}

class ReportsViewModel(private val db: AppDatabase) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private val transactionDao = db.transactionDao()
    private val categoryDao = db.categoryDao()
    private val paymentDao = db.paymentMethodDao()
    private val exportRecordDao = db.exportRecordDao()

    // Filters state
    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId.asStateFlow()

    private val _selectedPaymentMethodId = MutableStateFlow<Int?>(null)
    val selectedPaymentMethodId: StateFlow<Int?> = _selectedPaymentMethodId.asStateFlow()

    private val _minAmount = MutableStateFlow<Double?>(null)
    private val _maxAmount = MutableStateFlow<Double?>(null)

    // Recent exports
    private val _recentExports = MutableStateFlow<List<com.example.admin_ingresos.data.ExportRecord>>(emptyList())
    val recentExports = _recentExports.asStateFlow()

    init {
        setDateRange(DateRangePreset.THIS_MONTH)
    }

    // Export state
    private val _exportState = MutableStateFlow<ExportStatus>(ExportStatus.Idle)
    val exportState: StateFlow<ExportStatus> = _exportState.asStateFlow()

    fun clearExportState() {
        _exportState.value = ExportStatus.Idle
    }

    fun exportTransactionsCsv(context: Context) {
        viewModelScope.launch {
            _exportState.value = ExportStatus.Loading
            val range = _uiState.value.selectedDateRange
            if (range == null) {
                _exportState.value = ExportStatus.Error("Rango de fechas no seleccionado")
                return@launch
            }
            try {
                val transactions = withContext(Dispatchers.IO) { transactionDao.getTransactionsByDateRange(range.startDate, range.endDate) }
                val categories = withContext(Dispatchers.IO) { categoryDao.getCategoriesList() }
                val paymentMethods = withContext(Dispatchers.IO) { paymentDao.getAll() }
                val uri = withContext(Dispatchers.IO) {
                    com.example.admin_ingresos.data.ExportService(context).exportTransactionsToCSV(
                        transactions,
                        categories,
                        paymentMethods
                    )
                }
                if (uri != null) {
                    _exportState.value = ExportStatus.Success(uri)
                } else {
                    _exportState.value = ExportStatus.Error("Error al generar CSV")
                }
            } catch (e: Exception) {
                _exportState.value = ExportStatus.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun exportTransactionsPdf(context: Context) {
        viewModelScope.launch {
            _exportState.value = ExportStatus.Loading
            val range = _uiState.value.selectedDateRange
            if (range == null) {
                _exportState.value = ExportStatus.Error("Rango de fechas no seleccionado")
                return@launch
            }
            try {
                val transactions = withContext(Dispatchers.IO) { transactionDao.getTransactionsByDateRange(range.startDate, range.endDate) }
                val categories = withContext(Dispatchers.IO) { categoryDao.getCategoriesList() }
                val paymentMethods = withContext(Dispatchers.IO) { paymentDao.getAll() }
                val uri = withContext(Dispatchers.IO) {
                    com.example.admin_ingresos.data.ExportService(context).generateTransactionsPDFReport(
                        transactions,
                        categories,
                        paymentMethods
                    )
                }
                if (uri != null) {
                    _exportState.value = ExportStatus.Success(uri)
                } else {
                    _exportState.value = ExportStatus.Error("Error al generar PDF")
                }
            } catch (e: Exception) {
                _exportState.value = ExportStatus.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun shareTextSummary(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val range = _uiState.value.selectedDateRange ?: return@launch
            try {
                val transactions = transactionDao.getTransactionsByDateRange(range.startDate, range.endDate)
                val categories = categoryDao.getCategoriesList()
                withContext(Dispatchers.Main) {
                    com.example.admin_ingresos.data.ExportService(context).shareTextSummary(transactions, categories)
                }
            } catch (e: Exception) {
                // no-op: sharing failure handled by Android chooser
            }
        }
    }

    // Helpers used by UI to fetch data for direct exports
    suspend fun getAllTransactionsForCurrentRange(): List<Transaction> {
        val range = _uiState.value.selectedDateRange ?: return emptyList()
        return transactionDao.getTransactionsByDateRange(range.startDate, range.endDate)
    }

    suspend fun getCategories(): List<Category> {
        return categoryDao.getCategoriesList()
    }

    suspend fun getPaymentMethods(): List<com.example.admin_ingresos.data.PaymentMethod> {
        return paymentDao.getAll()
    }

    // Filter helpers
    fun setCategoryFilter(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
        loadReportData()
    }

    fun setPaymentMethodFilter(paymentMethodId: Int?) {
        _selectedPaymentMethodId.value = paymentMethodId
        loadReportData()
    }

    fun setAmountRange(min: Double?, max: Double?) {
        _minAmount.value = min
        _maxAmount.value = max
        loadReportData()
    }

    // Delete an export record by id
    fun deleteExportRecord(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                exportRecordDao.deleteById(id)
                loadRecentExports()
            } catch (_: Exception) {}
        }
    }

    // Drill-down: get transactions for a given category or date
    suspend fun getTransactionsForCategory(categoryId: Int, range: DateRange): List<Transaction> {
        val all = transactionDao.getTransactionsByDateRange(range.startDate, range.endDate)
        return all.filter { it.categoryId == categoryId }
    }

    // Compare two periods and return pair of ReportData (A,B)
    suspend fun comparePeriods(rangeA: DateRange, rangeB: DateRange): Pair<ReportData, ReportData> = withContext(Dispatchers.IO) {
        val txA = transactionDao.getTransactionsByDateRange(rangeA.startDate, rangeA.endDate)
        val txB = transactionDao.getTransactionsByDateRange(rangeB.startDate, rangeB.endDate)

        val reportA = buildReportFromTransactions(txA)
        val reportB = buildReportFromTransactions(txB)
        Pair(reportA, reportB)
    }

    private suspend fun buildReportFromTransactions(transactions: List<Transaction>): ReportData {
    val totalIncome = transactions.filter { it.type == com.example.admin_ingresos.data.Transaction.TYPE_INCOME }.sumOf { it.amount }
    val totalExpenses = transactions.filter { it.type == com.example.admin_ingresos.data.Transaction.TYPE_EXPENSE }.sumOf { it.amount }
    val totalTransfers = transactions.filter { it.type == com.example.admin_ingresos.data.Transaction.TYPE_TRANSFER }.sumOf { it.amount }
    val expenseByCategory = calculateExpenseByCategory(transactions.filter { it.type == com.example.admin_ingresos.data.Transaction.TYPE_EXPENSE })
    val incomeByCategory = calculateIncomeByCategory(transactions.filter { it.type == com.example.admin_ingresos.data.Transaction.TYPE_INCOME })
    val savingsByCategory = calculateSavingsByCategory(transactions.filter { it.type == com.example.admin_ingresos.data.Transaction.TYPE_TRANSFER })
    val incomeVsExpenseTrend = calculateIncomeVsExpenseTrend(transactions)
    val transfersGrowth = calculateCumulativeTransfers(transactions)

    return ReportData(
        totalIncome = totalIncome,
        totalExpenses = totalExpenses,
        totalTransfers = totalTransfers,
        netSavings = totalIncome - totalExpenses - totalTransfers,
        expenseByCategory = expenseByCategory,
        incomeByCategory = incomeByCategory,
            savingsByCategory = savingsByCategory,
        incomeVsExpenseTrend = incomeVsExpenseTrend,
        transfersGrowth = transfersGrowth
    )
    }

    // Public helper to get a DateRange for a preset (UI can call this)
    fun getRangeForPreset(preset: DateRangePreset): DateRange? {
        return when (preset) {
            DateRangePreset.TODAY -> getTodayRange()
            DateRangePreset.THIS_WEEK -> getThisWeekRange()
            DateRangePreset.THIS_MONTH -> getThisMonthRange()
            DateRangePreset.LAST_3_MONTHS -> getLastNMonthsRange(3)
            DateRangePreset.THIS_YEAR -> getThisYearRange()
            DateRangePreset.CUSTOM -> null
        }
    }

    // Load recent exports
    fun loadRecentExports() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = exportRecordDao.getRecentExports()
                _recentExports.value = list
            } catch (_: Exception) {
                _recentExports.value = emptyList()
            }
        }
    }

    /**
     * Refresh top-level UI data: report payload and recent exports.
     * Useful after seeding data or external changes.
     */
    fun reloadAll() {
        viewModelScope.launch {
            // reload exports and report data; each function handles its own coroutine/dispatcher
            loadRecentExports()
            loadReportData()
        }
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
                // Apply filters: category, payment method, min/max amount
                val minAmount = _minAmount.value
                val maxAmount = _maxAmount.value
                val categoriesFilter = _selectedCategoryId.value?.let { listOf(it) }
                val paymentFilter = _selectedPaymentMethodId.value?.let { listOf(it) }

                val transactions = transactionDao.getFilteredTransactions(
                    searchQuery = "",
                    startDate = range.startDate,
                    endDate = range.endDate,
                    categories = categoriesFilter,
                    paymentMethods = paymentFilter,
                    transactionTypes = null,
                    minAmount = minAmount,
                    maxAmount = maxAmount,
                    sortBy = "DATE_DESC"
                )

                val totalIncome = transactions.filter { it.type == com.example.admin_ingresos.data.Transaction.TYPE_INCOME }.sumOf { it.amount }
                val totalExpenses = transactions.filter { it.type == com.example.admin_ingresos.data.Transaction.TYPE_EXPENSE }.sumOf { it.amount }
                val totalTransfers = transactions.filter { it.type == com.example.admin_ingresos.data.Transaction.TYPE_TRANSFER }.sumOf { it.amount }

                val expenseByCategory = calculateExpenseByCategory(transactions.filter { it.type == com.example.admin_ingresos.data.Transaction.TYPE_EXPENSE })
                val incomeByCategory = calculateIncomeByCategory(transactions.filter { it.type == com.example.admin_ingresos.data.Transaction.TYPE_INCOME })
                val savingsByCategory = calculateSavingsByCategory(transactions.filter { it.type == com.example.admin_ingresos.data.Transaction.TYPE_TRANSFER })
                val incomeVsExpenseTrend = calculateIncomeVsExpenseTrend(transactions)

                val transfersGrowth = calculateCumulativeTransfers(transactions)

                val newReportData = ReportData(
                    totalIncome = totalIncome,
                    totalExpenses = totalExpenses,
                    totalTransfers = totalTransfers,
                    netSavings = totalIncome - totalExpenses - totalTransfers,
                    expenseByCategory = expenseByCategory,
                    incomeByCategory = incomeByCategory,
                    savingsByCategory = savingsByCategory,
                    incomeVsExpenseTrend = incomeVsExpenseTrend,
                    transfersGrowth = transfersGrowth
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

    private suspend fun calculateIncomeByCategory(incomes: List<Transaction>): List<CategoryExpenseShare> {
        val totalIncome = incomes.sumOf { it.amount }
        if (totalIncome == 0.0) return emptyList()

        return incomes
            .groupBy { it.categoryId }
            .map { (categoryId, transactions) ->
                val category = categoryDao.getCategoryById(categoryId) ?: Category.uncategorized()
                val categoryTotal = transactions.sumOf { it.amount }
                CategoryExpenseShare(
                    category = category,
                    amount = categoryTotal,
                    percentage = (categoryTotal / totalIncome).toFloat(),
                    color = Color(android.graphics.Color.parseColor(category.color))
                )
            }
            .sortedByDescending { it.amount }
    }

    private suspend fun calculateSavingsByCategory(transfers: List<Transaction>): List<CategoryExpenseShare> {
        // Use exactly the categories that appear in the Categories -> Ahorros section
        val ahorroCategories = categoryDao.getCategoriesList().filter { it.type == com.example.admin_ingresos.data.CategoryType.AHORRO }
        val ahorroCategoryIds = ahorroCategories.map { it.id }.toSet()

        // Only consider transfers that are explicitly assigned to an AHORRO category
        val filtered = transfers.filter { it.categoryId in ahorroCategoryIds }
        val totalTransfers = filtered.sumOf { it.amount }
        if (totalTransfers == 0.0) return emptyList()

        return filtered
            .groupBy { it.categoryId }
            .map { (categoryId, transactions) ->
                val category = categoryDao.getCategoryById(categoryId) ?: Category.uncategorized()
                val categoryTotal = transactions.sumOf { it.amount }
                CategoryExpenseShare(
                    category = category,
                    amount = categoryTotal,
                    percentage = (categoryTotal / totalTransfers).toFloat(),
                    color = try { Color(android.graphics.Color.parseColor(category.color)) } catch (_: Exception) { Color.Unspecified }
                )
            }
            .sortedByDescending { it.amount }
    }

    private fun calculateIncomeVsExpenseTrend(transactions: List<Transaction>): List<TrendDataPoint> {
        if (transactions.isEmpty()) return emptyList()

        return transactions
            .groupBy { getStartOfDay(it.date) } // Group by day
            .map { (day, dayTransactions) ->
                val income = dayTransactions.filter { it.type == com.example.admin_ingresos.data.Transaction.TYPE_INCOME }.sumOf { it.amount }
                val expense = dayTransactions.filter { it.type == com.example.admin_ingresos.data.Transaction.TYPE_EXPENSE }.sumOf { it.amount }
                val transfers = dayTransactions.filter { it.type == com.example.admin_ingresos.data.Transaction.TYPE_TRANSFER }.sumOf { it.amount }
                TrendDataPoint(timestamp = day, income = income, expense = expense, transfers = transfers)
            }
            .sortedBy { it.timestamp }
    }

    // proratedAmountForRange removed; budget calculations cleaned up

    // calculateBudgetVsActual removed per cleanup request

    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun calculateCumulativeTransfers(transactions: List<Transaction>): List<SavingsPoint> {
        if (transactions.isEmpty()) return emptyList()

        // Group transfers by day, sum, then compute running total ordered by day
        val transfersByDay = transactions
            .filter { it.type == com.example.admin_ingresos.data.Transaction.TYPE_TRANSFER }
            .groupBy { getStartOfDay(it.date) }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val days = transfersByDay.keys.sorted()
        val result = mutableListOf<SavingsPoint>()
        var running = 0.0
        for (day in days) {
            running += transfersByDay[day] ?: 0.0
            result.add(SavingsPoint(timestamp = day, amount = running))
        }

        return result
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