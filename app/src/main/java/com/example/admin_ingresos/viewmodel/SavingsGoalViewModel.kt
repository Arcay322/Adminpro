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
                    description = description,
                    targetDate = targetDate,
                    priority = priority
                )
                savingsGoalDao.insert(goal)
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
    
    fun addProgress(goalId: Long, amount: Double) {
        viewModelScope.launch {
            try {
                savingsGoalDao.addProgress(goalId, amount)
                _uiState.value = _uiState.value.copy(message = "Progreso añadido exitosamente")
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
