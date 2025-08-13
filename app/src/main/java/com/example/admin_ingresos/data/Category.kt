
package com.example.admin_ingresos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CategoryType { GASTO, INGRESO }

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val icon: String = "\ud83d\udce6", // emoji icon or icon name
    val color: String = "#85C1E9", // hex color code
    val isFavorite: Boolean = false,
    val order: Int = 0, // Para reordenamiento persistente
    val type: CategoryType = CategoryType.GASTO,
    val isArchived: Boolean = false
)

// Simple class that Room can handle
data class CategoryWithCount(
    val id: Int,
    val name: String,
    val icon: String,
    val color: String,
    val isFavorite: Boolean,
    val transactionCount: Int,
    val type: CategoryType,
    val isArchived: Boolean
)

// Data class for category analytics (will be built in code, not by Room)
data class CategoryStats(
    val id: Int,
    val name: String,
    val icon: String,
    val color: String,
    val isFavorite: Boolean,
    val usageCount: Int,
    val totalExpenses: Double,
    val totalIncome: Double,
    val lastUsed: Long?,
    val type: CategoryType,
    val isArchived: Boolean
) {
    val totalMovement: Double get() = totalExpenses + totalIncome
    val netAmount: Double get() = totalIncome - totalExpenses
}
