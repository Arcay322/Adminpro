package com.example.admin_ingresos.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAll(): List<Category>
    
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllFlow(): Flow<List<Category>>
    
    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAllCategories(): List<Category>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Int): Category?
    
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): Category?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun update(category: Category)
    
    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun delete(category: Category)
    
    @Delete
    suspend fun deleteCategory(category: Category)
    
    // Additional methods for category usage and statistics
    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId")
    suspend fun getCategoryUsageCount(categoryId: Int): Int
    
    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE categoryId = :categoryId AND type = 'Gasto'")
    suspend fun getCategoryTotalExpenses(categoryId: Int): Double
    
    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE categoryId = :categoryId AND type = 'Ingreso'")
    suspend fun getCategoryTotalIncome(categoryId: Int): Double
    
    @Query("SELECT MAX(date) FROM transactions WHERE categoryId = :categoryId")
    suspend fun getCategoryLastUsed(categoryId: Int): Long?
}
