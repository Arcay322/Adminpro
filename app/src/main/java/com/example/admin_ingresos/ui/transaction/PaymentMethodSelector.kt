package com.example.admin_ingresos.ui.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.data.PaymentMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodSelector(
    paymentMethods: List<PaymentMethod>,
    selectedPaymentMethodId: Int?,
    onPaymentMethodSelected: (Int) -> Unit,
    error: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedPaymentMethod = paymentMethods.find { it.id == selectedPaymentMethodId }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Método de Pago",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedPaymentMethod?.name ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Selecciona método de pago") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    paymentMethods.forEach { method ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = method.name,
                                    style = MaterialTheme.typography.bodyLarge
                                ) 
                            },
                            onClick = {
                                onPaymentMethodSelected(method.id)
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        }
    }
}
