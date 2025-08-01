package com.example.admin_ingresos.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.data.Transaction
import java.text.SimpleDateFormat
import java.util.*

data class ContextMenuAction(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val color: Color = Color.Unspecified,
    val isDestructive: Boolean = false
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LongPressTransactionItem(
    transaction: Transaction,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onSelectionToggle: (Transaction) -> Unit,
    onLongPress: (Transaction) -> Unit,
    onClick: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onSelectionToggle(transaction)
                    } else {
                        onClick(transaction)
                    }
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (isSelectionMode) {
                        onSelectionToggle(transaction)
                    } else {
                        onLongPress(transaction)
                    }
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection checkbox (visible in selection mode)
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelectionToggle(transaction) },
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
            
            // Transaction type indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (transaction.type == "Ingreso") 
                            Color(0xFF4CAF50).copy(alpha = 0.15f)
                        else 
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transaction.type == "Ingreso") 
                        Icons.Default.TrendingUp 
                    else 
                        Icons.Default.TrendingDown,
                    contentDescription = transaction.type,
                    tint = if (transaction.type == "Ingreso") 
                        Color(0xFF4CAF50)
                    else 
                        MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Transaction details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = dateFormat.format(Date(transaction.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Amount
            Text(
                text = "${if (transaction.type == "Ingreso") "+" else "-"}${String.format("%.2f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == "Ingreso") 
                    Color(0xFF4CAF50)
                else 
                    MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun BulkActionBar(
    selectedCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onExport: () -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection info
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClearSelection) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancelar selección",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Text(
                    text = "$selectedCount seleccionadas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedCount == 1) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                IconButton(onClick = onDuplicate) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicar",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                IconButton(onClick = onExport) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Exportar",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun ContextMenuDialog(
    transaction: Transaction,
    actions: List<ContextMenuAction>,
    onActionSelected: (ContextMenuAction) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Acciones",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                // Transaction info
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = transaction.description,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${if (transaction.type == "Ingreso") "+" else "-"}${String.format("%.2f", transaction.amount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (transaction.type == "Ingreso") 
                                Color(0xFF4CAF50)
                            else 
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Actions
                actions.forEach { action ->
                    TextButton(
                        onClick = { onActionSelected(action) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (action.isDestructive) 
                                MaterialTheme.colorScheme.error
                            else if (action.color != Color.Unspecified)
                                action.color
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = action.title,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = action.title,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun TransactionListWithContextMenu(
    transactions: List<Transaction>,
    selectedTransactions: Set<Transaction>,
    isSelectionMode: Boolean,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionLongPress: (Transaction) -> Unit,
    onSelectionToggle: (Transaction) -> Unit,
    onBulkEdit: () -> Unit,
    onBulkDelete: () -> Unit,
    onBulkDuplicate: () -> Unit,
    onBulkExport: () -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Bulk action bar (visible in selection mode)
        if (isSelectionMode && selectedTransactions.isNotEmpty()) {
            BulkActionBar(
                selectedCount = selectedTransactions.size,
                onEdit = onBulkEdit,
                onDelete = onBulkDelete,
                onDuplicate = onBulkDuplicate,
                onExport = onBulkExport,
                onClearSelection = onClearSelection,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // Transaction list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(transactions) { transaction ->
                LongPressTransactionItem(
                    transaction = transaction,
                    isSelected = selectedTransactions.contains(transaction),
                    isSelectionMode = isSelectionMode,
                    onSelectionToggle = onSelectionToggle,
                    onLongPress = onTransactionLongPress,
                    onClick = onTransactionClick
                )
            }
        }
    }
}

// Helper function to get default context menu actions
fun getDefaultContextMenuActions(): List<ContextMenuAction> {
    return listOf(
        ContextMenuAction(
            id = "edit",
            title = "Editar",
            icon = Icons.Default.Edit
        ),
        ContextMenuAction(
            id = "duplicate",
            title = "Duplicar",
            icon = Icons.Default.ContentCopy
        ),
        ContextMenuAction(
            id = "share",
            title = "Compartir",
            icon = Icons.Default.Share
        ),
        ContextMenuAction(
            id = "export",
            title = "Exportar",
            icon = Icons.Default.FileDownload
        ),
        ContextMenuAction(
            id = "delete",
            title = "Eliminar",
            icon = Icons.Default.Delete,
            color = MaterialTheme.colorScheme.error,
            isDestructive = true
        )
    )
}