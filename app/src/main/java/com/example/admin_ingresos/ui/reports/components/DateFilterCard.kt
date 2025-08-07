package com.example.admin_ingresos.ui.reports.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin_ingresos.ui.components.CashFlowCard
import com.example.admin_ingresos.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class DateRange(
    val startDate: Long,
    val endDate: Long,
    val label: String
)

enum class DateFilterPeriod(val label: String) {
    TODAY("Hoy"),
    YESTERDAY("Ayer"),
    THIS_WEEK("Esta semana"),
    THIS_MONTH("Este mes"),
    LAST_MONTH("Mes pasado"),
    THIS_YEAR("Este año"),
    CUSTOM("Personalizado")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterCard(
    selectedPeriod: DateFilterPeriod,
    onPeriodSelected: (DateFilterPeriod) -> Unit,
    customDateRange: DateRange?,
    onCustomDateRangeSelected: (DateRange) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var isSelectingStartDate by remember { mutableStateOf(true) }
    
    CashFlowCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Filtrar por Período",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Period chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateFilterPeriod.values().take(4).forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { onPeriodSelected(period) },
                        label = { 
                            Text(
                                text = period.label,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Second row for remaining periods
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateFilterPeriod.values().drop(4).forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { 
                            if (period == DateFilterPeriod.CUSTOM) {
                                showDatePicker = true
                                isSelectingStartDate = true
                            } else {
                                onPeriodSelected(period)
                            }
                        },
                        label = { 
                            Text(
                                text = period.label,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = if (period == DateFilterPeriod.CUSTOM) {
                            {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null
                    )
                }
            }
            
            // Custom date range display
            if (selectedPeriod == DateFilterPeriod.CUSTOM && customDateRange != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Rango personalizado",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = customDateRange.label,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        IconButton(
                            onClick = { 
                                showDatePicker = true
                                isSelectingStartDate = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar rango",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Date picker dialog would go here
    // For now, we'll use a simplified implementation
    if (showDatePicker) {
        DatePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateSelected = { selectedDate ->
                // Handle date selection
                val currentTime = System.currentTimeMillis()
                val startDate = if (isSelectingStartDate) selectedDate else (customDateRange?.startDate ?: currentTime)
                val endDate = if (!isSelectingStartDate) selectedDate else (customDateRange?.endDate ?: currentTime)
                
                if (isSelectingStartDate) {
                    isSelectingStartDate = false
                } else {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val label = "${dateFormat.format(Date(startDate))} - ${dateFormat.format(Date(endDate))}"
                    
                    onCustomDateRangeSelected(
                        DateRange(
                            startDate = startDate,
                            endDate = endDate,
                            label = label
                        )
                    )
                    showDatePicker = false
                    isSelectingStartDate = true
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    
    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { dateMillis ->
                        onDateSelected(dateMillis)
                    }
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = "Seleccionar fecha",
                    modifier = Modifier.padding(16.dp)
                )
            }
        )
    }
}

// Helper function to get date ranges
fun getDateRangeForPeriod(period: DateFilterPeriod): DateRange {
    val calendar = Calendar.getInstance()
    val today = calendar.timeInMillis
    
    return when (period) {
        DateFilterPeriod.TODAY -> {
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endOfDay = calendar.timeInMillis
            
            DateRange(startOfDay, endOfDay, "Hoy")
        }
        
        DateFilterPeriod.YESTERDAY -> {
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfYesterday = calendar.timeInMillis
            
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endOfYesterday = calendar.timeInMillis
            
            DateRange(startOfYesterday, endOfYesterday, "Ayer")
        }
        
        DateFilterPeriod.THIS_WEEK -> {
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfWeek = calendar.timeInMillis
            
            calendar.add(Calendar.DAY_OF_WEEK, 6)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endOfWeek = calendar.timeInMillis
            
            DateRange(startOfWeek, endOfWeek, "Esta semana")
        }
        
        DateFilterPeriod.THIS_MONTH -> {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfMonth = calendar.timeInMillis
            
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endOfMonth = calendar.timeInMillis
            
            DateRange(startOfMonth, endOfMonth, "Este mes")
        }
        
        DateFilterPeriod.LAST_MONTH -> {
            calendar.add(Calendar.MONTH, -1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfLastMonth = calendar.timeInMillis
            
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endOfLastMonth = calendar.timeInMillis
            
            DateRange(startOfLastMonth, endOfLastMonth, "Mes pasado")
        }
        
        DateFilterPeriod.THIS_YEAR -> {
            calendar.set(Calendar.DAY_OF_YEAR, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfYear = calendar.timeInMillis
            
            calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endOfYear = calendar.timeInMillis
            
            DateRange(startOfYear, endOfYear, "Este año")
        }
        
        DateFilterPeriod.CUSTOM -> {
            // Default range for custom (current month)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfMonth = calendar.timeInMillis
            
            DateRange(startOfMonth, today, "Personalizado")
        }
    }
}
