package com.example.admin_ingresos.ui.transaction

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.data.PaymentMethod
import androidx.compose.foundation.layout.fillMaxWidth

@Composable
fun PaymentMethodSelector(
    paymentMethods: List<PaymentMethod>,
    selectedPaymentMethodId: Int?,
    onPaymentMethodSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedPaymentMethod = paymentMethods.find { it.id == selectedPaymentMethodId }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedPaymentMethod?.name ?: "Selecciona método de pago")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            paymentMethods.forEach { method ->
                DropdownMenuItem(
                    text = { Text(method.name) },
                    onClick = {
                        onPaymentMethodSelected(method.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
