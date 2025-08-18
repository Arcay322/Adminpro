package com.example.admin_ingresos.ui.profile

import android.app.Application
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.admin_ingresos.ui.profile.AvatarUtils
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import com.example.admin_ingresos.ui.components.GlassCard
import com.example.admin_ingresos.ui.components.GlassmorphismScreen
import com.example.admin_ingresos.ui.icons.LucideIconMapper
import com.example.admin_ingresos.ui.theme.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.luminance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onSignOut: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val profileVm: ProfileViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory(context.applicationContext as Application) {})

    val name by profileVm.name.collectAsState()
    val email by profileVm.email.collectAsState()
    val avatarUri by profileVm.avatarUri.collectAsState()
    val currency by profileVm.currency.collectAsState()
    val darkMode by profileVm.darkMode.collectAsState()
    val phone by profileVm.phone.collectAsState()
    val bio by profileVm.bio.collectAsState()
    val language by profileVm.language.collectAsState()
    val notificationsEnabled by profileVm.notificationsEnabled.collectAsState()
    val fingerprintEnabledState by profileVm.fingerprintEnabled.collectAsState()
    val backgroundColorInt by profileVm.backgroundColor.collectAsState()
    // Helper to convert ARGB int to Compose Color
    val backgroundColorCompose = remember(backgroundColorInt) { androidx.compose.ui.graphics.Color(backgroundColorInt) }
    val forceLightMode by profileVm.forceLight.collectAsState()

    var editName by remember { mutableStateOf(name) }
    var editEmail by remember { mutableStateOf(email) }
    var editCurrency by remember { mutableStateOf(currency) }
    var editDarkMode by remember { mutableStateOf(darkMode) }
    var editPhone by remember { mutableStateOf(phone) }
    var editBio by remember { mutableStateOf(bio) }
    var editLanguage by remember { mutableStateOf(language) }
    var editNotificationsEnabled by remember { mutableStateOf(notificationsEnabled) }
    var editFingerprintEnabled by remember { mutableStateOf(fingerprintEnabledState) }
    var pickedAvatarUri by remember { mutableStateOf<Uri?>(avatarUri?.let { Uri.parse(it) }) }
    // var showSavedSnackbar by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            pickedAvatarUri = uri
            // save a local copy and persist avatar path
            val savedPath = AvatarUtils.saveAvatar(context, uri)
            if (savedPath != null) {
                profileVm.saveProfile(editName, editEmail, android.net.Uri.fromFile(java.io.File(savedPath)).toString(), editCurrency, editDarkMode, editPhone, editBio, editLanguage, editNotificationsEnabled, editFingerprintEnabled, pinInput)
                coroutineScope.launch { snackbarHostState.showSnackbar("Avatar guardado") }
            } else {
                coroutineScope.launch { snackbarHostState.showSnackbar("Error al guardar avatar") }
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        GlassmorphismScreen(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(innerPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onBackground)
                }
                Text(text = "Perfil", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Avatar & basic info
            GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = GlassWhiteStrong, cornerRadius = 20.dp) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(88.dp).clip(CircleShape).background(Brush.linearGradient(listOf(AccentVibrantStart, AccentVibrantEnd))), contentAlignment = Alignment.Center) {
                        if (pickedAvatarUri != null) {
                            Image(painter = rememberAsyncImagePainter(pickedAvatarUri), contentDescription = null, modifier = Modifier.size(80.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        } else {
                Icon(LucideIconMapper.Navigation.profile, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = if (editName.isNotBlank()) editName else "Usuario", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = if (editEmail.isNotBlank()) editEmail else "—", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { imagePicker.launch("image/*") }, colors = ButtonDefaults.buttonColors(containerColor = AccentVibrantStart)) { Text("Cambiar avatar") }
                            OutlinedButton(onClick = { pickedAvatarUri = null }) { Text("Quitar") }
                        }
                    }
                }
            }

            // Account details
            GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = GlassWhite, cornerRadius = 16.dp) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Cuenta", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nombre") },
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(containerColor = Color.Transparent),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary)
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(containerColor = Color.Transparent),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary)
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Teléfono") },
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(containerColor = Color.Transparent),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary)
                    )
                }
            }

            // Preferences
            GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = GlassWhite, cornerRadius = 16.dp) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Preferencias", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(text = "Moneda: $editCurrency", color = MaterialTheme.colorScheme.onBackground)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Idioma: $editLanguage", color = MaterialTheme.colorScheme.onBackground)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            DropdownMenuDemo(options = listOf("PEN", "USD", "EUR"), selected = editCurrency, onSelected = { editCurrency = it })
                            Spacer(modifier = Modifier.height(8.dp))
                            DropdownMenuDemo(options = listOf("es", "en"), selected = editLanguage, onSelected = { editLanguage = it })
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Modo oscuro", color = MaterialTheme.colorScheme.onBackground)
                        Switch(checked = editDarkMode, onCheckedChange = { editDarkMode = it }, colors = SwitchDefaults.colors(checkedThumbColor = AccentVibrantStart))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Notificaciones", color = MaterialTheme.colorScheme.onBackground)
                        Switch(checked = editNotificationsEnabled, onCheckedChange = { editNotificationsEnabled = it })
                    }
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio") },
                        colors = TextFieldDefaults.outlinedTextFieldColors(containerColor = Color.Transparent),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Personalización - Color de fondo
            GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = GlassWhite, cornerRadius = 16.dp) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Personalización", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
                    Text(text = "Color de fondo", color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(8.dp))
                    val swatches = listOf(
                        androidx.compose.ui.graphics.Color(0xFF000000), // Negro
                        androidx.compose.ui.graphics.Color(0xFF111111),
                        androidx.compose.ui.graphics.Color(0xFF1F2937), // Gris oscuro
                        androidx.compose.ui.graphics.Color(0xFF374151),
                        androidx.compose.ui.graphics.Color(0xFF4B5563),
                        androidx.compose.ui.graphics.Color(0xFF6B7280), // Gris medio
                        androidx.compose.ui.graphics.Color(0xFFD1D5DB), // Gris claro
                        androidx.compose.ui.graphics.Color(0xFFF3F4F6),
                        androidx.compose.ui.graphics.Color(0xFFFFFFFF) // Blanco
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        swatches.forEach { swatch ->
                            val swatchInt = swatch.copy(alpha = 1f).toArgb()
                            val selected = swatchInt == backgroundColorInt
                            val checkTint = if (swatch.luminance() > 0.5f) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White
                            Box(modifier = Modifier
                                .size(if (selected) 48.dp else 40.dp)
                                .clip(CircleShape)
                                .background(swatch)
                                .padding(2.dp)
                                .then(if (selected) Modifier.border(width = 2.dp, color = AccentVibrantStart, shape = CircleShape) else Modifier)
                                .clickable {
                                    profileVm.setBackgroundColor(swatchInt)
                                }
                            ) {
                                if (selected) {
                                    Icon(LucideIconMapper.Navigation.check, contentDescription = null, tint = checkTint, modifier = Modifier.align(Alignment.Center))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Forzar modo claro", color = MaterialTheme.colorScheme.onBackground)
                        Switch(checked = forceLightMode, onCheckedChange = { profileVm.setForceLightMode(it) })
                    }
                }
            }

            // Security
            GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = GlassWhite, cornerRadius = 16.dp) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Seguridad", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Huella dactilar", color = TextPrimary)
                        val fpEnabled = editFingerprintEnabled
                        Switch(checked = fpEnabled, onCheckedChange = { editFingerprintEnabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = AccentVibrantStart))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "PIN de seguridad", color = TextPrimary)
                        Button(onClick = { showPinDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = AccentVibrantStart)) { Text("Configurar PIN") }
                    }
                }
            }

            // Help & Support
            GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = GlassWhite, cornerRadius = 16.dp) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Ayuda y soporte", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text(text = "Preguntas frecuentes", color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    val ctx = LocalContext.current
                    Button(onClick = {
                        // Open mail app for contact
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@example.com")
                            putExtra(Intent.EXTRA_SUBJECT, "Soporte Adminpro")
                        }
                        ctx.startActivity(intent)
                    }, colors = ButtonDefaults.buttonColors(containerColor = AccentVibrantStart)) { Text("Contactar soporte") }
                }
            }

            // Actions
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = {
                    // Persist selected background color as part of profile save
                    profileVm.setBackgroundColor(backgroundColorInt)
                    profileVm.saveProfile(editName, editEmail, pickedAvatarUri?.toString(), editCurrency, editDarkMode, editPhone, editBio, editLanguage, editNotificationsEnabled, editFingerprintEnabled, pinInput)
                    coroutineScope.launch { snackbarHostState.showSnackbar("Perfil guardado") }
                }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = AccentVibrantStart)) {
                    Text("Guardar")
                }
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedButton(onClick = {
                    showDeleteDialog = true
                }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Eliminar cuenta")
                }
            }

            // Save feedback handled via Scaffold snackbarHostState

            if (showDeleteDialog) {
                AlertDialog(onDismissRequest = { showDeleteDialog = false }, title = { Text("Confirmar eliminación") }, text = { Text("Esto eliminará tu perfil localmente. ¿Continuar?") }, confirmButton = {
                    TextButton(onClick = {
                        profileVm.signOut()
                        showDeleteDialog = false
                        onSignOut?.invoke()
                    }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
                }, dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
                })
            }

            if (showPinDialog) {
                AlertDialog(onDismissRequest = { showPinDialog = false }, title = { Text("Configurar PIN") }, text = {
                    Column { OutlinedTextField(value = pinInput, onValueChange = { pinInput = it }, label = { Text("PIN (4 dígitos)") }) }
                }, confirmButton = {
                    TextButton(onClick = {
                        profileVm.saveProfile(editName, editEmail, pickedAvatarUri?.toString(), editCurrency, editDarkMode, editPhone, editBio, editLanguage, editNotificationsEnabled, editFingerprintEnabled, pinInput)
                        pinInput = ""
                        showPinDialog = false
                        coroutineScope.launch { snackbarHostState.showSnackbar("PIN guardado") }
                    }) { Text("Guardar") }
                }, dismissButton = {
                    TextButton(onClick = { showPinDialog = false }) { Text("Cancelar") }
                })
            }
        }
    }
}

}

@Composable
private fun DropdownMenuDemo(options: List<String>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = true }) { Text(selected) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(text = { Text(opt) }, onClick = { onSelected(opt); expanded = false })
            }
        }
    }
}

