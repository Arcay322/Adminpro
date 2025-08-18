package com.example.admin_ingresos.ui.history

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.admin_ingresos.AppDatabaseProvider
import com.example.admin_ingresos.data.Transaction
import com.example.admin_ingresos.ui.components.*
import com.example.admin_ingresos.ui.components.GlassmorphismScreen
import com.example.admin_ingresos.ui.icons.LucideIconMapper
import com.example.admin_ingresos.ui.theme.CashFlowPrimary
import com.example.admin_ingresos.ui.theme.AccentVibrantStart
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: TransactionHistoryViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return TransactionHistoryViewModel(AppDatabaseProvider.getDatabase(context)) as T
        }
    })
    val uiState by viewModel.uiState.collectAsState()

    // Filtrar y ordenar transacciones
    val filteredTransactions = remember(uiState.transactions, uiState.searchQuery, uiState.sortOption) {
        var filtered = uiState.transactions

        // Filtrar por búsqueda
        if (uiState.searchQuery.isNotBlank()) {
            filtered = filtered.filter { transaction ->
                transaction.description.contains(uiState.searchQuery, ignoreCase = true) ||
                        transaction.amount.toString().contains(uiState.searchQuery)
            }
        }

        // Ordenar
        when (uiState.sortOption) {
            SortOption.DATE_DESC -> filtered.sortedByDescending { it.date }
            SortOption.DATE_ASC -> filtered.sortedBy { it.date }
            SortOption.AMOUNT_DESC -> filtered.sortedByDescending { it.amount }
            SortOption.AMOUNT_ASC -> filtered.sortedBy { it.amount }
            SortOption.DESCRIPTION -> filtered.sortedBy { it.description }
            SortOption.CATEGORY -> filtered.sortedByDescending { it.description }
        }
    }

    // Agrupar transacciones por fecha
    val groupedTransactions = remember(filteredTransactions) {
        filteredTransactions.groupBy {
            val date = Date(it.date)
            SimpleDateFormat("dd MMMM yyyy", Locale("es", "ES")).format(date)
        }
    }

    GlassmorphismScreen(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header moderno
            item {
                ModernHistoryHeader()
            }

            // Estadísticas de transacciones
            if (filteredTransactions.isNotEmpty()) {
                item {
                    ModernTransactionStats(
                        transactions = filteredTransactions,
                        onAnalyticsClick = { viewModel.onAnalyticsClick(true) }
                    )
                }
            }

            // Búsqueda y filtros con glassmorphism
            item {
                ModernSearchAndFilters(
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = viewModel::onSearchQueryChanged,
                    selectedType = "Todos", // TODO: Implement type filter in ViewModel
                    onTypeChange = { /* TODO */ },
                    sortOption = uiState.sortOption,
                    onSortOrderChange = viewModel::onSortOptionChanged,
                    showFilters = uiState.showFilters,
                    onToggleFilters = { viewModel.onToggleFilters(!uiState.showFilters) }
                )
            }

            // Indicador de resultados
            if (filteredTransactions.isNotEmpty()) {
                item {
                    ModernResultsIndicator(
                        totalResults = filteredTransactions.size,
                        hasFilters = uiState.searchQuery.isNotBlank() // TODO: Enhance this
                    )
                }
            }

            // Lista de transacciones agrupadas
            if (groupedTransactions.isNotEmpty()) {
                groupedTransactions.forEach { (date, dayTransactions) ->
                    item {
                        ModernDateHeader(date = date, transactionCount = dayTransactions.size)
                    }

                    items(dayTransactions) {
                        ModernTransactionItem(
                            transaction = it,
                            onEdit = { viewModel.showEditDialog(it) },
                            onDelete = { viewModel.showDeleteDialog(it) },
                            onClick = { navController.navigate("transaction_detail/${it.id}") }
                        )
                    }
                }
            } else {
                item {
                    ModernEmptyHistoryState(
                        hasSearch = uiState.searchQuery.isNotBlank(),
                        onClearSearch = { viewModel.onSearchQueryChanged("") }
                    )
                }
            }
        }

        // Analytics Modal
        if (uiState.showAnalytics) {
            ModernAnalyticsModal(
                transactions = filteredTransactions,
                onClose = { viewModel.onAnalyticsClick(false) }
            )
        }

        // Delete Confirmation Dialog
        uiState.transactionToDelete?.let { transaction ->
            DeleteConfirmationDialog(
                transaction = transaction,
                onConfirm = { viewModel.deleteTransaction(transaction) },
                onDismiss = { viewModel.hideDeleteDialog() }
            )
        }

        // Edit Dialog
        uiState.transactionToEdit?.let { transaction ->
            EditTransactionDialog(
                transaction = transaction,
                onConfirm = { updatedTransaction -> viewModel.updateTransaction(updatedTransaction) },
                onDismiss = { viewModel.hideEditDialog() }
            )
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    transaction: Transaction,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Confirmar Eliminación",
                    color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "¿Estás seguro de que deseas eliminar la transacción '${transaction.description}'?\n\nEsta acción no se puede deshacer.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Eliminar",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancelar",
                    color = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.7f)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        iconContentColor = com.example.admin_ingresos.ui.theme.TextPrimary,
        titleContentColor = com.example.admin_ingresos.ui.theme.TextPrimary,
        textContentColor = com.example.admin_ingresos.ui.theme.TextPrimary
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transaction: Transaction,
    onConfirm: (Transaction) -> Unit,
    onDismiss: () -> Unit
) {
    var editDescription by remember { mutableStateOf(transaction.description) }
    var editAmount by remember { mutableStateOf(transaction.amount.toString()) }
    var editType by remember { mutableStateOf(transaction.type) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Editar Transacción",
                color = com.example.admin_ingresos.ui.theme.TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = editDescription,
                    onValueChange = { editDescription = it },
                    label = { Text("Descripción", color = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = com.example.admin_ingresos.ui.theme.TextPrimary,
                        unfocusedTextColor = com.example.admin_ingresos.ui.theme.TextPrimary,
                        focusedBorderColor = CashFlowPrimary,
                        unfocusedBorderColor = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.3f),
                        cursorColor = CashFlowPrimary
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = editAmount,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            editAmount = newValue
                        }
                    },
                    label = { Text("Monto", color = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = com.example.admin_ingresos.ui.theme.TextPrimary,
                        unfocusedTextColor = com.example.admin_ingresos.ui.theme.TextPrimary,
                        focusedBorderColor = CashFlowPrimary,
                        unfocusedBorderColor = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.3f),
                        cursorColor = CashFlowPrimary
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                Text("Tipo de transacción", color = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Ingreso", "Gasto").forEach { type ->
                        FilterChip(
                            selected = editType == type,
                            onClick = { editType = type },
                            label = { Text(type, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CashFlowPrimary,
                                selectedLabelColor = com.example.admin_ingresos.ui.theme.TextOnAccent,
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                                labelColor = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            val updatedAmount = editAmount.toDoubleOrNull()
            val isEnabled = editDescription.isNotBlank() && updatedAmount != null && updatedAmount > 0
            TextButton(
                onClick = {
                    if (isEnabled) {
                        val updatedTransaction = transaction.copy(
                            description = editDescription.trim(),
                            amount = updatedAmount!!,
                            type = editType
                        )
                        onConfirm(updatedTransaction)
                    }
                },
                enabled = isEnabled
            ) {
                Text("Guardar", color = if (isEnabled) CashFlowPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancelar",
                    color = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.7f)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    )
}


@Composable
fun ModernTransactionItem(
    transaction: Transaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit = {}
) {
    val isIncome = transaction.type == "Ingreso"
    val transactionColor = if (isIncome) Color(0xFF4CAF50) else Color(0xFFE57373)
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de transacción con glassmorphism
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                transactionColor.copy(alpha = 0.2f),
                                transactionColor.copy(alpha = 0.05f)
                            ),
                            radius = 60f
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Use a contrasting tint for icons placed over colored circles
                val iconTint = if (transactionColor.luminance() > 0.5f) com.example.admin_ingresos.ui.theme.TextOnAccent else com.example.admin_ingresos.ui.theme.TextPrimary
                Icon(
                    imageVector = if (isIncome) 
                        LucideIconMapper.getTransactionTypeIcon("Ingreso")
                    else 
                        LucideIconMapper.getTransactionTypeIcon("Gasto"),
                    contentDescription = transaction.type,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información de la transacción
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = com.example.admin_ingresos.ui.theme.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = SimpleDateFormat("dd MMM, HH:mm", Locale("es", "ES"))
                        .format(Date(transaction.date)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = com.example.admin_ingresos.ui.theme.TextSecondary
                )
                
                // Monto con estilo mejorado
                Text(
                    text = "${if (isIncome) "+" else "-"}${currencyFormat.format(transaction.amount)}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = transactionColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Botones de acción con glassmorphism
            Column {
                Row {
                    IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = LucideIconMapper.getNavigationIcon("Edit"),
                                contentDescription = "Editar",
                                tint = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = LucideIconMapper.getNavigationIcon("Delete"),
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernHistoryHeader() {
    // Standalone header (icon + title) moved outside any GlassCard to match Reports style
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = LucideIconMapper.getNavigationIcon("History"),
                contentDescription = "Historial",
                tint = AccentVibrantStart,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = "Historial de Transacciones",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = com.example.admin_ingresos.ui.theme.TextPrimary
                )
                Text(
                    text = "Visualiza y gestiona todas tus transacciones",
                    style = MaterialTheme.typography.bodyMedium,
                    color = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ModernTransactionStats(
    transactions: List<Transaction>,
    onClose: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {}
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    
    // Calcular estadísticas
    val totalIncome = transactions.filter { it.type == "Ingreso" }.sumOf { it.amount }
    val totalExpenses = transactions.filter { it.type == "Gasto" }.sumOf { it.amount }
    val netAmount = totalIncome - totalExpenses
    
    GlassCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header del resumen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Resumen Financiero",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = com.example.admin_ingresos.ui.theme.TextPrimary
                    )
                    Text(
                        text = "Balance actual de tus transacciones",
                        style = MaterialTheme.typography.bodyMedium,
                        color = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = LucideIconMapper.getNavigationIcon("Close"),
                        contentDescription = "Cerrar",
                        tint = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Tarjetas principales de resumen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Ingresos
                ImprovedStatsCard(
                    title = "Ingresos",
                    value = currencyFormat.format(totalIncome),
                    count = transactions.count { it.type == "Ingreso" },
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                
                // Gastos
                ImprovedStatsCard(
                    title = "Gastos", 
                    value = currencyFormat.format(totalExpenses),
                    count = transactions.count { it.type == "Gasto" },
                    color = Color(0xFFE57373),
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Balance total
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = if (netAmount >= 0) {
                                listOf(
                                    Color(0xFF4CAF50).copy(alpha = 0.2f),
                                    Color(0xFF81C784).copy(alpha = 0.1f)
                                )
                            } else {
                                listOf(
                                    Color(0xFFE57373).copy(alpha = 0.2f),
                                    Color(0xFFEF5350).copy(alpha = 0.1f)
                                )
                            }
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Balance Total",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.9f)
                        )
                        Text(
                            text = currencyFormat.format(netAmount),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (netAmount >= 0) Color(0xFF4CAF50) else Color(0xFFE57373),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Icon(
                        imageVector = if (netAmount >= 0) 
                            LucideIconMapper.getTransactionTypeIcon("Ingreso") 
                        else 
                            LucideIconMapper.getTransactionTypeIcon("Gasto"),
                        contentDescription = null,
                        tint = if (netAmount >= 0) Color(0xFF4CAF50) else Color(0xFFE57373),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            }
        }
    }


@Composable
fun ImprovedStatsCard(
    title: String,
    value: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.15f),
                        color.copy(alpha = 0.05f)
                    ),
                    radius = 100f
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "$count transacciones",
                style = MaterialTheme.typography.bodySmall,
                color = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.15f),
                        color.copy(alpha = 0.05f)
                    ),
                    radius = 100f
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable 
fun DetailedStatItem(
    label: String,
    value: String
) {
    Column {
            Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = com.example.admin_ingresos.ui.theme.TextPrimary
        )
    }
}

@Composable
fun ModernSearchAndFilters(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedType: String,
    onTypeChange: (String) -> Unit,
    sortOption: SortOption,
    onSortOrderChange: (SortOption) -> Unit,
    showFilters: Boolean,
    onToggleFilters: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search bar with glassmorphism
        GlassCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = LucideIconMapper.getNavigationIcon("Search"),
                    contentDescription = "Buscar",
                    tint = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "Buscar transacciones...",
                            color = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = com.example.admin_ingresos.ui.theme.TextPrimary,
                        unfocusedTextColor = com.example.admin_ingresos.ui.theme.TextPrimary,
                        focusedBorderColor = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.3f),
                        unfocusedBorderColor = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.2f),
                        cursorColor = CashFlowPrimary
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { keyboardController?.hide() }
                    ),
                    singleLine = true
                )
                
                IconButton(
                    onClick = { onToggleFilters() }
                ) {
                    Icon(
                        imageVector = LucideIconMapper.getNavigationIcon("Filter"),
                        contentDescription = "Filtros",
                        tint = if (selectedType != "Todos" || sortOption != SortOption.DATE_DESC) 
                            CashFlowPrimary else com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        // Filters section
        AnimatedVisibility(
            visible = showFilters,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            GlassCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Type filter
                    Text(
                        text = "Tipo de Transacción",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = com.example.admin_ingresos.ui.theme.TextPrimary
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listOf("Todos", "Ingreso", "Gasto")) { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { onTypeChange(type) },
                                label = { 
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = when (type) {
                                                "Todos" -> LucideIconMapper.getNavigationIcon("List")
                                                "Ingreso" -> LucideIconMapper.getTransactionTypeIcon("Ingreso")
                                                "Gasto" -> LucideIconMapper.getTransactionTypeIcon("Gasto")
                                                else -> LucideIconMapper.getNavigationIcon("List")
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = type,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CashFlowPrimary,
                                    selectedLabelColor = com.example.admin_ingresos.ui.theme.TextOnAccent,
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                                    labelColor = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                    
                    // Sort order
                    Text(
                        text = "Ordenar por",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(SortOption.values()) { option ->
                            FilterChip(
                                selected = sortOption == option,
                                onClick = { onSortOrderChange(option) },
                                label = { 
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = when (option) {
                                                SortOption.DATE_DESC -> LucideIconMapper.getNavigationIcon("CalendarDown")
                                                SortOption.DATE_ASC -> LucideIconMapper.getNavigationIcon("CalendarUp")
                                                SortOption.AMOUNT_DESC -> LucideIconMapper.getNavigationIcon("ArrowUp")
                                                SortOption.AMOUNT_ASC -> LucideIconMapper.getNavigationIcon("ArrowDown")
                                                SortOption.DESCRIPTION -> LucideIconMapper.getNavigationIcon("ArrowUp")
                                                SortOption.CATEGORY -> LucideIconMapper.getNavigationIcon("ArrowDown")
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = option.displayName, 
                                            maxLines = 1,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CashFlowPrimary,
                                    selectedLabelColor = com.example.admin_ingresos.ui.theme.TextOnAccent,
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                                    labelColor = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernResultsIndicator(
    totalResults: Int,
    hasFilters: Boolean
) {
    GlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = LucideIconMapper.getNavigationIcon("History"),
                    contentDescription = "Resultados",
                    tint = Color(0xFF667eea),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "$totalResults ${if (totalResults == 1) "transacción" else "transacciones"}",
                    style = MaterialTheme.typography.titleMedium,
                    color = com.example.admin_ingresos.ui.theme.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                if (hasFilters) {
                    Text(
                        text = "• Filtrado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CashFlowPrimary
                    )
                }
            }
            
            // Export button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.08f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LucideIconMapper.getNavigationIcon("Download"),
                    contentDescription = "Exportar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ModernEmptyHistoryState(
    hasSearch: Boolean,
    onClearSearch: () -> Unit
) {
    GlassCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated icon
            val infiniteTransition = rememberInfiniteTransition(label = "empty_animation")
            val animatedAlpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha_animation"
            )
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF667eea).copy(alpha = 0.2f),
                                Color(0xFF667eea).copy(alpha = 0.05f)
                            ),
                            radius = 100f
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (hasSearch) LucideIconMapper.getNavigationIcon("Search") else LucideIconMapper.getNavigationIcon("Receipt"),
                    contentDescription = if (hasSearch) "Sin resultados" else "Sin transacciones",
                    tint = Color(0xFF667eea).copy(alpha = animatedAlpha),
                    modifier = Modifier.size(48.dp)
                )
            }
            
            Text(
                text = if (hasSearch) "Sin resultados" else "No hay transacciones",
                style = MaterialTheme.typography.headlineSmall,
                color = com.example.admin_ingresos.ui.theme.TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = if (hasSearch) 
                    "No encontramos transacciones que coincidan con tu búsqueda" 
                else 
                    "Aún no tienes transacciones registradas. ¡Comienza añadiendo tu primera transacción!",
                style = MaterialTheme.typography.bodyLarge,
                color = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
            
            if (hasSearch) {
                OutlinedButton(
                    onClick = onClearSearch,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = com.example.admin_ingresos.ui.theme.TextPrimary
                    ),
                    border = BorderStroke(1.dp, com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.3f))
                ) {
                    Icon(
                        imageVector = LucideIconMapper.getNavigationIcon("Close"),
                        contentDescription = "Limpiar búsqueda",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Limpiar búsqueda")
                }
            }
        }
    }
}

@Composable 
fun ModernDateHeader(
    date: String,
    transactionCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = com.example.admin_ingresos.ui.theme.TextPrimary
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "($transactionCount)",
            style = MaterialTheme.typography.bodyMedium,
            color = com.example.admin_ingresos.ui.theme.TextSecondary
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Box(
            modifier = Modifier
                .height(1.dp)
                .weight(1f)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun ModernAnalyticsModal(
    transactions: List<Transaction>,
    onClose: () -> Unit
) {
    // Analytics modal content would go here
    // For now, just show a placeholder
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Analytics Detallado",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = LucideIconMapper.getNavigationIcon("Close"),
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Próximamente: Analytics detallado con gráficos y estadísticas avanzadas",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}