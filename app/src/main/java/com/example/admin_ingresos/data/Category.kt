package com.example.admin_ingresos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val icon: String = "📦", // emoji icon or icon name
    val isFavorite: Boolean = false
)

// Simple class that Room can handle
data class CategoryWithCount(
    val id: Int,
    val name: String,
    val icon: String,
    val isFavorite: Boolean,
    val transactionCount: Int
)

// Data class for category analytics (will be built in code, not by Room)
data class CategoryStats(
    val id: Int,
    val name: String,
    val icon: String,
    val isFavorite: Boolean,
    val usageCount: Int,
    val totalExpenses: Double,
    val totalIncome: Double,
    val lastUsed: Long?
) {
    val totalMovement: Double get() = totalExpenses + totalIncome
    val netAmount: Double get() = totalIncome - totalExpenses
}
