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
import com.example.admin_ingresos.ui.components.ThemedAlertDialog

@Composable
fun AddTransactionModal(
    show: Boolean,
    onDismiss: () -> Unit,
    onTransactionAdded: () -> Unit
) {
    if (show) {
        ThemedAlertDialog(
            onDismissRequest = onDismiss,
            title = {
                // Keep title text only (remove leading add icon to simplify header)
                Text("Agregar Transacción", style = MaterialTheme.typography.titleLarge)
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
