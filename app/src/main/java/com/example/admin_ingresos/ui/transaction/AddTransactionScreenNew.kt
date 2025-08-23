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
import com.example.admin_ingresos.ui.theme.GlassmorphicCard
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.admin_ingresos.ui.components.*
import com.example.admin_ingresos.ui.components.resolvedMenuContainerColor
import java.text.NumberFormat
import java.util.*
import kotlinx.coroutines.flow.first

// Import Coil para cargar imágenes en Compose
import coil.compose.rememberAsyncImagePainter
import com.example.admin_ingresos.ui.theme.AccentVibrantStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreenNew(onSave: () -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val db = remember { com.example.admin_ingresos.AppDatabaseProvider.getDatabase(context) }
    // Estado del formulario
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
    var receiptPhotoUri by remember { mutableStateOf<String?>(null) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // Función para crear archivo en el directorio privado persistente de la app (/files/receipts)
    fun createImageFile(): Uri? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val storageDir = File(context.filesDir, "receipts").apply { if (!exists()) mkdirs() }
            val file = File.createTempFile("recibo_${'$'}timeStamp", ".jpg", storageDir)
            androidx.core.content.FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                file
            )
        } catch (e: Exception) { null }
    }

    // Copia el contenido de una Uri (p. ej. de la galería) a un fichero en files/receipts y devuelve la Uri provista por FileProvider
    fun saveUriToAppStorage(context: android.content.Context, srcUri: Uri): Uri? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val storageDir = File(context.filesDir, "receipts").apply { if (!exists()) mkdirs() }
            val destFile = File(storageDir, "recibo_${'$'}timeStamp.jpg")
            context.contentResolver.openInputStream(srcUri).use { input ->
                if (input != null) {
                    java.io.FileOutputStream(destFile).use { out ->
                        input.copyTo(out)
                    }
                }
            }
            androidx.core.content.FileProvider.getUriForFile(context, context.packageName + ".fileprovider", destFile)
        } catch (e: Exception) { null }
    }

    // Launcher para galería
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            // Copiar la imagen seleccionada a almacenamiento privado de la app para que la URI sea persistente
            val saved = try {
                saveUriToAppStorage(context, uri)
            } catch (e: Exception) { null }
            receiptPhotoUri = saved?.toString() ?: uri.toString()
        }
    }
    // Launcher para cámara
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success && cameraImageUri != null) receiptPhotoUri = cameraImageUri.toString()
    }

    var showCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryError by remember { mutableStateOf<String?>(null) }
    var showPhotoMenu by remember { mutableStateOf(false) }

    var categoryUpdateTrigger by remember { mutableStateOf(0) }
    val categories by produceState(initialValue = emptyList<com.example.admin_ingresos.data.Category>(), db, categoryUpdateTrigger) {
        value = db.categoryDao().getAllCategories().first()
    }
    val paymentMethods by produceState(initialValue = emptyList<com.example.admin_ingresos.data.PaymentMethod>(), db) {
        value = db.paymentMethodDao().getAll()
    }

    // Validaciones
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

    LaunchedEffect(amount) { amountError = validateAmount(amount) }
    LaunchedEffect(description) { descriptionError = validateDescription(description) }
    LaunchedEffect(selectedCategoryId) { categoryError = validateCategory(selectedCategoryId) }

    val isFormValid = amountError == null && descriptionError == null &&
            categoryError == null && amount.isNotBlank() &&
            description.isNotBlank() && selectedCategoryId != null


    // Estados para disparar efectos secundarios
    var shouldSave by remember { mutableStateOf(false) }
    var pendingTransaction by remember { mutableStateOf<com.example.admin_ingresos.data.Transaction?>(null) }
    var shouldCreateCategory by remember { mutableStateOf(false) }
    var pendingCategoryName by remember { mutableStateOf("") }

    // Guardar transacción: solo cambia el estado
    fun handleSave() {
        if (!isFormValid) return
        isLoading = true

        val internalType = when (type) {
            "Ingreso" -> com.example.admin_ingresos.data.Transaction.TYPE_INCOME
            "Gasto" -> com.example.admin_ingresos.data.Transaction.TYPE_EXPENSE
            "Ahorro" -> com.example.admin_ingresos.data.Transaction.TYPE_TRANSFER
            else -> type
        }

        pendingTransaction = com.example.admin_ingresos.data.Transaction(
            amount = amount.toDouble(),
            type = internalType,
            categoryId = selectedCategoryId!!,
            description = description.trim(),
            date = System.currentTimeMillis(),
            paymentMethodId = selectedPaymentMethodId,
            receiptPhotoUri = receiptPhotoUri
        )
        shouldSave = true
    }

    // Efecto para guardar la transacción
    LaunchedEffect(shouldSave, pendingTransaction) {
        if (shouldSave && pendingTransaction != null) {
            db.transactionDao().insert(pendingTransaction!!)
            showSuccessMessage = true
            isLoading = false
            shouldSave = false
            pendingTransaction = null
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
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header minimalista (eliminado para evitar doble título)
        Spacer(modifier = Modifier.height(4.dp))

        Spacer(modifier = Modifier.height(10.dp))

        // Tipo de transacción (chips visuales)
        GlassmorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                Text(
                    text = "Tipo de Transacción",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { type = "Ingreso" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "Ingreso") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f).height(38.dp),
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ingreso", fontSize = MaterialTheme.typography.bodyMedium.fontSize)
                    }
                    Button(
                        onClick = { type = "Gasto" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "Gasto") Color(0xFFE57373) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f).height(38.dp),
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.TrendingDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gasto", fontSize = MaterialTheme.typography.bodyMedium.fontSize)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = { type = "Ahorro" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "Ahorro") Color(0xFF42A5F5) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f).height(38.dp),
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.Savings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ahorro", fontSize = MaterialTheme.typography.bodyMedium.fontSize)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Monto con icono
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier
                    .padding(horizontal = 14.dp, vertical = 14.dp)
                    .fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AttachMoney, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Monto", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            amount = newValue
                        }
                    },
                    label = { Text("0.00") },
                    prefix = { Text("$") },
                    isError = amountError != null,
                    supportingText = amountError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (type == "Ingreso") Color(0xFF4CAF50) else Color(0xFFE57373),
                        focusedLabelColor = if (type == "Ingreso") Color(0xFF4CAF50) else Color(0xFFE57373)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Descripción
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier
                    .padding(horizontal = 14.dp, vertical = 14.dp)
                    .fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Descripción", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Ej: Compra de supermercado") },
                    isError = descriptionError != null,
                    supportingText = descriptionError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Selector de categoría
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(Modifier.fillMaxWidth()) {
                Column(
                    Modifier
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Category, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Categoría", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    // Selector de categoría con ExposedDropdownMenuBox
                    var expanded by remember { mutableStateOf(false) }
                    val selectedCategory = categories.find { it.id == selectedCategoryId }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = {
                                if (selectedCategory == null) Text("Selecciona una categoría")
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            isError = categoryError != null
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(resolvedMenuContainerColor())
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategoryId = category.id
                                        expanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }
                    if (categoryError != null) Text(categoryError!!, color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
                IconButton(
                    onClick = { showCategoryDialog = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva categoría", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Dialog para crear nueva categoría
        if (showCategoryDialog) {
            ThemedAlertDialog(
                onDismissRequest = { showCategoryDialog = false },
                title = { Text("Nueva Categoría") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            label = { Text("Nombre de la categoría") },
                            isError = newCategoryError != null
                        )
                        if (newCategoryError != null) Text(newCategoryError!!, color = MaterialTheme.colorScheme.error)
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (newCategoryName.isBlank()) {
                            newCategoryError = "El nombre es requerido"
                        } else {
                            pendingCategoryName = newCategoryName.trim()
                            shouldCreateCategory = true
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = AccentVibrantStart, contentColor = Color.White)) { Text("Crear") }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showCategoryDialog = false }, colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentVibrantStart)) { Text("Cancelar") }
                }
            )
        }

        // Efecto para crear categoría
        LaunchedEffect(shouldCreateCategory, pendingCategoryName) {
            if (shouldCreateCategory && pendingCategoryName.isNotBlank()) {
                val newId = db.categoryDao().insert(com.example.admin_ingresos.data.Category(name = pendingCategoryName))
                selectedCategoryId = newId.toInt()
                categoryUpdateTrigger++ // Forzar recomposición y actualización visual
                showCategoryDialog = false
                newCategoryName = ""
                newCategoryError = null
                shouldCreateCategory = false
                pendingCategoryName = ""
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Selector de método de pago
        if (paymentMethods.isNotEmpty()) {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(10.dp)) {
                    Text("Método de pago", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(6.dp))
                    var expanded by remember { mutableStateOf(false) }
                    val selectedMethod = paymentMethods.find { it.id == selectedPaymentMethodId }
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selectedMethod?.name ?: "Selecciona un método de pago")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(resolvedMenuContainerColor())) {
                        paymentMethods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method.name) },
                                onClick = {
                                    selectedPaymentMethodId = method.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Adjuntar foto de recibo
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(10.dp)) {
                Text("Recibo", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Adjuntar foto o recibo", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { showPhotoMenu = true }) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = "Adjuntar foto")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Adjuntar")
                    }
                }
                if (receiptPhotoUri != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.foundation.Image(
                        painter = rememberAsyncImagePainter(receiptPhotoUri),
                        contentDescription = "Recibo adjunto",
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.CenterHorizontally)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Imagen subida", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                DropdownMenu(expanded = showPhotoMenu, onDismissRequest = { showPhotoMenu = false }, modifier = Modifier.background(resolvedMenuContainerColor())) {
                    DropdownMenuItem(text = { Text("Desde galería") }, onClick = {
                        galleryLauncher.launch("image/*")
                        showPhotoMenu = false
                    })
                    DropdownMenuItem(text = { Text("Tomar foto") }, onClick = {
                        val uri = createImageFile()
                        if (uri != null) {
                            cameraImageUri = uri
                            cameraLauncher.launch(uri)
                        }
                        showPhotoMenu = false
                    })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botones de acción
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) { Text("Cancelar") }
            Button(
                onClick = { handleSave() },
                modifier = Modifier.weight(1f).height(48.dp),
                enabled = isFormValid && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar")
                }
            }
        }
        if (showSuccessMessage) {
            ThemedAlertDialog(
                onDismissRequest = { showSuccessMessage = false },
                icon = {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
                },
                title = { Text("¡Transacción guardada!") },
                text = { Text("La transacción se agregó correctamente.") },
                confirmButton = {
                    Button(onClick = { showSuccessMessage = false }) {
                        Text("Aceptar")
                    }
                }
            )
        }
    }
}


@Composable
fun TypeChip(
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
fun AmountInputCard(
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
fun DescriptionInputCard(
    description: String,
    onDescriptionChange: (String) -> Unit,
    error: String?
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier
                .padding(horizontal = 14.dp, vertical = 14.dp)
                .fillMaxWidth()
        ) {
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

