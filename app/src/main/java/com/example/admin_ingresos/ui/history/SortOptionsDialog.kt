package com.example.admin_ingresos.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.data.SortOption

@Composable
fun SortButton(
    currentSortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortDialog by remember { mutableStateOf(false) }
    
    OutlinedButton(
        onClick = { showSortDialog = true },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Sort,
            contentDescription = "Ordenar",
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text("Ordenar")
    }
    
    if (showSortDialog) {
        SortOptionsDialog(
            currentSortOption = currentSortOption,
            onSortOptionSelected = { option ->
                onSortOptionSelected(option)
                showSortDialog = false
            },
            onDismiss = { showSortDialog = false }
        )
    }
}

@Composable
fun SortOptionsDialog(
    currentSortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Ordenar por",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.selectableGroup()
            ) {
                SortOption.values().forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = currentSortOption == option,
                                onClick = { onSortOptionSelected(option) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSortOption == option,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = option.displayName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = getSortDescription(option),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

private fun getSortDescription(sortOption: SortOption): String {
    return when (sortOption) {
        SortOption.DATE_DESC -> "Transacciones más recientes primero"
        SortOption.DATE_ASC -> "Transacciones más antiguas primero"
        SortOption.AMOUNT_DESC -> "Montos más altos primero"
        SortOption.AMOUNT_ASC -> "Montos más bajos primero"
        SortOption.CATEGORY -> "Ordenado alfabéticamente por categoría"
        SortOption.DESCRIPTION -> "Ordenado alfabéticamente por descripción"
    }
}