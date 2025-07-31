package com.example.admin_ingresos.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.admin_ingresos.R

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
    
    // Form state with validation
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
    
    val categories by produceState(initialValue = emptyList<com.example.admin_ingresos.data.Category>(), db) {
        value = db.categoryDao().getAll()
    }
    val paymentMethods by produceState(initialValue = emptyList<com.example.admin_ingresos.data.PaymentMethod>(), db) {
        value = db.paymentMethodDao().getAll()
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
        return if (categories.isEmpty()) null 
               else if (categoryId == null) "Selecciona una categoría" 
               else null
    }
    
    // Check if form is valid
    val isFormValid = amountError == null && descriptionError == null && 
                     categoryError == null && amount.isNotBlank() && 
                     description.isNotBlank() && 
                     (categories.isEmpty() || selectedCategoryId != null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Nueva Transacción",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Completa los campos para agregar una transacción",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
        
        // Transaction type selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Tipo de transacción",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        onClick = { type = "Gasto" },
                        label = { Text("Gasto") },
                        selected = type == "Gasto",
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.error,
                            selectedLabelColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        onClick = { type = "Ingreso" },
                        label = { Text("Ingreso") },
                        selected = type == "Ingreso",
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // Amount field
        OutlinedTextField(
            value = amount,
            onValueChange = { newValue ->
                amount = newValue
                amountError = validateAmount(newValue)
            },
            label = { Text("Monto") },
            placeholder = { Text("0.00") },
            leadingIcon = { Text("$", style = MaterialTheme.typography.titleMedium) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = amountError != null,
            supportingText = amountError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Description field with autocomplete
        AutoCompleteTextField(
            value = description,
            onValueChange = { newValue ->
                description = newValue
                descriptionError = validateDescription(newValue)
            },
            suggestions = descriptionSuggestions,
            onSuggestionSelected = { selectedDescription ->
                description = selectedDescription
                descriptionError = validateDescription(selectedDescription)
                
                // Auto-suggest category based on description
                LaunchedEffect(selectedDescription) {
                    val suggestedCategoryId = transactionViewModel.suggestCategoryForDescription(selectedDescription)
                    if (suggestedCategoryId != null && selectedCategoryId == null) {
                        selectedCategoryId = suggestedCategoryId
                        categoryError = validateCategory(suggestedCategoryId)
                    }
                }
            },
            label = { Text("Descripción") },
            placeholder = { Text("Ej: Compra en supermercado") },
            isError = descriptionError != null,
            supportingText = descriptionError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Category selector - Always show, even if empty
        CategorySelector(
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            onCategorySelected = { categoryId ->
                selectedCategoryId = categoryId
                categoryError = validateCategory(categoryId)
            },
            onNewCategoryAdded = { categoryName ->
                // Add new category to database
                transactionViewModel.addCategory(categoryName) { newCategoryId ->
                    selectedCategoryId = newCategoryId
                    categoryError = validateCategory(newCategoryId)
                }
            },
            error = categoryError
        )
        
        // Payment method selector - Always show, even if empty
        PaymentMethodSelector(
            paymentMethods = paymentMethods,
            selectedPaymentMethodId = selectedPaymentMethodId,
            onPaymentMethodSelected = { selectedPaymentMethodId = it }
        )
        
        // Receipt photo capture
        ReceiptCameraCapture(
            onPhotoTaken = { uri -> receiptPhotoUri = uri },
            currentPhotoUri = receiptPhotoUri
        )
        
        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = {
                    if (isFormValid) {
                        val amountDouble = amount.toDoubleOrNull() ?: 0.0
                        val categoryId = selectedCategoryId ?: 0
                        
                        // Check for duplicates before saving
                        LaunchedEffect(Unit) {
                            val duplicates = transactionViewModel.checkForDuplicates(
                                amount = amountDouble,
                                description = description,
                                categoryId = categoryId
                            )
                            
                            if (duplicates.isNotEmpty()) {
                                duplicateTransactions = duplicates
                                showDuplicateDialog = true
                            } else {
                                // No duplicates, save directly
                                transactionViewModel.saveTransaction(
                                    amount = amountDouble,
                                    type = type,
                                    categoryId = categoryId,
                                    description = description,
                                    date = System.currentTimeMillis(),
                                    paymentMethodId = selectedPaymentMethodId
                                )
                                onSave()
                            }
                        }
                    }
                },
                enabled = isFormValid,
                modifier = Modifier.weight(1f)
            ) {
                Text("Guardar")
            }
        }
        
        // Bottom spacing for better scrolling
        Spacer(modifier = Modifier.height(80.dp))
    }
    
    // Duplicate Transaction Dialog
    if (showDuplicateDialog) {
        AlertDialog(
            onDismissRequest = { showDuplicateDialog = false },
            title = { Text("Posible Transacción Duplicada") },
            text = { 
                Column {
                    Text("Se encontraron transacciones similares recientes:")
                    Spacer(modifier = Modifier.height(8.dp))
                    duplicateTransactions.take(3).forEach { transaction ->
                        Text(
                            text = "• ${transaction.description} - $${String.format("%.2f", transaction.amount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("¿Estás seguro de que quieres agregar esta transacción?")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Save anyway
                        val amountDouble = amount.toDoubleOrNull() ?: 0.0
                        val categoryId = selectedCategoryId ?: 0
                        transactionViewModel.saveTransaction(
                            amount = amountDouble,
                            type = type,
                            categoryId = categoryId,
                            description = description,
                            date = System.currentTimeMillis(),
                            paymentMethodId = selectedPaymentMethodId
                        )
                        showDuplicateDialog = false
                        onSave()
                    }
                ) {
                    Text("Sí, agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDuplicateDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
