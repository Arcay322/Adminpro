package com.example.admin_ingresos.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.admin_ingresos.ui.icons.LucideIconMapper
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.data.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDialog(
    transactions: List<Transaction>,
    categories: List<Category>,
    paymentMethods: List<PaymentMethod>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val exportService = remember { ExportService(context) }
    // Download/save-to-device state (mirrors ReportsScreen download flow)
    var pendingDownloadSourceUri by remember { mutableStateOf<Uri?>(null) }
    var isDownloadPending by remember { mutableStateOf(false) }
    var pendingDownloadMime by remember { mutableStateOf<String?>(null) }

    var exportResult by remember { mutableStateOf<String?>(null) }

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
                exportResult = "Descargado correctamente"
                Toast.makeText(context, "Descargado correctamente", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                exportResult = "Error al guardar: ${e.message}"
            } finally {
                pendingDownloadSourceUri = null
                isDownloadPending = false
                pendingDownloadMime = null
            }
        } else {
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
                exportResult = "Descargado correctamente"
                Toast.makeText(context, "Descargado correctamente", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                exportResult = "Error al guardar: ${e.message}"
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
                exportResult = "Descargado correctamente"
                Toast.makeText(context, "Descargado correctamente", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                exportResult = "Error al guardar: ${e.message}"
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
    
    var selectedFields by remember { mutableStateOf(ExportField.getDefaultFields()) }
    var includeHeaders by remember { mutableStateOf(true) }
    var exportFormat by remember { mutableStateOf(ExportFormat.CSV) }
    var isExporting by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.8f)
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Text(
                    text = "Exportar Transacciones",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${transactions.size} transacciones seleccionadas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Export options
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Format selection
                    item {
                        Text(
                            text = "Formato de exportación",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ExportFormat.values().forEach { format ->
                                FilterChip(
                                    onClick = { exportFormat = format },
                                    label = {
                                        Text(
                                            text = format.displayName,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    selected = exportFormat == format,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.04f),
                                        labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                )
                            }
                        }
                    }
                    
                    // Include headers option (only for CSV)
                    if (exportFormat == ExportFormat.CSV) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = includeHeaders,
                                    onCheckedChange = { includeHeaders = it }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Incluir encabezados",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Agregar nombres de columnas en la primera fila",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Field selection (only for CSV)
                    if (exportFormat == ExportFormat.CSV) {
                        item {
                            Text(
                                text = "Campos a exportar",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        items(ExportField.getAllFields()) { field ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedFields.contains(field),
                                    onCheckedChange = { checked ->
                                        selectedFields = if (checked) {
                                            selectedFields + field
                                        } else {
                                            selectedFields - field
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = field.displayName,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    } else {
                        // PDF format description
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Reporte PDF",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "El reporte PDF incluirá:\n• Resumen financiero\n• Tabla completa de transacciones\n• Estadísticas generales",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Export result message
                exportResult?.let { message ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(22.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(text = "Cancelar", maxLines = 1)
                    }

                    // Download button: saves file to device using a CreateDocument launcher
                    OutlinedButton(
                        onClick = {
                            val canExport = when (exportFormat) {
                                ExportFormat.CSV -> selectedFields.isNotEmpty()
                                ExportFormat.PDF -> true
                                ExportFormat.EXCEL -> true
                            }

                            if (canExport) {
                                scope.launch {
                                    isExporting = true
                                    exportResult = null

                                    try {
                                        val result = when (exportFormat) {
                                            ExportFormat.CSV -> exportService.exportTransactionsToCSV(
                                                transactions = transactions,
                                                categories = categories,
                                                paymentMethods = paymentMethods,
                                                includeHeaders = includeHeaders,
                                                customFields = selectedFields
                                            )
                                            ExportFormat.PDF -> exportService.generateTransactionsPDFReport(
                                                transactions = transactions,
                                                categories = categories,
                                                paymentMethods = paymentMethods,
                                                reportTitle = "Reporte de Transacciones"
                                            )
                                            ExportFormat.EXCEL -> exportService.exportTransactionsToXlsx(
                                                transactions = transactions,
                                                categories = categories,
                                                paymentMethods = paymentMethods
                                            )
                                        }

                                        when (result) {
                                            is com.example.admin_ingresos.data.ExportResult -> {
                                                val uri = result.uri
                                                if (uri != null) {
                                                    // Queue download
                                                    pendingDownloadSourceUri = uri
                                                    isDownloadPending = true
                                                    pendingDownloadMime = if (result.usedFallback) "text/csv" else exportFormat.mimeType
                                                    // Launch appropriate CreateDocument
                                                    if (pendingDownloadMime == "application/pdf") {
                                                        createPdfLauncher.launch("transacciones_${System.currentTimeMillis()}.pdf")
                                                    } else if (pendingDownloadMime == xlsxMime) {
                                                        createXlsxLauncher.launch("transacciones_${System.currentTimeMillis()}.xlsx")
                                                    } else {
                                                        createCsvLauncher.launch("transacciones_${System.currentTimeMillis()}.csv")
                                                    }
                                                    exportResult = "Generando archivo para descarga..."
                                                } else {
                                                    exportResult = "❌ Error al generar el archivo"
                                                }
                                            }
                                            is android.net.Uri -> {
                                                val uri = result as android.net.Uri
                                                pendingDownloadSourceUri = uri
                                                isDownloadPending = true
                                                pendingDownloadMime = exportFormat.mimeType
                                                if (pendingDownloadMime == "application/pdf") {
                                                    createPdfLauncher.launch("transacciones_${System.currentTimeMillis()}.pdf")
                                                } else if (pendingDownloadMime == xlsxMime) {
                                                    createXlsxLauncher.launch("transacciones_${System.currentTimeMillis()}.xlsx")
                                                } else {
                                                    createCsvLauncher.launch("transacciones_${System.currentTimeMillis()}.csv")
                                                }
                                                exportResult = "Generando archivo para descarga..."
                                            }
                                            null -> {
                                                exportResult = "❌ Error al exportar el archivo"
                                            }
                                            else -> {
                                                exportResult = "❌ Error al exportar el archivo"
                                            }
                                        }
                                    } catch (e: Exception) {
                                        exportResult = "❌ Error: ${e.message}"
                                    } finally {
                                        isExporting = false
                                    }
                                }
                            }
                        },
                        enabled = (exportFormat == ExportFormat.PDF || selectedFields.isNotEmpty()) && !isExporting,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(22.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            // Icon-only button for download (Lucide)
                            Icon(
                                imageVector = LucideIconMapper.Navigation.download,
                                contentDescription = "Descargar",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val canExport = when (exportFormat) {
                                ExportFormat.CSV -> selectedFields.isNotEmpty()
                                ExportFormat.PDF -> true // PDF doesn't need field selection
                                ExportFormat.EXCEL -> true // Excel will export full dataset
                            }

                            if (canExport) {
                                scope.launch {
                                    isExporting = true
                                    exportResult = null

                                    try {
                                        val result = when (exportFormat) {
                                            ExportFormat.CSV -> exportService.exportTransactionsToCSV(
                                                transactions = transactions,
                                                categories = categories,
                                                paymentMethods = paymentMethods,
                                                includeHeaders = includeHeaders,
                                                customFields = selectedFields
                                            )
                                            ExportFormat.PDF -> exportService.generateTransactionsPDFReport(
                                                transactions = transactions,
                                                categories = categories,
                                                paymentMethods = paymentMethods,
                                                reportTitle = "Reporte de Transacciones"
                                            )
                                            ExportFormat.EXCEL -> exportService.exportTransactionsToXlsx(
                                                transactions = transactions,
                                                categories = categories,
                                                paymentMethods = paymentMethods
                                            )
                                        }
                                        // Normalize result: CSV/PDF return Uri, EXCEL returns ExportResult
                                        when (result) {
                                            is com.example.admin_ingresos.data.ExportResult -> {
                                                val uri = result.uri
                                                if (uri != null) {
                                                    val mime = if (result.usedFallback) "text/csv" else exportFormat.mimeType
                                                    exportService.shareFile(uri, mime)
                                                    exportResult = if (result.usedFallback) "✅ Exportado (fallback CSV) y compartido" else "✅ ${exportFormat.displayName} exportado y compartido exitosamente"
                                                } else {
                                                    exportResult = "❌ Error al exportar el archivo"
                                                }
                                            }
                                            is android.net.Uri -> {
                                                val uri = result as android.net.Uri
                                                exportService.shareFile(uri, exportFormat.mimeType)
                                                exportResult = "✅ ${exportFormat.displayName} exportado y compartido exitosamente"
                                            }
                                            null -> {
                                                exportResult = "❌ Error al exportar el archivo"
                                            }
                                            else -> {
                                                exportResult = "❌ Error al exportar el archivo"
                                            }
                                        }
                                    } catch (e: Exception) {
                                        exportResult = "❌ Error: ${e.message}"
                                    } finally {
                                        isExporting = false
                                    }
                                }
                            }
                        },
                        enabled = (exportFormat == ExportFormat.PDF || selectedFields.isNotEmpty()) && !isExporting,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(22.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            // Icon-only button for share (Material)
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Compartir",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}