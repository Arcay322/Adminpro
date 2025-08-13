package com.example.admin_ingresos.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY `order` ASC, name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE isArchived = 0 AND type = :type ORDER BY `order` ASC, name ASC")
    fun getActiveCategoriesByType(type: String): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE isArchived = 1 ORDER BY `order` ASC, name ASC")
    fun getArchivedCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories ORDER BY `order` ASC, name ASC")
    suspend fun getCategoriesList(): List<Category>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): Category?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT MAX(`order`) FROM categories")
    suspend fun getMaxOrder(): Int?

    @Update
    suspend fun updateAll(categories: List<Category>)

    @Query("UPDATE categories SET isArchived = 1 WHERE id = :categoryId")
    suspend fun archiveCategory(categoryId: Int)

    @Query("UPDATE categories SET isArchived = 0 WHERE id = :categoryId")
    suspend fun unarchiveCategory(categoryId: Int)
}