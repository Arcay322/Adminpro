package com.example.admin_ingresos.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.admin_ingresos.AppDatabaseProvider
import com.example.admin_ingresos.data.Transaction
import com.example.admin_ingresos.data.ExportService
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen() {
    val context = LocalContext.current
    val database = remember { AppDatabaseProvider.getDatabase(context) }
    val viewModel: TransactionHistoryViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return TransactionHistoryViewModel(database) as T
        }
    })
    
    val transactions by viewModel.transactions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val searchSuggestions by viewModel.searchSuggestions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val paymentMethods by viewModel.paymentMethods.collectAsState()
    val currentSortOption by viewModel.currentSortOption.collectAsState()
    val filterPresets by viewModel.filterPresets.collectAsState()
    
    var showFilterSheet by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    
    // Selection state for long-press context menu
    var selectedTransactions by remember { mutableStateOf(setOf<Transaction>()) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var showContextMenu by remember { mutableStateOf<Transaction?>(null) }
    
    // Pull to refresh state
    val pullToRefreshState = rememberPullToRefreshState()
    
    // Handle pull to refresh
    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) {
            viewModel.loadTransactions()
            pullToRefreshState.endRefresh()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
        // Header
        Text(
            text = "Historial de Transacciones",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search bar with filters
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChanged = { query ->
                viewModel.searchTransactions(query)
            },
            searchSuggestions = searchSuggestions,
            currentFilter = currentFilter,
            filterPresets = filterPresets,
            onFilterClick = { showFilterSheet = true },
            onClearFilters = { viewModel.clearFilters() },
            onPresetSelected = { preset ->
                viewModel.applyFilterPreset(preset)
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Results summary and sort button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${transactions.size} transacciones encontradas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
                
                // Share summary button
                if (transactions.isNotEmpty()) {
                    val context = LocalContext.current
                    val exportService = remember { ExportService(context) }
                    
                    OutlinedButton(
                        onClick = {
                            exportService.shareTextSummary(
                                transactions = transactions,
                                categories = categories,
                                title = "Resumen Financiero - Admin Ingresos"
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir resumen",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compartir")
                    }
                }
                
                // Export button
                if (transactions.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { showExportDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Exportar",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Exportar")
                    }
                }
                
                SortButton(
                    currentSortOption = currentSortOption,
                    onSortOptionSelected = { sortOption ->
                        viewModel.setSortOption(sortOption)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Transaction list
        if (transactions.isEmpty() && !isLoading) {
            // Empty state
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔍",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isNotBlank() || !currentFilter.isEmpty()) {
                            "No se encontraron transacciones"
                        } else {
                            "No hay transacciones registradas"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (searchQuery.isNotBlank() || !currentFilter.isEmpty()) {
                            "Intenta ajustar los filtros de búsqueda"
                        } else {
                            "Agrega tu primera transacción para comenzar"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            com.example.admin_ingresos.ui.components.TransactionListWithContextMenu(
                transactions = transactions,
                selectedTransactions = selectedTransactions,
                isSelectionMode = isSelectionMode,
                onTransactionClick = { transaction ->
                    // Handle single transaction click
                    // TODO: Navigate to transaction details
                },
                onTransactionLongPress = { transaction ->
                    if (!isSelectionMode) {
                        // Enter selection mode and select the long-pressed transaction
                        isSelectionMode = true
                        selectedTransactions = setOf(transaction)
                    }
                },
                onSelectionToggle = { transaction ->
                    selectedTransactions = if (selectedTransactions.contains(transaction)) {
                        selectedTransactions - transaction
                    } else {
                        selectedTransactions + transaction
                    }
                    
                    // Exit selection mode if no items selected
                    if (selectedTransactions.isEmpty()) {
                        isSelectionMode = false
                    }
                },
                onBulkEdit = {
                    // TODO: Implement bulk edit
                    if (selectedTransactions.size == 1) {
                        // Navigate to edit screen for single transaction
                    }
                },
                onBulkDelete = {
                    // Delete selected transactions
                    selectedTransactions.forEach { transaction ->
                        viewModel.deleteTransaction(transaction)
                    }
                    selectedTransactions = emptySet()
                    isSelectionMode = false
                },
                onBulkDuplicate = {
                    // Duplicate selected transactions
                    selectedTransactions.forEach { transaction ->
                        viewModel.duplicateTransaction(transaction)
                    }
                    selectedTransactions = emptySet()
                    isSelectionMode = false
                },
                onBulkExport = {
                    // Export selected transactions
                    val context = LocalContext.current
                    val exportService = remember { ExportService(context) }
                    exportService.exportTransactionsToCSV(
                        transactions = selectedTransactions.toList(),
                        categories = categories,
                        paymentMethods = paymentMethods
                    )
                    selectedTransactions = emptySet()
                    isSelectionMode = false
                },
                onClearSelection = {
                    selectedTransactions = emptySet()
                    isSelectionMode = false
                }
            )
        }
        
        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = pullToRefreshState,
        )
    }
    
    // Filter bottom sheet
    if (showFilterSheet) {
        FilterBottomSheet(
            currentFilter = currentFilter,
            categories = categories,
            paymentMethods = paymentMethods,
            onFilterChanged = { filter ->
                viewModel.applyFilter(filter)
            },
            onDismiss = { showFilterSheet = false }
        )
    }
    
    // Export dialog
    if (showExportDialog) {
        ExportDialog(
            transactions = transactions,
            categories = categories,
            paymentMethods = paymentMethods,
            onDismiss = { showExportDialog = false }
        )
    }
    
    // Context menu dialog
    showContextMenu?.let { transaction ->
        com.example.admin_ingresos.ui.components.ContextMenuDialog(
            transaction = transaction,
            actions = com.example.admin_ingresos.ui.components.getDefaultContextMenuActions(),
            onActionSelected = { action ->
                when (action.id) {
                    "edit" -> {
                        // TODO: Navigate to edit screen
                    }
                    "duplicate" -> {
                        viewModel.duplicateTransaction(transaction)
                    }
                    "share" -> {
                        val exportService = ExportService(context)
                        exportService.shareTextSummary(
                            transactions = listOf(transaction),
                            categories = categories,
                            title = "Transacción - ${transaction.description}"
                        )
                    }
                    "export" -> {
                        val exportService = ExportService(context)
                        exportService.exportTransactionsToCSV(
                            transactions = listOf(transaction),
                            categories = categories,
                            paymentMethods = paymentMethods
                        )
                    }
                    "delete" -> {
                        viewModel.deleteTransaction(transaction)
                    }
                }
                showContextMenu = null
            },
            onDismiss = { showContextMenu = null }
        )
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with type and actions
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
                
                // Action buttons
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Description and amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
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
                
                Spacer(modifier = Modifier.width(16.dp))
                
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
            
            // Category and payment method info
            if (transaction.categoryId > 0 || transaction.paymentMethodId != null) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category info (would need to fetch from database)
                    if (transaction.categoryId > 0) {
                        AssistChip(
                            onClick = { },
                            label = { Text("Categoría ID: ${transaction.categoryId}") }
                        )
                    }
                    
                    // Payment method info (would need to fetch from database)
                    transaction.paymentMethodId?.let { paymentMethodId ->
                        AssistChip(
                            onClick = { },
                            label = { Text("Método ID: $paymentMethodId") }
                        )
                    }
                }
            }
        }
    }
}