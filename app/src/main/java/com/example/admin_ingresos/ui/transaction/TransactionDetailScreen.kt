package com.example.admin_ingresos.ui.transaction

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.AppDatabaseProvider
import com.example.admin_ingresos.data.Transaction
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.withContext
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import kotlin.math.max
import com.example.admin_ingresos.ui.components.*
import com.example.admin_ingresos.ui.icons.LucideIconMapper
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.clickable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: Int,
    onNavigateBack: () -> Unit,
    onOpenTransaction: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { AppDatabaseProvider.getDatabase(context) }
    var transaction by remember { mutableStateOf<Transaction?>(null) }
    var categoryName by remember { mutableStateOf<String?>(null) }
    var categoryIcon by remember { mutableStateOf<ImageVector?>(null) }
    var categoryColorStr by remember { mutableStateOf<String?>(null) }
    var paymentMethodName by remember { mutableStateOf<String?>(null) }
    var paymentMethodIcon by remember { mutableStateOf(null as androidx.compose.ui.graphics.vector.ImageVector?) }
    // Trend data for the category (last N months)
    var trendLabels by remember { mutableStateOf<List<String>>(emptyList()) }
    var trendValues by remember { mutableStateOf<List<Double>>(emptyList()) }
    var trendAverage by remember { mutableStateOf(0.0) }
    var trendInsight by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(transactionId) {
        withContext(kotlinx.coroutines.Dispatchers.IO) {
            val tx = db.transactionDao().getById(transactionId)
            transaction = tx
                tx?.let {
                    // load category name
                    val cat = db.categoryDao().getCategoryById(it.categoryId)
                    categoryName = cat?.name ?: "Sin categoría"
                    categoryColorStr = cat?.color
                    // get icon by category name
                    categoryIcon = categoryName?.let { name -> LucideIconMapper.getIconFromCategoryName(name) }

                    // load payment methods list and find selected
                    val methods = db.paymentMethodDao().getAll()
                    val pm = methods.find { m -> m.id == it.paymentMethodId }
                    paymentMethodName = pm?.name
                    paymentMethodIcon = pm?.name?.let { name -> LucideIconMapper.getPaymentMethodIcon(name) }

                    // Build trend data for the last 6 months for this category (matching the transaction type)
                    try {
                        val allCat = db.transactionDao().getByCategory(it.categoryId)
                            .filter { t -> t.type.equals(it.type, ignoreCase = true) }

                        val months = 6
                        val cal = java.util.Calendar.getInstance()
                        val labels = mutableListOf<String>()
                        val values = mutableListOf<Double>()

                        // Prepare month-year keys for last `months` months (oldest -> newest)
                        val monthKeys = mutableListOf<Pair<Int, Int>>() // month, year
                        for (i in months - 1 downTo 0) {
                            val c = java.util.Calendar.getInstance()
                            c.add(java.util.Calendar.MONTH, -i)
                            monthKeys.add(Pair(c.get(java.util.Calendar.MONTH), c.get(java.util.Calendar.YEAR)))
                        }

                        for ((m, y) in monthKeys) {
                            val sum = allCat.filter { t ->
                                val c = java.util.Calendar.getInstance()
                                c.timeInMillis = t.date
                                c.get(java.util.Calendar.MONTH) == m && c.get(java.util.Calendar.YEAR) == y
                            }.sumOf { t -> t.amount }
                            // label short month name
                            val lbl = java.text.SimpleDateFormat("MMM", Locale("es")).apply { timeZone = java.util.TimeZone.getDefault() }.format(java.util.Calendar.getInstance().also { it.set(java.util.Calendar.MONTH, m); it.set(java.util.Calendar.YEAR, y) }.time)
                            labels.add(lbl)
                            values.add(sum)
                        }

                        trendLabels = labels
                        trendValues = values
                        trendAverage = if (values.isNotEmpty()) values.average() else 0.0
                        // Compare most recent month with average
                        val last = values.lastOrNull() ?: 0.0
                        // Tailor insight text depending on whether this is a transfer (savings) or expense/income
                        trendInsight = if (it.type.equals(com.example.admin_ingresos.data.Transaction.TYPE_TRANSFER, ignoreCase = true)) {
                            when {
                                trendAverage <= 0.0 -> "Sin datos suficientes para calcular un promedio."
                                last > trendAverage * 1.1 -> "Has ahorrado más de lo habitual en ${categoryName ?: "esta categoría"}. Promedio: ${com.example.admin_ingresos.data.CurrencyUtils.format(trendAverage, context)}."
                                last < trendAverage * 0.9 -> "Has ahorrado menos de lo habitual en ${categoryName ?: "esta categoría"}. Promedio: ${com.example.admin_ingresos.data.CurrencyUtils.format(trendAverage, context)}."
                                else -> "Tu ahorro en ${categoryName ?: "esta categoría"} está dentro de lo habitual. Promedio: ${com.example.admin_ingresos.data.CurrencyUtils.format(trendAverage, context)}."
                            }
                        } else {
                            when {
                                trendAverage <= 0.0 -> "Sin datos suficientes para calcular un promedio."
                                last > trendAverage * 1.1 -> "Tu gasto promedio en ${categoryName ?: "esta categoría"} es de ${com.example.admin_ingresos.data.CurrencyUtils.format(trendAverage, context)}. ¡Este gasto fue un poco más alto de lo normal!"
                                last < trendAverage * 0.9 -> "Tu gasto promedio en ${categoryName ?: "esta categoría"} es de ${com.example.admin_ingresos.data.CurrencyUtils.format(trendAverage, context)}. ¡Este gasto fue más bajo de lo normal!"
                                else -> "Tu gasto promedio en ${categoryName ?: "esta categoría"} es de ${com.example.admin_ingresos.data.CurrencyUtils.format(trendAverage, context)}. Este gasto está dentro de lo normal."
                            }
                        }
                    } catch (e: Exception) {
                        // ignore
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(androidx.compose.ui.res.stringResource(id = com.example.admin_ingresos.R.string.transaction_detail_title), style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = androidx.compose.ui.res.stringResource(id = com.example.admin_ingresos.R.string.back), tint = MaterialTheme.colorScheme.primary) }
        },
                actions = {
                    // Use Lucide icons for edit/delete with requested tints
                    IconButton(onClick = { /* TODO: navegar a editar */ }) {
                        Icon(
                            imageVector = com.example.admin_ingresos.ui.icons.LucideIconMapper.Navigation.edit,
                            contentDescription = androidx.compose.ui.res.stringResource(id = com.example.admin_ingresos.R.string.edit),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        // Apply only the top padding from the Scaffold/NavHost to avoid adding
        // extra bottom padding (which would create a visible gap above the
        // app bottom navigation). The outer NavHost/Scaffold already provides
        // appropriate bottom inset for the BottomNavigationBar.
        Surface(modifier = Modifier.padding(top = padding.calculateTopPadding()).fillMaxSize()) {
            if (transaction == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val tx = transaction!!
                // derive category colors once so we can use them for card container/border and icon
                val bgColor = try { Color(android.graphics.Color.parseColor(categoryColorStr ?: "")) } catch (_: Exception) { com.example.admin_ingresos.ui.theme.getCategoryColor(categoryName) }
                val iconTint = androidx.compose.ui.graphics.lerp(bgColor, Color.White, 0.18f)
                Column(modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Big amount card (prominent) with improved hierarchy
                    CashFlowCard(modifier = Modifier.fillMaxWidth(), containerColor = bgColor.copy(alpha = 0.18f), borderColor = bgColor.copy(alpha = 0.20f)) {
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon in circular background
                            // Category icon using stored category color (fallback to helper) and a slightly lighter icon tint
                            Box(modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(bgColor.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = categoryIcon ?: LucideIconMapper.Navigation.transactions,
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(categoryName ?: androidx.compose.ui.res.stringResource(id = com.example.admin_ingresos.R.string.profile_empty_placeholder), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = com.example.admin_ingresos.ui.theme.TextPrimary)

                                // Amount and type row
                                val formatted = com.example.admin_ingresos.data.CurrencyUtils.format(tx.amount, context)
                                val amountColor = when {
                                    tx.type.equals(com.example.admin_ingresos.data.Transaction.TYPE_INCOME, ignoreCase = true) -> MaterialTheme.colorScheme.primary
                                    tx.type.equals(com.example.admin_ingresos.data.Transaction.TYPE_TRANSFER, ignoreCase = true) -> Color(0xFF42A5F5)
                                    else -> MaterialTheme.colorScheme.error
                                }

                                Text(formatted, style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold), color = amountColor)

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(java.text.SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("es")).format(java.util.Date(tx.date)), style = MaterialTheme.typography.bodySmall, color = com.example.admin_ingresos.ui.theme.TextSecondary)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Transaction type label and icon (show 'Ahorro' + piggy for transfers)
                                        val displayTypeLabel = if (tx.type.equals(com.example.admin_ingresos.data.Transaction.TYPE_TRANSFER, ignoreCase = true)) "Ahorro" else tx.type
                                        val displayTypeIcon = if (tx.type.equals(com.example.admin_ingresos.data.Transaction.TYPE_TRANSFER, ignoreCase = true)) com.example.admin_ingresos.ui.icons.LucideIconMapper.getSavingsGoalIcon("other") else com.example.admin_ingresos.ui.icons.LucideIconMapper.getTransactionTypeIcon(tx.type)

                                        Text(displayTypeLabel, style = MaterialTheme.typography.labelMedium, color = amountColor)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(imageVector = displayTypeIcon, contentDescription = null, tint = amountColor, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            // (Icon for transaction type now appears next to the type label above.)
                        }
                    }

                    // Single attributes card
                    // Consolidated details card: payment method and notes
                    CashFlowCard(modifier = Modifier.fillMaxWidth(), containerColor = bgColor.copy(alpha = 0.18f), borderColor = bgColor.copy(alpha = 0.20f)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Payment method row (if available)
                            if (!paymentMethodName.isNullOrBlank()) {
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = paymentMethodIcon ?: LucideIconMapper.Navigation.dollarSign, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(paymentMethodName ?: "-", style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            // Notes / Description
                            Text(text = tx.description.ifBlank { androidx.compose.ui.res.stringResource(id = com.example.admin_ingresos.R.string.no_description) }, style = MaterialTheme.typography.bodyMedium, maxLines = 6, overflow = TextOverflow.Ellipsis)

                            // ID subtle
                            Text("ID: ${tx.id}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // New Comprobante card: thumbnail clickable -> open full screen
                    if (!tx.receiptPhotoUri.isNullOrBlank()) {
                        var showFull by remember { mutableStateOf(false) }
                        CashFlowCard(modifier = Modifier.fillMaxWidth(), containerColor = bgColor.copy(alpha = 0.18f), borderColor = bgColor.copy(alpha = 0.20f)) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(androidx.compose.ui.res.stringResource(id = com.example.admin_ingresos.R.string.receipt_attached_label), style = MaterialTheme.typography.titleSmall)
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    val painter = rememberAsyncImagePainter(tx.receiptPhotoUri)
                                    Image(
                                        painter = painter,
                                        contentDescription = "Recibo",
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .then(Modifier)
                                            .clickable { showFull = true },
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(androidx.compose.ui.res.stringResource(id = com.example.admin_ingresos.R.string.view_photo), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(androidx.compose.ui.res.stringResource(id = com.example.admin_ingresos.R.string.tap_thumbnail), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        if (showFull) {
                            // Fullscreen dialog to show image
                            Dialog(onDismissRequest = { showFull = false }) {
                                Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 8.dp, modifier = Modifier.fillMaxSize()) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        val painter = rememberAsyncImagePainter(tx.receiptPhotoUri)
                                        Image(painter = painter, contentDescription = "Recibo grande", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                                        IconButton(onClick = { showFull = false }, modifier = Modifier.align(Alignment.TopEnd)) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cerrar")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Compact intelligent insight (replace the chart): last month, change vs previous month, average and short insight
                    if (trendLabels.isNotEmpty() && trendValues.isNotEmpty()) {
                        CashFlowCard(modifier = Modifier.fillMaxWidth(), containerColor = bgColor.copy(alpha = 0.18f), borderColor = bgColor.copy(alpha = 0.20f)) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val title = when {
                                    tx.type.equals(com.example.admin_ingresos.data.Transaction.TYPE_TRANSFER, ignoreCase = true) -> androidx.compose.ui.res.stringResource(id = com.example.admin_ingresos.R.string.insight_savings_title, categoryName ?: androidx.compose.ui.res.stringResource(id = com.example.admin_ingresos.R.string.this_category))
                                    tx.type.equals(com.example.admin_ingresos.data.Transaction.TYPE_INCOME, ignoreCase = true) -> androidx.compose.ui.res.stringResource(id = com.example.admin_ingresos.R.string.insight_income_title, categoryName ?: androidx.compose.ui.res.stringResource(id = com.example.admin_ingresos.R.string.this_category))
                                    else -> androidx.compose.ui.res.stringResource(id = com.example.admin_ingresos.R.string.insight_expense_title, categoryName ?: androidx.compose.ui.res.stringResource(id = com.example.admin_ingresos.R.string.this_category))
                                }
                                Text(title, style = MaterialTheme.typography.titleSmall)

                                // Compute simple KPIs
                                val last = trendValues.lastOrNull() ?: 0.0
                                val prev = trendValues.getOrNull(trendValues.size - 2) ?: 0.0
                                val avg = trendAverage
                                val pctChange: Double? = if (prev == 0.0) null else ((last - prev) / prev * 100.0)
                                val changeIsUp = (pctChange ?: 0.0) > 0.0

                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text(androidx.compose.ui.res.stringResource(id = com.example.admin_ingresos.R.string.last_month), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(com.example.admin_ingresos.data.CurrencyUtils.format(last, context, maxFractionDigits = 0), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                                            if (pctChange != null) {
                                                Icon(
                                                    imageVector = if (changeIsUp) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                    contentDescription = null,
                                                    tint = if (changeIsUp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                                )
                                                Text(text = "${if (pctChange > 0) "+" else ""}${"%.0f".format(pctChange)}%", style = MaterialTheme.typography.bodyMedium, color = if (changeIsUp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                                            } else {
                                                Text(text = androidx.compose.ui.res.stringResource(id = com.example.admin_ingresos.R.string.no_comparison), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(androidx.compose.ui.res.stringResource(id = com.example.admin_ingresos.R.string.average_label, com.example.admin_ingresos.data.CurrencyUtils.format(avg, context, maxFractionDigits = 0)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                // Short, human-friendly insight derived from previous calculation (fallback to existing insight text)
                                val smartInsight = if (tx.type.equals(com.example.admin_ingresos.data.Transaction.TYPE_TRANSFER, ignoreCase = true)) {
                                    when {
                                        avg <= 0.0 -> "Sin datos suficientes para generar un insight."
                                        pctChange == null -> "No hay datos anteriores para comparación."
                                        pctChange > 0 -> "Este mes ahorraste ${com.example.admin_ingresos.data.CurrencyUtils.format(last, context)} — ${"%.0f".format(pctChange)}% más que el mes anterior."
                                        pctChange < 0 -> "Este mes ahorraste ${com.example.admin_ingresos.data.CurrencyUtils.format(last, context)} — ${"%.0f".format(-pctChange)}% menos que el mes anterior."
                                        else -> "Tu ahorro está estable respecto al periodo anterior."
                                    }
                                } else {
                                    when {
                                        avg <= 0.0 -> "Sin datos suficientes para generar un insight."
                                        pctChange == null -> "No hay datos anteriores para comparación."
                                        pctChange > 10 -> "Atención: este mes tu ${if (tx.type.equals("Ingreso", true)) "ingreso" else "gasto"} en ${categoryName ?: "esta categoría"} aumentó significativamente respecto al mes anterior."
                                        pctChange < -10 -> "Buen trabajo: este mes tu ${if (tx.type.equals("Ingreso", true)) "ingreso" else "gasto"} en ${categoryName ?: "esta categoría"} disminuyó respecto al mes anterior."
                                        else -> "Este mes está dentro del rango normal para esta categoría."
                                    }
                                }

                                Text(smartInsight, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
