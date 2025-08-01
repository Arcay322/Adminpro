package com.example.admin_ingresos.ui.budget

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.admin_ingresos.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BudgetViewModel(
    private val database: AppDatabase,
    private val notificationService: NotificationService,
    private val preferencesManager: PreferencesManager,
    private val context: android.content.Context
) : ViewModel() {
    
    private val budgetDao = database.budgetDao()
    private val categoryDao = database.categoryDao()
    private val alertService = BudgetAlertService(
        context = context,
        database = database,
        notificationService = notificationService,
        preferencesManager = preferencesManager
    )
    
    // UI State
    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()
    
    // Budget list
    val budgetsWithCategories = budgetDao.getBudgetsWithCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Categories for budget creation
    val categories = flow { emit(categoryDao.getAll()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Budget progress data
    private val _budgetProgress = MutableStateFlow<List<BudgetProgress>>(emptyList())
    val budgetProgress: StateFlow<List<BudgetProgress>> = _budgetProgress.asStateFlow()
    
    init {
        loadBudgetProgress()
    }
    
    fun createBudget(
        categoryId: Int,
        amount: Double,
        period: BudgetPeriod,
        startDate: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val endDate = startDate + period.durationInMillis
                
                // Check for overlapping budgets
                val overlappingCount = budgetDao.countOverlappingBudgets(
                    categoryId = categoryId,
                    newStartDate = startDate,
                    newEndDate = endDate
                )
                
                if (overlappingCount > 0) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Ya existe un presupuesto activo para esta categoría en el período seleccionado"
                    )
                    return@launch
                }
                
                val budget = Budget(
                    categoryId = categoryId,
                    amount = amount,
                    period = period,
                    startDate = startDate,
                    endDate = endDate
                )
                
                budgetDao.insertBudget(budget)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showCreateDialog = false
                )
                
                loadBudgetProgress()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al crear presupuesto: ${e.message}"
                )
            }
        }
    }
    
    fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                budgetDao.updateBudget(budget.copy(updatedAt = System.currentTimeMillis()))
                
                _uiState.value = _uiState.value.copy(isLoading = false)
                loadBudgetProgress()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al actualizar presupuesto: ${e.message}"
                )
            }
        }
    }
    
    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                budgetDao.deleteBudget(budget)
                loadBudgetProgress()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al eliminar presupuesto: ${e.message}"
                )
            }
        }
    }
    
    fun deactivateBudget(budgetId: Int) {
        viewModelScope.launch {
            try {
                budgetDao.deactivateBudget(budgetId)
                loadBudgetProgress()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al desactivar presupuesto: ${e.message}"
                )
            }
        }
    }
    
    private fun loadBudgetProgress() {
        viewModelScope.launch {
            try {
                val currentTime = System.currentTimeMillis()
                val progressData = budgetDao.getCurrentBudgetProgress(currentTime)
                
                val budgetProgressList = progressData.map { raw ->
                    val category = Category(
                        id = raw.categoryId,
                        name = raw.categoryName,
                        icon = raw.categoryIcon,
                        color = raw.categoryColor
                    )
                    
                    val budget = Budget(
                        id = raw.id,
                        categoryId = raw.categoryId,
                        amount = raw.amount,
                        period = raw.period,
                        startDate = raw.startDate,
                        endDate = raw.endDate,
                        isActive = raw.isActive,
                        createdAt = raw.createdAt,
                        updatedAt = raw.updatedAt
                    )
                    
                    val remaining = (budget.amount - raw.spent).coerceAtLeast(0.0)
                    val percentage = if (budget.amount > 0) (raw.spent / budget.amount).toFloat() else 0f
                    val isOverBudget = raw.spent > budget.amount
                    
                    val daysRemaining = ((budget.endDate - currentTime) / (24 * 60 * 60 * 1000)).toInt()
                        .coerceAtLeast(0)
                    
                    BudgetProgress(
                        budget = budget,
                        category = category,
                        spent = raw.spent,
                        remaining = remaining,
                        percentage = percentage,
                        isOverBudget = isOverBudget,
                        daysRemaining = daysRemaining
                    )
                }
                
                _budgetProgress.value = budgetProgressList
                
                // Check for budget alerts
                alertService?.checkBudgetAlerts()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al cargar progreso de presupuestos: ${e.message}"
                )
            }
        }
    }
    
    // UI Actions
    fun showCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true)
    }
    
    fun hideCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = false, error = null)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class BudgetUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCreateDialog: Boolean = false
)