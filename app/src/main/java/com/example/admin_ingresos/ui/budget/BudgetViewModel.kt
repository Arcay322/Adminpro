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

    // Inactive budgets (for reactivation UI)
    val inactiveBudgetsWithCategories = budgetDao.getInactiveBudgetsWithCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Categories for budget creation
    val categories = categoryDao.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Budget progress data
    private val _budgetProgress = MutableStateFlow<List<BudgetProgress>>(emptyList())
    val budgetProgress: StateFlow<List<BudgetProgress>> = _budgetProgress.asStateFlow()

    init {
        // On startup, deactivate any non-recurring budgets whose endDate has passed
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            try {
                budgetDao.deactivateExpiredBudgets(now)
            } catch (e: Exception) {
                // ignore failures here; continue to load progress
            }

            // Initial load after attempting deactivation
            loadBudgetProgress()

            // Listen to transaction changes to update budget progress
            database.transactionDao().getAllTransactions().collect {
                loadBudgetProgress()
            }
        }
    }

    fun createBudget(
        categoryId: Int,
        amount: Double,
        period: BudgetPeriod,
        startDate: Long = System.currentTimeMillis(),
        asTemplate: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                // Always create budgets with concrete start/end dates and active state.
                // The 'asTemplate' toggle only indicates recurrence intent (handled elsewhere),
                // it must not create inactive budgets with zeroed dates.
                val insertStart = startDate
                val insertEnd = startDate + period.durationInMillis

                // Check for overlapping budgets
                // Only check overlapping if this is a concrete budget (not a template)
                val overlappingCount = if (!asTemplate) {
                    budgetDao.countOverlappingBudgets(
                        categoryId = categoryId,
                        newStartDate = insertStart,
                        newEndDate = insertEnd
                    )
                } else 0

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
                    startDate = insertStart,
                    endDate = insertEnd,
                    isActive = true
                )

                val insertedId = budgetDao.insertBudget(budget)

                // Defensive: ensure the inserted budget is active and has valid dates.
                // Defensive: ensure the inserted budget is active and has valid dates.
                val now = System.currentTimeMillis()
                val insertedBudget = budgetDao.getBudgetById(insertedId.toInt())
                if (insertedBudget != null) {
                    var needsUpdate = false
                    var updated = insertedBudget
                    if (!insertedBudget.isActive) {
                        updated = updated.copy(isActive = true)
                        needsUpdate = true
                    }
                    if (insertedBudget.startDate <= 0L || insertedBudget.endDate <= insertedBudget.startDate) {
                        val newStart = now
                        val newEnd = now + budget.period.durationInMillis
                        updated = updated.copy(startDate = newStart, endDate = newEnd)
                        needsUpdate = true
                    }
                    if (needsUpdate) {
                        updated = updated.copy(updatedAt = now)
                        budgetDao.updateBudget(updated)
                    }
                }

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

    fun activateBudget(budgetId: Int) {
        viewModelScope.launch {
            try {
                // If this budget is a template (stored with startDate==0 and endDate==0),
                // activating it should set a real start/end window so it becomes visible
                // to the "current" queries that filter by startDate <= now <= endDate.
                val existing = budgetDao.getBudgetById(budgetId)
                val now = System.currentTimeMillis()
                if (existing != null) {
                    val toUpdate = if (existing.startDate == 0L && existing.endDate == 0L) {
                        existing.copy(
                            startDate = now,
                            endDate = now + existing.period.durationInMillis,
                            isActive = true,
                            updatedAt = now
                        )
                    } else {
                        existing.copy(isActive = true, updatedAt = now)
                    }
                    budgetDao.updateBudget(toUpdate)
                } else {
                    // Fallback: try the simple activate path
                    budgetDao.activateBudget(budgetId)
                }

                loadBudgetProgress()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al activar presupuesto: ${e.message}"
                )
            }
        }
    }

    fun showEditDialog(budget: Budget) {
        _uiState.value = _uiState.value.copy(
            showEditDialog = true,
            editingBudget = budget
        )
    }

    fun hideEditDialog() {
        _uiState.value = _uiState.value.copy(
            showEditDialog = false,
            editingBudget = null
        )
    }

    fun updateBudgetAmount(budgetId: Int, newAmount: Double) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val existingBudget = budgetDao.getBudgetById(budgetId)
                existingBudget?.let { budget ->
                    val updatedBudget = budget.copy(
                        amount = newAmount,
                        updatedAt = System.currentTimeMillis()
                    )
                    budgetDao.updateBudget(updatedBudget)
                    loadBudgetProgress()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showEditDialog = false,
                        editingBudget = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al actualizar presupuesto: ${e.message}"
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
    val showCreateDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val editingBudget: Budget? = null
)
