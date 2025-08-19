package com.example.admin_ingresos.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAll(): List<Transaction>
    
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    suspend fun getByType(type: String): List<Transaction>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY date DESC")
    suspend fun getByCategory(categoryId: Int): List<Transaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): Transaction?

    // If a transaction linked to a savings goal is deleted, decrement the goal currentAmount atomically
    @androidx.room.Transaction
    suspend fun deleteAndReconcile(transaction: Transaction, savingsGoalDao: com.example.admin_ingresos.data.dao.SavingsGoalDao) {
        // If this transaction is linked to a goal, subtract its amount from the goal
        if (transaction.goalId != null) {
            // Use DAO method to subtract progress (add negative)
            savingsGoalDao.addProgress(transaction.goalId, -transaction.amount)
        }
        delete(transaction)
    }
    
    @Query("SELECT DISTINCT description FROM transactions WHERE description != '' ORDER BY description ASC")
    suspend fun getDistinctDescriptions(): List<String>
    
    @Query("SELECT * FROM transactions WHERE description LIKE :pattern ORDER BY date DESC")
    suspend fun getTransactionsByDescriptionPattern(pattern: String): List<Transaction>
    
    @Query("""
        SELECT * FROM transactions 
        WHERE amount = :amount 
        AND description = :description 
        AND categoryId = :categoryId 
        AND date BETWEEN :startDate AND :endDate
        ORDER BY date DESC
        LIMIT 5
    """)
    suspend fun findSimilarTransactions(
        amount: Double,
        description: String,
        categoryId: Int,
        startDate: Long,
        endDate: Long
    ): List<Transaction>
    
    @Query("""
        SELECT * FROM transactions 
        WHERE (description LIKE '%' || :query || '%' 
        OR CAST(amount AS TEXT) LIKE '%' || :query || '%')
        ORDER BY date DESC
    """)
    suspend fun searchTransactions(query: String): List<Transaction>
    
    @Query("""
        SELECT * FROM transactions 
        WHERE description LIKE '%' || :query || '%'
        ORDER BY date DESC
    """)
    suspend fun searchByDescription(query: String): List<Transaction>
    
    @Query("""
        SELECT * FROM transactions 
        WHERE amount BETWEEN :minAmount AND :maxAmount
        ORDER BY date DESC
    """)
    suspend fun searchByAmountRange(minAmount: Double, maxAmount: Double): List<Transaction>
    
    @Query("""
        SELECT t.* FROM transactions t
        LEFT JOIN categories c ON t.categoryId = c.id
        LEFT JOIN payment_methods p ON t.paymentMethodId = p.id
        WHERE (:searchQuery = '' OR t.description LIKE '%' || :searchQuery || '%' OR CAST(t.amount AS TEXT) LIKE '%' || :searchQuery || '%')
        AND (:startDate IS NULL OR t.date >= :startDate)
        AND (:endDate IS NULL OR t.date <= :endDate)
        AND (:categories IS NULL OR t.categoryId IN (:categories))
        AND (:paymentMethods IS NULL OR t.paymentMethodId IN (:paymentMethods))
        AND (:transactionTypes IS NULL OR t.type IN (:transactionTypes))
        AND (:minAmount IS NULL OR t.amount >= :minAmount)
        AND (:maxAmount IS NULL OR t.amount <= :maxAmount)
        ORDER BY 
            CASE WHEN :sortBy = 'DATE_DESC' THEN t.date END DESC,
            CASE WHEN :sortBy = 'DATE_ASC' THEN t.date END ASC,
            CASE WHEN :sortBy = 'AMOUNT_DESC' THEN t.amount END DESC,
            CASE WHEN :sortBy = 'AMOUNT_ASC' THEN t.amount END ASC,
            CASE WHEN :sortBy = 'CATEGORY' THEN c.name END ASC,
            CASE WHEN :sortBy = 'DESCRIPTION' THEN t.description END ASC,
            t.date DESC
    """)
    suspend fun getFilteredTransactions(
        searchQuery: String = "",
        startDate: Long? = null,
        endDate: Long? = null,
        categories: List<Int>? = null,
        paymentMethods: List<Int>? = null,
        transactionTypes: List<String>? = null,
        minAmount: Double? = null,
        maxAmount: Double? = null,
        sortBy: String = "DATE_DESC"
    ): List<Transaction>
    
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getTransactionsByDateRange(startDate: Long, endDate: Long): List<Transaction>
    
    @Query("""
        SELECT DISTINCT description FROM transactions 
        WHERE description LIKE '%' || :query || '%' 
        AND description != ''
        ORDER BY description ASC
        LIMIT 10
    """)
    suspend fun getSearchSuggestions(query: String): List<String>

    @MapInfo(keyColumn = "categoryId")
    @Query("""
        SELECT 
            categoryId, 
            COUNT(*) as transactionCount, 
            SUM(amount) as totalAmount 
        FROM `transactions` 
        GROUP BY categoryId
    """)
    fun getAllCategoryStats(): Flow<Map<Int, CategoryTransactionStats>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY date DESC")
    fun getTransactionsByCategoryIdFlow(categoryId: Int): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Transaction?

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    fun getByIdFlow(id: Int): Flow<Transaction?>

    // --- NUEVA FUNCIÓN AÑADIDA PARA FILTROS DE TIEMPO ---
    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByCategoryIdAndDateRangeFlow(categoryId: Int, startDate: Long, endDate: Long): Flow<List<Transaction>>
}