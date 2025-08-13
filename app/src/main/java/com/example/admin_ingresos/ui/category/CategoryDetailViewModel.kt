package com.example.admin_ingresos.ui.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.admin_ingresos.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

// Enum para manejar los filtros de tiempo
enum class TimeFilter(val displayName: String) {
    THIS_MONTH("Este Mes"),
    LAST_MONTH("Mes Pasado"),
    THIS_YEAR("Este Año"),
    ALL_TIME("Todo")
}

// Estado de la UI actualizado para incluir el presupuesto y el filtro
data class CategoryDetailUiState(
    val category: Category? = null,
    val transactions: List<Transaction> = emptyList(),
    val activeBudget: Budget? = null,
    val totalAmountForPeriod: Double = 0.0,
    val selectedFilter: TimeFilter = TimeFilter.THIS_MONTH,
    val isLoading: Boolean = true,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryDetailViewModel(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao,
    categoryId: Int?
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryDetailUiState())
    val uiState: StateFlow<CategoryDetailUiState> = _uiState

    // Flow para manejar el cambio de filtro de tiempo
    private val selectedFilter = MutableStateFlow(TimeFilter.THIS_MONTH)

    init {
        if (categoryId != null) {
            loadStaticData(categoryId)

            viewModelScope.launch {
                selectedFilter.flatMapLatest { filter ->
                    val (startDate, endDate) = calculateDateRange(filter)
                    if (filter == TimeFilter.ALL_TIME) {
                        transactionDao.getTransactionsByCategoryIdFlow(categoryId)
                    } else {
                        transactionDao.getTransactionsByCategoryIdAndDateRangeFlow(categoryId, startDate, endDate)
                    }
                }.catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }.collect { transactions ->
                    _uiState.update {
                        it.copy(
                            transactions = transactions,
                            totalAmountForPeriod = transactions.sumOf { t -> t.amount },
                            isLoading = false
                        )
                    }
                }
            }
        } else {
            _uiState.update { it.copy(isLoading = false, error = "ID de categoría no encontrado.") }
        }
    }

    /**
     * Función pública para que la UI pueda cambiar el filtro de tiempo.
     */
    fun setTimeFilter(filter: TimeFilter) {
        _uiState.update { it.copy(selectedFilter = filter, isLoading = true) }
        selectedFilter.value = filter
    }

    private fun loadStaticData(categoryId: Int) {
        viewModelScope.launch {
            try {
                val category = categoryDao.getCategoryById(categoryId)
                val budget = budgetDao.getActiveBudgetByCategory(categoryId)
                _uiState.update { it.copy(category = category, activeBudget = budget) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al cargar datos iniciales.") }
            }
        }
    }
    
    // Funciones auxiliares para calcular los rangos de fecha
    private fun calculateDateRange(filter: TimeFilter): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        return when (filter) {
            TimeFilter.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                setMidnight(calendar)
                val start = calendar.timeInMillis
                
                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                setEndOfDay(calendar)
                val end = calendar.timeInMillis
                start to end
            }
            TimeFilter.LAST_MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                setMidnight(calendar)
                val start = calendar.timeInMillis

                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                setEndOfDay(calendar)
                val end = calendar.timeInMillis
                start to end
            }
            TimeFilter.THIS_YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                setMidnight(calendar)
                val start = calendar.timeInMillis

                calendar.add(Calendar.YEAR, 1)
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                setEndOfDay(calendar)
                val end = calendar.timeInMillis
                start to end
            }
            TimeFilter.ALL_TIME -> 0L to Long.MAX_VALUE
        }
    }

    private fun setMidnight(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }

    private fun setEndOfDay(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
    }
}