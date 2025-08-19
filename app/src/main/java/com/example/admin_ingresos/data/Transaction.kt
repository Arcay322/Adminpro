package com.example.admin_ingresos.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val type: String, // "Ingreso" o "Gasto"
    val categoryId: Int,
    val description: String,
    val date: Long, // timestamp
    val paymentMethodId: Int?,
    val goalId: Long? = null,
    val receiptPhotoUri: String? = null // URI de la foto del recibo
)

{
    companion object {
        const val TYPE_INCOME = "Ingreso"
        const val TYPE_EXPENSE = "Gasto"
        const val TYPE_TRANSFER = "Transfer"
    }
}
