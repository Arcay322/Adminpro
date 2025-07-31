package com.example.admin_ingresos.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.admin_ingresos.data.AppDatabase
import com.example.admin_ingresos.data.Transaction
import kotlinx.coroutines.launch

class AddTransactionViewModel(private val db: AppDatabase) : ViewModel() {
    fun saveTransaction(
        amount: Double,
        type: String,
        categoryId: Int,
        description: String,
        date: Long,
        paymentMethodId: Int?
    ) {
        viewModelScope.launch {
            db.transactionDao().insert(
                Transaction(
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    description = description,
                    date = date,
                    paymentMethodId = paymentMethodId
                )
            )
        }
    }
}
