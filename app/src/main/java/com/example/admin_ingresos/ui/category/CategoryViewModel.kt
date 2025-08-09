package com.example.admin_ingresos.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.admin_ingresos.data.AppDatabase
import com.example.admin_ingresos.data.Category
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CategoryViewModel(private val db: AppDatabase) : ViewModel() {
    private val _transactionCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val transactionCounts: StateFlow<Map<Int, Int>> = _transactionCounts.asStateFlow()

    private val _totalAmounts = MutableStateFlow<Map<Int, Double>>(emptyMap())
    val totalAmounts: StateFlow<Map<Int, Double>> = _totalAmounts.asStateFlow()
    private val transactionDao = db.transactionDao()
    suspend fun getTransactionCount(categoryId: Int): Int {
        return transactionDao.getByCategory(categoryId).size
    }

    suspend fun getTotalAmount(categoryId: Int): Double {
        return transactionDao.getByCategory(categoryId).sumOf { it.amount }
    }
    val categories: StateFlow<List<Category>> = db.categoryDao().getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            db.transactionDao().getAllTransactions().collect { transactions ->
                val counts = mutableMapOf<Int, Int>()
                val totals = mutableMapOf<Int, Double>()
                transactions.groupBy { it.categoryId }.forEach { (catId, txs) ->
                    counts[catId] = txs.size
                    totals[catId] = txs.sumOf { it.amount }
                }
                _transactionCounts.value = counts
                _totalAmounts.value = totals
            }
        }
    }

    fun addCategory(category: Category) {
        viewModelScope.launch {
            // Asignar orden al final
            val current = db.categoryDao().getCategoriesList()
            db.categoryDao().insert(category.copy(order = current.size))
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            db.categoryDao().update(category)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            db.categoryDao().delete(category)
        }
    }

    fun reorderCategories(newOrder: List<Category>) {
        viewModelScope.launch {
            db.categoryDao().reorderCategories(newOrder.map { it.id })
        }
    }
}
