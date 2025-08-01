package com.example.admin_ingresos.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    
    @Query("SELECT * FROM budgets WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllActiveBudgets(): Flow<List<Budget>>
    
    @Query("SELECT * FROM budgets ORDER BY createdAt DESC")
    fun getAllBudgets(): Flow<List<Budget>>
    
    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetById(id: Int): Budget?
    
    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND isActive = 1")
    suspend fun getActiveBudgetByCategory(categoryId: Int): Budget?
    
    @Transaction
    @Query("SELECT * FROM budgets WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getBudgetsWithCategories(): Flow<List<BudgetWithCategory>>
    
    @Query("""
        SELECT b.*, c.name as categoryName, c.icon as categoryIcon, c.color as categoryColor,
               COALESCE(SUM(CASE WHEN t.type = 'Gasto' AND t.date >= b.startDate AND t.date <= b.endDate THEN t.amount ELSE 0 END), 0) as spent
        FROM budgets b
        LEFT JOIN categories c ON b.categoryId = c.id
        LEFT JOIN transactions t ON b.categoryId = t.categoryId
        WHERE b.isActive = 1 AND b.startDate <= :currentTime AND b.endDate >= :currentTime
        GROUP BY b.id
        ORDER BY b.createdAt DESC
    """)
    suspend fun getCurrentBudgetProgress(currentTime: Long): List<BudgetProgressRaw>
    
    @Query("""
        SELECT COALESCE(SUM(CASE WHEN t.type = 'Gasto' AND t.date >= :startDate AND t.date <= :endDate THEN t.amount ELSE 0 END), 0)
        FROM transactions t
        WHERE t.categoryId = :categoryId
    """)
    suspend fun getSpentAmountForPeriod(categoryId: Int, startDate: Long, endDate: Long): Double
    
    @Query("""
        SELECT b.*, c.name as categoryName, c.icon as categoryIcon, c.color as categoryColor,
               COALESCE(SUM(CASE WHEN t.type = 'Gasto' AND t.date >= b.startDate AND t.date <= b.endDate THEN t.amount ELSE 0 END), 0) as spent
        FROM budgets b
        LEFT JOIN categories c ON b.categoryId = c.id
        LEFT JOIN transactions t ON b.categoryId = t.categoryId
        WHERE b.categoryId = :categoryId AND b.isActive = 1 AND b.startDate <= :currentTime AND b.endDate >= :currentTime
        GROUP BY b.id
        LIMIT 1
    """)
    suspend fun getBudgetProgressForCategory(categoryId: Int, currentTime: Long): BudgetProgressRaw?
    
    @Insert
    suspend fun insertBudget(budget: Budget): Long
    
    @Update
    suspend fun updateBudget(budget: Budget)
    
    @Delete
    suspend fun deleteBudget(budget: Budget)
    
    @Query("UPDATE budgets SET isActive = 0 WHERE id = :id")
    suspend fun deactivateBudget(id: Int)
    
    @Query("DELETE FROM budgets WHERE categoryId = :categoryId")
    suspend fun deleteBudgetsByCategory(categoryId: Int)
    
    @Query("""
        SELECT COUNT(*) FROM budgets 
        WHERE categoryId = :categoryId AND isActive = 1 
        AND ((startDate <= :newStartDate AND endDate >= :newStartDate) 
        OR (startDate <= :newEndDate AND endDate >= :newEndDate)
        OR (startDate >= :newStartDate AND endDate <= :newEndDate))
        AND id != :excludeId
    """)
    suspend fun countOverlappingBudgets(
        categoryId: Int, 
        newStartDate: Long, 
        newEndDate: Long, 
        excludeId: Int = 0
    ): Int
}

// Data class for raw budget progress query result
data class BudgetProgressRaw(
    val id: Int,
    val categoryId: Int,
    val amount: Double,
    val period: BudgetPeriod,
    val startDate: Long,
    val endDate: Long,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val spent: Double
)