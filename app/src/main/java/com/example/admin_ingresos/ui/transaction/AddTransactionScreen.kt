package com.example.admin_ingresos.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.admin_ingresos.data.AppDatabase
import android.app.Application
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.admin_ingresos.ui.transaction.AddTransactionViewModel

@Composable
fun AddTransactionScreen(onSave: () -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val db = remember { com.example.admin_ingresos.AppDatabaseProvider.getDatabase(context) }
    val transactionViewModel: AddTransactionViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AddTransactionViewModel(db) as T
        }
    })
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var type by remember { mutableStateOf("Gasto") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var date by remember { mutableStateOf("Hoy") }
    var selectedPaymentMethodId by remember { mutableStateOf<Int?>(null) }
    val categories by produceState(initialValue = emptyList<com.example.admin_ingresos.data.Category>(), db) {
        value = db.categoryDao().getAll()
    }
    val paymentMethods by produceState(initialValue = emptyList<com.example.admin_ingresos.data.PaymentMethod>(), db) {
        value = db.paymentMethodDao().getAll()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.bg_slate_50))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Nueva Transacción", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { type = "Gasto" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == "Gasto") colorResource(id = R.color.red_500) else Color.LightGray
                ),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Gasto") }
            Button(
                onClick = { type = "Ingreso" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == "Ingreso") colorResource(id = R.color.green_600) else Color.LightGray
                ),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Ingreso") }
        }
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Monto") },
            singleLine = true
        )
        com.example.admin_ingresos.ui.transaction.CategorySelector(
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            onCategorySelected = { selectedCategoryId = it }
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            singleLine = true
        )
        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Fecha") },
            singleLine = true
        )
        com.example.admin_ingresos.ui.transaction.PaymentMethodSelector(
            paymentMethods = paymentMethods,
            selectedPaymentMethodId = selectedPaymentMethodId,
            onPaymentMethodSelected = { selectedPaymentMethodId = it }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull() ?: 0.0
                    transactionViewModel.saveTransaction(
                        amount = amountDouble,
                        type = type,
                        categoryId = selectedCategoryId ?: 0,
                        description = description.text,
                        date = System.currentTimeMillis(),
                        paymentMethodId = selectedPaymentMethodId
                    )
                    onSave()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.indigo_500))
            ) {
                Text("Guardar")
            }
            OutlinedButton(onClick = onCancel, shape = RoundedCornerShape(12.dp)) {
                Text("Cancelar")
            }
        }
    }
}
