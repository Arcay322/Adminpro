package com.example.admin_ingresos.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.data.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentFilter: TransactionFilter,
    categories: List<Category>,
    paymentMethods: List<PaymentMethod>,
    onFilterChanged: (TransactionFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var tempFilter by remember { mutableStateOf(currentFilter) }
    var selectedDatePreset by remember { mutableStateOf(DateRangePreset.CUSTOM) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isSelectingStartDate by remember { mutableStateOf(true) }
    
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtros",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Row {
                    TextButton(
                        onClick = {
                            tempFilter = TransactionFilter()
                            selectedDatePreset = DateRangePreset.CUSTOM
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar filtros",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Limpiar")
                    }
                    
                    Button(
                        onClick = {
                            onFilterChanged(tempFilter)
                            onDismiss()
                        }
                    ) {
                        Text("Aplicar")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date Range Section
                item {
                    FilterSection(title = "Rango de fechas") {
                        Column {
                            // Date presets
                            DateRangePreset.values().forEach { preset ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = selectedDatePreset == preset,
                                            onClick = {
                                                selectedDatePreset = preset
                                                tempFilter = when (preset) {
                                                    DateRangePreset.TODAY -> tempFilter.copy(dateRange = DateRange.today())
                                                    DateRangePreset.THIS_WEEK -> tempFilter.copy(dateRange = DateRange.thisWeek())
                                                    DateRangePreset.THIS_MONTH -> tempFilter.copy(dateRange = DateRange.thisMonth())
                                                    DateRangePreset.LAST_MONTH -> tempFilter.copy(dateRange = DateRange.lastMonth())
                                                    DateRangePreset.LAST_30_DAYS -> tempFilter.copy(dateRange = DateRange.last30Days())
                                                    DateRangePreset.LAST_90_DAYS -> tempFilter.copy(dateRange = DateRange.last90Days())
                                                    DateRangePreset.CUSTOM -> tempFilter.copy(dateRange = null)
                                                }
                                            },
                                            role = Role.RadioButton
                                        )
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedDatePreset == preset,
                                        onClick = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = preset.displayName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            
                            // Custom date range
                            if (selectedDatePreset == DateRangePreset.CUSTOM) {
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            isSelectingStartDate = true
                                            showDatePicker = true
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = tempFilter.dateRange?.let { 
                                                dateFormat.format(Date(it.startDate)) 
                                            } ?: "Desde"
                                        )
                                    }
                                    
                                    OutlinedButton(
                                        onClick = {
                                            isSelectingStartDate = false
                                            showDatePicker = true
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = tempFilter.dateRange?.let { 
                                                dateFormat.format(Date(it.endDate)) 
                                            } ?: "Hasta"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Transaction Types Section
                item {
                    FilterSection(title = "Tipo de transacción") {
                        val transactionTypes = listOf("Ingreso", "Gasto")
                        
                        transactionTypes.forEach { type ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = tempFilter.transactionTypes.contains(type),
                                    onCheckedChange = { checked ->
                                        tempFilter = if (checked) {
                                            tempFilter.copy(transactionTypes = tempFilter.transactionTypes + type)
                                        } else {
                                            tempFilter.copy(transactionTypes = tempFilter.transactionTypes - type)
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = type,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
                
                // Categories Section
                if (categories.isNotEmpty()) {
                    item {
                        FilterSection(title = "Categorías") {
                            categories.forEach { category ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = tempFilter.categories.contains(category.id),
                                        onCheckedChange = { checked ->
                                            tempFilter = if (checked) {
                                                tempFilter.copy(categories = tempFilter.categories + category.id)
                                            } else {
                                                tempFilter.copy(categories = tempFilter.categories - category.id)
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = category.icon,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Payment Methods Section
                if (paymentMethods.isNotEmpty()) {
                    item {
                        FilterSection(title = "Métodos de pago") {
                            paymentMethods.forEach { paymentMethod ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = tempFilter.paymentMethods.contains(paymentMethod.id),
                                        onCheckedChange = { checked ->
                                            tempFilter = if (checked) {
                                                tempFilter.copy(paymentMethods = tempFilter.paymentMethods + paymentMethod.id)
                                            } else {
                                                tempFilter.copy(paymentMethods = tempFilter.paymentMethods - paymentMethod.id)
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = paymentMethod.icon,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = paymentMethod.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Amount Range Section
                item {
                    FilterSection(title = "Rango de monto") {
                        var minAmount by remember { mutableStateOf(tempFilter.amountRange?.minAmount?.toString() ?: "") }
                        var maxAmount by remember { mutableStateOf(tempFilter.amountRange?.maxAmount?.toString() ?: "") }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = minAmount,
                                onValueChange = { 
                                    minAmount = it
                                    val min = it.toDoubleOrNull()
                                    val max = maxAmount.toDoubleOrNull()
                                    if (min != null && max != null) {
                                        tempFilter = tempFilter.copy(amountRange = AmountRange(min, max))
                                    } else if (min == null && max == null) {
                                        tempFilter = tempFilter.copy(amountRange = null)
                                    }
                                },
                                label = { Text("Monto mínimo") },
                                placeholder = { Text("0.00") },
                                leadingIcon = { Text("$") },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                                ),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            
                            OutlinedTextField(
                                value = maxAmount,
                                onValueChange = { 
                                    maxAmount = it
                                    val min = minAmount.toDoubleOrNull()
                                    val max = it.toDoubleOrNull()
                                    if (min != null && max != null) {
                                        tempFilter = tempFilter.copy(amountRange = AmountRange(min, max))
                                    } else if (min == null && max == null) {
                                        tempFilter = tempFilter.copy(amountRange = null)
                                    }
                                },
                                label = { Text("Monto máximo") },
                                placeholder = { Text("0.00") },
                                leadingIcon = { Text("$") },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                                ),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                
                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        // TODO: Implement date picker dialog
        // For now, we'll use a simple approach
        LaunchedEffect(Unit) {
            showDatePicker = false
        }
    }
}

@Composable
fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            content()
        }
    }
}