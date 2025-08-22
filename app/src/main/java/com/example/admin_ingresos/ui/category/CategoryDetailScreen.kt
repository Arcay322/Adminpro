package com.example.admin_ingresos.ui.category

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.lerp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.data.Budget
import com.example.admin_ingresos.data.Category
import com.example.admin_ingresos.data.CategoryType
import com.example.admin_ingresos.data.Transaction
import com.example.admin_ingresos.ui.components.GlassCard
import com.example.admin_ingresos.ui.components.GlassmorphismScreen
import com.example.admin_ingresos.ui.icons.LucideIconMapper
import com.example.admin_ingresos.ui.theme.AccentVibrantStart
import com.example.admin_ingresos.ui.theme.BackgroundEnd
import com.example.admin_ingresos.ui.theme.ExpenseRed
import com.example.admin_ingresos.ui.theme.GlassWhiteSubtle
import com.example.admin_ingresos.ui.theme.IncomeGreen
import com.example.admin_ingresos.ui.theme.TextPrimary
import com.example.admin_ingresos.ui.theme.TextSecondary
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CategoryDetailScreen(
    viewModel: CategoryDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val category = uiState.category

    val groupedTransactions = uiState.transactions.groupBy { transaction ->
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
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Header con el resumen de la categoría y presupuesto
                    item {
                        CategoryDetailHeader(
                            category = category,
                            totalAmount = uiState.totalAmountForPeriod,
                            transactionCount = uiState.transactions.size,
                            activeBudget = uiState.activeBudget
                        )
                    }

                    // Filtros de tiempo
                    item {
                        TimeFilterChips(
                            selectedFilter = uiState.selectedFilter,
                            onFilterSelected = { viewModel.setTimeFilter(it) }
                        )
                    }

                    // Lista de transacciones
                    if (uiState.transactions.isEmpty()) {
                        item {
                            EmptyState()
                        }
                    } else {
                        groupedTransactions.forEach { (date, dayTransactions) ->
                            stickyHeader {
                                DateHeader(date = date)
                            }
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
private fun CategoryDetailHeader(
    category: Category,
    totalAmount: Double,
    transactionCount: Int,
    activeBudget: Budget?
) {
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "ES")) }
    val categoryColor by remember(category.color) { mutableStateOf(Color(android.graphics.Color.parseColor(category.color))) }
    val amountColor = if (category.type == CategoryType.GASTO) ExpenseRed else AccentVibrantStart

    // --- Lógica unificada para obtener el icono ---
    val iconVector = remember(category.icon) {
        val iconOption = LucideIconMapper.getAvailableCategoryIcons().find { it.name == category.icon }
        if (iconOption != null) {
            LucideIconMapper.getIconFromEmoji(iconOption.icon)
        } else {
            LucideIconMapper.getCategoryIcon(category)
        }
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = categoryColor.copy(alpha = 0.1f),
        borderColor = categoryColor.copy(alpha = 0.2f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = category.name,
                                    tint = categoryColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
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
                        text = "$transactionCount ${if (transactionCount == 1) "transacción" else "transacciones"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            if (category.type == CategoryType.AHORRO) {
                // Show savings-style inverted gradient progress (red -> green)
                Spacer(modifier = Modifier.height(16.dp))
                val target = activeBudget?.amount ?: (totalAmount + 1.0)
                val progress = (totalAmount / target).toFloat().coerceIn(0f, 1f)
                com.example.admin_ingresos.ui.theme.GradientProgressBar(
                    progress = progress,
                    startColor = ExpenseRed,
                    endColor = IncomeGreen,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "${formatter.format(totalAmount)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(text = "${formatter.format(target)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            } else if (activeBudget != null && category.type == CategoryType.GASTO) {
                Spacer(modifier = Modifier.height(16.dp))
                BudgetProgressInfo(
                    budget = activeBudget,
                    spentAmount = totalAmount,
                    formatter = formatter
                )
            }
        }
    }
}

@Composable
private fun BudgetProgressInfo(
    budget: Budget,
    spentAmount: Double,
    formatter: NumberFormat
) {
    val progress = (spentAmount / budget.amount).toFloat().coerceIn(0f, 1f)
    // Percentage text color interpolates between IncomeGreen (0f) and ExpenseRed (1f)
    val pctColor = lerp(IncomeGreen, ExpenseRed, progress)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Presupuesto: ${formatter.format(budget.amount)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = "${(progress * 100).toInt()}% Usado",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = pctColor
            )
        }

        com.example.admin_ingresos.ui.theme.GradientProgressBar(
            progress = progress,
            startColor = IncomeGreen,
            endColor = ExpenseRed,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeFilterChips(
    selectedFilter: TimeFilter,
    onFilterSelected: (TimeFilter) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(TimeFilter.values()) { filter ->
            val selected = selectedFilter == filter
            FilterChip(
                selected = selected,
                onClick = { onFilterSelected(filter) },
                label = {
                    // --- CORRECCIÓN AQUÍ: Icono dentro de la etiqueta ---
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val icon = when (filter) {
                            TimeFilter.THIS_MONTH -> LucideIconMapper.getNavigationIcon("Calendar")
                            TimeFilter.LAST_MONTH -> LucideIconMapper.getNavigationIcon("Rewind")
                            TimeFilter.THIS_YEAR -> LucideIconMapper.getNavigationIcon("CalendarDays")
                            TimeFilter.ALL_TIME -> LucideIconMapper.getNavigationIcon("Infinity")
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                        Text(filter.displayName)
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = GlassWhiteSubtle,
                    labelColor = TextSecondary,
                    iconColor = TextSecondary,
                    selectedContainerColor = AccentVibrantStart,
                    selectedLabelColor = TextPrimary,
                    selectedLeadingIconColor = TextPrimary
                )
            )
        }
    }
}


@Composable
private fun DateHeader(date: String) {
    Text(
        text = date,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = TextPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundEnd.copy(alpha = 0.8f))
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
                text = "No hay transacciones registradas para este período.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}