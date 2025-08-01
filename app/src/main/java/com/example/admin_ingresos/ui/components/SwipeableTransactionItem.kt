package com.example.admin_ingresos.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.data.Transaction
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

enum class SwipeAction {
    NONE, EDIT, DELETE, DUPLICATE
}

@Composable
fun SwipeableTransactionItem(
    transaction: Transaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val swipeThreshold = with(density) { 80.dp.toPx() }
    
    // Animate offset back to 0 when released
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "offsetX"
    )
    
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        // Background actions
        SwipeActionsBackground(
            swipeOffset = animatedOffsetX,
            swipeThreshold = swipeThreshold
        )
        
        // Main transaction item
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            // Determine action based on final offset
                            when {
                                offsetX <= -swipeThreshold * 2 -> {
                                    onDelete()
                                    offsetX = 0f
                                }
                                offsetX <= -swipeThreshold -> {
                                    onEdit()
                                    offsetX = 0f
                                }
                                offsetX >= swipeThreshold -> {
                                    onDuplicate()
                                    offsetX = 0f
                                }
                                else -> {
                                    offsetX = 0f
                                }
                            }
                        }
                    ) { _, dragAmount ->
                        val newOffset = offsetX + dragAmount.x
                        // Limit the swipe range
                        offsetX = newOffset.coerceIn(-swipeThreshold * 2.5f, swipeThreshold * 1.5f)
                    }
                },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            TransactionItemContent(transaction = transaction)
        }
    }
}

@Composable
fun SwipeActionsBackground(
    swipeOffset: Float,
    swipeThreshold: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left side actions (Edit and Delete)
        if (swipeOffset < 0) {
            Row {
                // Edit action
                if (swipeOffset <= -swipeThreshold) {
                    SwipeActionButton(
                        icon = Icons.Default.Edit,
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        contentDescription = "Editar"
                    )
                }
                
                // Delete action
                if (swipeOffset <= -swipeThreshold * 2) {
                    SwipeActionButton(
                        icon = Icons.Default.Delete,
                        backgroundColor = MaterialTheme.colorScheme.error,
                        contentDescription = "Eliminar"
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Right side actions (Duplicate)
        if (swipeOffset > 0 && swipeOffset >= swipeThreshold) {
            SwipeActionButton(
                icon = Icons.Default.ContentCopy,
                backgroundColor = MaterialTheme.colorScheme.secondary,
                contentDescription = "Duplicar"
            )
        }
    }
}

@Composable
fun SwipeActionButton(
    icon: ImageVector,
    backgroundColor: Color,
    contentDescription: String
) {
    Box(
        modifier = Modifier
            .width(80.dp)
            .fillMaxHeight()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun TransactionItemContent(
    transaction: Transaction
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // Header with type and amount
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Transaction type chip
            AssistChip(
                onClick = { },
                label = { 
                    Text(
                        text = transaction.type,
                        style = MaterialTheme.typography.labelSmall
                    ) 
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (transaction.type == "Ingreso") {
                        Color(0xFF4CAF50).copy(alpha = 0.2f)
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    },
                    labelColor = if (transaction.type == "Ingreso") {
                        Color(0xFF4CAF50)
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
            )
            
            // Amount
            Text(
                text = "${if (transaction.type == "Ingreso") "+" else "-"}$${String.format("%.2f", transaction.amount)}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (transaction.type == "Ingreso") {
                    Color(0xFF4CAF50)
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Description and date
        Column {
            Text(
                text = transaction.description,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = dateFormat.format(Date(transaction.date)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        // Category and payment method info
        if (transaction.categoryId > 0 || transaction.paymentMethodId != null) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category info
                if (transaction.categoryId > 0) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Categoría ID: ${transaction.categoryId}") }
                    )
                }
                
                // Payment method info
                transaction.paymentMethodId?.let { paymentMethodId ->
                    AssistChip(
                        onClick = { },
                        label = { Text("Método ID: $paymentMethodId") }
                    )
                }
            }
        }
        
        // Swipe hint
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "← Desliza para editar/eliminar | Desliza para duplicar →",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}