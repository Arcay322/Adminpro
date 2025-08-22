package com.example.admin_ingresos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val emoji: String,
    val color: String = "#4CAF50",
    val description: String? = null,
    val targetDate: Date? = null,
    val createdAt: Date = Date(),
    val isActive: Boolean = true,
    val priority: Int = 0 // 0 = baja, 1 = media, 2 = alta
    ,
    // Link each savings goal to its own AHORRO category (nullable for migration compatibility)
    val categoryId: Int? = null
) {
    val progressPercentage: Float
        get() = if (targetAmount > 0) (currentAmount / targetAmount).toFloat().coerceAtMost(1f) else 0f
    
    val remainingAmount: Double
        get() = (targetAmount - currentAmount).coerceAtLeast(0.0)
    
    val isCompleted: Boolean
        get() = currentAmount >= targetAmount
}
