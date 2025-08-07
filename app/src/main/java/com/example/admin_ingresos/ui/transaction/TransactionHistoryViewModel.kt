package com.example.admin_ingresos.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.admin_ingresos.data.AppDatabase

class TransactionHistoryViewModel(private val database: AppDatabase) : ViewModel() {
    
    private val _transactions = MutableStateFlow<List<com.example.admin_ingresos.data.Transaction>>(emptyList())
    val transactions: StateFlow<List<com.example.admin_ingresos.data.Transaction>> = _transactions.asStateFlow()
    
    init {
        loadTransactions()
    }
    
    private fun loadTransactions() {
        viewModelScope.launch {
            try {
                _transactions.value = database.transactionDao().getAll()
            } catch (e: Exception) {
                // Handle error
                _transactions.value = emptyList()
            }
        }
    }
    
    fun refreshTransactions() {
        loadTransactions()
    }
}
