package com.example.admin_ingresos.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Embedded
import androidx.room.Relation

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val categoryId: Int,
    val amount: Double,
    val period: BudgetPeriod,
    val startDate: Long,
    val endDate: Long,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class BudgetPeriod(val displayName: String, val durationInMillis: Long) {
    WEEKLY("Semanal", 7 * 24 * 60 * 60 * 1000L),
    MONTHLY("Mensual", 30 * 24 * 60 * 60 * 1000L),
    QUARTERLY("Trimestral", 90 * 24 * 60 * 60 * 1000L),
    YEARLY("Anual", 365 * 24 * 60 * 60 * 1000L)
}

data class BudgetWithCategory(
    @Embedded val budget: Budget,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: Category
)

data class BudgetProgress(
    val budget: Budget,
    val category: Category,
    val spent: Double,
    val remaining: Double,
    val percentage: Float,
    val isOverBudget: Boolean,
    val daysRemaining: Int
) {
    val status: BudgetStatus
        get() = when {
            percentage >= 1.2f -> BudgetStatus.OVER_BUDGET
            percentage >= 1.0f -> BudgetStatus.EXCEEDED
            percentage >= 0.8f -> BudgetStatus.WARNING
            else -> BudgetStatus.ON_TRACK
        }
}

enum class BudgetStatus(val displayName: String) {
    ON_TRACK("En progreso"),
    WARNING("Advertencia"),
    EXCEEDED("Excedido"),
    OVER_BUDGET("Sobre presupuesto")
}