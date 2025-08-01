package com.example.admin_ingresos.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.*

// Brand Header Component
@Composable
fun CashFlowHeader(
    title: String,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💰",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "CashFlow",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            Row {
                actions()
            }
        }
    }
}

// Glassmorphism Card Component
@Composable
fun CashFlowCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    glassLevel: Int = 2, // 1-4, más alto = más transparente
    content: @Composable ColumnScope.() -> Unit
) {
    val glassColor = when (glassLevel) {
        1 -> com.example.admin_ingresos.ui.theme.GlassSurface1
        2 -> com.example.admin_ingresos.ui.theme.GlassSurface2
        3 -> com.example.admin_ingresos.ui.theme.GlassSurface3  
        4 -> com.example.admin_ingresos.ui.theme.GlassSurface4
        else -> com.example.admin_ingresos.ui.theme.GlassSurface2
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = glassColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column(content = content)
    }
}

// Glassmorphism Balance Card with Gradient
@Composable
fun GlassmorphismBalanceCard(
    balance: String,
    title: String = "Balance Total",
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        // Gradient Background
        Card(
            modifier = Modifier
                .fillMaxSize(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                com.example.admin_ingresos.ui.theme.GradientStart,
                                com.example.admin_ingresos.ui.theme.GradientMiddle,
                                com.example.admin_ingresos.ui.theme.GradientEnd
                            )
                        )
                    )
            )
        }
        
        // Glass Overlay
        Card(
            modifier = Modifier
                .fillMaxSize(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = com.example.admin_ingresos.ui.theme.GlassSurface2
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = balance,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    color = Color.White
                )
                
                subtitle?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// Glassmorphism Metric Card
@Composable
fun GlassmorphismMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        // Gradient Background
        Card(
            modifier = Modifier
                .fillMaxSize(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                color.copy(alpha = 0.8f),
                                color.copy(alpha = 0.6f)
                            )
                        )
                    )
            )
        }
        
        // Glass Overlay
        Card(
            modifier = Modifier
                .fillMaxSize(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.2f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.White.copy(alpha = 0.9f)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Metric Card Component
@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconColor: Color = Color.Unspecified,
    trend: Float? = null,
    onClick: (() -> Unit)? = null
) {
    val finalIconColor = if (iconColor == Color.Unspecified) {
        MaterialTheme.colorScheme.primary
    } else {
        iconColor
    }
    CashFlowCard(
        onClick = onClick,
        glassLevel = 2
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = finalIconColor,
                modifier = Modifier.size(24.dp)
            )
            
            trend?.let {
                val isPositive = it > 0
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isPositive) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = if (isPositive) Color(0xFF4CAF50) else Color(0xFFE57373),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${kotlin.math.abs(it).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFE57373)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

// Empty State Component
@Composable
fun EmptyState(
    title: String,
    description: String,
    icon: ImageVector = Icons.Default.Info,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(text = actionText)
            }
        }
    }
}

// Loading State Component
@Composable
fun LoadingState(
    message: String = "Cargando..."
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

// Enhanced FAB
@Composable
fun CashFlowFAB(
    onClick: () -> Unit,
    icon: ImageVector = Icons.Default.Add,
    text: String? = null
) {
    if (text != null) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text)
        }
    } else {
        FloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Agregar"
            )
        }
    }
}

// Quick Action Button
@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

// ENHANCED TRANSACTION FORM COMPONENTS

@Composable
fun TypeSelectorCard(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    CashFlowCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Tipo de Transacción",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TypeOptionCard(
                    text = "Gasto",
                    icon = Icons.Default.TrendingDown,
                    selected = selectedType == "Gasto",
                    onClick = { onTypeSelected("Gasto") },
                    color = Color(0xFFE57373),
                    modifier = Modifier.weight(1f)
                )
                
                TypeOptionCard(
                    text = "Ingreso",
                    icon = Icons.Default.TrendingUp,
                    selected = selectedType == "Ingreso",
                    onClick = { onTypeSelected("Ingreso") },
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TypeOptionCard(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        border = if (selected) BorderStroke(2.dp, color) else null,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountInputCard(
    amount: String,
    onAmountChange: (String) -> Unit,
    error: String?,
    transactionType: String,
    modifier: Modifier = Modifier
) {
    CashFlowCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Monto",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                placeholder = { 
                    Text(
                        "0.00",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Light
                        )
                    ) 
                },
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (transactionType == "Ingreso") Color(0xFF4CAF50) else Color(0xFFE57373)
                ),
                leadingIcon = { 
                    Text(
                        "$", 
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (transactionType == "Ingreso") Color(0xFF4CAF50) else Color(0xFFE57373)
                    ) 
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = error != null,
                supportingText = error?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (transactionType == "Ingreso") Color(0xFF4CAF50) else Color(0xFFE57373)
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DescriptionInputCard(
    description: String,
    onDescriptionChange: (String) -> Unit,
    suggestions: List<String>,
    onSuggestionSelected: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier
) {
    CashFlowCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Descripción",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                placeholder = { Text("¿En qué gastaste o ganaste dinero?") },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null
                    ) 
                },
                isError = error != null,
                supportingText = error?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            
            if (suggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Sugerencias",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(suggestions) { suggestion ->
                        SuggestionChip(
                            onClick = { onSuggestionSelected(suggestion) },
                            label = { Text(suggestion) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedCategorySelector(
    categories: List<com.example.admin_ingresos.data.Category>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int) -> Unit,
    onNewCategoryAdded: (String) -> Unit,
    error: String?,
    transactionType: String,
    modifier: Modifier = Modifier
) {
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val selectedCategory = categories.find { it.id == selectedCategoryId }
    
    CashFlowCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Categoría",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                TextButton(
                    onClick = { showAddCategoryDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nueva")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (categories.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Category,
                    title = "Sin categorías",
                    description = "Agrega tu primera categoría para organizar mejor tus transacciones"
                )
            } else {
                // Dropdown style selector
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "Seleccionar categoría",
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = getCategoryIcon(selectedCategory?.name ?: ""),
                                contentDescription = null,
                                tint = if (transactionType == "Ingreso") Color(0xFF4CAF50) else Color(0xFFE57373)
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        isError = error != null,
                        supportingText = error?.let { { Text(it) } },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (transactionType == "Ingreso") Color(0xFF4CAF50) else Color(0xFFE57373)
                        )
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = getCategoryIcon(category.name),
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(category.name)
                                    }
                                },
                                onClick = {
                                    onCategorySelected(category.id)
                                    expanded = false
                                },
                                leadingIcon = if (selectedCategoryId == category.id) {
                                    { 
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        ) 
                                    }
                                } else null
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onAddCategory = { categoryName ->
                onNewCategoryAdded(categoryName)
                showAddCategoryDialog = false
            }
        )
    }
}

// Helper function for category icons
private fun getCategoryIcon(categoryName: String): ImageVector {
    return when (categoryName.lowercase()) {
        "comida", "alimentación", "supermercado", "restaurant" -> Icons.Default.Restaurant
        "transporte", "taxi", "uber", "gasolina" -> Icons.Default.DirectionsCar
        "entretenimiento", "ocio", "cine", "diversión" -> Icons.Default.Movie
        "salud", "médico", "farmacia", "hospital" -> Icons.Default.LocalHospital
        "ropa", "vestimenta", "shopping" -> Icons.Default.Checkroom
        "hogar", "casa", "servicios", "luz", "agua" -> Icons.Default.Home
        "educación", "estudio", "universidad", "cursos" -> Icons.Default.School
        "trabajo", "salario", "sueldo" -> Icons.Default.Work
        "gimnasio", "deporte", "fitness" -> Icons.Default.FitnessCenter
        "tecnología", "software", "apps" -> Icons.Default.Computer
        "viajes", "vacaciones", "turismo" -> Icons.Default.Flight
        "mascotas", "veterinario" -> Icons.Default.Pets
        "regalos", "celebraciones" -> Icons.Default.CardGiftcard
        else -> Icons.Default.Category
    }
}

@Composable
private fun CategoryChip(
    category: com.example.admin_ingresos.data.Category,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = onClick,
        label = { Text(category.name) },
        selected = selected,
        leadingIcon = if (selected) {
            { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
        } else null
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onAddCategory: (String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Categoría") },
        text = {
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("Nombre de la categoría") },
                placeholder = { Text("Ej: Comida, Transporte") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (categoryName.isNotBlank()) {
                        onAddCategory(categoryName.trim())
                    }
                },
                enabled = categoryName.isNotBlank()
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPaymentMethodDialog(
    onDismiss: () -> Unit,
    onAddPaymentMethod: (String) -> Unit
) {
    var methodName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Método de Pago") },
        text = {
            OutlinedTextField(
                value = methodName,
                onValueChange = { methodName = it },
                label = { Text("Nombre del método de pago") },
                placeholder = { Text("Ej: Efectivo, Tarjeta, Transferencia") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (methodName.isNotBlank()) {
                        onAddPaymentMethod(methodName.trim())
                    }
                },
                enabled = methodName.isNotBlank()
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun PaymentMethodCard(
    paymentMethods: List<com.example.admin_ingresos.data.PaymentMethod>,
    selectedPaymentMethodId: Int?,
    onPaymentMethodSelected: (Int?) -> Unit,
    onNewPaymentMethodAdded: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    showExpanded: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(showExpanded || paymentMethods.isEmpty()) }
    var showAddPaymentMethodDialog by remember { mutableStateOf(false) }
    
    CashFlowCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Método de Pago",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (selectedPaymentMethodId != null) {
                            val selectedMethod = paymentMethods.find { it.id == selectedPaymentMethodId }
                            selectedMethod?.let {
                                Text(
                                    text = it.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else if (paymentMethods.isEmpty()) {
                            Text(
                                text = "No configurado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (paymentMethods.isEmpty()) {
                        TextButton(
                            onClick = { showAddPaymentMethodDialog = true },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Agregar",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (paymentMethods.isEmpty()) {
                        Text(
                            text = "No hay métodos de pago configurados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    onClick = { onPaymentMethodSelected(null) },
                                    label = { Text("Ninguno") },
                                    selected = selectedPaymentMethodId == null
                                )
                            }
                            items(paymentMethods) { method ->
                                FilterChip(
                                    onClick = { onPaymentMethodSelected(method.id) },
                                    label = { Text(method.name) },
                                    selected = selectedPaymentMethodId == method.id,
                                    leadingIcon = if (selectedPaymentMethodId == method.id) {
                                        { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Botón para agregar nuevo método de pago
                        OutlinedButton(
                            onClick = { showAddPaymentMethodDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Agregar método de pago",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo para agregar método de pago
    if (showAddPaymentMethodDialog) {
        AddPaymentMethodDialog(
            onDismiss = { showAddPaymentMethodDialog = false },
            onAddPaymentMethod = { methodName ->
                onNewPaymentMethodAdded(methodName)
                showAddPaymentMethodDialog = false
            }
        )
    }
}

@Composable
fun ActionButtons(
    isFormValid: Boolean,
    isLoading: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    CashFlowCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
            
            Button(
                onClick = onSave,
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
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Guardar")
            }
        }
    }
}

// Photo Capture Component
@Composable
fun ReceiptPhotoCard(
    photoUri: android.net.Uri?,
    onPhotoTaken: (android.net.Uri?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showImageOptions by remember { mutableStateOf(false) }
    
    // Camera launcher
    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Photo was taken successfully, URI is already set
        }
    }
    
    // Gallery launcher
    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onPhotoTaken(it) }
    }
    
    // Get context at the Composable level
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Camera permission launcher
    val cameraPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Create temp file for camera
            val tempFile = kotlin.io.createTempFile("photo", ".jpg", context.cacheDir)
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
            onPhotoTaken(uri)
            cameraLauncher.launch(uri)
        }
    }
    
    CashFlowCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Foto del Recibo (Opcional)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (photoUri != null) {
                        Text(
                            text = " - Adjunta",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                if (photoUri != null) {
                    IconButton(
                        onClick = { onPhotoTaken(null) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar foto",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (photoUri != null) {
                // Show photo preview
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable { showImageOptions = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Photo,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Foto adjunta",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Toca para cambiar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Photo capture options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { 
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cámara")
                    }
                    
                    OutlinedButton(
                        onClick = { 
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Galería")
                    }
                }
            }
        }
    }
    
    // Image options dialog when photo exists
    if (showImageOptions) {
        AlertDialog(
            onDismissRequest = { showImageOptions = false },
            title = { Text("Cambiar foto") },
            text = { Text("¿Cómo quieres cambiar la foto?") },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            showImageOptions = false
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    ) {
                        Text("Cámara")
                    }
                    TextButton(
                        onClick = {
                            showImageOptions = false
                            galleryLauncher.launch("image/*")
                        }
                    ) {
                        Text("Galería")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showImageOptions = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Success notification component
@Composable
fun TransactionSuccessDialog(
    isVisible: Boolean,
    transactionType: String,
    amount: String,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (transactionType == "Ingreso") Color(0xFF4CAF50) else Color(0xFFE57373)
                    ),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(32.dp),
                        tint = Color.White
                    )
                }
            },
            title = {
                Text(
                    text = "¡${transactionType} registrado!",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Se ha guardado exitosamente:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = amount,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (transactionType == "Ingreso") Color(0xFF4CAF50) else Color(0xFFE57373),
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (transactionType == "Ingreso") Color(0xFF4CAF50) else Color(0xFFE57373)
                    )
                ) {
                    Text("Continuar", color = Color.White)
                }
            }
        )
    }
}
