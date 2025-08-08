package com.example.admin_ingresos.ui.reports

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.admin_ingresos.ui.components.*
import com.example.admin_ingresos.data.Transaction
import com.example.admin_ingresos.data.Category
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreenNew() {
    val context = LocalContext.current
    val db = remember { com.example.admin_ingresos.AppDatabaseProvider.getDatabase(context) }
    
    // State management
    var selectedPeriod by remember { mutableStateOf("Este mes") }
    var selectedReportType by remember { mutableStateOf("Resumen") }
    
    // Data loading
    val transactions by produceState(initialValue = emptyList<Transaction>(), db, selectedPeriod) {
        value = when (selectedPeriod) {
            "Hoy" -> filterByToday(db.transactionDao().getAll())
            "Esta semana" -> filterByThisWeek(db.transactionDao().getAll())
            "Este mes" -> filterByThisMonth(db.transactionDao().getAll())
            "Este año" -> filterByThisYear(db.transactionDao().getAll())
            else -> db.transactionDao().getAll()
        }
    }
    
    val categories by produceState(initialValue = emptyList<Category>(), db) {
 value = db.categoryDao().getCategoriesList()
    }
    
    // Calculate data for reports
    val totalIncome = transactions.filter { it.type == "Ingreso" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == "Gasto" }.sumOf { it.amount }
    val netAmount = totalIncome - totalExpense
    
    val expensesByCategory = transactions
        .filter { it.type == "Gasto" }
        .groupBy { it.categoryId }
        .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
        .map { (categoryId, amount) ->
            val category = categories.find { it.id == categoryId }
            CategoryExpense(category?.name ?: "Sin categoría", amount)
        }
        .sortedByDescending { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Enhanced Header
        CashFlowHeader(
            title = "Reportes",
            subtitle = getPeriodSubtitle(selectedPeriod, transactions.size),
            actions = {
                IconButton(onClick = { /* TODO: Export report */ }) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Exportar",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period selector
            item {
                PeriodSelectorCard(
                    selectedPeriod = selectedPeriod,
                    onPeriodChange = { selectedPeriod = it }
                )
            }
            
            // Report type selector
            item {
                ReportTypeSelectorCard(
                    selectedType = selectedReportType,
                    onTypeChange = { selectedReportType = it }
                )
            }
            
            // Main summary card
            item {
                MainSummaryCard(
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    netAmount = netAmount,
                    transactionCount = transactions.size
                )
            }
            
            when (selectedReportType) {
                "Resumen" -> {
                    // Pie chart for expenses by category
                    if (expensesByCategory.isNotEmpty()) {
                        item {
                            ExpensesByCategoryChart(expensesByCategory)
                        }
                    }
                    
                    // Monthly trend
                    item {
                        MonthlyTrendChart(transactions)
                    }
                    
                    // Top categories
                    item {
                        TopCategoriesCard(expensesByCategory.take(5))
                    }
                }
                
                "Categorías" -> {
                    // Detailed category breakdown
                    items(expensesByCategory) { categoryExpense ->
                        CategoryDetailCard(
                            categoryExpense = categoryExpense,
                            totalExpense = totalExpense,
                            transactions = transactions.filter { 
                                categories.find { cat -> cat.id == it.categoryId }?.name == categoryExpense.categoryName 
                            }
                        )
                    }
                }
                
                "Tendencias" -> {
                    // Daily spending pattern
                    item {
                        DailySpendingPatternCard(transactions)
                    }
                    
                    // Weekly comparison
                    item {
                        WeeklyComparisonCard(transactions)
                    }
                    
                    // Insights
                    item {
                        FinancialInsightsCard(
                            totalIncome = totalIncome,
                            totalExpense = totalExpense,
                            expensesByCategory = expensesByCategory,
                            transactions = transactions
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodSelectorCard(
    selectedPeriod: String,
    onPeriodChange: (String) -> Unit
) {
    CashFlowCard {
        Column {
            Text(
                text = "Período",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Hoy", "Esta semana", "Este mes", "Este año").forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { onPeriodChange(period) },
                        label = { Text(period) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportTypeSelectorCard(
    selectedType: String,
    onTypeChange: (String) -> Unit
) {
    CashFlowCard {
        Column {
            Text(
                text = "Tipo de Reporte",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Resumen", "Categorías", "Tendencias").forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { onTypeChange(type) },
                        label = { Text(type) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MainSummaryCard(
    totalIncome: Double,
    totalExpense: Double,
    netAmount: Double,
    transactionCount: Int
) {
    CashFlowCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Resumen Financiero",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryMetric(
                    title = "Ingresos",
                    value = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(totalIncome),
                    icon = Icons.Default.TrendingUp,
                    color = Color(0xFF4CAF50)
                )
                
                SummaryMetric(
                    title = "Gastos",
                    value = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(totalExpense),
                    icon = Icons.Default.TrendingDown,
                    color = Color(0xFFE57373)
                )
                
                SummaryMetric(
                    title = "Balance",
                    value = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(netAmount),
                    icon = if (netAmount >= 0) Icons.Default.AccountBalanceWallet else Icons.Default.Warning,
                    color = if (netAmount >= 0) Color(0xFF4CAF50) else Color(0xFFE57373)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Transacciones",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = transactionCount.toString(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                if (totalExpense > 0) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Tasa de ahorro",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val savingsRate = if (totalIncome > 0) ((totalIncome - totalExpense) / totalIncome * 100) else 0.0
                        Text(
                            text = "${String.format("%.1f", savingsRate)}%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (savingsRate >= 20) Color(0xFF4CAF50) else if (savingsRate >= 10) Color(0xFFFF9800) else Color(0xFFE57373)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = color
        )
    }
}

@Composable
private fun ExpensesByCategoryChart(expensesByCategory: List<CategoryExpense>) {
    CashFlowCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gastos por Categoría",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                TextButton(onClick = { /* TODO: Ver detalles */ }) {
                    Text(
                        text = "Ver todo",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (expensesByCategory.isNotEmpty()) {
                // Convert to CategoryData for DonutChart
                val categoryColors = listOf(
                    Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7),
                    Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF03DAC5),
                    Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39),
                    Color(0xFFFFEB3B), Color(0xFFFFC107), Color(0xFFFF9800)
                )
                
                val total = expensesByCategory.sumOf { it.amount }
                val categories = expensesByCategory.take(6).mapIndexed { index, expense ->
                    CategoryData(
                        name = expense.categoryName,
                        percentage = (expense.amount / total * 100).toFloat(),
                        color = categoryColors[index % categoryColors.size]
                    )
                }
                
                // DonutChart with Canvas drawing
                DonutChart(
                    categories = categories,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(vertical = 10.dp),
                    totalAmount = total
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Leyenda con datos reales
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(expensesByCategory.take(6)) { expense ->
                        val categoryData = categories.find { it.name == expense.categoryName }
                        if (categoryData != null) {
                            CategoryLegendItem(
                                category = categoryData,
                                amount = expense.amount
                            )
                        }
                    }
                }
            } else {
                // Mensaje cuando no hay datos
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Sin datos",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Sin gastos este período",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    categories: List<CategoryData>,
    modifier: Modifier = Modifier,
    totalAmount: Double = 0.0
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Asegurar que el Canvas sea cuadrado para mantener la forma circular
        val size = 200.dp
        Canvas(
            modifier = Modifier
                .size(size)
                .aspectRatio(1f) // Forzar proporción 1:1 para un círculo perfecto
        ) {
            val total = categories.sumOf { it.percentage.toDouble() }
            var currentAngle = -90f
            val canvasSize = this.size.minDimension
            val strokeWidth = (canvasSize * 0.2f) // 20% del diámetro para el grosor del anillo
            val radius = (canvasSize - strokeWidth) / 2f
            val center = androidx.compose.ui.geometry.Offset(
                x = this.size.width / 2f,
                y = this.size.height / 2f
            )
            
            categories.forEach { category ->
                val sweepAngle = (category.percentage / total * 360).toFloat()
                
                drawArc(
                    color = category.color,
                    startAngle = currentAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(strokeWidth),
                    topLeft = androidx.compose.ui.geometry.Offset(
                        x = center.x - radius - strokeWidth / 2f,
                        y = center.y - radius - strokeWidth / 2f
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        width = (radius + strokeWidth / 2f) * 2f,
                        height = (radius + strokeWidth / 2f) * 2f
                    )
                )
                
                currentAngle += sweepAngle
            }
        }
        
        // Contenido central del donut - mejorado para evitar saltos de línea
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = "Total",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (totalAmount > 0) formatter.format(totalAmount) else "$0",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CategoryLegendItem(category: CategoryData, amount: Double = 0.0) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(category.color, CircleShape)
        )
        
        Column {
            Text(
                text = category.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                softWrap = false
            )
            Text(
                text = if (amount > 0) formatter.format(amount) else "${category.percentage.toInt()}%",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun MonthlyTrendChart(transactions: List<Transaction>) {
    CashFlowCard {
        Column {
            Text(
                text = "Tendencia Mensual",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            AndroidView(
                factory = { context ->
                    LineChart(context).apply {
                        description.isEnabled = false
                        legend.isEnabled = true
                        xAxis.setDrawGridLines(false)
                        axisLeft.setDrawGridLines(true)
                        axisRight.isEnabled = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                update = { chart ->
                    // Process data for monthly trend
                    val monthlyData = transactions
                        .groupBy { 
                            val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                            "${cal.get(Calendar.MONTH)}-${cal.get(Calendar.YEAR)}"
                        }
                        .map { (month, monthTransactions) ->
                            val income = monthTransactions.filter { it.type == "Ingreso" }.sumOf { it.amount }
                            val expense = monthTransactions.filter { it.type == "Gasto" }.sumOf { it.amount }
                            Triple(month, income, expense)
                        }
                        .sortedBy { it.first }
                    
                    val incomeEntries = monthlyData.mapIndexed { index, (_, income, _) ->
                        Entry(index.toFloat(), income.toFloat())
                    }
                    
                    val expenseEntries = monthlyData.mapIndexed { index, (_, _, expense) ->
                        Entry(index.toFloat(), expense.toFloat())
                    }
                    
                    val incomeDataSet = LineDataSet(incomeEntries, "Ingresos").apply {
                        color = Color(0xFF4CAF50).toArgb()
                        setCircleColor(Color(0xFF4CAF50).toArgb())
                        lineWidth = 2f
                    }
                    
                    val expenseDataSet = LineDataSet(expenseEntries, "Gastos").apply {
                        color = Color(0xFFE57373).toArgb()
                        setCircleColor(Color(0xFFE57373).toArgb())
                        lineWidth = 2f
                    }
                    
                    chart.data = LineData(incomeDataSet, expenseDataSet)
                    chart.invalidate()
                }
            )
        }
    }
}

@Composable
private fun TopCategoriesCard(topCategories: List<CategoryExpense>) {
    CashFlowCard {
        Column {
            Text(
                text = "Principales Categorías de Gasto",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            topCategories.forEach { categoryExpense ->
                CategoryRankingItem(categoryExpense)
                if (categoryExpense != topCategories.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryRankingItem(categoryExpense: CategoryExpense) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = getCategoryIcon(categoryExpense.categoryName),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = categoryExpense.categoryName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(categoryExpense.amount),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// Data classes and helper functions
data class CategoryExpense(
    val categoryName: String,
    val amount: Double
)

private fun getPeriodSubtitle(period: String, transactionCount: Int): String {
    return "$transactionCount transacciones en ${period.lowercase()}"
}

// Helper functions for filtering (same as in TransactionHistoryScreenNew.kt)
private fun filterByToday(transactions: List<Transaction>): List<Transaction> {
    val today = Calendar.getInstance()
    return transactions.filter {
        val transactionDate = Calendar.getInstance().apply { timeInMillis = it.date }
        today.get(Calendar.DAY_OF_YEAR) == transactionDate.get(Calendar.DAY_OF_YEAR) &&
        today.get(Calendar.YEAR) == transactionDate.get(Calendar.YEAR)
    }
}

private fun filterByThisWeek(transactions: List<Transaction>): List<Transaction> {
    val today = Calendar.getInstance()
    val startOfWeek = Calendar.getInstance().apply {
        timeInMillis = today.timeInMillis
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    
    return transactions.filter { it.date >= startOfWeek.timeInMillis }
}

private fun filterByThisMonth(transactions: List<Transaction>): List<Transaction> {
    val today = Calendar.getInstance()
    return transactions.filter {
        val transactionDate = Calendar.getInstance().apply { timeInMillis = it.date }
        today.get(Calendar.MONTH) == transactionDate.get(Calendar.MONTH) &&
        today.get(Calendar.YEAR) == transactionDate.get(Calendar.YEAR)
    }
}

private fun filterByThisYear(transactions: List<Transaction>): List<Transaction> {
    val today = Calendar.getInstance()
    return transactions.filter {
        val transactionDate = Calendar.getInstance().apply { timeInMillis = it.date }
        today.get(Calendar.YEAR) == transactionDate.get(Calendar.YEAR)
    }
}

@Composable
private fun CategoryDetailCard(
    categoryExpense: CategoryExpense,
    totalExpense: Double,
    transactions: List<Transaction>
) {
    CashFlowCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getCategoryIcon(categoryExpense.categoryName),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = categoryExpense.categoryName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    val percentage = if (totalExpense > 0) (categoryExpense.amount / totalExpense * 100) else 0.0
                    Text(
                        text = "${String.format("%.1f", percentage)}% del total • ${transactions.size} transacciones",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(categoryExpense.amount),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = if (totalExpense > 0) (categoryExpense.amount / totalExpense).toFloat() else 0f,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun DailySpendingPatternCard(transactions: List<Transaction>) {
    CashFlowCard {
        Column {
            Text(
                text = "Patrón de Gastos Diarios",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            val dailySpending = transactions
                .filter { it.type == "Gasto" }
                .groupBy { 
                    val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                    cal.get(Calendar.DAY_OF_WEEK)
                }
                .mapValues { (_, dayTransactions) -> dayTransactions.sumOf { it.amount } }
            
            val dayNames = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
            val maxSpending = dailySpending.values.maxOrNull() ?: 1.0
            
            dayNames.forEachIndexed { index, dayName ->
                val dayOfWeek = if (index == 6) 1 else index + 2 // Calendar.SUNDAY = 1
                val spending = dailySpending[dayOfWeek] ?: 0.0
                val percentage = if (maxSpending > 0) spending / maxSpending else 0.0
                
                DaySpendingItem(
                    dayName = dayName,
                    amount = spending,
                    percentage = percentage.toFloat()
                )
                
                if (index < dayNames.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DaySpendingItem(
    dayName: String,
    amount: Double,
    percentage: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(40.dp)
        )
        
        LinearProgressIndicator(
            progress = percentage,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        
        Text(
            text = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(amount),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(100.dp)
        )
    }
}

@Composable
private fun WeeklyComparisonCard(transactions: List<Transaction>) {
    CashFlowCard {
        Column {
            Text(
                text = "Comparación Semanal",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            val thisWeekExpenses = filterByThisWeek(transactions).filter { it.type == "Gasto" }.sumOf { it.amount }
            val lastWeekExpenses = filterByLastWeek(transactions).filter { it.type == "Gasto" }.sumOf { it.amount }
            
            val weekChange = if (lastWeekExpenses > 0) {
                ((thisWeekExpenses - lastWeekExpenses) / lastWeekExpenses * 100)
            } else 0.0
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeekComparisonItem(
                    title = "Esta semana",
                    amount = thisWeekExpenses,
                    isCurrentWeek = true
                )
                
                WeekComparisonItem(
                    title = "Semana anterior",
                    amount = lastWeekExpenses,
                    isCurrentWeek = false
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (weekChange >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = if (weekChange >= 0) Color(0xFFE57373) else Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = if (weekChange >= 0) {
                        "Aumento del ${String.format("%.1f", weekChange)}% respecto a la semana anterior"
                    } else {
                        "Reducción del ${String.format("%.1f", -weekChange)}% respecto a la semana anterior"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WeekComparisonItem(
    title: String,
    amount: Double,
    isCurrentWeek: Boolean
) {
    Column(
        horizontalAlignment = if (isCurrentWeek) Alignment.Start else Alignment.End
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(amount),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = if (isCurrentWeek) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun FinancialInsightsCard(
    totalIncome: Double,
    totalExpense: Double,
    expensesByCategory: List<CategoryExpense>,
    transactions: List<Transaction>
) {
    CashFlowCard {
        Column {
            Text(
                text = "Insights Financieros",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            val insights = generateFinancialInsights(totalIncome, totalExpense, expensesByCategory, transactions)
            
            insights.forEach { insight ->
                InsightItem(insight)
                if (insight != insights.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun InsightItem(insight: FinancialInsight) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = insight.icon,
            contentDescription = null,
            tint = insight.color,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = insight.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = insight.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Helper functions and data classes
data class FinancialInsight(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

private fun filterByLastWeek(transactions: List<Transaction>): List<Transaction> {
    val today = Calendar.getInstance()
    val startOfLastWeek = Calendar.getInstance().apply {
        timeInMillis = today.timeInMillis
        add(Calendar.WEEK_OF_YEAR, -1)
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    
    val endOfLastWeek = Calendar.getInstance().apply {
        timeInMillis = startOfLastWeek.timeInMillis
        add(Calendar.DAY_OF_YEAR, 6)
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
    }
    
    return transactions.filter { it.date >= startOfLastWeek.timeInMillis && it.date <= endOfLastWeek.timeInMillis }
}

private fun generateFinancialInsights(
    totalIncome: Double,
    totalExpense: Double,
    expensesByCategory: List<CategoryExpense>,
    transactions: List<Transaction>
): List<FinancialInsight> {
    val insights = mutableListOf<FinancialInsight>()
    
    // Savings rate insight
    val savingsRate = if (totalIncome > 0) ((totalIncome - totalExpense) / totalIncome * 100) else 0.0
    when {
        savingsRate >= 20 -> {
            insights.add(FinancialInsight(
                title = "¡Excelente ahorro!",
                description = "Estás ahorrando ${String.format("%.1f", savingsRate)}% de tus ingresos. ¡Sigue así!",
                icon = Icons.Default.Star,
                color = Color(0xFF4CAF50)
            ))
        }
        savingsRate >= 10 -> {
            insights.add(FinancialInsight(
                title = "Buen ahorro",
                description = "Ahorras ${String.format("%.1f", savingsRate)}% de tus ingresos. Considera aumentar un poco más.",
                icon = Icons.Default.ThumbUp,
                color = Color(0xFFFF9800)
            ))
        }
        savingsRate >= 0 -> {
            insights.add(FinancialInsight(
                title = "Ahorro bajo",
                description = "Solo ahorras ${String.format("%.1f", savingsRate)}% de tus ingresos. Intenta reducir algunos gastos.",
                icon = Icons.Default.Warning,
                color = Color(0xFFE57373)
            ))
        }
        else -> {
            insights.add(FinancialInsight(
                title = "Gastos excesivos",
                description = "Estás gastando más de lo que ingresas. Revisa tus gastos urgentemente.",
                icon = Icons.Default.Error,
                color = Color(0xFFE57373)
            ))
        }
    }
    
    // Top category insight
    if (expensesByCategory.isNotEmpty()) {
        val topCategory = expensesByCategory.first()
        val percentage = (topCategory.amount / totalExpense * 100)
        insights.add(FinancialInsight(
            title = "Categoría principal: ${topCategory.categoryName}",
            description = "Representa el ${String.format("%.1f", percentage)}% de tus gastos totales.",
            icon = getCategoryIcon(topCategory.categoryName),
            color = Color(0xFF2196F3)
        ))
    }
    
    // Transaction frequency insight
    val avgTransactionsPerDay = if (transactions.isNotEmpty()) {
        val dayRange = (transactions.maxOf { it.date } - transactions.minOf { it.date }) / (24 * 60 * 60 * 1000)
        if (dayRange > 0) transactions.size.toDouble() / dayRange else 0.0
    } else 0.0
    
    if (avgTransactionsPerDay > 0) {
        insights.add(FinancialInsight(
            title = "Actividad financiera",
            description = "Registras un promedio de ${String.format("%.1f", avgTransactionsPerDay)} transacciones por día.",
            icon = Icons.Default.Timeline,
            color = Color(0xFF9C27B0)
        ))
    }
    
    return insights
}

private fun getCategoryIcon(categoryName: String): ImageVector {
    return when (categoryName.lowercase()) {
        "comida", "alimentación", "supermercado" -> Icons.Default.Restaurant
        "transporte" -> Icons.Default.DirectionsCar
        "entretenimiento", "ocio" -> Icons.Default.Movie
        "salud" -> Icons.Default.LocalHospital
        "ropa", "vestimenta" -> Icons.Default.Checkroom
        "hogar", "casa" -> Icons.Default.Home
        "educación", "estudio" -> Icons.Default.School
        "trabajo", "salario" -> Icons.Default.Work
        "servicios" -> Icons.Default.Build
        "otros" -> Icons.Default.Category
        else -> Icons.Default.Category
    }
}

// Data class for DonutChart
data class CategoryData(
    val name: String,
    val percentage: Float,
    val color: Color
)
