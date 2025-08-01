package com.example.admin_ingresos.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp

data class SettingsItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val iconColor: Color = Color.Unspecified,
    val onClick: () -> Unit
)

data class SettingsCategory(
    val title: String,
    val items: List<SettingsItem>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToThemeSettings: () -> Unit = {},
    onNavigateToCurrencySettings: () -> Unit = {},
    onNavigateToDataManagement: () -> Unit = {},
    onNavigateToNotificationSettings: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferencesManager = remember { com.example.admin_ingresos.data.PreferencesManager(context) }
    
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    
    // Settings categories
    val settingsCategories = remember {
        listOf(
            SettingsCategory(
                title = "Apariencia",
                items = listOf(
                    SettingsItem(
                        id = "theme",
                        title = "Tema",
                        subtitle = "Claro, oscuro o automático",
                        icon = Icons.Default.Palette,
                        iconColor = Color(0xFF9C27B0),
                        onClick = onNavigateToThemeSettings
                    ),
                    SettingsItem(
                        id = "language",
                        title = "Idioma",
                        subtitle = "Español",
                        icon = Icons.Default.Language,
                        iconColor = Color(0xFF2196F3),
                        onClick = { /* TODO: Implement language settings */ }
                    )
                )
            ),
            SettingsCategory(
                title = "Finanzas",
                items = listOf(
                    SettingsItem(
                        id = "currency",
                        title = "Moneda",
                        subtitle = "Configurar moneda principal",
                        icon = Icons.Default.AttachMoney,
                        iconColor = Color(0xFF4CAF50),
                        onClick = onNavigateToCurrencySettings
                    ),
                    SettingsItem(
                        id = "categories",
                        title = "Categorías",
                        subtitle = "Gestionar categorías de transacciones",
                        icon = Icons.Default.Category,
                        iconColor = Color(0xFFFF9800),
                        onClick = { /* TODO: Navigate to category management */ }
                    ),
                    SettingsItem(
                        id = "payment_methods",
                        title = "Métodos de Pago",
                        subtitle = "Configurar métodos de pago",
                        icon = Icons.Default.CreditCard,
                        iconColor = Color(0xFF607D8B),
                        onClick = { /* TODO: Navigate to payment methods */ }
                    )
                )
            ),
            SettingsCategory(
                title = "Notificaciones",
                items = listOf(
                    SettingsItem(
                        id = "notifications",
                        title = "Notificaciones",
                        subtitle = "Configurar alertas y recordatorios",
                        icon = Icons.Default.Notifications,
                        iconColor = Color(0xFFFF5722),
                        onClick = onNavigateToNotificationSettings
                    ),
                    SettingsItem(
                        id = "budget_alerts",
                        title = "Alertas de Presupuesto",
                        subtitle = "Notificaciones de límites de gasto",
                        icon = Icons.Default.Warning,
                        iconColor = Color(0xFFFFC107),
                        onClick = { /* TODO: Navigate to budget alerts */ }
                    )
                )
            ),
            SettingsCategory(
                title = "Datos",
                items = listOf(
                    SettingsItem(
                        id = "backup",
                        title = "Respaldo y Restauración",
                        subtitle = "Gestionar copias de seguridad",
                        icon = Icons.Default.Backup,
                        iconColor = Color(0xFF3F51B5),
                        onClick = onNavigateToDataManagement
                    ),
                    SettingsItem(
                        id = "export",
                        title = "Exportar Datos",
                        subtitle = "Exportar transacciones y reportes",
                        icon = Icons.Default.FileDownload,
                        iconColor = Color(0xFF009688),
                        onClick = { /* TODO: Navigate to export options */ }
                    ),
                    SettingsItem(
                        id = "import",
                        title = "Importar Datos",
                        subtitle = "Importar desde otros archivos",
                        icon = Icons.Default.FileUpload,
                        iconColor = Color(0xFF795548),
                        onClick = { /* TODO: Navigate to import options */ }
                    )
                )
            ),
            SettingsCategory(
                title = "Privacidad y Seguridad",
                items = listOf(
                    SettingsItem(
                        id = "privacy",
                        title = "Privacidad",
                        subtitle = "Configuración de privacidad",
                        icon = Icons.Default.Security,
                        iconColor = Color(0xFFE91E63),
                        onClick = { /* TODO: Navigate to privacy settings */ }
                    ),
                    SettingsItem(
                        id = "biometric",
                        title = "Autenticación Biométrica",
                        subtitle = "Proteger con huella o Face ID",
                        icon = Icons.Default.Fingerprint,
                        iconColor = Color(0xFF673AB7),
                        onClick = { /* TODO: Navigate to biometric settings */ }
                    )
                )
            ),
            SettingsCategory(
                title = "Soporte",
                items = listOf(
                    SettingsItem(
                        id = "help",
                        title = "Ayuda y Tutoriales",
                        subtitle = "Guías de uso de la aplicación",
                        icon = Icons.Default.Help,
                        iconColor = Color(0xFF00BCD4),
                        onClick = { /* TODO: Navigate to help */ }
                    ),
                    SettingsItem(
                        id = "feedback",
                        title = "Enviar Comentarios",
                        subtitle = "Reportar problemas o sugerencias",
                        icon = Icons.Default.Feedback,
                        iconColor = Color(0xFF8BC34A),
                        onClick = { /* TODO: Open feedback form */ }
                    ),
                    SettingsItem(
                        id = "about",
                        title = "Acerca de",
                        subtitle = "Información de la aplicación",
                        icon = Icons.Default.Info,
                        iconColor = Color(0xFF9E9E9E),
                        onClick = onNavigateToAbout
                    )
                )
            )
        )
    }
    
    // Filter settings based on search query
    val filteredCategories = remember(searchQuery, settingsCategories) {
        if (searchQuery.isBlank()) {
            settingsCategories
        } else {
            settingsCategories.mapNotNull { category ->
                val filteredItems = category.items.filter { item ->
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.subtitle?.contains(searchQuery, ignoreCase = true) == true
                }
                if (filteredItems.isNotEmpty()) {
                    category.copy(items = filteredItems)
                } else null
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with search
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Configuración",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Search bar
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { isSearchActive = false },
                    active = isSearchActive,
                    onActiveChange = { isSearchActive = it },
                    placeholder = { Text("Buscar configuración...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar"
                        )
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar"
                                )
                            }
                        }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Search suggestions could go here
                }
            }
        }
        
        // Settings content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (filteredCategories.isEmpty()) {
                item {
                    EmptySearchResults(
                        query = searchQuery,
                        onClearSearch = { searchQuery = "" }
                    )
                }
            } else {
                filteredCategories.forEach { category ->
                    item {
                        SettingsCategorySection(
                            category = category,
                            searchQuery = searchQuery
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

@Composable
fun SettingsCategorySection(
    category: SettingsCategory,
    searchQuery: String = "",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Category title
        if (searchQuery.isBlank()) {
            Text(
                text = category.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )
        }
        
        // Category items
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                category.items.forEachIndexed { index, item ->
                    SettingsItemRow(
                        item = item,
                        showDivider = index < category.items.size - 1
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsItemRow(
    item: SettingsItem,
    showDivider: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { item.onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (item.iconColor != Color.Unspecified) 
                            item.iconColor.copy(alpha = 0.1f)
                        else 
                            MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = if (item.iconColor != Color.Unspecified) 
                        item.iconColor
                    else 
                        MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                item.subtitle?.let { subtitle ->
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Arrow
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ir a ${item.title}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        
        // Divider
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
fun EmptySearchResults(
    query: String,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Sin resultados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "No se encontraron configuraciones para \"$query\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(onClick = onClearSearch) {
                Text("Limpiar búsqueda")
            }
        }
    }
}