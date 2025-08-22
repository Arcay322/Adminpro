package com.example.admin_ingresos.ui.savings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.admin_ingresos.AppDatabaseProvider
import com.example.admin_ingresos.data.Transaction
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.ui.components.GlassmorphismScreen
import com.example.admin_ingresos.ui.icons.LucideIconMapper
import com.example.admin_ingresos.ui.theme.AccentVibrantStart
import com.example.admin_ingresos.ui.theme.TextPrimary
import com.example.admin_ingresos.ui.theme.TextSecondary
import com.example.admin_ingresos.ui.theme.GlassWhiteSubtle
import com.example.admin_ingresos.ui.theme.ExpenseRed
import com.example.admin_ingresos.ui.theme.getCategoryColor
import com.example.admin_ingresos.ui.category.TimeFilter
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.LinearProgressIndicator
import com.example.admin_ingresos.ui.theme.IncomeGreen
// Using regular list items for date headers to avoid stickyHeader compatibility issues

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SavingsGoalDetailScreen(
    savingsGoalViewModel: com.example.admin_ingresos.viewmodel.SavingsGoalViewModel,
    savingsGoalId: Long,
    onNavigateBack: () -> Unit
) {
    // Obtain DB locally (don't access ViewModel's private fields)
    val context = LocalContext.current
    val db = remember { AppDatabaseProvider.getDatabase(context) }

    // Observe the goal via DAO flow
    val goal by db.savingsGoalDao().getByIdFlow(savingsGoalId).collectAsState(initial = null)
    // Observe related transactions via DAO flow
    val txList by db.transactionDao().getTransactionsByGoalIdFlow(savingsGoalId).collectAsState(initial = emptyList())
    // Local time filter state (mirror CategoryDetail behaviour)
    var selectedFilter by remember { mutableStateOf(TimeFilter.THIS_MONTH) }

    // Compute filtered transactions based on selected filter
    val (startDate, endDate) = calculateDateRange(selectedFilter)
    val filteredTx = if (selectedFilter == TimeFilter.ALL_TIME) txList else txList.filter { it.date in startDate..endDate }

    GlassmorphismScreen {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = goal?.name ?: "Meta",
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
                if (goal == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentVibrantStart)
                }
            } else {
                val formatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "ES")) }

                // Group transactions by day for sticky headers
                val groupedTransactions = filteredTx.groupBy { tx ->
                    val date = Date(tx.date)
                    SimpleDateFormat("dd MMMM yyyy", Locale("es", "ES")).format(date)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 0.dp)
                ) {
                    item {
                        // Header styled like CategoryDetailHeader using the goal's color
                        val bgColor = try { Color(android.graphics.Color.parseColor(goal!!.color)) } catch (_: Exception) { getCategoryColor(goal!!.name) }
                        com.example.admin_ingresos.ui.components.GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = bgColor.copy(alpha = 0.10f),
                            borderColor = bgColor.copy(alpha = 0.2f)
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
                                            .background(bgColor.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = LucideIconMapper.getSavingsGoalIcon(goal!!.emoji),
                                            contentDescription = goal!!.name,
                                            tint = bgColor,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Column {
                                        Text("Meta", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                        Text(formatter.format(goal!!.currentAmount), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = AccentVibrantStart)
                                        Text("${filteredTx.size} ${if (filteredTx.size == 1) "transacción" else "transacciones"}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                }

                                // Progress bar inside the card
                                if (goal!!.targetAmount > 0.0) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    GoalProgressInfo(
                                        targetAmount = goal!!.targetAmount,
                                        currentAmount = goal!!.currentAmount,
                                        formatter = formatter
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // (Removed duplicate progress bar — progress is shown inside the header card)

                        // Time filter chips (Este Mes / Mes Pasado / Este Año / Todo)
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(TimeFilter.values()) { filter ->
                                val selected = selectedFilter == filter
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedFilter = filter },
                                    label = {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            val icon = when (filter) {
                                                TimeFilter.THIS_MONTH -> LucideIconMapper.getNavigationIcon("Calendar")
                                                TimeFilter.LAST_MONTH -> LucideIconMapper.getNavigationIcon("Rewind")
                                                TimeFilter.THIS_YEAR -> LucideIconMapper.getNavigationIcon("CalendarDays")
                                                TimeFilter.ALL_TIME -> LucideIconMapper.getNavigationIcon("Infinity")
                                            }
                                            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize))
                                            Text(filter.displayName)
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = GlassWhiteSubtle,
                                        labelColor = TextSecondary,
                                        iconColor = TextSecondary,
                                        selectedContainerColor = bgColor,
                                        selectedLabelColor = TextPrimary,
                                        selectedLeadingIconColor = TextPrimary
                                    )
                                )
                            }
                        }
                    }

                    if (filteredTx.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(LucideIconMapper.getNavigationIcon("Receipt"), contentDescription = null, modifier = Modifier.size(48.dp), tint = TextSecondary)
                                    Spacer(Modifier.height(16.dp))
                                    Text("No hay movimientos para esta meta", color = TextSecondary)
                                }
                            }
                        }
                    } else {
                        // Render grouped transactions with date headers (regular items for compatibility)
                        groupedTransactions.forEach { (date, dayTransactions) ->
                            item {
                                DateHeader(date = date)
                            }
                            items(dayTransactions, key = { it.id }) { transaction ->
                                TransactionRow(transaction = transaction)
                            }
                        }
                    }
                }
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
        color = TextPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.9f))
            .padding(vertical = 8.dp)
    )
}

@Composable
private fun GoalProgressInfo(targetAmount: Double, currentAmount: Double, formatter: NumberFormat) {
    val progress = (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f)
    val progressColor = when {
        progress > 0.9f -> ExpenseRed
        progress > 0.7f -> Color(0xFFFBBF24)
        else -> IncomeGreen
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Meta: ${formatter.format(targetAmount)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = "${(progress * 100).toInt()}% Completado",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = progressColor
            )
        }
        // Gradient progress bar: start red -> end green for savings
        com.example.admin_ingresos.ui.theme.GradientProgressBar(
            progress = progress,
            startColor = ExpenseRed,
            endColor = IncomeGreen,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// Helper to compute date ranges for filters (same logic as CategoryDetailViewModel)
private fun calculateDateRange(filter: TimeFilter): Pair<Long, Long> {
    val calendar = java.util.Calendar.getInstance()
    return when (filter) {
        TimeFilter.THIS_MONTH -> {
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
            setMidnight(calendar)
            val start = calendar.timeInMillis

            calendar.add(java.util.Calendar.MONTH, 1)
            calendar.add(java.util.Calendar.DAY_OF_MONTH, -1)
            setEndOfDay(calendar)
            val end = calendar.timeInMillis
            start to end
        }
        TimeFilter.LAST_MONTH -> {
            calendar.add(java.util.Calendar.MONTH, -1)
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
            setMidnight(calendar)
            val start = calendar.timeInMillis

            calendar.add(java.util.Calendar.MONTH, 1)
            calendar.add(java.util.Calendar.DAY_OF_MONTH, -1)
            setEndOfDay(calendar)
            val end = calendar.timeInMillis
            start to end
        }
        TimeFilter.THIS_YEAR -> {
            calendar.set(java.util.Calendar.DAY_OF_YEAR, 1)
            setMidnight(calendar)
            val start = calendar.timeInMillis

            calendar.add(java.util.Calendar.YEAR, 1)
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
            setEndOfDay(calendar)
            val end = calendar.timeInMillis
            start to end
        }
        TimeFilter.ALL_TIME -> 0L to Long.MAX_VALUE
    }
}

private fun setMidnight(calendar: java.util.Calendar) {
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
    calendar.set(java.util.Calendar.MINUTE, 0)
    calendar.set(java.util.Calendar.SECOND, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)
}

private fun setEndOfDay(calendar: java.util.Calendar) {
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
    calendar.set(java.util.Calendar.MINUTE, 59)
    calendar.set(java.util.Calendar.SECOND, 59)
    calendar.set(java.util.Calendar.MILLISECOND, 999)
}

@Composable
private fun TransactionRow(transaction: Transaction) {
    val formatter = remember { java.text.NumberFormat.getCurrencyInstance(Locale("es", "ES")) }
    val amountColor = if (transaction.type == com.example.admin_ingresos.data.Transaction.TYPE_EXPENSE) com.example.admin_ingresos.ui.theme.ExpenseRed else com.example.admin_ingresos.ui.theme.AccentVibrantStart
    val dateFormat = remember { java.text.SimpleDateFormat("HH:mm", Locale.getDefault()) }

    androidx.compose.foundation.layout.Row(
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
                text = dateFormat.format(java.util.Date(transaction.date)),
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
