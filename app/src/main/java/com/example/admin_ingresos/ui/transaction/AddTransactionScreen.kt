package com.example.admin_ingresos.ui.transaction

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.admin_ingresos.ui.components.*
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Enhanced Header with back button
        CashFlowHeader(
            title = "Nueva Transacción",
            subtitle = "Registra tu ingreso o gasto"
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Type Selector with visual improvements
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically() + fadeIn()
                ) {
                    TypeSelectorCard(
                        selectedType = type,
                        onTypeSelected = { type = it }
                    )
                }
            }
            
            // Amount Input with better design
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn()
                ) {
                    AmountInputCard(
                        amount = amount,
                        onAmountChange = { newValue ->
                            amount = newValue
                            amountError = validateAmount(newValue)
                        },
                        error = amountError,
                        transactionType = type
                    )
                }
            }
            
            // Description with smart suggestions
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it / 3 }) + fadeIn()
                ) {
                    DescriptionInputCard(
                        description = description,
                        onDescriptionChange = { newValue ->
                            description = newValue
                            descriptionError = validateDescription(newValue)
                        },
                        suggestions = descriptionSuggestions,
                        onSuggestionSelected = { selectedDescription ->
                            description = selectedDescription
                            descriptionError = validateDescription(selectedDescription)
                        },
                        error = descriptionError
                    )
                }
            }
            
            // Enhanced Category Selector
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn()
                ) {
                    EnhancedCategorySelector(
                        categories = categoriesList,
                        selectedCategoryId = selectedCategoryId,
                        onCategorySelected = { categoryId ->
                            selectedCategoryId = categoryId
                            categoryError = validateCategory(categoryId)
                        },
                        onNewCategoryAdded = { categoryName ->
                            transactionViewModel.addCategory(categoryName) { newCategoryId ->
                                selectedCategoryId = newCategoryId
                                categoryError = validateCategory(newCategoryId)
                                // Update local categories list immediately
                                categoriesList = categoriesList + com.example.admin_ingresos.data.Category(
                                    id = newCategoryId,
                                    name = categoryName
                                )
                            }
                        },
                        error = categoryError,
                        transactionType = type
                    )
                }
            }
            
            // Payment Method with modern design (collapsible)
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn()
                ) {
                    PaymentMethodCard(
                        paymentMethods = paymentMethods,
                        selectedPaymentMethodId = selectedPaymentMethodId,
                        onPaymentMethodSelected = { selectedPaymentMethodId = it },
                        onNewPaymentMethodAdded = { methodName ->
                            // Agregar nuevo método de pago a la base de datos
                            val newPaymentMethod = com.example.admin_ingresos.data.PaymentMethod(
                                name = methodName
                            )
                            // Usar una corrutina para insertar en la base de datos
                            CoroutineScope(Dispatchers.IO).launch {
                                val newId = db.paymentMethodDao().insert(newPaymentMethod)
                                withContext(Dispatchers.Main) {
                                    selectedPaymentMethodId = newId.toInt()
                                }
                            }
                        },
                        showExpanded = false
                    )
                }
            }
            
            // Receipt Photo Component
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn()
                ) {
                    ReceiptPhotoCard(
                        photoUri = receiptPhotoUri,
                        onPhotoTaken = { uri -> receiptPhotoUri = uri }
                    )
                }
            }
            
            // Action buttons with enhanced design
            item {
                Spacer(modifier = Modifier.height(8.dp))
                
                ActionButtons(
                    isFormValid = isFormValid,
                    isLoading = isLoading,
                    onSave = {
                        if (isFormValid) {
                            isLoading = true
                            savedTransactionType = type
                            savedAmount = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(amount.toDouble())
                            
                            // Save the transaction directly for now, we'll improve duplicate detection later
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
                    },
                    onCancel = onCancel
                )
            }
            
            // Bottom spacer
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    // Duplicate Transaction Dialog
    if (showDuplicateDialog) {
        DuplicateTransactionDialog(
            duplicateTransactions = duplicateTransactions,
            onDismiss = { 
                showDuplicateDialog = false
                isLoading = false
            },
            onConfirm = {
                transactionViewModel.saveTransaction(
                    amount = amount.toDouble(),
                    type = type,
                    categoryId = selectedCategoryId ?: 0,
                    description = description.trim(),
                    date = System.currentTimeMillis(),
                    paymentMethodId = selectedPaymentMethodId
                )
                showDuplicateDialog = false
                isLoading = false
                onSave()
            }
        )
    }
    
    // Success Dialog
    if (showSuccessDialog) {
        TransactionSuccessDialog(
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
private fun DuplicateTransactionDialog(
    duplicateTransactions: List<com.example.admin_ingresos.data.Transaction>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "⚠️ Posible Duplicado",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            ) 
        },
        text = { 
            Column {
                Text(
                    "Se encontraron transacciones similares recientes:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                duplicateTransactions.take(3).forEach { transaction ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = transaction.description,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(transaction.amount)} • ${transaction.type}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "¿Estás seguro de que quieres agregar esta transacción?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Sí, agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
