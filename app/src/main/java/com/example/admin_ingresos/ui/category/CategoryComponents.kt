package com.example.admin_ingresos.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.admin_ingresos.data.Category
import com.example.admin_ingresos.ui.icons.LucideIconMapper
import com.example.admin_ingresos.ui.theme.*
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySearchBar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onFilterClick: () -> Unit,
    onAddCategoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = { Text("Buscar categorías...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChanged("") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar"
                        )
                    }
                }
            },
            modifier = Modifier.weight(1f),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CashFlowPrimary,
                focusedLabelColor = CashFlowPrimary
            )
        )
        
        IconButton(
            onClick = onFilterClick,
            modifier = Modifier
                .size(56.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    RoundedCornerShape(12.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Filtros",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        IconButton(
            onClick = onAddCategoryClick,
            modifier = Modifier
                .size(56.dp)
                .background(
                    CashFlowPrimary,
                    RoundedCornerShape(12.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar categoría",
                tint = Color.White
            )
        }
    }
}

@Composable
fun CategoryStatsCard(
    totalCategories: Int,
    favoriteCategories: Int,
    mostUsedCategory: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 Resumen de Categorías",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(
                    value = totalCategories.toString(),
                    label = "Total",
                    color = CashFlowPrimary
                )
                
                StatItem(
                    value = favoriteCategories.toString(),
                    label = "Favoritas",
                    color = Color(0xFFFFD700)
                )
                
                StatItem(
                    value = if (mostUsedCategory != "N/A") "✓" else "-",
                    label = "Más usada",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            if (mostUsedCategory != "N/A") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Categoría más utilizada: $mostUsedCategory",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun CategorySortButton(
    currentSortOption: CategorySortOption,
    onSortOptionSelected: (CategorySortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        OutlinedButton(
            onClick = { expanded = true }
        ) {
            Icon(
                imageVector = Icons.Default.Sort,
                contentDescription = "Ordenar",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(currentSortOption.displayName)
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            CategorySortOption.values().forEach { sortOption ->
                DropdownMenuItem(
                    text = { Text(sortOption.displayName) },
                    onClick = {
                        onSortOptionSelected(sortOption)
                        expanded = false
                    },
                    leadingIcon = {
                        if (sortOption == currentSortOption) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Seleccionado"
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SelectionActionBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onBulkEdit: () -> Unit,
    onBulkDelete: () -> Unit,
    onBulkFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$selectedCount seleccionadas",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBulkFavorite) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Marcar favoritas",
                        tint = Color.White
                    )
                }
                
                IconButton(onClick = onBulkEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = Color.White
                    )
                }
                
                IconButton(onClick = onBulkDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color.White
                    )
                }
                
                IconButton(onClick = onClearSelection) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancelar",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategoryDialog(
    category: Category? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String, color: String) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(category?.icon ?: "category") }
    var selectedColor by remember { mutableStateOf(category?.color ?: "#2196F3") }
    var showIconPicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    
    val predefinedColors = listOf(
        "#F44336", "#E91E63", "#9C27B0", "#673AB7",
        "#3F51B5", "#2196F3", "#03DAC5", "#009688",
        "#4CAF50", "#8BC34A", "#CDDC39", "#FFEB3B",
        "#FFC107", "#FF9800", "#FF5722", "#795548"
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = if (category == null) "Crear Categoría" else "Editar Categoría",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Campo de nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la categoría") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CashFlowPrimary,
                        focusedLabelColor = CashFlowPrimary
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Selector de icono
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Icono:",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedButton(
                        onClick = { showIconPicker = true }
                    ) {
                        Icon(
                            imageVector = LucideIconMapper.getIconFromCategoryName(selectedIcon),
                            contentDescription = "Icono seleccionado",
                            tint = Color(android.graphics.Color.parseColor(selectedColor))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cambiar")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Selector de color
                Column {
                    Text(
                        text = "Color:",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(8),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(120.dp)
                    ) {
                        items(predefinedColors) { color ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        Color(android.graphics.Color.parseColor(color)),
                                        CircleShape
                                    )
                                    .clickable { selectedColor = color }
                                    .then(
                                        if (selectedColor == color) {
                                            Modifier.background(
                                                MaterialTheme.colorScheme.outline,
                                                CircleShape
                                            )
                                        } else {
                                            Modifier
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColor == color) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seleccionado",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(name, selectedIcon, selectedColor)
                            }
                        },
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CashFlowPrimary
                        )
                    ) {
                        Text(if (category == null) "Crear" else "Guardar")
                    }
                }
            }
        }
    }
    
    // Icon picker dialog
    if (showIconPicker) {
        IconPickerDialog(
            selectedIcon = selectedIcon,
            onIconSelected = { icon ->
                selectedIcon = icon
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false }
        )
    }
}

@Composable
fun IconPickerDialog(
    selectedIcon: String,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val categoryIcons = listOf(
        "comida", "transporte", "entretenimiento", "salud",
        "ropa", "hogar", "educación", "trabajo",
        "servicios", "viajes", "belleza", "deportes",
        "tecnología", "libros", "mascotas", "familia",
        "inversiones", "ahorro", "emergencia", "otros"
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Seleccionar Icono",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(categoryIcons) { iconName ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    if (iconName == selectedIcon) 
                                        CashFlowPrimary.copy(alpha = 0.2f)
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onIconSelected(iconName) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIconMapper.getIconFromCategoryName(iconName),
                                contentDescription = iconName,
                                tint = if (iconName == selectedIcon) 
                                    CashFlowPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}
