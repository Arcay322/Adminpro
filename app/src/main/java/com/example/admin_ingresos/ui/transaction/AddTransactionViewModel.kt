package com.example.admin_ingresos.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.admin_ingresos.data.AppDatabase
import com.example.admin_ingresos.data.Category
import com.example.admin_ingresos.data.Transaction
import kotlinx.coroutines.launch

class AddTransactionViewModel(private val db: AppDatabase) : ViewModel() {
    fun saveTransaction(
        amount: Double,
        type: String,
        categoryId: Int,
        description: String,
        date: Long,
        paymentMethodId: Int?
    ) {
        viewModelScope.launch {
            db.transactionDao().insert(
                Transaction(
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    description = description,
                    date = date,
                    paymentMethodId = paymentMethodId
                )
            )
        }
    }
    
    fun addCategory(categoryName: String, onCategoryAdded: (Int) -> Unit) {
        viewModelScope.launch {
            val newCategory = Category(name = categoryName)
            val categoryId = db.categoryDao().insert(newCategory)
            onCategoryAdded(categoryId.toInt())
        }
    }
    
    suspend fun getDescriptionSuggestions(query: String): List<String> {
        return try {
            db.transactionDao().getDistinctDescriptions()
                .filter { it.contains(query, ignoreCase = true) }
                .take(5)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun suggestCategoryForDescription(description: String): Int? {
        return try {
            // Find the most common category for similar descriptions
            val transactions = db.transactionDao().getTransactionsByDescriptionPattern("%$description%")
            val categoryFrequency = transactions.groupBy { it.categoryId }
                .mapValues { it.value.size }
            categoryFrequency.maxByOrNull { it.value }?.key
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun checkForDuplicates(
        amount: Double,
        description: String,
        categoryId: Int
    ): List<Transaction> {
        return try {
            val currentTime = System.currentTimeMillis()
            val oneDayAgo = currentTime - (24 * 60 * 60 * 1000L) // 24 hours ago
            val oneWeekFromNow = currentTime + (7 * 24 * 60 * 60 * 1000L) // 1 week from now
            
            db.transactionDao().findSimilarTransactions(
                amount = amount,
                description = description,
                categoryId = categoryId,
                startDate = oneDayAgo,
                endDate = oneWeekFromNow
            )
        } catch (e: Exception) {
            emptyList()
        }
    }
}
