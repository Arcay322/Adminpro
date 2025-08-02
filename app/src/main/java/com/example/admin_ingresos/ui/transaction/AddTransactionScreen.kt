package com.example.admin_ingresos.ui.transaction

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.admin_ingresos.ui.components.*
import com.example.admin_ingresos.ui.theme.*
import com.example.admin_ingresos.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(onSave: () -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val db = remember { com.example.admin_ingresos.AppDatabaseProvider.getDatabase(context) }
    val transactionViewModel: AddTransactionViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AddTransactionViewModel(db) as T
        }
    })
    
    // Enhanced form state with validation
    var amount by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var type by remember { mutableStateOf("Gasto") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    var selectedPaymentMethodId by remember { mutableStateOf<Int?>(null) }
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var duplicateTransactions by remember { mutableStateOf<List<com.example.admin_ingresos.data.Transaction>>(emptyList()) }
    var receiptPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var savedTransactionType by remember { mutableStateOf("") }
    var savedAmount by remember { mutableStateOf("") }
    
    val categories by produceState(initialValue = emptyList<com.example.admin_ingresos.data.Category>(), db) {
        value = db.categoryDao().getAll()
    }
    val paymentMethods by produceState(initialValue = emptyList<com.example.admin_ingresos.data.PaymentMethod>(), db) {
        value = db.paymentMethodDao().getAll()
    }
    
    // Categories state that updates automatically
    var categoriesList by remember { mutableStateOf(categories) }
    
    // Update categories list when categories change
    LaunchedEffect(categories) {
        categoriesList = categories
    }
    
    // Autocomplete suggestions
    var descriptionSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // Load suggestions when description changes
    LaunchedEffect(description) {
        if (description.length >= 2) {
            descriptionSuggestions = transactionViewModel.getDescriptionSuggestions(description)
        } else {
            descriptionSuggestions = emptyList()
        }
    }
    
    // Validation functions
    fun validateAmount(value: String): String? {
        return when {
            value.isBlank() -> "El monto es requerido"
            value.toDoubleOrNull() == null -> "Ingresa un monto válido"
            value.toDoubleOrNull()!! <= 0 -> "El monto debe ser mayor a 0"
            else -> null
        }
    }
    
    fun validateDescription(value: String): String? {
        return when {
            value.isBlank() -> "La descripción es requerida"
            value.length < 3 -> "La descripción debe tener al menos 3 caracteres"
            else -> null
        }
    }
    
    fun validateCategory(categoryId: Int?): String? {
        // Make category optional if no categories exist
        return if (categoriesList.isEmpty()) null 
               else if (categoryId == null) "Selecciona una categoría" 
               else null
    }
    
    // Check if form is valid
    val isFormValid = amountError == null && descriptionError == null && 
                     categoryError == null && amount.isNotBlank() && 
                     description.isNotBlank() && 
                     (categoriesList.isEmpty() || selectedCategoryId != null)

    // Fondo glassmorphism con efectos
    GlassmorphismScreen {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header glassmorphism
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                cornerRadius = 24.dp,
                backgroundColor = GlassWhiteStrong
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextPrimary
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Nueva Transacción",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Registra tu ingreso o gasto",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                    
                    Box(modifier = Modifier.size(48.dp)) // Spacer para balance
                }
            }
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Type Selector con nuevo diseño glassmorphism
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically() + fadeIn()
                    ) {
                        ModernSegmentedButton(
                            selectedOption = type,
                            options = listOf("Ingreso", "Gasto"),
                            onOptionSelected = { type = it },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                
                // Amount Input glassmorphism
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn()
                    ) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = GlassWhite,
                            borderColor = if (amountError != null) ExpenseRed.copy(alpha = 0.6f) else GlassBorder
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "Monto",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary
                                )
                                
                                // Campo de entrada con estilo glassmorphism
                                OutlinedTextField(
                                    value = amount,
                                    onValueChange = { newValue ->
                                        amount = newValue
                                        amountError = validateAmount(newValue)
                                    },
                                    placeholder = {
                                        Text(
                                            "0.00",
                                            color = TextSecondary,
                                            fontSize = 24.sp
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    isError = amountError != null,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentVibrantStart,
                                        unfocusedBorderColor = GlassBorderSubtle,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        cursorColor = AccentVibrantStart,
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent
                                    ),
                                    textStyle = LocalTextStyle.current.copy(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                
                                amountError?.let { error ->
                                    Text(
                                        text = error,
                                        color = ExpenseRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Description glassmorphism
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(initialOffsetY = { it / 3 }) + fadeIn()
                    ) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = GlassWhite,
                            borderColor = if (descriptionError != null) ExpenseRed.copy(alpha = 0.6f) else GlassBorder
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "Descripción",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary
                                )
                                
                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { newValue ->
                                        description = newValue
                                        descriptionError = validateDescription(newValue)
                                    },
                                    placeholder = {
                                        Text(
                                            "¿Para qué fue este ${type.lowercase()}?",
                                            color = TextSecondary
                                        )
                                    },
                                    isError = descriptionError != null,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentVibrantStart,
                                        unfocusedBorderColor = GlassBorderSubtle,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        cursorColor = AccentVibrantStart,
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent
                                    )
                                )
                                
                                // Suggestions
                                if (descriptionSuggestions.isNotEmpty()) {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(descriptionSuggestions.take(5)) { suggestion ->
                                            AssistChip(
                                                onClick = {
                                                    description = suggestion
                                                    descriptionError = validateDescription(suggestion)
                                                },
                                                label = { Text(suggestion, fontSize = 12.sp) },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = GlassWhiteSubtle,
                                                    labelColor = TextSecondary
                                                ),
                                                border = BorderStroke(
                                                    width = 1.dp,
                                                    color = GlassBorderSubtle
                                                )
                                            )
                                        }
                                    }
                                }
                                
                                descriptionError?.let { error ->
                                    Text(
                                        text = error,
                                        color = ExpenseRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Enhanced Category Selector glassmorphism
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn()
                    ) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = GlassWhite,
                            borderColor = if (categoryError != null) ExpenseRed.copy(alpha = 0.6f) else GlassBorder
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "Categoría",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary
                                )
                                
                                if (categoriesList.isNotEmpty()) {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(categoriesList) { category ->
                                            val isSelected = selectedCategoryId == category.id
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    selectedCategoryId = if (isSelected) null else category.id
                                                    categoryError = validateCategory(selectedCategoryId)
                                                },
                                                label = { Text(category.name) }
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "No hay categorías disponibles",
                                        color = TextSecondary,
                                        fontSize = 14.sp
                                    )
                                }
                                
                                categoryError?.let { error ->
                                    Text(
                                        text = error,
                                        color = ExpenseRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Payment Method glassmorphism - colapsible
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn()
                    ) {
                        var isExpanded by remember { mutableStateOf(false) }
                        
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = GlassWhite,
                            onClick = { isExpanded = !isExpanded }
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Método de Pago",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextSecondary
                                    )
                                    
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = TextSecondary
                                    )
                                }
                                
                                selectedPaymentMethodId?.let { id ->
                                    val selectedMethod = paymentMethods.find { it.id == id }
                                    selectedMethod?.let {
                                        Text(
                                            text = it.name,
                                            fontSize = 14.sp,
                                            color = AccentVibrantStart,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                
                                AnimatedVisibility(visible = isExpanded) {
                                    if (paymentMethods.isNotEmpty()) {
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 12.dp)
                                        ) {
                                            items(paymentMethods) { method ->
                                                val isSelected = selectedPaymentMethodId == method.id
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = {
                                                        selectedPaymentMethodId = if (isSelected) null else method.id
                                                    },
                                                    label = { Text(method.name) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Receipt Photo glassmorphism (opcional)
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn()
                    ) {
                        var showReceiptSection by remember { mutableStateOf(false) }
                        
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = GlassWhiteSubtle,
                            onClick = { showReceiptSection = !showReceiptSection }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = TextSecondary
                                    )
                                    Text(
                                        text = "Agregar Comprobante",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextSecondary
                                    )
                                }
                                
                                Icon(
                                    imageVector = if (showReceiptSection) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                        }
                    }
                }
                
                // Action buttons glassmorphism
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel button
                        GlassCard(
                            modifier = Modifier.weight(1f),
                            backgroundColor = GlassWhiteSubtle,
                            borderColor = OutlineVariant,
                            onClick = onCancel
                        ) {
                            Text(
                                text = "Cancelar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        // Save button
                        GlassCard(
                            modifier = Modifier.weight(1f),
                            backgroundColor = if (isFormValid) Color.Transparent else GlassWhite,
                            onClick = if (isFormValid) {
                                {
                                    if (isFormValid) {
                                        isLoading = true
                                        savedTransactionType = type
                                        savedAmount = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(amount.toDouble())
                                        
                                        // Save the transaction
                                        transactionViewModel.saveTransaction(
                                            amount = amount.toDouble(),
                                            type = type,
                                            categoryId = selectedCategoryId ?: 0,
                                            description = description.trim(),
                                            date = System.currentTimeMillis(),
                                            paymentMethodId = selectedPaymentMethodId
                                        )
                                        isLoading = false
                                        showSuccessDialog = true
                                    }
                                }
                            } else null
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isFormValid) {
                                            Brush.linearGradient(
                                                colors = listOf(AccentVibrantStart, AccentVibrantEnd)
                                            )
                                        } else {
                                            Brush.linearGradient(
                                                colors = listOf(Color.Transparent, Color.Transparent)
                                            )
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clip(RoundedCornerShape(16.dp))
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = TextOnAccent,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Guardar",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFormValid) TextOnAccent else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Bottom spacer
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
    
    // Success Dialog glassmorphism
    if (showSuccessDialog) {
        GlassSuccessDialog(
            isVisible = showSuccessDialog,
            transactionType = savedTransactionType,
            amount = savedAmount,
            onDismiss = {
                showSuccessDialog = false
                // Reset form
                amount = ""
                description = ""
                selectedCategoryId = null
                selectedPaymentMethodId = null
                onSave()
            }
        )
    }
}

@Composable
private fun GlassSuccessDialog(
    isVisible: Boolean,
    transactionType: String,
    amount: String,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = IncomeGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "¡${transactionType} Guardado!",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }
            },
            text = { 
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Tu $transactionType por $amount ha sido registrado exitosamente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentVibrantStart
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Continuar", color = TextOnAccent, fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = SurfaceGlass,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }
}

@Composable
private fun DuplicateTransactionDialog(
    duplicateTransactions: List<com.example.admin_ingresos.data.Transaction>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Posible Duplicado",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }
        },
        text = { 
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Se encontraron transacciones similares recientes:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                
                duplicateTransactions.take(3).forEach { transaction ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = GlassWhiteSubtle,
                        borderColor = GlassBorderSubtle
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = transaction.description,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "${NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(transaction.amount)} • ${transaction.type}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
                
                Text(
                    "¿Estás seguro de que quieres agregar esta transacción?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentVibrantStart
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Sí, agregar", color = TextOnAccent, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = TextSecondary
                )
            ) {
                Text("Cancelar", fontWeight = FontWeight.Medium)
            }
        },
        containerColor = SurfaceGlass,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}
