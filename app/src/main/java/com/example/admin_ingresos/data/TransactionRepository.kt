package com.example.admin_ingresos.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val db: AppDatabase) {
    suspend fun getAllTransactions() = db.transactionDao().getAll()
    fun getAllTransactionsFlow(): Flow<List<Transaction>> = db.transactionDao().getAllTransactions()
    suspend fun getTransactionsByType(type: String) = db.transactionDao().getByType(type)
    suspend fun getTransactionsByCategory(categoryId: Int) = db.transactionDao().getByCategory(categoryId)
    suspend fun insertTransaction(transaction: Transaction) = db.transactionDao().insert(transaction)
    suspend fun updateTransaction(transaction: Transaction) = db.transactionDao().update(transaction)
    suspend fun deleteTransaction(transaction: Transaction) = db.transactionDao().delete(transaction)
}
