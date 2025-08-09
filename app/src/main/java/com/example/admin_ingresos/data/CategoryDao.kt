package com.example.admin_ingresos.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY `order` ASC, name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories ORDER BY `order` ASC, name ASC")
    suspend fun getCategoriesList(): List<Category>

    @Query("UPDATE categories SET `order` = :newOrder WHERE id = :categoryId")
    suspend fun updateCategoryOrder(categoryId: Int, newOrder: Int)

    @androidx.room.Transaction
    suspend fun reorderCategories(categoryIdsInOrder: List<Int>) {
        categoryIdsInOrder.forEachIndexed { index, id ->
            updateCategoryOrder(id, index)
        }
    }

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): Category?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)
}
