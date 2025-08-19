package com.example.admin_ingresos.data.dao

import androidx.room.*
import com.example.admin_ingresos.data.model.SavingsGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    
    @Query("SELECT * FROM savings_goals WHERE isActive = 1 ORDER BY priority DESC, createdAt DESC")
    fun getAllActiveFlow(): Flow<List<SavingsGoal>>
    
    @Query("SELECT * FROM savings_goals WHERE isActive = 1 ORDER BY priority DESC, createdAt DESC")
    suspend fun getAllActive(): List<SavingsGoal>
    
    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getById(id: Long): SavingsGoal?
    
    @Query("SELECT * FROM savings_goals WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<SavingsGoal?>
    
    @Insert
    suspend fun insert(savingsGoal: SavingsGoal): Long
    
    @Update
    suspend fun update(savingsGoal: SavingsGoal)
    
    @Delete
    suspend fun delete(savingsGoal: SavingsGoal)
    
    @Query("UPDATE savings_goals SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)
    
    @Query("UPDATE savings_goals SET currentAmount = currentAmount + :amount WHERE id = :id")
    suspend fun addProgress(id: Long, amount: Double)

    // Allow inserting a transaction directly from this DAO to enable atomic operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: com.example.admin_ingresos.data.Transaction): Long

    // Atomic operation: add progress to savings goal and insert a corresponding transaction
    @androidx.room.Transaction
    suspend fun addProgressWithTransaction(id: Long, amount: Double, transaction: com.example.admin_ingresos.data.Transaction) {
        addProgress(id, amount)
        insertTransaction(transaction)
    }
    
    @Query("SELECT COUNT(*) FROM savings_goals WHERE isActive = 1")
    suspend fun getActiveCount(): Int
    
    @Query("SELECT SUM(currentAmount) FROM savings_goals WHERE isActive = 1")
    suspend fun getTotalSaved(): Double
    
    @Query("SELECT SUM(targetAmount) FROM savings_goals WHERE isActive = 1")
    suspend fun getTotalTarget(): Double
}
