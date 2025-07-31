package com.example.admin_ingresos.data

import androidx.room.*

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAll(): List<Transaction>

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
}
