package com.example.admin_ingresos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import com.example.admin_ingresos.ui.transaction.AddTransactionScreenNew

@Composable
fun AddTransactionModal(
    show: Boolean,
    onDismiss: () -> Unit,
    onTransactionAdded: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar Transacción", style = MaterialTheme.typography.titleLarge)
                }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth()) {
                    AddTransactionScreenNew(
                        onSave = {
                            onTransactionAdded()
                            onDismiss()
                        },
                        onCancel = onDismiss
                    )
                }
            },
            confirmButton = {},
            dismissButton = {},
            shape = RoundedCornerShape(24.dp)
        )
    }
}
