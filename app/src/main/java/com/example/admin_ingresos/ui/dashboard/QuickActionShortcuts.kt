package com.example.admin_ingresos.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class QuickActionShortcut(
    val id: String,
    val title: String,
    val description: String,
    val amount: Double,
    val type: String, // "Ingreso" or "Gasto"
    val categoryId: Int?,
    val paymentMethodId: Int?,
    val icon: ImageVector,
    val color: Color,
    val frequency: Int = 0, // How often this transaction is used
    val lastUsed: LocalDateTime? = null,
    val isRecurring: Boolean = false
)

@Composable
fun QuickActionShortcutsWidget(
    shortcuts: List<QuickActionShortcut>,
    onShortcutClick: (QuickActionShortcut) -> Unit,
    onAddShortcut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Accesos Rápidos",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onAddShortcut) {
                    Text("Personalizar")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (shortcuts.isEmpty()) {
                EmptyShortcutsState(onAddShortcut = onAddShortcut)
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(shortcuts) { shortcut ->
                        QuickActionShortcutItem(
                            shortcut = shortcut,
                            onClick = { onShortcutClick(shortcut) }
                        )
                    }
                    
                    // Add new shortcut button
                    item {
                        AddShortcutButton(onClick = onAddShortcut)
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionShortcutItem(
    shortcut: QuickActionShortcut,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(140.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = shortcut.color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = shortcut.icon,
                contentDescription = shortcut.title,
                tint = shortcut.color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = shortcut.title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            
            Text(
                text = "$${String.format("%.0f", shortcut.amount)}",
                style = MaterialTheme.typography.bodySmall,
                color = shortcut.color,
                fontWeight = FontWeight.Bold
            )
            
            if (shortcut.isRecurring) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Recurrente",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddShortcutButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(140.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar acceso rápido",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Agregar",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Acceso",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyShortcutsState(
    onAddShortcut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.TouchApp,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Sin accesos rápidos",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = "Crea accesos directos para tus transacciones más frecuentes",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(onClick = onAddShortcut) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Crear Acceso Rápido")
        }
    }
}

@Composable
fun SmartSuggestionsWidget(
    suggestions: List<TransactionSuggestion>,
    onSuggestionClick: (TransactionSuggestion) -> Unit,
    onDismissSuggestion: (TransactionSuggestion) -> Unit,
    modifier: Modifier = Modifier
) {
    if (suggestions.isEmpty()) return
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sugerencias Inteligentes",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            suggestions.forEach { suggestion ->
                SuggestionItem(
                    suggestion = suggestion,
                    onClick = { onSuggestionClick(suggestion) },
                    onDismiss = { onDismissSuggestion(suggestion) }
                )
                if (suggestion != suggestions.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun SuggestionItem(
    suggestion: TransactionSuggestion,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = suggestion.icon,
                contentDescription = null,
                tint = suggestion.color,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = suggestion.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = suggestion.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "$${String.format("%.0f", suggestion.amount)}",
                style = MaterialTheme.typography.bodyMedium,
                color = suggestion.color,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Descartar sugerencia",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

data class TransactionSuggestion(
    val id: String,
    val title: String,
    val reason: String,
    val amount: Double,
    val type: String,
    val categoryId: Int?,
    val icon: ImageVector,
    val color: Color,
    val confidence: Float // 0.0 to 1.0
)

// Helper function to generate smart suggestions based on transaction history
fun generateSmartSuggestions(
    transactions: List<com.example.admin_ingresos.data.Transaction>,
    categories: List<com.example.admin_ingresos.data.Category>
): List<TransactionSuggestion> {
    val suggestions = mutableListOf<TransactionSuggestion>()
    
    // Analyze recurring patterns
    val recurringTransactions = transactions
        .groupBy { "${it.description}_${it.amount}_${it.categoryId}" }
        .filter { it.value.size >= 3 } // Appears at least 3 times
        .map { (key, txList) ->
            val avgAmount = txList.map { it.amount }.average()
            val lastTx = txList.maxByOrNull { it.date }
            val category = categories.find { it.id == lastTx?.categoryId }
            
            TransactionSuggestion(
                id = "recurring_$key",
                title = lastTx?.description ?: "Transacción recurrente",
                reason = "Aparece frecuentemente (${txList.size} veces)",
                amount = avgAmount,
                type = lastTx?.type ?: "Gasto",
                categoryId = lastTx?.categoryId,
                icon = if (lastTx?.type == "Ingreso") Icons.Default.TrendingUp else Icons.Default.ShoppingCart,
                color = if (lastTx?.type == "Ingreso") Color(0xFF4CAF50) else Color(0xFFFF5722),
                confidence = (txList.size / 10f).coerceAtMost(1f)
            )
        }
    
    suggestions.addAll(recurringTransactions.take(3))
    
    // Monthly recurring suggestions (rent, utilities, etc.)
    val monthlyPatterns = transactions
        .filter { it.amount > 100 } // Significant amounts
        .groupBy { "${it.description}_${it.categoryId}" }
        .filter { it.value.size >= 2 }
        .map { (key, txList) ->
            val avgAmount = txList.map { it.amount }.average()
            val lastTx = txList.maxByOrNull { it.date }
            
            TransactionSuggestion(
                id = "monthly_$key",
                title = lastTx?.description ?: "Gasto mensual",
                reason = "Patrón mensual detectado",
                amount = avgAmount,
                type = lastTx?.type ?: "Gasto",
                categoryId = lastTx?.categoryId,
                icon = Icons.Default.CalendarMonth,
                color = Color(0xFFFF9800),
                confidence = 0.8f
            )
        }
    
    suggestions.addAll(monthlyPatterns.take(2))
    
    return suggestions.sortedByDescending { it.confidence }.take(5)
}
// Hel
per function to generate quick shortcuts based on frequent transactions
fun generateQuickShortcuts(
    transactions: List<com.example.admin_ingresos.data.Transaction>,
    categories: List<com.example.admin_ingresos.data.Category>
): List<QuickActionShortcut> {
    val shortcuts = mutableListOf<QuickActionShortcut>()
    
    // Most frequent transactions
    val frequentTransactions = transactions
        .groupBy { "${it.description}_${it.amount}_${it.categoryId}" }
        .map { (key, txList) ->
            val firstTx = txList.first()
            val category = categories.find { it.id == firstTx.categoryId }
            
            QuickActionShortcut(
                id = "frequent_$key",
                title = firstTx.description.take(15),
                description = category?.name ?: "Sin categoría",
                amount = firstTx.amount,
                type = firstTx.type,
                categoryId = firstTx.categoryId,
                paymentMethodId = firstTx.paymentMethodId,
                icon = when (category?.name?.lowercase()) {
                    "comida", "restaurante", "supermercado" -> Icons.Default.Restaurant
                    "transporte", "gasolina", "uber" -> Icons.Default.DirectionsCar
                    "entretenimiento", "cine", "juegos" -> Icons.Default.Movie
                    "salud", "medicina", "doctor" -> Icons.Default.LocalHospital
                    "educación", "libros", "cursos" -> Icons.Default.School
                    "hogar", "casa", "renta" -> Icons.Default.Home
                    "trabajo", "salario", "freelance" -> Icons.Default.Work
                    else -> if (firstTx.type == "Ingreso") Icons.Default.TrendingUp else Icons.Default.ShoppingCart
                },
                color = if (firstTx.type == "Ingreso") 
                    Color(0xFF4CAF50) 
                else 
                    Color(0xFFFF5722),
                frequency = txList.size,
                lastUsed = null,
                isRecurring = txList.size >= 3
            )
        }
        .sortedByDescending { it.frequency }
        .take(6)
    
    shortcuts.addAll(frequentTransactions)
    
    // Add some default shortcuts if we don't have enough
    if (shortcuts.size < 4) {
        val defaultShortcuts = listOf(
            QuickActionShortcut(
                id = "default_coffee",
                title = "Café",
                description = "Comida",
                amount = 5.0,
                type = "Gasto",
                categoryId = null,
                paymentMethodId = null,
                icon = Icons.Default.LocalCafe,
                color = Color(0xFF8D6E63),
                frequency = 0
            ),
            QuickActionShortcut(
                id = "default_gas",
                title = "Gasolina",
                description = "Transporte",
                amount = 50.0,
                type = "Gasto",
                categoryId = null,
                paymentMethodId = null,
                icon = Icons.Default.LocalGasStation,
                color = Color(0xFFFF9800),
                frequency = 0
            ),
            QuickActionShortcut(
                id = "default_salary",
                title = "Salario",
                description = "Trabajo",
                amount = 2000.0,
                type = "Ingreso",
                categoryId = null,
                paymentMethodId = null,
                icon = Icons.Default.AccountBalance,
                color = Color(0xFF4CAF50),
                frequency = 0,
                isRecurring = true
            )
        )
        
        shortcuts.addAll(defaultShortcuts.take(4 - shortcuts.size))
    }
    
    return shortcuts
}