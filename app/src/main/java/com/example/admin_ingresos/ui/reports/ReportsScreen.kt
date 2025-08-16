package com.example.admin_ingresos.ui.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.admin_ingresos.AppDatabaseProvider
import com.example.admin_ingresos.ui.components.GlassCard
import com.example.admin_ingresos.ui.dashboard.CategoryData
import com.example.admin_ingresos.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ReportsScreen() {
    val context = LocalContext.current
    val viewModel: ReportsViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ReportsViewModel(AppDatabaseProvider.getDatabase(context)) as T
        }
    })

    val uiState by viewModel.uiState.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showLoadingOverlay by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    // Launcher placeholder to grant URI permission if needed (not strictly necessary for FileProvider)
    val shareLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { /* no-op */ }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BackgroundStart, BackgroundEnd)
                )
            )
    ) {
        // Loading overlay for export
        if (showLoadingOverlay) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(color = androidx.compose.ui.graphics.Color(0x88000000))
            ) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Exportando...", color = Color.White)
                }
            }
        }
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Reportes",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = { showExportDialog = true }) {
                            Icon(Icons.Default.Share, contentDescription = "Exportar", tint = Color.White)
                        }
                    }
                }

                item {
                    DateRangeSelector(selectedPreset = uiState.dateRangePreset, onPresetSelected = { viewModel.setDateRange(it) })
                }

                item {
                    FinancialSummary(reportData = uiState.reportData)
                }

                item {
                    IncomeVsExpenseTrendChart(reportData = uiState.reportData)
                }

                item {
                    ExpenseByCategoryChart(reportData = uiState.reportData)
                }

                item {
                    BudgetVsActualComparison(reportData = uiState.reportData)
                }
            }
        }
    }

    // Export dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Exportar Reporte") },
            text = { Text("Selecciona el formato para exportar las transacciones del periodo seleccionado.") },
            confirmButton = {
                Row {
                    TextButton(onClick = {
                        showExportDialog = false
                            viewModel.exportTransactionsCsv(context)
                            // show overlay while exporting
                            showLoadingOverlay = true
                    }) { Text("CSV") }
                    TextButton(onClick = {
                        showExportDialog = false
                            viewModel.exportTransactionsPdf(context)
                            showLoadingOverlay = true
                    }) { Text("PDF") }
                    TextButton(onClick = {
                        showExportDialog = false
                            viewModel.shareTextSummary(context)
                            // textual share uses chooser; show a subtle loading state briefly
                            showLoadingOverlay = true
                    }) { Text("Texto") }
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Observe export state and trigger sharing when ready
    // Observe export state and react: share on success, show snackbar on error, hide loading overlay
    LaunchedEffect(exportState) {
        when (exportState) {
            is com.example.admin_ingresos.ui.reports.ExportStatus.Success -> {
                val uri = (exportState as com.example.admin_ingresos.ui.reports.ExportStatus.Success).uri
                com.example.admin_ingresos.data.ExportService(context).shareFile(uri, mimeType = if (uri.toString().endsWith(".pdf")) "application/pdf" else "text/csv")
                viewModel.clearExportState()
                showLoadingOverlay = false
                snackbarHostState.showSnackbar("Exportado correctamente")
            }
            is com.example.admin_ingresos.ui.reports.ExportStatus.Error -> {
                val message = (exportState as com.example.admin_ingresos.ui.reports.ExportStatus.Error).message
                viewModel.clearExportState()
                showLoadingOverlay = false
                snackbarHostState.showSnackbar("Error: $message")
            }
            is com.example.admin_ingresos.ui.reports.ExportStatus.Loading -> {
                showLoadingOverlay = true
            }
            is com.example.admin_ingresos.ui.reports.ExportStatus.Idle -> {
                showLoadingOverlay = false
            }
        }
    }

    // Snackbar host
    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun DateRangeSelector(selectedPreset: DateRangePreset, onPresetSelected: (DateRangePreset) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(DateRangePreset.values()) { preset ->
            FilterChip(
                selected = selectedPreset == preset,
                onClick = { onPresetSelected(preset) },
                label = { Text(preset.displayName) }
            )
        }
    }
}

@Composable
fun FinancialSummary(reportData: ReportData) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PE"))

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard("Ingresos Totales", formatter.format(reportData.totalIncome), IncomeGreen, Modifier.weight(1f))
            SummaryCard("Gastos Totales", formatter.format(reportData.totalExpenses), ExpenseRed, Modifier.weight(1f))
        }
        SummaryCard("Balance Neto", formatter.format(reportData.netSavings), TextPrimary, Modifier.fillMaxWidth())
    }
}

@Composable
fun SummaryCard(title: String, amount: String, color: Color, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Text(
                text = amount,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun ExpenseByCategoryChart(reportData: ReportData) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Gastos por Categoría",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (reportData.expenseByCategory.isNotEmpty()) {
                DonutChart(
                    categories = reportData.expenseByCategory.map { CategoryData(it.category.name, it.percentage, it.color) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(vertical = 10.dp),
                    totalAmount = reportData.totalExpenses
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(reportData.expenseByCategory) { categoryShare ->
                        CategoryLegendItem(
                            category = CategoryData(categoryShare.category.name, categoryShare.percentage, categoryShare.color),
                            amount = categoryShare.amount
                        )
                    }
                }
            } else {
                Text(text = "No hay datos de gastos para este período.", color = TextSecondary)
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
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PE"))

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val size = 200.dp
        Canvas(
            modifier = Modifier
                .size(size)
                .aspectRatio(1f)
        ) {
            val total = categories.sumOf { it.percentage.toDouble() }
            var currentAngle = -90f
            val canvasSize = this.size.minDimension
            val strokeWidth = (canvasSize * 0.2f)
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

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = "Total",
                fontSize = 14.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (totalAmount > 0) formatter.format(totalAmount) else "S/0.00",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CategoryLegendItem(category: CategoryData, amount: Double = 0.0) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PE"))

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
                color = TextPrimary,
                maxLines = 1,
                softWrap = false
            )
            Text(
                text = if (amount > 0) formatter.format(amount) else "${category.percentage.toInt()}%",
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun IncomeVsExpenseTrendChart(reportData: ReportData) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tendencia de Ingresos vs. Gastos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (reportData.incomeVsExpenseTrend.isNotEmpty()) {
                LineChart(data = reportData.incomeVsExpenseTrend, modifier = Modifier.fillMaxWidth().height(200.dp))
            } else {
                Text(text = "No hay datos de tendencias para este período.", color = TextSecondary)
            }
        }
    }
}

@Composable
fun LineChart(data: List<TrendDataPoint>, modifier: Modifier = Modifier) {
    val maxAmount = data.maxOfOrNull { maxOf(it.income, it.expense) }?.toFloat() ?: 1f

    Canvas(modifier = modifier) { 
        val pathIncome = Path()
        val pathExpense = Path()

        data.forEachIndexed { index, point ->
            val x = size.width * (index.toFloat() / (data.size - 1).toFloat())
            val yIncome = size.height * (1 - (point.income.toFloat() / maxAmount))
            val yExpense = size.height * (1 - (point.expense.toFloat() / maxAmount))

            if (index == 0) {
                pathIncome.moveTo(x, yIncome)
                pathExpense.moveTo(x, yExpense)
            } else {
                pathIncome.lineTo(x, yIncome)
                pathExpense.lineTo(x, yExpense)
            }
        }

        drawPath(path = pathIncome, color = IncomeGreen, style = Stroke(width = 3.dp.toPx()))
        drawPath(path = pathExpense, color = ExpenseRed, style = Stroke(width = 3.dp.toPx()))
    }
}

@Composable
fun BudgetVsActualComparison(reportData: ReportData) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Presupuesto vs. Gasto Real",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (reportData.budgetVsActual.isNotEmpty()) {
                reportData.budgetVsActual.forEach { budgetComparison ->
                    BudgetComparisonItem(budgetComparison)
                }
            } else {
                Text(text = "No hay presupuestos activos para este período.", color = TextSecondary)
            }
        }
    }
}

@Composable
fun BudgetComparisonItem(item: BudgetComparison) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = item.category.name, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = "${(item.progress * 100).toInt()}%", color = TextSecondary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = item.progress.coerceAtMost(1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = if (item.progress > 1f) ExpenseRed else IncomeGreen,
            trackColor = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Gastado: ${formatter.format(item.actualAmount)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(text = "Presupuesto: ${formatter.format(item.budget.amount)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}
