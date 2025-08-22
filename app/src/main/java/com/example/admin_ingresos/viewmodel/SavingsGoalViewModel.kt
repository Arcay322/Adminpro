package com.example.admin_ingresos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.admin_ingresos.data.AppDatabase
import com.example.admin_ingresos.data.model.SavingsGoal
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

class SavingsGoalViewModel(private val database: AppDatabase) : ViewModel() {
    
    private val savingsGoalDao = database.savingsGoalDao()
    
    val savingsGoals = savingsGoalDao.getAllActiveFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val _uiState = MutableStateFlow(SavingsGoalUiState())
    val uiState = _uiState.asStateFlow()
    
    fun addSavingsGoal(
        name: String,
        targetAmount: Double,
    emoji: String,
    color: String = "#4CAF50",
    description: String? = null,
    targetDate: Date? = null,
    priority: Int = 0
    ) {
        viewModelScope.launch {
            try {
                val goal = SavingsGoal(
                    name = name,
                    targetAmount = targetAmount,
                    emoji = emoji,
                    color = color,
                    description = description,
                    targetDate = targetDate,
                    priority = priority
                )
                // Insert goal first to obtain its id
                val newId = savingsGoalDao.insert(goal)

                // Create a dedicated AHORRO category for this goal (per-goal category)
                val categoryDao = database.categoryDao()
                val perGoalCategory = com.example.admin_ingresos.data.Category(
                    name = name,
                    icon = emoji,
                    color = color,
                    isFavorite = false,
                    type = com.example.admin_ingresos.data.CategoryType.AHORRO
                )
                val catId = try {
                    categoryDao.insert(perGoalCategory).toInt()
                } catch (_: Exception) {
                    // fallback: find or reuse a generic Ahorro category
                    val existing = categoryDao.getCategoriesList().find { it.name.equals(name, ignoreCase = true) && it.type == com.example.admin_ingresos.data.CategoryType.AHORRO }
                    existing?.id ?: -1
                }

                // Update saved goal to include categoryId
                if (catId > 0) {
                    val savedGoal = savingsGoalDao.getById(newId)
                    if (savedGoal != null) {
                        val updated = savedGoal.copy(categoryId = catId)
                        savingsGoalDao.update(updated)
                    }
                }
                // Ensure there's an AHORRO category for savings goals
                try {
                    val categoryDao = database.categoryDao()
                    val existing = categoryDao.getCategoriesList().find { it.name.equals("Ahorro", ignoreCase = true) && it.type == com.example.admin_ingresos.data.CategoryType.AHORRO }
                    if (existing == null) {
                        val newCat = com.example.admin_ingresos.data.Category(
                            name = "Ahorro",
                            icon = "PiggyBank",
                            color = "#10B981",
                            isFavorite = false,
                            type = com.example.admin_ingresos.data.CategoryType.AHORRO
                        )
                        categoryDao.insert(newCat)
                    }
                } catch (_: Exception) {
                    // non-fatal: category creation failure shouldn't break goal creation
                }
                _uiState.value = _uiState.value.copy(message = "Meta creada exitosamente")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error al crear la meta: ${e.message}")
            }
        }
    }
    
    fun updateSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            try {
                savingsGoalDao.update(goal)
                _uiState.value = _uiState.value.copy(message = "Meta actualizada exitosamente")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error al actualizar la meta: ${e.message}")
            }
        }
    }
    
    fun deleteSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            try {
                savingsGoalDao.delete(goal)
                _uiState.value = _uiState.value.copy(message = "Meta eliminada exitosamente")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error al eliminar la meta: ${e.message}")
            }
        }
    }

    fun deleteSavingsGoalWithTransactions(goal: SavingsGoal) {
        viewModelScope.launch {
            try {
                // delete transactions linked to this goal first
                try {
                    val txDao = database.transactionDao()
                    txDao.deleteByGoalId(goal.id)
                } catch (_: Exception) {
                    // non-fatal: continue to delete the goal even if transactions deletion fails
                }

                savingsGoalDao.delete(goal)
                _uiState.value = _uiState.value.copy(message = "Meta eliminada junto con sus transacciones")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error al eliminar la meta: ${e.message}")
            }
        }
    }
    
    fun addProgress(goalId: Long, amount: Double) {
        viewModelScope.launch {
            try {
                // Update the savings goal stored amount
                savingsGoalDao.addProgress(goalId, amount)

                // Prepare atomic insertion: ensure category exists and perform both operations inside DAO transaction
                try {
                    val categoryDao = database.categoryDao()

                    // Prefer the per-goal category if available
                    val goal = savingsGoalDao.getById(goalId)
                    val savingsCategoryId: Int = goal?.categoryId ?: run {
                        val existing = categoryDao.getCategoriesList().find { it.name.equals("Ahorro", ignoreCase = true) }
                        if (existing != null) existing.id else {
                            val newCat = com.example.admin_ingresos.data.Category(
                                name = "Ahorro",
                                icon = "PiggyBank",
                                color = "#10B981",
                                isFavorite = false,
                                type = com.example.admin_ingresos.data.CategoryType.AHORRO
                            )
                            categoryDao.insert(newCat).toInt()
                        }
                    }

                    val goalName = goal?.name ?: "meta"

                    val tx = com.example.admin_ingresos.data.Transaction(
                        amount = amount,
                        type = com.example.admin_ingresos.data.Transaction.TYPE_TRANSFER,
                        categoryId = savingsCategoryId,
                        description = "Aporte de ahorro a $goalName",
                        date = java.util.Date().time,
                        paymentMethodId = null,
                        goalId = goalId
                    )

                    // Use DAO @Transaction to perform both operations atomically
                    savingsGoalDao.addProgressWithTransaction(goalId, amount, tx)
                    _uiState.value = _uiState.value.copy(message = "Progreso añadido exitosamente")
                } catch (inner: Exception) {
                    _uiState.value = _uiState.value.copy(error = "Error al añadir progreso (transacción): ${inner.message}")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error al añadir progreso: ${e.message}")
            }
        }
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }
}

data class SavingsGoalUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)
