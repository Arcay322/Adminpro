package com.example.admin_ingresos.ui.transaction

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.admin_ingresos.ui.components.*
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreenNew(onSave: () -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val db = remember { com.example.admin_ingresos.AppDatabaseProvider.getDatabase(context) }
    val viewModel: AddTransactionViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AddTransactionViewModel(db) as T
        }
    })
    
    // Form state with improved validation
    var amount by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var type by remember { mutableStateOf("Gasto") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    var selectedPaymentMethodId by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    
    val categories by produceState(initialValue = emptyList<com.example.admin_ingresos.data.Category>(), db) {
        value = db.categoryDao().getAll()
    }
    val paymentMethods by produceState(initialValue = emptyList<com.example.admin_ingresos.data.PaymentMethod>(), db) {
        value = db.paymentMethodDao().getAll()
    }
    
    // Enhanced validation functions
    fun validateAmount(value: String): String? {
        return when {
            value.isBlank() -> "El monto es requerido"
            value.toDoubleOrNull() == null -> "Ingresa un monto válido"
            value.toDoubleOrNull()!! <= 0 -> "El monto debe ser mayor a 0"
            value.toDoubleOrNull()!! > 999999999 -> "El monto es demasiado grande"
            else -> null
        }
    }
    
    fun validateDescription(value: String): String? {
        return when {
            value.isBlank() -> "La descripción es requerida"
            value.length < 3 -> "La descripción debe tener al menos 3 caracteres"
            value.length > 100 -> "La descripción es demasiado larga"
            else -> null
        }
    }
    
    fun validateCategory(categoryId: Int?): String? {
        return if (categoryId == null) "Selecciona una categoría" else null
    }
    
    // Real-time validation
    LaunchedEffect(amount) {
        amountError = validateAmount(amount)
    }
    
    LaunchedEffect(description) {
        descriptionError = validateDescription(description)
    }
    
    LaunchedEffect(selectedCategoryId) {
        categoryError = validateCategory(selectedCategoryId)
    }
    
    // Check if form is valid
    val isFormValid = amountError == null && descriptionError == null && 
                     categoryError == null && amount.isNotBlank() && 
                     description.isNotBlank() && selectedCategoryId != null

    // Handle save
    fun handleSave() {
        if (!isFormValid) return
        
        isLoading = true
        val amountValue = amount.toDoubleOrNull() ?: return
        
        try {
            viewModel.saveTransaction(
                amount = amountValue,
                type = type,
                categoryId = selectedCategoryId!!,
                description = description.trim(),
                date = System.currentTimeMillis(),
                paymentMethodId = selectedPaymentMethodId
            )
            
            showSuccessMessage = true
        } catch (e: Exception) {
            // Handle error
        } finally {
            isLoading = false
        }
    }
    
    // Auto close after showing success
    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            kotlinx.coroutines.delay(1500)
            onSave()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Enhanced Header
        CashFlowHeader(
            title = "Nueva Transacción",
            subtitle = "Registra tus ingresos y gastos",
            actions = {
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Success message
            AnimatedVisibility(
                visible = showSuccessMessage,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "¡Transacción guardada exitosamente!",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                }
            }
            
            // Type selector with improved design
            TypeSelectorCard(
                selectedType = type,
                onTypeSelected = { type = it }
            )
            
            // Amount input with currency formatting
            AmountInputCard(
                amount = amount,
                onAmountChange = { amount = it },
                error = amountError,
                type = type
            )
            
            // Description input
            DescriptionInputCard(
                description = description,
                onDescriptionChange = { description = it },
                error = descriptionError
            )
            
            // Category selector
            if (categories.isNotEmpty()) {
                EnhancedCategorySelector(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelected = { selectedCategoryId = it },
                    error = categoryError
                )
            }
            
            // Payment method selector (optional)
            if (paymentMethods.isNotEmpty()) {
                EnhancedPaymentMethodSelector(
                    paymentMethods = paymentMethods,
                    selectedPaymentMethodId = selectedPaymentMethodId,
                    onPaymentMethodSelected = { selectedPaymentMethodId = it }
                )
            }
            
            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Cancelar")
                }
                
                Button(
                    onClick = { handleSave() },
                    modifier = Modifier.weight(1f),
                    enabled = isFormValid && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Guardar Transacción")
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeSelectorCard(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    CashFlowCard {
        Column {
            Text(
                text = "Tipo de Transacción",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TypeChip(
                    type = "Ingreso",
                    isSelected = selectedType == "Ingreso",
                    onClick = { onTypeSelected("Ingreso") },
                    color = Color(0xFF4CAF50),
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
                
                TypeChip(
                    type = "Gasto",
                    isSelected = selectedType == "Gasto",
                    onClick = { onTypeSelected("Gasto") },
                    color = Color(0xFFE57373),
                    icon = Icons.Default.TrendingDown,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TypeChip(
    type: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onClick() },
        modifier = modifier.height(60.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = type,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = type,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AmountInputCard(
    amount: String,
    onAmountChange: (String) -> Unit,
    error: String?,
    type: String
) {
    CashFlowCard {
        Column {
            Text(
                text = "Monto",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            OutlinedTextField(
                value = amount,
                onValueChange = { newValue ->
                    // Only allow numbers and decimal point
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        onAmountChange(newValue)
                    }
                },
                label = { Text("0.00") },
                prefix = {
                    Text(
                        text = "$",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (type == "Ingreso") Color(0xFF4CAF50) else Color(0xFFE57373)
                    )
                },
                isError = error != null,
                supportingText = error?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (type == "Ingreso") Color(0xFF4CAF50) else Color(0xFFE57373),
                    focusedLabelColor = if (type == "Ingreso") Color(0xFF4CAF50) else Color(0xFFE57373)
                )
            )
            
            // Quick amount buttons
            if (amount.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("10000", "50000", "100000", "500000").forEach { quickAmount ->
                        FilterChip(
                            onClick = { onAmountChange(quickAmount) },
                            label = { 
                                Text(
                                    text = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
                                        .format(quickAmount.toDouble()),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            selected = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DescriptionInputCard(
    description: String,
    onDescriptionChange: (String) -> Unit,
    error: String?
) {
    CashFlowCard {
        Column {
            Text(
                text = "Descripción",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Ej: Compra de supermercado") },
                isError = error != null,
                supportingText = error?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }
    }
}
