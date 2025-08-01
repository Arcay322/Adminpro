package com.example.admin_ingresos.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    
    var selectedFields by remember { mutableStateOf(ExportField.getDefaultFields()) }
    var includeHeaders by remember { mutableStateOf(true) }
    var exportFormat by remember { mutableStateOf(ExportFormat.CSV) }
    var isExporting by remember { mutableStateOf(false) }
    var exportResult by remember { mutableStateOf<String?>(null) }
    
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
                                    label = { Text(format.displayName) },
                                    selected = exportFormat == format,
                                    modifier = Modifier.weight(1f)
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
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = {
                            val canExport = when (exportFormat) {
                                ExportFormat.CSV -> selectedFields.isNotEmpty()
                                ExportFormat.PDF -> true // PDF doesn't need field selection
                            }
                            
                            if (canExport) {
                                scope.launch {
                                    isExporting = true
                                    exportResult = null
                                    
                                    try {
                                        val uri = when (exportFormat) {
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
                                        }
                                        
                                        if (uri != null) {
                                            exportService.shareFile(uri, exportFormat.mimeType)
                                            exportResult = "✅ ${exportFormat.displayName} exportado y compartido exitosamente"
                                        } else {
                                            exportResult = "❌ Error al exportar el archivo"
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
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Exportar")
                        }
                    }
                }
            }
        }
    }
}