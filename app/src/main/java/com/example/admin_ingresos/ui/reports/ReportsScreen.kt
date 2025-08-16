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
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.animation.core.animateFloatAsState
import java.text.DecimalFormat
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
    val coroutineScope = rememberCoroutineScope()

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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = {
                                coroutineScope.launch {
                                    showLoadingOverlay = true
                                    try {
                                        com.example.admin_ingresos.data.TestDataSeeder.seedBudgetsForReports(AppDatabaseProvider.getDatabase(context))
                                        viewModel.reloadAll()
                                        snackbarHostState.showSnackbar("Presupuestos de ejemplo creados")
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Error: ${e.message}")
                                    } finally {
                                        showLoadingOverlay = false
                                    }
                                }
                            }) {
                                Text("Sembrar presupuestos", color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = { showExportDialog = true }) {
                                Icon(Icons.Default.Share, contentDescription = "Exportar", tint = Color.White)
                            }
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
    val sdf = remember { SimpleDateFormat("dd MMM", Locale("es", "PE")) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tendencia de Ingresos vs. Gastos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Legend
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(IncomeGreen, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ingresos", color = TextSecondary, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(ExpenseRed, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gastos", color = TextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (reportData.incomeVsExpenseTrend.isNotEmpty()) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                ) {
                    LineChart(data = reportData.incomeVsExpenseTrend, modifier = Modifier.fillMaxSize())
                }
            } else {
                Text(text = "No hay datos de tendencias para este período.", color = TextSecondary)
            }
        }
    }
}

@Composable
fun LineChart(data: List<TrendDataPoint>, modifier: Modifier = Modifier) {
    val lastTapX = remember { mutableStateOf<Float?>(null) }
    val selectedIndex = remember { mutableStateOf(-1) }
    val canvasWidth = remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val formatter = remember { java.text.NumberFormat.getCurrencyInstance(Locale("es", "PE")) }
    val sdf = remember { SimpleDateFormat("dd MMM", Locale("es", "PE")) }

    if (data.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Sin datos", color = TextSecondary)
        }
        return
    }

    val maxAmount = (data.flatMap { listOf(it.income, it.expense) }.maxOrNull() ?: 1.0).toFloat()

    Box(modifier = modifier) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { canvasWidth.value = it.size.width.toFloat() }
            .pointerInput(data) {
                detectTapGestures { offset ->
                    lastTapX.value = offset.x
                    val w = canvasWidth.value.takeIf { it > 0f } ?: return@detectTapGestures
                    val idx = if (data.size > 1) ((offset.x / w) * (data.size - 1)).toInt() else 0
                    selectedIndex.value = idx.coerceIn(0, data.size - 1)
                }
            }
        ) {
            // draw subtle horizontal grid lines
            val gridColor = Color.White.copy(alpha = 0.06f)
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = size.height * (i.toFloat() / gridLines.toFloat())
                drawLine(color = gridColor, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1.dp.toPx())
            }

            val pathIncome = Path()
            val pathExpense = Path()
            val areaIncome = Path()
            val areaExpense = Path()

            data.forEachIndexed { index, point ->
                val x = if (data.size > 1) size.width * (index.toFloat() / (data.size - 1).toFloat()) else size.width / 2f
                val yIncome = size.height * (1f - (point.income.toFloat() / maxAmount))
                val yExpense = size.height * (1f - (point.expense.toFloat() / maxAmount))

                if (index == 0) {
                    pathIncome.moveTo(x, yIncome)
                    pathExpense.moveTo(x, yExpense)
                    areaIncome.moveTo(x, size.height)
                    areaIncome.lineTo(x, yIncome)
                    areaExpense.moveTo(x, size.height)
                    areaExpense.lineTo(x, yExpense)
                } else {
                    pathIncome.lineTo(x, yIncome)
                    pathExpense.lineTo(x, yExpense)
                    areaIncome.lineTo(x, yIncome)
                    areaExpense.lineTo(x, yExpense)
                }
            }

            // close area paths
            areaIncome.lineTo(size.width, size.height)
            areaIncome.close()
            areaExpense.lineTo(size.width, size.height)
            areaExpense.close()

            // draw filled areas (subtle)
            drawPath(path = areaIncome, color = IncomeGreen.copy(alpha = 0.10f))
            drawPath(path = areaExpense, color = ExpenseRed.copy(alpha = 0.10f))

            // draw lines
            drawPath(path = pathIncome, color = IncomeGreen, style = Stroke(width = 2.dp.toPx()))
            drawPath(path = pathExpense, color = ExpenseRed, style = Stroke(width = 2.dp.toPx()))

            // draw points
            data.forEachIndexed { index, point ->
                val x = if (data.size > 1) size.width * (index.toFloat() / (data.size - 1).toFloat()) else size.width / 2f
                val yIncome = size.height * (1f - (point.income.toFloat() / maxAmount))
                val yExpense = size.height * (1f - (point.expense.toFloat() / maxAmount))
                drawCircle(color = IncomeGreen, radius = 3.dp.toPx(), center = Offset(x, yIncome))
                drawCircle(color = ExpenseRed, radius = 3.dp.toPx(), center = Offset(x, yExpense))
            }

            // highlight selected index if any
            val sel = selectedIndex.value
            if (sel in data.indices) {
                val sPoint = data[sel]
                val x = if (data.size > 1) size.width * (sel.toFloat() / (data.size - 1).toFloat()) else size.width / 2f
                // vertical guide
                drawLine(color = Color.White.copy(alpha = 0.18f), start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 1.dp.toPx())
                // larger circles to highlight
                val yi = size.height * (1f - (sPoint.income.toFloat() / maxAmount))
                val ye = size.height * (1f - (sPoint.expense.toFloat() / maxAmount))
                drawCircle(Color.White, radius = 6.dp.toPx(), center = Offset(x, yi))
                drawCircle(Color.White, radius = 6.dp.toPx(), center = Offset(x, ye))
            }
        }

        // Tooltip overlay
        val sel = selectedIndex.value
        val w = canvasWidth.value
        if (sel in data.indices && w > 0f) {
            val point = data[sel]
            val tooltipX = if (data.size > 1) ((sel.toFloat() / (data.size - 1).toFloat()) * w) else (w / 2f)
            // convert px -> dp offset
            val xDp = with(density) { tooltipX.toDp() }
            Box(modifier = Modifier
                .offset { IntOffset((tooltipX - 60f).toInt().coerceIn(0, w.toInt() - 120), -70) }
                .wrapContentSize()
            ) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)), modifier = Modifier.padding(4.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = sdf.format(Date(point.timestamp)), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Ingresos: ${formatter.format(point.income)}", color = IncomeGreen, fontSize = 12.sp)
                        Text(text = "Gastos: ${formatter.format(point.expense)}", color = ExpenseRed, fontSize = 12.sp)
                    }
                }
            }
        }
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
                // show highest progress first (items closest to/exceeding budget)
                val sorted = reportData.budgetVsActual.sortedByDescending { it.progress }
                sorted.forEach { budgetComparison ->
                    BudgetComparisonItem(budgetComparison)
                    Spacer(modifier = Modifier.height(8.dp))
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.progress > 1f) {
                    Icon(Icons.Default.Warning, contentDescription = "Excedido", tint = ExpenseRed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = item.category.name, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            // percentage badge (formatted, capped)
                val pctFloat = (item.progress * 100f)
                val pctCapped = pctFloat.coerceAtMost(999.9f)
                val pctForm = DecimalFormat("#0.#").format(pctCapped) + "%"
                val badgeColor = when {
                    item.progress > 1f -> ExpenseRed
                    item.progress >= 0.75f -> androidx.compose.ui.graphics.Color(0xFFF0A500) // yellow-ish
                    else -> IncomeGreen
                }
                Text(text = pctForm, color = badgeColor, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Custom progress bar with animation and color thresholds
        val displayProgress = when {
            item.progress.isFinite() -> item.progress.coerceAtLeast(0f)
            else -> 0f
        }
        val barColor = when {
            item.progress > 1f -> ExpenseRed
            item.progress >= 0.75f -> androidx.compose.ui.graphics.Color(0xFFF0A500)
            else -> IncomeGreen
        }
        BudgetProgressBar(progress = displayProgress, color = barColor, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(text = "Gastado: ${formatter.format(item.actualAmount)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text(text = "Presupuesto: ${formatter.format(item.budget.amount)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            // show absolute over/remaining amounts
            val overAmount = item.actualAmount - item.budget.amount
            if (overAmount > 0.0) {
                Text(text = "Sobre: ${formatter.format(overAmount)}", color = ExpenseRed, style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            } else {
                Text(text = "Libre: ${formatter.format(-overAmount)}", color = TextSecondary, style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            }
        }
    }
}

@Composable
fun BudgetProgressBar(progress: Float, color: Color, modifier: Modifier = Modifier) {
    val animated = animateFloatAsState(targetValue = progress.coerceIn(0f, 1f))
    Box(modifier = modifier.height(14.dp)) {
        // track
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2A2A2A), shape = CircleShape)
        )
        // fill
        Box(modifier = Modifier
            .fillMaxWidth(animated.value)
            .height(14.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.95f))
        )
    // end percentage pill removed to avoid duplication with top-line badge
    }
}
