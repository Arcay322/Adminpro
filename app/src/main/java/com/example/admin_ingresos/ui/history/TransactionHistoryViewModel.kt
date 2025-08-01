package com.example.admin_ingresos.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.admin_ingresos.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TransactionHistoryViewModel(private val db: AppDatabase) : ViewModel() {
    private val transactionDao = db.transactionDao()
    private val categoryDao = db.categoryDao()
    private val paymentMethodDao = db.paymentMethodDao()
    
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    private val _currentFilter = MutableStateFlow(TransactionFilter())
    val currentFilter: StateFlow<TransactionFilter> = _currentFilter
    
    private val _searchSuggestions = MutableStateFlow<List<String>>(emptyList())
    val searchSuggestions: StateFlow<List<String>> = _searchSuggestions
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _currentSortOption = MutableStateFlow(SortOption.DATE_DESC)
    val currentSortOption: StateFlow<SortOption> = _currentSortOption
    
    // Filter presets
    private val _filterPresets = MutableStateFlow(FilterPreset.getDefaultPresets())
    val filterPresets: StateFlow<List<FilterPreset>> = _filterPresets.asStateFlow()
    
    // Categories and payment methods for filtering
    val categories = categoryDao.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val paymentMethods = paymentMethodDao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    init {
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _transactions.value = transactionDao.getAll()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun searchTransactions(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (query.isBlank()) {
                    loadTransactions()
                } else {
                    _transactions.value = transactionDao.searchTransactions(query)
                    // Load search suggestions
                    _searchSuggestions.value = transactionDao.getSearchSuggestions(query)
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun applyFilter(filter: TransactionFilter) {
        _currentFilter.value = filter
        applyCurrentFiltersAndSort()
    }
    
    fun setSortOption(sortOption: SortOption) {
        _currentSortOption.value = sortOption
        applyCurrentFiltersAndSort()
    }
    
    private fun applyCurrentFiltersAndSort() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val sortBy = when (_currentSortOption.value) {
                    SortOption.DATE_DESC -> "DATE_DESC"
                    SortOption.DATE_ASC -> "DATE_ASC"
                    SortOption.AMOUNT_DESC -> "AMOUNT_DESC"
                    SortOption.AMOUNT_ASC -> "AMOUNT_ASC"
                    SortOption.CATEGORY -> "CATEGORY"
                    SortOption.DESCRIPTION -> "DESCRIPTION"
                }
                
                _transactions.value = transactionDao.getFilteredTransactions(
                    searchQuery = _currentFilter.value.searchQuery,
                    startDate = _currentFilter.value.dateRange?.startDate,
                    endDate = _currentFilter.value.dateRange?.endDate,
                    categories = _currentFilter.value.categories.takeIf { it.isNotEmpty() },
                    paymentMethods = _currentFilter.value.paymentMethods.takeIf { it.isNotEmpty() },
                    transactionTypes = _currentFilter.value.transactionTypes.takeIf { it.isNotEmpty() },
                    minAmount = _currentFilter.value.amountRange?.minAmount,
                    maxAmount = _currentFilter.value.amountRange?.maxAmount,
                    sortBy = sortBy
                )
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearFilters() {
        _currentFilter.value = TransactionFilter()
        _searchQuery.value = ""
        loadTransactions()
    }
    
    fun applyFilterPreset(preset: FilterPreset) {
        _currentFilter.value = preset.filter
        _searchQuery.value = preset.filter.searchQuery
        applyCurrentFiltersAndSort()
    }
    
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                transactionDao.delete(transaction)
                // Reload transactions after deletion
                if (_currentFilter.value.isEmpty() && _searchQuery.value.isBlank()) {
                    loadTransactions()
                } else if (_searchQuery.value.isNotBlank()) {
                    searchTransactions(_searchQuery.value)
                } else {
                    applyCurrentFiltersAndSort()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun duplicateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                val duplicatedTransaction = transaction.copy(
                    id = 0, // Let Room auto-generate new ID
                    date = System.currentTimeMillis() // Set current timestamp
                )
                transactionDao.insert(duplicatedTransaction)
                
                // Reload transactions after duplication
                if (_currentFilter.value.isEmpty() && _searchQuery.value.isBlank()) {
                    loadTransactions()
                } else if (_searchQuery.value.isNotBlank()) {
                    searchTransactions(_searchQuery.value)
                } else {
                    applyCurrentFiltersAndSort()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
