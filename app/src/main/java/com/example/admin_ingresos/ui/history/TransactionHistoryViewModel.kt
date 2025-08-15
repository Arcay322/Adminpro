package com.example.admin_ingresos.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.admin_ingresos.data.AppDatabase
import com.example.admin_ingresos.data.Category
import com.example.admin_ingresos.data.FilterPreset
import com.example.admin_ingresos.data.PaymentMethod
import com.example.admin_ingresos.data.Transaction
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

// --- Data Classes for State Management ---

data class TransactionHistoryUiState(
    val isLoading: Boolean = false,
    val transactions: List<Transaction> = emptyList(),
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.DATE_DESC,
    val showFilters: Boolean = false,
    val transactionToDelete: Transaction? = null,
    val transactionToEdit: Transaction? = null,
    val transactionToDuplicate: Transaction? = null,
    val showAnalytics: Boolean = false,
    val error: String? = null
)

data class TransactionFilter(
    val searchQuery: String = "",
    val dateRange: DateRange? = null,
    val categories: List<Int> = emptyList(),
    val paymentMethods: List<Int> = emptyList(),
    val transactionTypes: List<String> = emptyList(),
    val amountRange: AmountRange? = null
) {
    fun isEmpty(): Boolean {
        return dateRange == null && categories.isEmpty() && paymentMethods.isEmpty() &&
                transactionTypes.isEmpty() && amountRange == null && searchQuery.isBlank()
    }
}

data class DateRange(val startDate: Long, val endDate: Long) {
    companion object
}
data class AmountRange(val minAmount: Double, val maxAmount: Double)

enum class SortOption(val displayName: String) {
    DATE_DESC("Fecha (Reciente)"),
    DATE_ASC("Fecha (Antiguo)"),
    AMOUNT_DESC("Monto (Mayor)"),
    AMOUNT_ASC("Monto (Menor)"),
    DESCRIPTION("Nombre (A-Z)"),
    CATEGORY("Nombre (Z-A)");
}


class TransactionHistoryViewModel(private val db: AppDatabase) : ViewModel() {
    private val transactionDao = db.transactionDao()
    private val categoryDao = db.categoryDao()
    private val paymentMethodDao = db.paymentMethodDao()

    private val _uiState = MutableStateFlow(TransactionHistoryUiState())
    val uiState: StateFlow<TransactionHistoryUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val transactions = transactionDao.getAll()
                _uiState.update { it.copy(transactions = transactions, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al cargar transacciones", isLoading = false) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onSortOptionChanged(sortOption: SortOption) {
        _uiState.update { it.copy(sortOption = sortOption) }
    }

    fun onToggleFilters(show: Boolean) {
        _uiState.update { it.copy(showFilters = show) }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.delete(transaction)
            loadTransactions() // Recargar
            _uiState.update { it.copy(transactionToDelete = null) }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.update(transaction)
            loadTransactions() // Recargar
            _uiState.update { it.copy(transactionToEdit = null) }
        }
    }
    
    fun duplicateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            val duplicatedTransaction = transaction.copy(
                id = 0,
                date = System.currentTimeMillis()
            )
            transactionDao.insert(duplicatedTransaction)
            loadTransactions()
            _uiState.update { it.copy(transactionToDuplicate = null) }
        }
    }

    // --- Dialog visibility handlers ---
    fun showDeleteDialog(transaction: Transaction) {
        _uiState.update { it.copy(transactionToDelete = transaction) }
    }

    fun hideDeleteDialog() {
        _uiState.update { it.copy(transactionToDelete = null) }
    }

    fun showEditDialog(transaction: Transaction) {
        _uiState.update { it.copy(transactionToEdit = transaction) }
    }

    fun hideEditDialog() {
        _uiState.update { it.copy(transactionToEdit = null) }
    }
    
    fun showDuplicateDialog(transaction: Transaction) {
        _uiState.update { it.copy(transactionToDuplicate = transaction) }
    }

    fun hideDuplicateDialog() {
        _uiState.update { it.copy(transactionToDuplicate = null) }
    }
    
    fun onAnalyticsClick(show: Boolean) {
        _uiState.update { it.copy(showAnalytics = show) }
    }
}