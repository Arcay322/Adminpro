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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.admin_ingresos.AppDatabaseProvider
import com.example.admin_ingresos.data.Transaction
import com.example.admin_ingresos.ui.components.*
import com.example.admin_ingresos.ui.components.resolvedMenuContainerColor
import com.example.admin_ingresos.ui.components.GlassmorphismScreen
import com.example.admin_ingresos.ui.components.ThemedAlertDialog
import com.example.admin_ingresos.ui.icons.LucideIconMapper
import com.example.admin_ingresos.ui.theme.CashFlowPrimary
import com.example.admin_ingresos.ui.theme.AccentVibrantStart
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    var selectedType by remember { mutableStateOf("Todos") }
    var showExportDialog by remember { mutableStateOf(false) }
    var paymentMethods by remember { mutableStateOf<List<com.example.admin_ingresos.data.PaymentMethod>>(emptyList()) }

    // Filtrar y ordenar transacciones
    val filteredTransactions = remember(uiState.transactions, uiState.searchQuery, uiState.sortOption, selectedType) {
        var filtered = uiState.transactions

        // Filtrar por búsqueda
        if (uiState.searchQuery.isNotBlank()) {
            filtered = filtered.filter { transaction ->
                transaction.description.contains(uiState.searchQuery, ignoreCase = true) ||
                        transaction.amount.toString().contains(uiState.searchQuery)
            }
        }

        // Filtrar por tipo (Ingreso/Gasto/Ahorro)
        if (selectedType != "Todos") {
            filtered = filtered.filter { transaction ->
                when (selectedType) {
                    "Ingreso" -> transaction.type == com.example.admin_ingresos.data.Transaction.TYPE_INCOME
                    "Gasto" -> transaction.type == com.example.admin_ingresos.data.Transaction.TYPE_EXPENSE
                    "Ahorro" -> transaction.type == com.example.admin_ingresos.data.Transaction.TYPE_TRANSFER
                    else -> true
                }
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
            // Main balance (copied from Dashboard MainBalanceCards)
            item {
                MainBalanceCardsHistory(
                    transactions = filteredTransactions,
                    onViewDetails = { viewModel.onAnalyticsClick(true) }
                )
            }

            // Estadísticas de transacciones (se removió el contenedor aquí porque MainBalanceCardsHistory ya muestra el resumen)

            // Búsqueda y filtros con glassmorphism
            item {
                ModernSearchAndFilters(
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = viewModel::onSearchQueryChanged,
                    selectedType = selectedType,
                    onTypeChange = { selectedType = it },
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
                        hasFilters = uiState.searchQuery.isNotBlank(), // TODO: Enhance this
                        onExportClick = { showExportDialog = true }
                    )
                }
            }

            // Lista de transacciones agrupadas
            if (groupedTransactions.isNotEmpty()) {
                groupedTransactions.forEach { (date, dayTransactions) ->
                    item {
                        ModernDateHeader(date = date, transactionCount = dayTransactions.size)
                    }

                            items(dayTransactions) { tx ->
                                val category = uiState.categories.find { c -> c.id == tx.categoryId } ?: com.example.admin_ingresos.data.Category.uncategorized()
                                ModernTransactionItem(
                                    transaction = tx,
                                    category = category,
                                    onEdit = { viewModel.showEditDialog(tx) },
                                    onDelete = { viewModel.showDeleteDialog(tx) },
                                    onClick = { navController.navigate("transaction_detail/${tx.id}") }
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

        // Export dialog
        if (showExportDialog) {
            ExportDialog(
                transactions = filteredTransactions,
                categories = uiState.categories,
                paymentMethods = paymentMethods,
                onDismiss = { showExportDialog = false }
            )
        }
    }

    // Load payment methods once so ExportDialog can use them
    LaunchedEffect(Unit) {
        try {
            paymentMethods = withContext(Dispatchers.IO) {
                AppDatabaseProvider.getDatabase(context).paymentMethodDao().getAll()
            }
        } catch (_: Exception) {
            // ignore
        }
    }
}

@Composable
fun MainBalanceCardsHistory(
    transactions: List<com.example.admin_ingresos.data.Transaction>,
    onViewDetails: () -> Unit
) {
    // Compute totals and delegate rendering to the shared MainBalanceCards so visuals match Dashboard exactly.
    val totalIncome = transactions.filter { it.type == com.example.admin_ingresos.data.Transaction.TYPE_INCOME }.sumOf { it.amount }
    val totalExpenses = transactions.filter { it.type == com.example.admin_ingresos.data.Transaction.TYPE_EXPENSE }.sumOf { it.amount }
    val totalTransfers = transactions.filter { it.type == com.example.admin_ingresos.data.Transaction.TYPE_TRANSFER }.sumOf { it.amount }
    val netAmount = totalIncome - totalExpenses - totalTransfers

    // provide subtitles showing transaction counts instead of "Este mes"
    val incomeCount = transactions.count { it.type == com.example.admin_ingresos.data.Transaction.TYPE_INCOME }
    val expensesCount = transactions.count { it.type == com.example.admin_ingresos.data.Transaction.TYPE_EXPENSE }
    val transfersCount = transactions.count { it.type == com.example.admin_ingresos.data.Transaction.TYPE_TRANSFER }
    val totalCount = transactions.size

    MainBalanceCards(
        currentBalance = netAmount,
        monthlyIncome = totalIncome,
        monthlyExpenses = totalExpenses,
        monthlyTransfers = totalTransfers,
        modifier = Modifier.fillMaxWidth(),
        onViewDetails = onViewDetails,
        balanceSubtitle = if (totalCount == 1) "$totalCount transacción" else "$totalCount transacciones",
        incomeSubtitle = if (incomeCount == 1) "$incomeCount transacción" else "$incomeCount transacciones",
        expensesSubtitle = if (expensesCount == 1) "$expensesCount transacción" else "$expensesCount transacciones",
        transfersSubtitle = if (transfersCount == 1) "$transfersCount transacción" else "$transfersCount transacciones"
    )
}

@Composable
fun DeleteConfirmationDialog(
    transaction: Transaction,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ThemedAlertDialog(
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

    ThemedAlertDialog(
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
                    listOf("Ingreso", "Gasto", "Ahorro").forEach { type ->
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
    category: com.example.admin_ingresos.data.Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit = {}
) {
    val isIncome = transaction.type == com.example.admin_ingresos.data.Transaction.TYPE_INCOME
    val isExpense = transaction.type == com.example.admin_ingresos.data.Transaction.TYPE_EXPENSE
    val isTransfer = transaction.type == com.example.admin_ingresos.data.Transaction.TYPE_TRANSFER
    // derive category color from hex string; fallback to neutral
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(category.color))
    } catch (e: Exception) {
        Color(0xFF85C1E9)
    }
    // amount color: income green, expense red, but for transfers (ahorro) use the category color
    val amountColor = when {
        isIncome -> Color(0xFF4CAF50)
        isTransfer -> Color(0xFF2196F3) // blue for savings/transfer
        else -> Color(0xFFE57373)
    }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        onClick = onClick
    ) {
        // container Box so we can overlay the overflow menu on the top-right
        Box(modifier = Modifier.fillMaxWidth()) {
            var menuExpanded by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
            // Icono de categoría con glassmorphism
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                categoryColor.copy(alpha = 0.2f),
                                categoryColor.copy(alpha = 0.05f)
                            ),
                            radius = 60f
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Preferir icono guardado por nombre (opciones disponibles) antes del fallback
                val available = com.example.admin_ingresos.ui.icons.LucideIconMapper.getAvailableCategoryIcons()
                val iconOption = available.find { it.name.equals(category.icon, ignoreCase = true) }
                val catIcon = if (iconOption != null) {
                    com.example.admin_ingresos.ui.icons.LucideIconMapper.getIconFromEmoji(iconOption.icon)
                } else {
                    com.example.admin_ingresos.ui.icons.LucideIconMapper.getCategoryIcon(category)
                }

                Icon(
                    imageVector = catIcon,
                    contentDescription = category.name,
                    tint = categoryColor,
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
                
                // Monto con estilo mejorado (no-wrap)
                val absAmt = kotlin.math.abs(transaction.amount)
                val sign = when {
                    isExpense -> "-"
                    isIncome -> "+"
                    else -> "" // transfers/ahorro: no sign
                }

                Text(
                    text = "$sign${currencyFormat.format(absAmt).replace(" ", "\u00A0")}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = amountColor,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            }

            // Overflow menu placed in the top-right corner of the card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = LucideIconMapper.Navigation.more,
                        contentDescription = "Más opciones",
                        tint = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(resolvedMenuContainerColor())
                ) {
                    DropdownMenuItem(text = { Text("Editar") }, onClick = {
                        menuExpanded = false
                        onEdit()
                    }, leadingIcon = { Icon(LucideIconMapper.getNavigationIcon("Edit"), null) })

                    DropdownMenuItem(text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) }, onClick = {
                        menuExpanded = false
                        onDelete()
                    }, leadingIcon = { Icon(LucideIconMapper.getNavigationIcon("Delete"), null, tint = MaterialTheme.colorScheme.error) })
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
                imageVector = LucideIconMapper.Navigation.transactions,
                contentDescription = "Transacciones",
                tint = AccentVibrantStart,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = "Transacciones",
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
    val totalIncome = transactions.filter { it.type == com.example.admin_ingresos.data.Transaction.TYPE_INCOME }.sumOf { it.amount }
    val totalExpenses = transactions.filter { it.type == com.example.admin_ingresos.data.Transaction.TYPE_EXPENSE }.sumOf { it.amount }
    val totalTransfers = transactions.filter { it.type == com.example.admin_ingresos.data.Transaction.TYPE_TRANSFER }.sumOf { it.amount }
    val netAmount = totalIncome - totalExpenses - totalTransfers
    
    GlassCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // (Old summary cards removed; MainBalanceCardsHistory provides the summary above.)
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
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = color,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
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

@OptIn(ExperimentalMaterial3Api::class)
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
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = LucideIconMapper.getNavigationIcon("Search"),
                    contentDescription = "Buscar",
                    tint = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )

                // Box with subtle border so the input matches other glass containers
                var searchFocused by remember { mutableStateOf(false) }
                // Use the shared glassmorphism modifier so the search box matches other containers
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .glassmorphism(
                            cornerRadius = 10.dp,
                            borderWidth = 1.dp
                        ),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = com.example.admin_ingresos.ui.theme.TextPrimary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 12.dp, end = 12.dp)
                            .onFocusChanged { searchFocused = it.isFocused },
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Buscar transacciones...",
                                        color = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.6f),
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { onToggleFilters() }
                    ) {
                        Icon(
                            imageVector = LucideIconMapper.getNavigationIcon("Filter"),
                            contentDescription = "Filtros",
                            tint = if (selectedType != "Todos" || sortOption != SortOption.DATE_DESC)
                                CashFlowPrimary else com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Filtros",
                        style = MaterialTheme.typography.bodySmall,
                        color = com.example.admin_ingresos.ui.theme.TextPrimary.copy(alpha = 0.7f)
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
                        items(listOf("Todos", "Ingreso", "Gasto", "Ahorro")) { type ->
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
                                                "Ingreso" -> LucideIconMapper.getTransactionTypeIcon(com.example.admin_ingresos.data.Transaction.TYPE_INCOME)
                                                "Gasto" -> LucideIconMapper.getTransactionTypeIcon(com.example.admin_ingresos.data.Transaction.TYPE_EXPENSE)
                                                "Ahorro" -> LucideIconMapper.getTransactionTypeIcon(com.example.admin_ingresos.data.Transaction.TYPE_TRANSFER)
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
                        color = com.example.admin_ingresos.ui.theme.TextPrimary
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
    hasFilters: Boolean,
    onExportClick: () -> Unit
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
            
            // Export button (clickable)
            IconButton(
                onClick = onExportClick,
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
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
        
    Spacer(modifier = Modifier.weight(1f))
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