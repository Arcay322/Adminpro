package com.example.admin_ingresos.ui.category

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.data.Category
import com.example.admin_ingresos.data.CategoryType
import com.example.admin_ingresos.data.Transaction
import com.example.admin_ingresos.ui.components.GlassCard
import com.example.admin_ingresos.ui.components.GlassmorphismScreen
import com.example.admin_ingresos.ui.icons.LucideIconMapper
import com.example.admin_ingresos.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CategoryDetailScreen(
    viewModel: CategoryDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val category = uiState.category
    val transactions = uiState.transactions

    // Agrupar transacciones por fecha (igual que en la pantalla de historial)
    val groupedTransactions = transactions.groupBy { transaction ->
        val date = Date(transaction.date)
        SimpleDateFormat("dd MMMM yyyy", Locale("es", "ES")).format(date)
    }

    GlassmorphismScreen {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = category?.name ?: "Detalles",
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver atrás",
                                tint = TextPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentVibrantStart)
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = uiState.error ?: "Error desconocido",
                        color = ExpenseRed,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }
            } else if (category != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header con el resumen de la categoría
                    item {
                        CategoryDetailHeader(category = category, transactions = transactions)
                    }

                    // Lista de transacciones
                    if (transactions.isEmpty()) {
                        item {
                            EmptyState()
                        }
                    } else {
                        groupedTransactions.forEach { (date, dayTransactions) ->
                            // Encabezado de fecha
                            stickyHeader {
                                DateHeader(date = date)
                            }
                            // Items de transacción
                            items(dayTransactions, key = { it.id }) { transaction ->
                                TransactionListItem(transaction = transaction)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryDetailHeader(category: Category, transactions: List<Transaction>) {
    val totalAmount = transactions.sumOf { it.amount }
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "ES")) }
    val categoryColor by remember(category.color) { mutableStateOf(Color(android.graphics.Color.parseColor(category.color))) }
    val amountColor = if (category.type == CategoryType.GASTO) ExpenseRed else AccentVibrantStart

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = categoryColor.copy(alpha = 0.1f),
        borderColor = categoryColor.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icono de la categoría
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                val iconVector = LucideIconMapper.getCategoryIcon(category)
                Icon(
                    imageVector = iconVector,
                    contentDescription = category.name,
                    tint = TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            // Stats
            Column {
                Text(
                    text = "Total ${category.type.name.lowercase()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = formatter.format(totalAmount),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                Text(
                    text = "${transactions.size} ${if (transactions.size == 1) "transacción" else "transacciones"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun DateHeader(date: String) {
    Text(
        text = date,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary,
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundEnd.copy(alpha = 0.8f)) // Fondo para que se lea sobre el contenido
            .padding(vertical = 8.dp)
    )
}

@Composable
private fun TransactionListItem(transaction: Transaction) {
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "ES")) }
    val amountColor = if (transaction.type == CategoryType.GASTO.name) ExpenseRed else AccentVibrantStart
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.description,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = dateFormat.format(Date(transaction.date)),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Text(
            text = formatter.format(transaction.amount),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(
                imageVector = LucideIconMapper.getNavigationIcon("Receipt"),
                contentDescription = "Sin transacciones",
                modifier = Modifier.size(48.dp),
                tint = TextSecondary
            )
            Text(
                text = "Sin Transacciones",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary
            )
            Text(
                text = "No hay transacciones registradas para esta categoría.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}