package com.example.admin_ingresos.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.admin_ingresos.data.TransactionRepository
import com.example.admin_ingresos.data.Transaction
import com.example.admin_ingresos.data.AppDatabase
import com.example.admin_ingresos.ui.icons.LucideIconMapper
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

data class DashboardUiState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentBalance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpenses: Double = 0.0,
    val recentTransactions: List<DashboardTransaction> = emptyList(),
    val categoryExpenses: List<CategoryExpense> = emptyList()
)

data class DashboardTransaction(
    val id: Int,
    val description: String,
    val amount: Double,
    val category: String,
    val categoryColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val date: String,
    val isIncome: Boolean
)

data class CategoryExpense(
    val name: String,
    val amount: Double,
    val percentage: Float,
    val color: Color
)

class DashboardViewModel(
    private val repository: TransactionRepository,
    private val database: AppDatabase
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    // Use Flow to automatically listen to database changes
    val transactions = repository.getAllTransactionsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Also listen to category changes for instant updates
    private val categories = database.categoryDao().getAllFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList<com.example.admin_ingresos.data.Category>()
        )
    
    // TODO: Listen to savings goals changes (commented out until database migration is complete)
    /*
    val savingsGoals = database.savingsGoalDao().getAllActiveFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    */
    
    // TODO: Weekly cash flow data based on real transactions (commented out until complete)
    /*
    val weeklyData = transactions
        .map { transactionList ->
            getWeeklyFlowData(transactionList)
        }
        .stateIn(
            scope = viewModelScope, 
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    */

    init {
        // Load data when ViewModel is created
        loadDashboardData()
        
        // Listen to transaction changes
        viewModelScope.launch {
            transactions.collect { transactionList ->
                calculateDashboardMetrics(transactionList)
            }
        }
        
        // Listen to category changes to update colors and icons instantly
        viewModelScope.launch {
            categories.collect { categoryList ->
                // Recalculate metrics when categories change (for color/icon updates)
                calculateDashboardMetrics(transactions.value)
            }
        }
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val transactionsList = repository.getAllTransactions()
                _uiState.value = _uiState.value.copy(
                    transactions = transactionsList,
                    isLoading = false
                )
                calculateDashboardMetrics(transactionsList)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
            }
        }
    }
    
    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val transactionsList = repository.getAllTransactions()
                calculateDashboardMetrics(transactionsList)
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar los datos"
                )
            }
        }
    }
    
    private suspend fun calculateDashboardMetrics(transactions: List<Transaction>) {
        try {
            val currentCategories = categories.value // Usar las categorías ya observadas
            
            // Calcular balance actual
            val totalIncome = transactions.filter { it.type == "Ingreso" }.sumOf { it.amount }
            val totalExpenses = transactions.filter { it.type == "Gasto" }.sumOf { it.amount }
            val currentBalance = totalIncome - totalExpenses
            
            // Calcular ingresos y gastos del mes actual
            val currentMonth = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            
            val monthlyTransactions = transactions.filter { it.date >= currentMonth }
            val monthlyIncome = monthlyTransactions.filter { it.type == "Ingreso" }.sumOf { it.amount }
            val monthlyExpenses = monthlyTransactions.filter { it.type == "Gasto" }.sumOf { it.amount }
            
            // Obtener transacciones recientes (últimas 10)
            val recentTransactions = transactions
                .sortedByDescending { it.date }
                .take(10)
                .map { transaction ->
                    val category = currentCategories.find { it.id == transaction.categoryId }
                    DashboardTransaction(
                        id = transaction.id,
                        description = transaction.description,
                        amount = transaction.amount,
                        category = category?.name ?: "Sin categoría",
                        categoryColor = category?.let { parseColorFromCategory(it) } ?: getCategoryColor(category?.name),
                        icon = category?.let { getCategoryIconFromData(it) } ?: getCategoryIcon(category?.name),
                        date = formatDate(transaction.date),
                        isIncome = transaction.type == "Ingreso"
                    )
                }
            
            // Calcular gastos por categoría
            val expensesByCategory = monthlyTransactions
                .filter { it.type == "Gasto" }
                .groupBy { it.categoryId }
                .map { (categoryId, categoryTransactions) ->
                    val category = currentCategories.find { it.id == categoryId }
                    val totalAmount = categoryTransactions.sumOf { it.amount }
                    CategoryExpense(
                        name = category?.name ?: "Sin categoría",
                        amount = totalAmount,
                        percentage = if (monthlyExpenses > 0) ((totalAmount / monthlyExpenses) * 100).toFloat() else 0f,
                        color = category?.let { parseColorFromCategory(it) } ?: getCategoryColor(category?.name)
                    )
                }
                .sortedByDescending { it.amount }
                .take(5)
            
            _uiState.value = _uiState.value.copy(
                transactions = transactions,
                currentBalance = currentBalance,
                monthlyIncome = monthlyIncome,
                monthlyExpenses = monthlyExpenses,
                recentTransactions = recentTransactions,
                categoryExpenses = expensesByCategory
            )
            
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Error al calcular métricas: ${e.message}"
            )
        }
    }
    
    private fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val now = Date()
        val diffInMillis = now.time - date.time
        val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)
        
        return when {
            diffInDays == 0L -> "Hoy"
            diffInDays == 1L -> "Ayer"
            diffInDays < 7 -> "Hace ${diffInDays} días"
            else -> {
                val calendar = Calendar.getInstance().apply { time = date }
                "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}"
            }
        }
    }
    
    private fun parseColorFromCategory(category: com.example.admin_ingresos.data.Category): Color {
        return try {
            // Parsear el color hex almacenado en la base de datos
            val colorHex = category.color
            if (colorHex.isNotEmpty() && colorHex.startsWith("#")) {
                // Convertir hex string a Color
                val colorInt = android.graphics.Color.parseColor(colorHex)
                Color(colorInt)
            } else {
                // Fallback a colores mejorados basados en el nombre
                getCategoryColorImproved(category.name)
            }
        } catch (e: Exception) {
            // En caso de error, usar color basado en el nombre
            getCategoryColorImproved(category.name)
        }
    }
    
    private fun getCategoryColorImproved(categoryName: String?): Color {
        // Generar colores más variados y vibrantes para las categorías
        val colorPalette = listOf(
            Color(0xFFFF6B6B), // Rojo coral
            Color(0xFF4ECDC4), // Turquesa
            Color(0xFFFFBE0B), // Amarillo dorado
            Color(0xFF8B5CF6), // Púrpura
            Color(0xFFFF7A00), // Naranja
            Color(0xFF10B981), // Verde esmeralda
            Color(0xFF06B6D4), // Cian
            Color(0xFFF59E0B), // Ámbar
            Color(0xFFE11D48), // Rosa
            Color(0xFF7C3AED), // Violeta
            Color(0xFF059669), // Verde teal
            Color(0xFFDC2626)  // Rojo
        )
        
        // Usar el hash del nombre para obtener un color consistente
        val hashCode = categoryName?.hashCode() ?: 0
        val colorIndex = kotlin.math.abs(hashCode) % colorPalette.size
        return colorPalette[colorIndex]
    }

    private fun getCategoryColor(categoryName: String?): Color {
        return when (categoryName?.lowercase()) {
            "alimentación", "comida", "restaurante" -> Color(0xFFFF6B6B)
            "transporte", "gasolina", "uber" -> Color(0xFF4ECDC4)
            "entretenimiento", "cine", "diversión" -> Color(0xFFFFBE0B)
            "servicios", "luz", "agua", "internet" -> Color(0xFF8B5CF6)
            "compras", "ropa", "shopping" -> Color(0xFFFF7A00)
            "salud", "médico", "farmacia" -> Color(0xFF10B981)
            "educación", "curso", "libros" -> Color(0xFF06B6D4)
            "viajes", "hotel", "vuelo" -> Color(0xFFF59E0B)
            else -> Color(0xFF6B7280)
        }
    }
    
    /*
    private fun getWeeklyFlowData(transactions: List<com.example.admin_ingresos.data.Transaction>): List<DayData> {
        val calendar = Calendar.getInstance()
        val currentWeekData = mutableMapOf<Int, Pair<Double, Double>>() // dayOfWeek to (income, expense)
        
        // Initialize with zeros for all days of the week (Monday = 1, Sunday = 7)
        for (i in 1..7) {
            currentWeekData[i] = Pair(0.0, 0.0)
        }
        
        // Get current week's start (Monday)
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val weekStart = calendar.time
        
        // Get week's end (Sunday)
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val weekEnd = calendar.time
        
        // Filter transactions for current week
        val weekTransactions = transactions.filter { transaction ->
            transaction.date.time >= weekStart.time && transaction.date.time <= weekEnd.time
        }
        
        // Group by day of week
        weekTransactions.forEach { transaction ->
            val transactionCalendar = Calendar.getInstance()
            transactionCalendar.time = transaction.date
            transactionCalendar.firstDayOfWeek = Calendar.MONDAY
            
            val dayOfWeek = transactionCalendar.get(Calendar.DAY_OF_WEEK)
            val adjustedDay = if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1
            
            val currentData = currentWeekData[adjustedDay] ?: Pair(0.0, 0.0)
            
            if (transaction.type == "Ingreso") {
                currentWeekData[adjustedDay] = Pair(
                    currentData.first + transaction.amount,
                    currentData.second
                )
            } else {
                currentWeekData[adjustedDay] = Pair(
                    currentData.first,
                    currentData.second + transaction.amount
                )
            }
        }
        
        // Convert to DayData list
        val dayLabels = listOf("L", "M", "X", "J", "V", "S", "D")
        return (1..7).map { dayIndex ->
            val data = currentWeekData[dayIndex] ?: Pair(0.0, 0.0)
            DayData(
                day = dayLabels[dayIndex - 1],
                income = data.first,
                expense = data.second
            )
        }
    }
    */
    
    private fun getCategoryIconFromData(category: com.example.admin_ingresos.data.Category): androidx.compose.ui.graphics.vector.ImageVector {
        // Usar el sistema de iconos Lucide para obtener iconos profesionales y consistentes
        return LucideIconMapper.getCategoryIcon(category)
    }

    private fun getCategoryIcon(categoryName: String?): androidx.compose.ui.graphics.vector.ImageVector {
        // Fallback usando el nombre de categoría si no hay datos completos
        return if (categoryName != null) {
            LucideIconMapper.getIconFromCategoryName(categoryName)
        } else {
            LucideIconMapper.Navigation.add // Default fallback
        }
    }
    
    fun refreshData() {
        loadDashboardData()
    }
}
