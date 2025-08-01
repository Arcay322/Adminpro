package com.example.admin_ingresos.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun DashboardCustomizationDialog(
    widgetConfigs: List<DashboardWidgetConfig>,
    onConfigsChanged: (List<DashboardWidgetConfig>) -> Unit,
    onDismiss: () -> Unit
) {
    var configs by remember { mutableStateOf(widgetConfigs.sortedBy { it.order }) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Personalizar Dashboard",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Arrastra para reordenar • Toca para mostrar/ocultar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Widget list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(configs) { index, config ->
                        DashboardWidgetConfigItem(
                            config = config,
                            onVisibilityToggle = { 
                                configs = configs.toMutableList().apply {
                                    this[index] = config.copy(isVisible = !config.isVisible)
                                }
                            },
                            onMoveUp = if (index > 0) {
                                {
                                    configs = configs.toMutableList().apply {
                                        val temp = this[index]
                                        this[index] = this[index - 1].copy(order = temp.order)
                                        this[index - 1] = temp.copy(order = this[index].order)
                                        sortBy { it.order }
                                    }
                                }
                            } else null,
                            onMoveDown = if (index < configs.size - 1) {
                                {
                                    configs = configs.toMutableList().apply {
                                        val temp = this[index]
                                        this[index] = this[index + 1].copy(order = temp.order)
                                        this[index + 1] = temp.copy(order = this[index].order)
                                        sortBy { it.order }
                                    }
                                }
                            } else null
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            // Reset to defaults
                            configs = getDefaultWidgetConfigs()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Restablecer")
                    }
                    
                    Button(
                        onClick = {
                            onConfigsChanged(configs)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardWidgetConfigItem(
    config: DashboardWidgetConfig,
    onVisibilityToggle: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (config.isVisible) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (config.isVisible) 2.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onVisibilityToggle() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag handle
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Arrastrar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Widget icon
            Icon(
                imageVector = getWidgetIcon(config.type),
                contentDescription = null,
                tint = if (config.isVisible) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Widget info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = config.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (config.isVisible) 
                        MaterialTheme.colorScheme.onSurface 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (config.isVisible) FontWeight.Medium else FontWeight.Normal
                )
                Text(
                    text = getWidgetDescription(config.type),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Move buttons
            Column {
                IconButton(
                    onClick = onMoveUp ?: {},
                    enabled = onMoveUp != null,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Mover arriba",
                        modifier = Modifier.size(20.dp),
                        tint = if (onMoveUp != null) 
                            MaterialTheme.colorScheme.onSurfaceVariant 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
                IconButton(
                    onClick = onMoveDown ?: {},
                    enabled = onMoveDown != null,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Mover abajo",
                        modifier = Modifier.size(20.dp),
                        tint = if (onMoveDown != null) 
                            MaterialTheme.colorScheme.onSurfaceVariant 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Visibility toggle
            Switch(
                checked = config.isVisible,
                onCheckedChange = { onVisibilityToggle() }
            )
        }
    }
}

fun getWidgetIcon(type: WidgetType): ImageVector {
    return when (type) {
        WidgetType.BALANCE -> Icons.Default.AccountBalance
        WidgetType.INCOME -> Icons.Default.TrendingUp
        WidgetType.EXPENSES -> Icons.Default.TrendingDown
        WidgetType.RECENT_TRANSACTIONS -> Icons.Default.History
        WidgetType.EXPENSE_CHART -> Icons.Default.PieChart
        WidgetType.QUICK_ACTIONS -> Icons.Default.Speed
        WidgetType.BUDGET_OVERVIEW -> Icons.Default.Savings
        WidgetType.SAVINGS_GOAL -> Icons.Default.Flag
    }
}

fun getWidgetDescription(type: WidgetType): String {
    return when (type) {
        WidgetType.BALANCE -> "Muestra tu balance total actual"
        WidgetType.INCOME -> "Resumen de ingresos del período"
        WidgetType.EXPENSES -> "Resumen de gastos del período"
        WidgetType.RECENT_TRANSACTIONS -> "Lista de transacciones recientes"
        WidgetType.EXPENSE_CHART -> "Gráfico interactivo de gastos por categoría"
        WidgetType.QUICK_ACTIONS -> "Botones de acceso rápido"
        WidgetType.BUDGET_OVERVIEW -> "Estado de tus presupuestos"
        WidgetType.SAVINGS_GOAL -> "Progreso de metas de ahorro"
    }
}

fun getDefaultWidgetConfigs(): List<DashboardWidgetConfig> {
    return listOf(
        DashboardWidgetConfig(WidgetType.BALANCE, "Balance", true, 0),
        DashboardWidgetConfig(WidgetType.INCOME, "Ingresos", true, 1),
        DashboardWidgetConfig(WidgetType.EXPENSES, "Gastos", true, 2),
        DashboardWidgetConfig(WidgetType.QUICK_ACTIONS, "Acciones Rápidas", true, 3),
        DashboardWidgetConfig(WidgetType.EXPENSE_CHART, "Gráfico de Gastos", true, 4),
        DashboardWidgetConfig(WidgetType.RECENT_TRANSACTIONS, "Transacciones Recientes", true, 5),
        DashboardWidgetConfig(WidgetType.BUDGET_OVERVIEW, "Presupuestos", false, 6),
        DashboardWidgetConfig(WidgetType.SAVINGS_GOAL, "Metas de Ahorro", false, 7)
    )
}

// Widget preferences manager
class DashboardPreferences {
    companion object {
        private var _widgetConfigs = mutableStateOf(getDefaultWidgetConfigs())
        val widgetConfigs: State<List<DashboardWidgetConfig>> = _widgetConfigs
        
        fun updateWidgetConfigs(configs: List<DashboardWidgetConfig>) {
            _widgetConfigs.value = configs.mapIndexed { index, config ->
                config.copy(order = index)
            }
        }
        
        fun toggleWidgetVisibility(type: WidgetType) {
            _widgetConfigs.value = _widgetConfigs.value.map { config ->
                if (config.type == type) {
                    config.copy(isVisible = !config.isVisible)
                } else {
                    config
                }
            }
        }
        
        fun moveWidget(fromIndex: Int, toIndex: Int) {
            val configs = _widgetConfigs.value.toMutableList()
            val item = configs.removeAt(fromIndex)
            configs.add(toIndex, item)
            _widgetConfigs.value = configs.mapIndexed { index, config ->
                config.copy(order = index)
            }
        }
        
        fun resetToDefaults() {
            _widgetConfigs.value = getDefaultWidgetConfigs()
        }
    }
}