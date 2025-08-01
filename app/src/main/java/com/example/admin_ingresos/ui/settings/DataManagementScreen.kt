package com.example.admin_ingresos.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class DataManagementAction(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val isDestructive: Boolean = false,
    val requiresConfirmation: Boolean = false
)

@Composable
fun DataManagementScreen(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var lastBackupDate by remember { mutableStateOf<Date?>(null) }
    
    val dataActions = remember {
        listOf(
            DataManagementAction(
                id = "backup",
                title = "Crear Respaldo",
                description = "Guardar todos los datos en un archivo",
                icon = Icons.Default.Backup,
                iconColor = Color(0xFF4CAF50)
            ),
            DataManagementAction(
                id = "restore",
                title = "Restaurar Datos",
                description = "Importar datos desde un archivo de respaldo",
                icon = Icons.Default.Restore,
                iconColor = Color(0xFF2196F3)
            ),
            DataManagementAction(
                id = "export_csv",
                title = "Exportar a CSV",
                description = "Exportar transacciones en formato CSV",
                icon = Icons.Default.TableChart,
                iconColor = Color(0xFF009688)
            ),
            DataManagementAction(
                id = "export_pdf",
                title = "Exportar Reporte PDF",
                description = "Generar reporte completo en PDF",
                icon = Icons.Default.PictureAsPdf,
                iconColor = Color(0xFFFF5722)
            ),
            DataManagementAction(
                id = "import_data",
                title = "Importar Datos",
                description = "Importar transacciones desde archivo CSV",
                icon = Icons.Default.FileUpload,
                iconColor = Color(0xFF795548)
            ),
            DataManagementAction(
                id = "clear_cache",
                title = "Limpiar Caché",
                description = "Eliminar archivos temporales y caché",
                icon = Icons.Default.CleaningServices,
                iconColor = Color(0xFFFF9800),
                requiresConfirmation = true
            ),
            DataManagementAction(
                id = "clear_data",
                title = "Borrar Todas las Transacciones",
                description = "Eliminar todas las transacciones (mantiene configuración)",
                icon = Icons.Default.DeleteSweep,
                iconColor = Color(0xFFE91E63),
                isDestructive = true,
                requiresConfirmation = true
            ),
            DataManagementAction(
                id = "reset_app",
                title = "Restablecer Aplicación",
                description = "Eliminar todos los datos y configuraciones",
                icon = Icons.Default.RestartAlt,
                iconColor = Color(0xFFF44336),
                isDestructive = true,
                requiresConfirmation = true
            )
        )
    }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = "Gestión de Datos",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Backup, restauración y limpieza",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Storage info
        item {
            StorageInfoCard()
        }
        
        // Backup section
        item {
            Text(
                text = "Respaldo y Restauración",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        
        item {
            BackupStatusCard(
                lastBackupDate = lastBackupDate,
                onCreateBackup = { showBackupDialog = true }
            )
        }
        
        // Data actions
        item {
            Text(
                text = "Acciones de Datos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )
        }
        
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column {
                    dataActions.forEachIndexed { index, action ->
                        DataActionItem(
                            action = action,
                            isLoading = isLoading,
                            onClick = {
                                when (action.id) {
                                    "backup" -> showBackupDialog = true
                                    "restore" -> showRestoreDialog = true
                                    "clear_data" -> showClearDataDialog = true
                                    "reset_app" -> showResetDialog = true
                                    "export_csv" -> {
                                        scope.launch {
                                            isLoading = true
                                            // TODO: Implement CSV export
                                            kotlinx.coroutines.delay(2000)
                                            isLoading = false
                                        }
                                    }
                                    "export_pdf" -> {
                                        scope.launch {
                                            isLoading = true
                                            // TODO: Implement PDF export
                                            kotlinx.coroutines.delay(2000)
                                            isLoading = false
                                        }
                                    }
                                    "import_data" -> {
                                        // TODO: Implement data import
                                    }
                                    "clear_cache" -> {
                                        scope.launch {
                                            isLoading = true
                                            // TODO: Implement cache clearing
                                            kotlinx.coroutines.delay(1000)
                                            isLoading = false
                                        }
                                    }
                                }
                            },
                            showDivider = index < dataActions.size - 1
                        )
                    }
                }
            }
        }
        
        // Data statistics
        item {
            DataStatisticsCard()
        }
        
        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
    
    // Dialogs
    if (showBackupDialog) {
        BackupDialog(
            onConfirm = {
                scope.launch {
                    isLoading = true
                    // TODO: Implement backup creation
                    kotlinx.coroutines.delay(3000)
                    lastBackupDate = Date()
                    isLoading = false
                    showBackupDialog = false
                }
            },
            onDismiss = { showBackupDialog = false }
        )
    }
    
    if (showRestoreDialog) {
        RestoreDialog(
            onConfirm = {
                scope.launch {
                    isLoading = true
                    // TODO: Implement data restoration
                    kotlinx.coroutines.delay(3000)
                    isLoading = false
                    showRestoreDialog = false
                }
            },
            onDismiss = { showRestoreDialog = false }
        )
    }
    
    if (showClearDataDialog) {
        ClearDataDialog(
            onConfirm = {
                scope.launch {
                    isLoading = true
                    // TODO: Implement data clearing
                    kotlinx.coroutines.delay(2000)
                    isLoading = false
                    showClearDataDialog = false
                }
            },
            onDismiss = { showClearDataDialog = false }
        )
    }
    
    if (showResetDialog) {
        ResetAppDialog(
            onConfirm = {
                scope.launch {
                    isLoading = true
                    // TODO: Implement app reset
                    kotlinx.coroutines.delay(2000)
                    isLoading = false
                    showResetDialog = false
                }
            },
            onDismiss = { showResetDialog = false }
        )
    }
}

@Composable
fun StorageInfoCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Información de Almacenamiento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StorageInfoItem("Datos de la app", "2.4 MB")
                StorageInfoItem("Archivos de respaldo", "1.2 MB")
                StorageInfoItem("Caché", "0.8 MB")
            }
        }
    }
}

@Composable
fun StorageInfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun BackupStatusCard(
    lastBackupDate: Date?,
    onCreateBackup: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Estado del Respaldo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (lastBackupDate != null) {
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        Text(
                            text = "Último respaldo: ${dateFormat.format(lastBackupDate)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "No hay respaldos creados",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                Button(onClick = onCreateBackup) {
                    Icon(
                        imageVector = Icons.Default.Backup,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crear Respaldo")
                }
            }
        }
    }
}

@Composable
fun DataActionItem(
    action: DataManagementAction,
    isLoading: Boolean,
    onClick: () -> Unit,
    showDivider: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isLoading) { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(action.iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.title,
                    tint = action.iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (action.isDestructive) 
                        MaterialTheme.colorScheme.error
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = action.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Loading or arrow
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Ejecutar ${action.title}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
fun DataStatisticsCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Estadísticas de Datos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DataStatItem("Transacciones", "1,234")
                DataStatItem("Categorías", "15")
                DataStatItem("Presupuestos", "8")
            }
        }
    }
}

@Composable
fun DataStatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Dialog Composables
@Composable
fun BackupDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Backup,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text("Crear Respaldo")
        },
        text = {
            Text("Se creará un archivo de respaldo con todas tus transacciones, categorías y configuraciones. Este proceso puede tomar unos momentos.")
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Crear Respaldo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun RestoreDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Restore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text("Restaurar Datos")
        },
        text = {
            Text("Se restaurarán los datos desde un archivo de respaldo. Esto reemplazará todos los datos actuales. ¿Estás seguro?")
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Restaurar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ClearDataDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text("Borrar Todas las Transacciones")
        },
        text = {
            Text("Se eliminarán TODAS las transacciones permanentemente. Las configuraciones y categorías se mantendrán. Esta acción no se puede deshacer.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Borrar Todo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ResetAppDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text("Restablecer Aplicación")
        },
        text = {
            Text("Se eliminarán TODOS los datos y configuraciones, regresando la app a su estado inicial. Esta acción no se puede deshacer.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Restablecer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}