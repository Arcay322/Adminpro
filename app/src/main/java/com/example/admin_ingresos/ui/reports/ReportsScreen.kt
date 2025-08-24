package com.example.admin_ingresos.ui.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.ViewModelProvider
import com.example.admin_ingresos.ui.history.DateRange
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
import com.example.admin_ingresos.ui.components.GlassmorphismScreen
import com.example.admin_ingresos.ui.components.MainBalanceCards
import com.example.admin_ingresos.ui.components.ThemedAlertDialog
import com.example.admin_ingresos.ui.dashboard.CategoryData
import com.example.admin_ingresos.ui.icons.LucideIconMapper
import com.example.admin_ingresos.ui.theme.*
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.window.Dialog

@Composable
fun ReportsScreen(initialSection: String = "") {
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
    var showDownloadDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Launcher placeholder to grant URI permission if needed (not strictly necessary for FileProvider)
    val shareLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { /* no-op */ }

    // CreateDocument launchers for user-driven downloads (save as)
    var pendingDownloadSourceUri by remember { mutableStateOf<Uri?>(null) }
    var isDownloadPending by remember { mutableStateOf(false) }
    var pendingDownloadMime by remember { mutableStateOf<String?>(null) }

    val createCsvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { destUri ->
        if (destUri != null && pendingDownloadSourceUri != null) {
                try {
                context.contentResolver.openInputStream(pendingDownloadSourceUri!!).use { input ->
                    context.contentResolver.openOutputStream(destUri).use { output ->
                        if (input != null && output != null) {
                            input.copyTo(output)
                        }
                    }
                }
                coroutineScope.launch { snackbarHostState.showSnackbar("Descargado correctamente") }
            } catch (e: Exception) {
                coroutineScope.launch { snackbarHostState.showSnackbar("Error al guardar: ${e.message}") }
            } finally {
                pendingDownloadSourceUri = null
                isDownloadPending = false
                pendingDownloadMime = null
            }
        } else {
            // user cancelled
            pendingDownloadSourceUri = null
            pendingDownloadMime = null
        }
    }

    val createPdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { destUri ->
        if (destUri != null && pendingDownloadSourceUri != null) {
                try {
                context.contentResolver.openInputStream(pendingDownloadSourceUri!!).use { input ->
                    context.contentResolver.openOutputStream(destUri).use { output ->
                        if (input != null && output != null) {
                            input.copyTo(output)
                        }
                    }
                }
                coroutineScope.launch { snackbarHostState.showSnackbar("Descargado correctamente") }
            } catch (e: Exception) {
                coroutineScope.launch { snackbarHostState.showSnackbar("Error al guardar: ${e.message}") }
            } finally {
                pendingDownloadSourceUri = null
                isDownloadPending = false
                pendingDownloadMime = null
            }
        } else {
            pendingDownloadSourceUri = null
            isDownloadPending = false
            pendingDownloadMime = null
        }
    }

    val xlsxMime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    val createXlsxLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(xlsxMime)) { destUri ->
        if (destUri != null && pendingDownloadSourceUri != null) {
            try {
                    context.contentResolver.openInputStream(pendingDownloadSourceUri!!).use { input ->
                        context.contentResolver.openOutputStream(destUri).use { output ->
                            if (input != null && output != null) {
                                input.copyTo(output)
                            }
                        }
                    }
                    coroutineScope.launch { snackbarHostState.showSnackbar("Descargado correctamente") }
                } catch (e: Exception) {
                    coroutineScope.launch { snackbarHostState.showSnackbar("Error al guardar: ${e.message}") }
                } finally {
                    pendingDownloadSourceUri = null
                    isDownloadPending = false
                    pendingDownloadMime = null
                }
        } else {
            pendingDownloadSourceUri = null
            isDownloadPending = false
            pendingDownloadMime = null
        }
    }

    GlassmorphismScreen(modifier = Modifier.fillMaxSize()) {
        // Loading overlay for export
        if (showLoadingOverlay) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background.copy(alpha = 0.53f))
            ) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Exportando...", color = com.example.admin_ingresos.ui.theme.TextPrimary)
                }
            }
        }
        val listState = rememberLazyListState()

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = LucideIconMapper.Navigation.reports,
                                contentDescription = "Reportes",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(verticalArrangement = Arrangement.Center) {
                                Text(
                                    text = "Reportes",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Analiza tus ingresos, gastos y ahorros por periodo",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showExportDialog = true }) {
                                Icon(Icons.Default.Share, contentDescription = "Exportar", tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = {
                                showDownloadDialog = true
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Abriendo diálogo de descargas...")
                                }
                            }) {
                                Icon(LucideIconMapper.Navigation.download, contentDescription = "Descargar", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                item {
                    DateRangeSelector(selectedPreset = uiState.dateRangePreset, onPresetSelected = { preset, custom -> viewModel.setDateRange(preset, custom) })
                }

                item {
                    MainBalanceCardsReport(
                        reportData = uiState.reportData,
                        dateRangePreset = uiState.dateRangePreset,
                        selectedRange = uiState.selectedDateRange,
                        onViewDetails = { /* no-op for now */ }
                    )
                }

                item {
                    IncomeVsExpenseTrendChart(reportData = uiState.reportData)
                }

                item {
                    SavingsGrowthChart(reportData = uiState.reportData)
                }

                item {
                    ExpenseByCategoryChart(reportData = uiState.reportData)
                }
                item {
                    IncomeByCategoryChart(reportData = uiState.reportData)
                }

                item {
                    SavingsByCategoryChart(reportData = uiState.reportData)
                }

            }
        }

        // If caller requested a specific section, scroll to it after the content renders
        LaunchedEffect(uiState.isLoading, initialSection) {
            if (!uiState.isLoading && initialSection == "expenseByCategory") {
                // find the index of the ExpenseByCategoryChart - in this current layout it's the 5th item (0-based) but
                // to be robust we scroll to a small offset where that item is likely placed.
                try {
                    // safe scroll to index 5 (after header, date range, balance, trend, savings)
                        listState.animateScrollToItem(index = 5)
                } catch (_: Exception) { }
            }
        }
    }

    // Download dialog
    if (showDownloadDialog) {
        ThemedAlertDialog(
            onDismissRequest = { showDownloadDialog = false },
            shape = RoundedCornerShape(28.dp),
            borderStroke = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
            content = {
                Column(modifier = Modifier.fillMaxWidth().widthIn(min = 320.dp).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text(text = "Descargar Reporte", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Selecciona la resolución y formato para la descarga.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                        Button(
                            onClick = {
                                showDownloadDialog = false
                                isDownloadPending = true
                                pendingDownloadMime = "text/csv"
                                viewModel.exportTransactionsCsv(context)
                                showLoadingOverlay = true
                            },
                            modifier = Modifier.widthIn(min = 76.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) { Text("CSV", maxLines = 1, softWrap = false) }

                        Button(
                            onClick = {
                                showDownloadDialog = false
                                isDownloadPending = true
                                pendingDownloadMime = "application/pdf"
                                viewModel.exportTransactionsPdf(context)
                                showLoadingOverlay = true
                            },
                            modifier = Modifier.widthIn(min = 76.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) { Text("PDF", maxLines = 1, softWrap = false) }

                        Button(
                            onClick = {
                                showDownloadDialog = false
                                showLoadingOverlay = true
                                coroutineScope.launch {
                                    try {
                                        val transactions = viewModel.getAllTransactionsForCurrentRange()
                                        val categories = viewModel.getCategories()
                                        val paymentMethods = viewModel.getPaymentMethods()
                                        val exportResult = com.example.admin_ingresos.data.ExportService(context).exportTransactionsToXlsx(transactions, categories, paymentMethods)
                                        if (exportResult.uri != null) {
                                            pendingDownloadSourceUri = exportResult.uri
                                            isDownloadPending = true
                                            pendingDownloadMime = xlsxMime
                                            createXlsxLauncher.launch("reporte_${System.currentTimeMillis()}.xlsx")
                                        } else {
                                            coroutineScope.launch { snackbarHostState.showSnackbar("Error al generar Excel") }
                                        }
                                    } catch (e: Exception) {
                                        coroutineScope.launch { snackbarHostState.showSnackbar("Error: ${e.message}") }
                                    } finally {
                                        showLoadingOverlay = false
                                    }
                                }
                            },
                            modifier = Modifier.widthIn(min = 76.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) { Text("Excel", maxLines = 1, softWrap = false) }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showDownloadDialog = false }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)) {
                            Text("Cerrar")
                        }
                    }
                }
            }
        )
    }

    // Export dialog (share / quick export)
    if (showExportDialog) {
        ThemedAlertDialog(
            onDismissRequest = { showExportDialog = false },
            shape = RoundedCornerShape(28.dp),
            borderStroke = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
            content = {
                Column(modifier = Modifier.fillMaxWidth().widthIn(min = 320.dp).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text(text = "Compartir Reporte", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Selecciona el formato para exportar las transacciones del periodo seleccionado.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                        Button(
                            onClick = {
                                showExportDialog = false
                                viewModel.exportTransactionsCsv(context)
                                showLoadingOverlay = true
                            },
                            modifier = Modifier.widthIn(min = 76.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) { Text("CSV", maxLines = 1, softWrap = false) }

                        Button(
                            onClick = {
                                showExportDialog = false
                                viewModel.exportTransactionsPdf(context)
                                showLoadingOverlay = true
                            },
                            modifier = Modifier.widthIn(min = 76.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) { Text("PDF", maxLines = 1, softWrap = false) }

                        Button(
                            onClick = {
                                showExportDialog = false
                                viewModel.shareTextSummary(context)
                            },
                            modifier = Modifier.widthIn(min = 76.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) { Text("Texto", maxLines = 1, softWrap = false) }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showExportDialog = false }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)) {
                            Text("Cancelar")
                        }
                    }
                }
            }
        )
    }

    // Observe export state and trigger sharing when ready
    // Observe export state and react: share on success, show snackbar on error, hide loading overlay
    LaunchedEffect(exportState) {
        when (exportState) {
            is com.example.admin_ingresos.ui.reports.ExportStatus.Success -> {
                val uri = (exportState as com.example.admin_ingresos.ui.reports.ExportStatus.Success).uri
                // If a download was requested by the user, open a Save-As dialog and copy the generated file
                if (isDownloadPending && uri != null) {
                    pendingDownloadSourceUri = uri
                    // launch appropriate create document based on pending mime
                    if (pendingDownloadMime == "application/pdf") {
                        createPdfLauncher.launch("reporte_${System.currentTimeMillis()}.pdf")
                    } else {
                        createCsvLauncher.launch("reporte_${System.currentTimeMillis()}.csv")
                    }
                    // clear viewmodel state but keep overlay until user finishes
                    viewModel.clearExportState()
                    showLoadingOverlay = false
                    // snackbar will be shown after copy completes in launcher callback
                } else if (uri != null) {
                    // Default behavior: open share chooser
                    com.example.admin_ingresos.data.ExportService(context).shareFile(uri, mimeType = if (uri.toString().endsWith(".pdf")) "application/pdf" else "text/csv")
                    viewModel.clearExportState()
                    showLoadingOverlay = false
                    snackbarHostState.showSnackbar("Exportado correctamente")
                } else {
                    viewModel.clearExportState()
                    showLoadingOverlay = false
                    snackbarHostState.showSnackbar("Exportación finalizada, pero no se encontró el archivo generado")
                }
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
fun DateRangeSelector(selectedPreset: DateRangePreset, onPresetSelected: (DateRangePreset, DateRange?) -> Unit) {
    var showCustomDialog by remember { mutableStateOf(false) }
    var pendingCustomPreset by remember { mutableStateOf<DateRangePreset?>(null) }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(DateRangePreset.values()) { preset ->
            val selected = selectedPreset == preset
            val presetIcon = when (preset) {
                DateRangePreset.TODAY -> LucideIconMapper.Navigation.clock
                DateRangePreset.THIS_WEEK -> LucideIconMapper.getNavigationIcon("calendardays")
                DateRangePreset.THIS_MONTH -> LucideIconMapper.getNavigationIcon("calendar")
                DateRangePreset.LAST_3_MONTHS -> LucideIconMapper.getNavigationIcon("calendardown")
                DateRangePreset.THIS_YEAR -> LucideIconMapper.getNavigationIcon("calendarup")
                DateRangePreset.CUSTOM -> LucideIconMapper.getNavigationIcon("calendar")
            }

            FilterChip(
                selected = selected,
                onClick = {
                    if (preset == DateRangePreset.CUSTOM) {
                        // open dialog to pick custom range
                        pendingCustomPreset = preset
                        showCustomDialog = true
                    } else {
                        onPresetSelected(preset, null)
                    }
                },
                leadingIcon = {
                    Icon(
                        imageVector = presetIcon,
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.onPrimary else TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                },
                label = { Text(preset.displayName) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }

    if (showCustomDialog && pendingCustomPreset != null) {
        DateRangeDialog(onDismiss = { showCustomDialog = false; pendingCustomPreset = null }) { customRange ->
            showCustomDialog = false
            onPresetSelected(pendingCustomPreset!!, customRange)
            pendingCustomPreset = null
        }
    }
}

@Composable
fun FinancialSummary(reportData: ReportData) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PE"))

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Large balance card first (like the design samples)
        BalanceCardLarge(
            title = "Balance Total",
            amount = formatter.format(reportData.netSavings).replace(" ", "\u00A0"),
            subtitle = "Actualizado ahora",
            modifier = Modifier.fillMaxWidth(),
            icon = com.example.admin_ingresos.ui.icons.LucideIconMapper.getNavigationIcon("DollarSign")
        )

        // Three small summary cards: Ingresos / Gastos / Ahorro
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniSummaryCard(
                    title = "Ingresos",
                    amount = formatter.format(reportData.totalIncome).replace(" ", "\u00A0"),
                    color = IncomeGreen,
                    modifier = Modifier.weight(1f),
                    leadingIcon = com.example.admin_ingresos.ui.icons.LucideIconMapper.getTransactionTypeIcon(com.example.admin_ingresos.data.Transaction.TYPE_INCOME)
                )

                MiniSummaryCard(
                    title = "Gastos",
                    amount = formatter.format(reportData.totalExpenses).replace(" ", "\u00A0"),
                    color = ExpenseRed,
                    modifier = Modifier.weight(1f),
                    leadingIcon = com.example.admin_ingresos.ui.icons.LucideIconMapper.getTransactionTypeIcon(com.example.admin_ingresos.data.Transaction.TYPE_EXPENSE)
                )

                MiniSummaryCard(
                    title = "Ahorro",
                    amount = formatter.format(reportData.totalTransfers).replace(" ", "\u00A0"),
                    color = Color(0xFF42A5F5),
                    modifier = Modifier.weight(1f)
                )
        }
    }
}

private fun presetToSubtitle(preset: DateRangePreset, range: DateRange?): String {
    // Map preset to a human-friendly subtitle that matches the active filter
    return when (preset) {
        DateRangePreset.TODAY -> "Hoy"
        DateRangePreset.THIS_WEEK -> "Esta semana"
        DateRangePreset.THIS_MONTH -> "Este mes"
        DateRangePreset.LAST_3_MONTHS -> "Últimos 3 meses"
        DateRangePreset.THIS_YEAR -> "Este año"
        DateRangePreset.CUSTOM -> {
            if (range != null) {
                try {
                    val sdf = SimpleDateFormat("dd MMM", Locale("es", "PE"))
                    val start = sdf.format(Date(range.startDate))
                    val end = sdf.format(Date(range.endDate))
                    "$start - $end"
                } catch (e: Exception) {
                    "Personalizado"
                }
            } else {
                "Personalizado"
            }
        }
    }
}

@Composable
fun MainBalanceCardsReport(
    reportData: ReportData,
    dateRangePreset: DateRangePreset,
    selectedRange: DateRange?,
    onViewDetails: () -> Unit
) {
    // Compute a subtitle matching the currently selected date-range filter and pass it
    val subtitle = remember(dateRangePreset, selectedRange) { presetToSubtitle(dateRangePreset, selectedRange) }

    MainBalanceCards(
        currentBalance = reportData.netSavings,
        monthlyIncome = reportData.totalIncome,
        monthlyExpenses = reportData.totalExpenses,
        monthlyTransfers = reportData.totalTransfers,
        modifier = Modifier.fillMaxWidth(),
        onViewDetails = onViewDetails,
        incomeSubtitle = subtitle,
        expensesSubtitle = subtitle,
        transfersSubtitle = subtitle
    )
}

@Composable
fun BalanceCardLarge(title: String, amount: String, subtitle: String? = null, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text(text = title, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = amount, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 34.sp)
                }
                // icon or subtle ghost circle for visual accent
                if (icon != null) {
                    Box(modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.02f), shape = CircleShape), contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = AccentVibrantStart, modifier = Modifier.size(18.dp))
                    }
                } else {
                    Box(modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f), shape = CircleShape))
                }
            }
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}

@Composable
fun MiniSummaryCard(
    title: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    subtitle: String? = null
) {
    // Use a colored border to indicate type (green/red) per design request
    GlassCard(modifier = modifier, borderColor = color.copy(alpha = 0.45f)) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leadingIcon != null) {
                    Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(color.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
                        Icon(imageVector = leadingIcon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Column {
                    Text(text = title, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = amount,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        lineHeight = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }

            // keep the right side clean (no icon) — border indicates type
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

@Composable
fun DateRangeDialog(onDismiss: () -> Unit, onConfirm: (DateRange) -> Unit) {
    var startText by remember { mutableStateOf("") }
    var endText by remember { mutableStateOf("") }
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Rango personalizado", style = MaterialTheme.typography.titleMedium, color = TextPrimary)

                OutlinedTextField(value = startText, onValueChange = { startText = it }, label = { Text("Fecha inicio (dd/MM/yyyy)") }, singleLine = true)
                OutlinedTextField(value = endText, onValueChange = { endText = it }, label = { Text("Fecha fin (dd/MM/yyyy)") }, singleLine = true)

                error?.let { Text(text = it, color = ExpenseRed) }

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        try {
                            val start = sdf.parse(startText)?.time
                            val end = sdf.parse(endText)?.time
                            if (start == null || end == null) throw IllegalArgumentException("Fechas inválidas")
                            if (end < start) throw IllegalArgumentException("La fecha fin debe ser posterior a la de inicio")
                            onConfirm(DateRange(start, end))
                        } catch (e: Exception) {
                            error = e.message ?: "Formato inválido"
                        }
                    }) { Text("Aplicar") }
                }
            }
        }
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
fun IncomeByCategoryChart(reportData: ReportData) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Ingresos por Categoría",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (reportData.incomeByCategory.isNotEmpty()) {
                DonutChart(
                    categories = reportData.incomeByCategory.map { CategoryData(it.category.name, it.percentage, it.color) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(vertical = 10.dp),
                    totalAmount = reportData.totalIncome
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(reportData.incomeByCategory) { categoryShare ->
                        CategoryLegendItem(
                            category = CategoryData(categoryShare.category.name, categoryShare.percentage, categoryShare.color),
                            amount = categoryShare.amount
                        )
                    }
                }
            } else {
                Text(text = "No hay datos de ingresos para este período.", color = TextSecondary)
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

            // Legend (Ingresos / Gastos)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(IncomeGreen), contentAlignment = Alignment.Center) {
                        Icon(LucideIconMapper.getTransactionTypeIcon("ingreso"), contentDescription = null, tint = IncomeGreen, modifier = Modifier.size(12.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ingresos", color = TextSecondary, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(ExpenseRed), contentAlignment = Alignment.Center) {
                        Icon(LucideIconMapper.getTransactionTypeIcon("gasto"), contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(12.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gastos", color = TextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (reportData.incomeVsExpenseTrend.isNotEmpty()) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                ) {
                    // Force showTransfers=false to keep this chart as Ingresos vs Gastos only
                    LineChart(data = reportData.incomeVsExpenseTrend, showTransfers = false, modifier = Modifier.fillMaxSize())
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Insight card for the income vs expense chart using GlassCard and the tone rules requested
                val incomeDelta = computeDeltaFromTrend(reportData.incomeVsExpenseTrend) { it.income }
                val expenseDelta = computeDeltaFromTrend(reportData.incomeVsExpenseTrend) { it.expense }
                val net = reportData.totalIncome - reportData.totalExpenses

                // Detect simple peaks: find the day with max expense and max income
                val maxExpensePoint = reportData.incomeVsExpenseTrend.maxByOrNull { it.expense }
                val avgExpense = if (reportData.incomeVsExpenseTrend.isNotEmpty()) reportData.incomeVsExpenseTrend.map { it.expense }.average() else 0.0
                val expensePeakDetected = maxExpensePoint != null && avgExpense > 0.0 && maxExpensePoint.expense > avgExpense * 1.5

                val maxIncomePoint = reportData.incomeVsExpenseTrend.maxByOrNull { it.income }
                val avgIncome = if (reportData.incomeVsExpenseTrend.isNotEmpty()) reportData.incomeVsExpenseTrend.map { it.income }.average() else 0.0
                val incomePeakDetected = maxIncomePoint != null && avgIncome > 0.0 && maxIncomePoint.income > avgIncome * 1.5

                // choose glass container color: green if net positive, red if net negative
                val containerColor = if (net >= 0.0) IncomeGreen.copy(alpha = 0.12f) else ExpenseRed.copy(alpha = 0.12f)
                val borderColor = if (net >= 0.0) IncomeGreen.copy(alpha = 0.28f) else ExpenseRed.copy(alpha = 0.28f)

                GlassCard(modifier = Modifier.padding(top = 8.dp), backgroundColor = containerColor, borderColor = borderColor) {
                    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (net >= 0.0) com.example.admin_ingresos.ui.icons.LucideIconMapper.getTransactionTypeIcon(com.example.admin_ingresos.data.Transaction.TYPE_INCOME)
                                else com.example.admin_ingresos.ui.icons.LucideIconMapper.getTransactionTypeIcon(com.example.admin_ingresos.data.Transaction.TYPE_EXPENSE),
                                contentDescription = null,
                                tint = if (net >= 0.0) IncomeGreen else ExpenseRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // keep the rest of the column content under
                        }
                        // Paragraph 1: net balance summary
                        if (net > 0.0) {
                            Text(text = "¡Excelente! Este mes tus ingresos superaron a tus gastos en ${formatter.format(net)}.", color = TextPrimary, fontWeight = FontWeight.Bold)
                        } else if (net < 0.0) {
                            Text(text = "Ten cuidado, tus gastos han superado tus ingresos en ${formatter.format(-net)} este mes.", color = TextPrimary, fontWeight = FontWeight.Bold)
                        } else {
                            Text(text = "Ingresos y gastos están equilibrados este mes.", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Paragraph 2: peak or delta context (higher contrast for readability). Action sentence highlighted in red if needed.
                        when {
                            expensePeakDetected -> {
                                val topCategory = reportData.expenseByCategory.firstOrNull()?.category?.name ?: "esta categoría"
                                val dateText = maxExpensePoint?.timestamp?.let { SimpleDateFormat("dd MMM", Locale("es", "PE")).format(Date(it)) } ?: "el periodo"
                                Text(text = "Tu gasto en $topCategory en $dateText fue el pico más alto de este periodo.", color = TextPrimary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Considera revisar tus hábitos de gasto.", color = ExpenseRed, fontSize = 12.sp)
                            }
                            incomePeakDetected -> {
                                val dateText = maxIncomePoint?.timestamp?.let { SimpleDateFormat("dd MMM", Locale("es", "PE")).format(Date(it)) } ?: "el periodo"
                                Text(text = "Tus ingresos fueron más altos en $dateText.", color = TextPrimary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Revisa si provino de una bonificación u otro ingreso excepcional.", color = IncomeGreen, fontSize = 12.sp)
                            }
                            else -> {
                                if (!incomeDelta.isNaN() && incomeDelta >= 0.0) {
                                    Text(text = "Ingresos ${calcDeltaText(incomeDelta)} vs periodo anterior", color = IncomeGreen, fontSize = 12.sp)
                                } else if (!expenseDelta.isNaN()) {
                                    Text(text = "Gastos ${calcDeltaText(expenseDelta)} vs periodo anterior", color = ExpenseRed, fontSize = 12.sp)
                                } else {
                                    Text(text = "Sin cambios significativos en el periodo.", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                Text(text = "No hay datos de tendencias para este período.", color = TextSecondary)
            }
        }
    }
}

@Composable
fun SavingsGrowthChart(reportData: ReportData) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PE"))

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Crecimiento de tus Ahorros",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (reportData.transfersGrowth.isNotEmpty()) {
                val data = reportData.transfersGrowth
                val maxVal = (data.maxOfOrNull { it.amount } ?: 1.0).toFloat()

                // Chart + insight are separate composable children so no composable is called from inside Canvas' draw scope
                Column {
                    // precompute theme-backed colors outside of Canvas to avoid calling @Composable APIs inside draw lambda
                    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                    val gridLines = 4

                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            for (i in 0..gridLines) {
                                val y = h * (i.toFloat() / gridLines.toFloat())
                                drawLine(color = gridColor, start = Offset(0f, y), end = Offset(w, y), strokeWidth = 1.dp.toPx())
                            }

                            val path = Path()
                            val area = Path()
                            data.forEachIndexed { idx, p ->
                                val x = if (data.size > 1) w * (idx.toFloat() / (data.size - 1).toFloat()) else w / 2f
                                val y = h * (1f - (p.amount.toFloat() / maxVal))
                                if (idx == 0) {
                                    path.moveTo(x, y)
                                    area.moveTo(x, h)
                                    area.lineTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                    area.lineTo(x, y)
                                }
                            }

                            // close area
                            area.lineTo(w, h)
                            area.close()

                            // draw area and line (match weights/alphas)
                            val savingsColor = Color(0xFF42A5F5)
                            drawPath(path = area, color = savingsColor.copy(alpha = 0.08f))
                            drawPath(path = path, color = savingsColor, style = Stroke(width = 2.dp.toPx()))

                            // points
                            data.forEachIndexed { idx, p ->
                                val x = if (data.size > 1) w * (idx.toFloat() / (data.size - 1).toFloat()) else w / 2f
                                val y = h * (1f - (p.amount.toFloat() / maxVal))
                                drawCircle(color = savingsColor, center = Offset(x, y), radius = 3.dp.toPx())
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // compute insight values here (composable scope)
                    val last = data.last()
                    val first = data.first()
                    val growthPct = if (first.amount == 0.0) Double.NaN else ((last.amount - first.amount) / first.amount) * 100.0

                    GlassCard(
                            modifier = Modifier
                                .padding(top = 8.dp),
                            backgroundColor = Color(0xFF42A5F5).copy(alpha = 0.08f),
                            borderColor = Color(0xFF42A5F5).copy(alpha = 0.24f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = com.example.admin_ingresos.ui.icons.LucideIconMapper.getTransactionTypeIcon(com.example.admin_ingresos.data.Transaction.TYPE_TRANSFER),
                                        contentDescription = null,
                                        tint = Color(0xFF42A5F5),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(text = "Saldo actual: ${formatter.format(last.amount)}", color = TextPrimary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                if (!growthPct.isNaN()) {
                                    val pctText = if (growthPct >= 0) "+${"%.1f".format(growthPct)}%" else "${"%.1f".format(growthPct)}%"
                                    Text(text = "Tu saldo de ahorro ha crecido $pctText desde el inicio del periodo.", color = TextSecondary, fontSize = 12.sp)
                                } else {
                                    Text(text = "Vas por buen camino para alcanzar tu meta.", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                }
            } else {
                Text(text = "No hay datos de ahorro para este período.", color = TextSecondary)
            }
        }
    }
}

@Composable
fun SavingsByCategoryChart(reportData: ReportData) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Ahorros por Categoría",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (reportData.savingsByCategory.isNotEmpty()) {
                DonutChart(
                    categories = reportData.savingsByCategory.map { CategoryData(it.category.name, it.percentage, it.color) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(vertical = 10.dp),
                    totalAmount = reportData.totalTransfers
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(reportData.savingsByCategory) { categoryShare ->
                        CategoryLegendItem(
                            category = CategoryData(categoryShare.category.name, categoryShare.percentage, categoryShare.color),
                            amount = categoryShare.amount
                        )
                    }
                }
            } else {
                Text(text = "No hay datos de ahorros para este período.", color = TextSecondary)
            }
        }
    }
}

@Composable
fun LineChart(data: List<TrendDataPoint>, showTransfers: Boolean = true, modifier: Modifier = Modifier) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
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

    val maxAmount = (data.flatMap { listOf(it.income, it.expense) + if (showTransfers) listOf(it.transfers) else emptyList() }.maxOrNull() ?: 1.0).toFloat()

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
            val gridColor = onSurfaceVariant
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = size.height * (i.toFloat() / gridLines.toFloat())
                drawLine(color = gridColor, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1.dp.toPx())
            }

            val pathIncome = Path()
            val pathExpense = Path()
            val pathTransfers = Path()
            val areaIncome = Path()
            val areaExpense = Path()
            val areaTransfers = Path()

            data.forEachIndexed { index, point ->
                val x = if (data.size > 1) size.width * (index.toFloat() / (data.size - 1).toFloat()) else size.width / 2f
                val yIncome = size.height * (1f - (point.income.toFloat() / maxAmount))
                val yExpense = size.height * (1f - (point.expense.toFloat() / maxAmount))
                val yTransfers = size.height * (1f - (point.transfers.toFloat() / maxAmount))

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
                    pathTransfers.lineTo(x, yTransfers)
                    areaIncome.lineTo(x, yIncome)
                    areaExpense.lineTo(x, yExpense)
                    areaTransfers.lineTo(x, yTransfers)
                }
            }

            // close area paths
            areaIncome.lineTo(size.width, size.height)
            areaIncome.close()
            areaExpense.lineTo(size.width, size.height)
            areaExpense.close()
            areaTransfers.lineTo(size.width, size.height)
            areaTransfers.close()

            // draw filled areas (subtle)
            drawPath(path = areaIncome, color = IncomeGreen.copy(alpha = 0.10f))
            drawPath(path = areaExpense, color = ExpenseRed.copy(alpha = 0.10f))
            if (showTransfers) drawPath(path = areaTransfers, color = Color(0xFF42A5F5).copy(alpha = 0.08f))

            // draw lines
            drawPath(path = pathIncome, color = IncomeGreen, style = Stroke(width = 2.dp.toPx()))
            drawPath(path = pathExpense, color = ExpenseRed, style = Stroke(width = 2.dp.toPx()))
            if (showTransfers) drawPath(path = pathTransfers, color = Color(0xFF42A5F5), style = Stroke(width = 2.dp.toPx()))

            // draw points
            data.forEachIndexed { index, point ->
                val x = if (data.size > 1) size.width * (index.toFloat() / (data.size - 1).toFloat()) else size.width / 2f
                val yIncome = size.height * (1f - (point.income.toFloat() / maxAmount))
                val yExpense = size.height * (1f - (point.expense.toFloat() / maxAmount))
                val yTransfers = size.height * (1f - (point.transfers.toFloat() / maxAmount))
                drawCircle(color = IncomeGreen, radius = 3.dp.toPx(), center = Offset(x, yIncome))
                drawCircle(color = ExpenseRed, radius = 3.dp.toPx(), center = Offset(x, yExpense))
                if (showTransfers) drawCircle(color = Color(0xFF42A5F5), radius = 3.dp.toPx(), center = Offset(x, yTransfers))
            }

            // highlight selected index if any
            val sel = selectedIndex.value
            if (sel in data.indices) {
                val sPoint = data[sel]
                val x = if (data.size > 1) size.width * (sel.toFloat() / (data.size - 1).toFloat()) else size.width / 2f
                // vertical guide
                drawLine(color = onSurface.copy(alpha = 0.18f), start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 1.dp.toPx())
                // larger circles to highlight
                val yi = size.height * (1f - (sPoint.income.toFloat() / maxAmount))
                val ye = size.height * (1f - (sPoint.expense.toFloat() / maxAmount))
                val yt = size.height * (1f - (sPoint.transfers.toFloat() / maxAmount))
                drawCircle(onSurface, radius = 6.dp.toPx(), center = Offset(x, yi))
                drawCircle(onSurface, radius = 6.dp.toPx(), center = Offset(x, ye))
                if (showTransfers) drawCircle(onSurface, radius = 6.dp.toPx(), center = Offset(x, yt))
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
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.padding(4.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = sdf.format(Date(point.timestamp)), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Ingresos: ${formatter.format(point.income).replace(" ", "\u00A0")}", color = IncomeGreen, fontSize = 12.sp, maxLines = 1)
                        Text(text = "Gastos: ${formatter.format(point.expense).replace(" ", "\u00A0")}", color = ExpenseRed, fontSize = 12.sp, maxLines = 1)
                        if (showTransfers) Text(text = "Ahorro: ${formatter.format(point.transfers).replace(" ", "\u00A0")}", color = Color(0xFF42A5F5), fontSize = 12.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
fun TendenciasInsights(reportData: ReportData) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PE"))

    Column {
        // compute simple deltas from the trend (last vs previous point). Falls back to 0.
        val incomeDelta = computeDeltaFromTrend(reportData.incomeVsExpenseTrend) { it.income }
        val expenseDelta = computeDeltaFromTrend(reportData.incomeVsExpenseTrend) { it.expense }
        // approximate savings delta from net (income - expense) trend
        val savingsDelta = computeDeltaFromTrend(reportData.incomeVsExpenseTrend) { it.income - it.expense }

        // Top row: two small cards (Ingresos, Gastos)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InsightSmallCard(
                title = "Ingresos",
                value = formatter.format(reportData.totalIncome).replace(" ", "\u00A0"),
                percentText = calcDeltaText(incomeDelta),
                percentColor = if (incomeDelta >= 0) IncomeGreen else ExpenseRed,
                iconColor = IncomeGreen,
                modifier = Modifier.weight(1f)
            )

            InsightSmallCard(
                title = "Gastos",
                value = formatter.format(reportData.totalExpenses).replace(" ", "\u00A0"),
                percentText = calcDeltaText(expenseDelta),
                percentColor = if (expenseDelta >= 0) ExpenseRed else IncomeGreen,
                iconColor = ExpenseRed,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Large wide card: Ahorro actual
        InsightLargeCard(
            title = "Ahorro actual",
            value = formatter.format(reportData.totalTransfers).replace(" ", "\u00A0"),
            percentText = calcDeltaText(savingsDelta),
            percentColor = if (savingsDelta >= 0) IncomeGreen else ExpenseRed,
            iconColor = Color(0xFF42A5F5),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun InsightSmallCard(
    title: String,
    value: String,
    percentText: String,
    percentColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(iconColor.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                    // small glyph
                    Canvas(modifier = Modifier.size(18.dp)) {
                        drawCircle(color = iconColor.copy(alpha = 0.9f), radius = size.minDimension / 2f)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = title, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(text = percentText, color = percentColor, fontWeight = FontWeight.Bold)
                Text(text = "vs. mes pasado", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun InsightLargeCard(
    title: String,
    value: String,
    percentText: String,
    percentColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(iconColor.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(20.dp)) {
                        drawCircle(color = iconColor.copy(alpha = 0.95f), radius = size.minDimension / 2f)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
                }
                // small percent on the right
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = percentText, color = percentColor, fontWeight = FontWeight.Bold)
                    Text(text = "vs. mes pasado", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
    }
}

private fun calcDeltaText(delta: Double): String {
    if (delta.isNaN()) return ""
    val sign = if (delta >= 0) "+" else ""
    val pct = kotlin.math.abs(delta)
    val formatted = if (pct == kotlin.math.floor(pct)) "%d%%".format(pct.toInt()) else "%.1f%%".format(pct)
    return "$sign$formatted"
}

private fun computeDeltaFromTrend(data: List<TrendDataPoint>, selector: (TrendDataPoint) -> Double): Double {
    if (data.size < 2) return Double.NaN
    val last = selector(data.last())
    val prev = selector(data[data.size - 2])
    if (prev == 0.0) return Double.NaN
    return ((last - prev) / kotlin.math.abs(prev)) * 100.0
}

// Budget section removed per request. The previous composables for "Presupuesto vs. Gasto Real"
// were intentionally deleted to clean up the codebase. If you need to restore later, check
// version control or ask to re-implement a lighter-weight budget feature.
