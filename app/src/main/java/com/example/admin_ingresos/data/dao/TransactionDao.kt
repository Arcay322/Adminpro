package com.example.admin_ingresos.data.dao

import androidx.room.*
import com.example.admin_ingresos.data.Transaction

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun findById(id: Int): Transaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    // Bulk conversion helper: mark historical transactions linked to a goal as Transfer/Ahorro
    @Query("UPDATE transactions SET type = :transferType WHERE goalId IS NOT NULL")
    suspend fun convertGoalTransactionsToTransfer(transferType: String = "Transfer")

    // When deleting a transaction that is linked to a savings goal, callers should first
    // adjust the savings goal's currentAmount accordingly. A helper transaction could be
    // implemented here but SavingsGoalDao already exposes addProgress, so the UI layer or
    // a higher-level repository can coordinate the two operations atomically if needed.
}
