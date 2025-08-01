package com.example.admin_ingresos.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

data class CurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String,
    val flag: String,
    val country: String
)

data class DateFormatOption(
    val id: String,
    val name: String,
    val pattern: String,
    val example: String
)

data class NumberFormatOption(
    val id: String,
    val name: String,
    val decimalSeparator: String,
    val thousandsSeparator: String,
    val example: String
)

@Composable
fun CurrencyLocalizationScreen(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferencesManager = remember { com.example.admin_ingresos.data.PreferencesManager(context) }
    
    var selectedCurrency by remember { mutableStateOf(preferencesManager.currency) }
    var selectedDateFormat by remember { mutableStateOf("dd/MM/yyyy") }
    var selectedNumberFormat by remember { mutableStateOf("es") }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    
    val currencies = remember {
        listOf(
            CurrencyInfo("USD", "Dólar Estadounidense", "$", "🇺🇸", "Estados Unidos"),
            CurrencyInfo("EUR", "Euro", "€", "🇪🇺", "Unión Europea"),
            CurrencyInfo("MXN", "Peso Mexicano", "$", "🇲🇽", "México"),
            CurrencyInfo("COP", "Peso Colombiano", "$", "🇨🇴", "Colombia"),
            CurrencyInfo("ARS", "Peso Argentino", "$", "🇦🇷", "Argentina"),
            CurrencyInfo("CLP", "Peso Chileno", "$", "🇨🇱", "Chile"),
            CurrencyInfo("PEN", "Sol Peruano", "S/", "🇵🇪", "Perú"),
            CurrencyInfo("BRL", "Real Brasileño", "R$", "🇧🇷", "Brasil"),
            CurrencyInfo("GBP", "Libra Esterlina", "£", "🇬🇧", "Reino Unido"),
            CurrencyInfo("JPY", "Yen Japonés", "¥", "🇯🇵", "Japón"),
            CurrencyInfo("CAD", "Dólar Canadiense", "C$", "🇨🇦", "Canadá"),
            CurrencyInfo("AUD", "Dólar Australiano", "A$", "🇦🇺", "Australia")
        )
    }
    
    val dateFormats = remember {
        listOf(
            DateFormatOption("dd/MM/yyyy", "Día/Mes/Año", "dd/MM/yyyy", "25/12/2024"),
            DateFormatOption("MM/dd/yyyy", "Mes/Día/Año", "MM/dd/yyyy", "12/25/2024"),
            DateFormatOption("yyyy-MM-dd", "Año-Mes-Día", "yyyy-MM-dd", "2024-12-25"),
            DateFormatOption("dd-MM-yyyy", "Día-Mes-Año", "dd-MM-yyyy", "25-12-2024"),
            DateFormatOption("dd.MM.yyyy", "Día.Mes.Año", "dd.MM.yyyy", "25.12.2024")
        )
    }
    
    val numberFormats = remember {
        listOf(
            NumberFormatOption("es", "Español (1.234,56)", ",", ".", "1.234,56"),
            NumberFormatOption("en", "Inglés (1,234.56)", ".", ",", "1,234.56"),
            NumberFormatOption("fr", "Francés (1 234,56)", ",", " ", "1 234,56"),
            NumberFormatOption("de", "Alemán (1.234,56)", ",", ".", "1.234,56")
        )
    }
    
    val selectedCurrencyInfo = currencies.find { it.code == selectedCurrency }
    
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
                            text = "Moneda y Localización",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Configurar formatos regionales",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Currency section
        item {
            Text(
                text = "Moneda",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCurrencyDialog = true },
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
                    // Currency icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedCurrencyInfo?.flag ?: "💰",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Currency info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedCurrencyInfo?.name ?: "Seleccionar moneda",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${selectedCurrencyInfo?.code ?: ""} • ${selectedCurrencyInfo?.symbol ?: ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Cambiar moneda",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Preview section
        item {
            CurrencyPreviewCard(
                currency = selectedCurrencyInfo,
                dateFormat = selectedDateFormat,
                numberFormat = selectedNumberFormat
            )
        }
        
        // Date format section
        item {
            Text(
                text = "Formato de Fecha",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )
        }
        
        items(dateFormats) { format ->
            DateFormatCard(
                format = format,
                isSelected = selectedDateFormat == format.id,
                onSelect = { selectedDateFormat = format.id }
            )
        }
        
        // Number format section
        item {
            Text(
                text = "Formato de Números",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )
        }
        
        items(numberFormats) { format ->
            NumberFormatCard(
                format = format,
                isSelected = selectedNumberFormat == format.id,
                onSelect = { selectedNumberFormat = format.id }
            )
        }
        
        // Regional settings
        item {
            Text(
                text = "Configuración Regional",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )
        }
        
        item {
            RegionalSettingsCard()
        }
        
        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
    
    // Currency selection dialog
    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            currencies = currencies,
            selectedCurrency = selectedCurrency,
            onCurrencySelected = { currency ->
                selectedCurrency = currency.code
                preferencesManager.currency = currency.code
                preferencesManager.currencySymbol = currency.symbol
                showCurrencyDialog = false
            },
            onDismiss = { showCurrencyDialog = false }
        )
    }
}

@Composable
fun CurrencyPreviewCard(
    currency: CurrencyInfo?,
    dateFormat: String,
    numberFormat: String,
    modifier: Modifier = Modifier
) {
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
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Vista Previa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Sample transaction
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Supermercado",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatSampleDate(dateFormat),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = formatSampleAmount(1234.56, currency?.symbol ?: "$", numberFormat),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Sample balance
            Text(
                text = "Balance: ${formatSampleAmount(5678.90, currency?.symbol ?: "$", numberFormat)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun DateFormatCard(
    format: DateFormatOption,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelect
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = format.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = format.example,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun NumberFormatCard(
    format: NumberFormatOption,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelect
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = format.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Ejemplo: ${format.example}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RegionalSettingsCard(modifier: Modifier = Modifier) {
    var use24HourFormat by remember { mutableStateOf(true) }
    var firstDayOfWeek by remember { mutableStateOf("monday") }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 24-hour format toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Formato de 24 horas",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (use24HourFormat) "14:30" else "2:30 PM",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = use24HourFormat,
                    onCheckedChange = { use24HourFormat = it }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // First day of week
            Column {
                Text(
                    text = "Primer día de la semana",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = { firstDayOfWeek = "sunday" },
                        label = { Text("Domingo") },
                        selected = firstDayOfWeek == "sunday"
                    )
                    FilterChip(
                        onClick = { firstDayOfWeek = "monday" },
                        label = { Text("Lunes") },
                        selected = firstDayOfWeek == "monday"
                    )
                }
            }
        }
    }
}

@Composable
fun CurrencySelectionDialog(
    currencies: List<CurrencyInfo>,
    selectedCurrency: String,
    onCurrencySelected: (CurrencyInfo) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredCurrencies = remember(searchQuery, currencies) {
        if (searchQuery.isBlank()) {
            currencies
        } else {
            currencies.filter { currency ->
                currency.name.contains(searchQuery, ignoreCase = true) ||
                currency.code.contains(searchQuery, ignoreCase = true) ||
                currency.country.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Seleccionar Moneda",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar moneda...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Currency list
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredCurrencies) { currency ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCurrencySelected(currency) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currency.flag,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.size(32.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currency.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${currency.code} • ${currency.symbol}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            if (currency.code == selectedCurrency) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seleccionado",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// Helper functions
private fun formatSampleDate(format: String): String {
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    return sdf.format(Date())
}

private fun formatSampleAmount(amount: Double, symbol: String, format: String): String {
    return when (format) {
        "es" -> "$symbol${String.format(Locale("es"), "%.2f", amount)}"
        "en" -> "$symbol${String.format(Locale.US, "%.2f", amount)}"
        "fr" -> "$symbol${String.format(Locale.FRANCE, "%.2f", amount)}"
        "de" -> "$symbol${String.format(Locale.GERMANY, "%.2f", amount)}"
        else -> "$symbol${String.format("%.2f", amount)}"
    }
}