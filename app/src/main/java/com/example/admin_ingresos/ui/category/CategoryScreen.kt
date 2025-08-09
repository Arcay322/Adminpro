package com.example.admin_ingresos.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

import com.example.admin_ingresos.data.Category
import com.example.admin_ingresos.ui.components.GlassCard
import com.example.admin_ingresos.ui.components.GlassmorphismScreen
import com.example.admin_ingresos.ui.icons.LucideIconMapper
import com.example.admin_ingresos.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    categories: List<Category>,
    onAddCategory: (Category) -> Unit,
    onEditCategory: (Category) -> Unit,
    onDeleteCategory: (Category) -> Unit,
    // onReorderCategory ya no es necesario si no hay botones para ello
    // onReorderCategory: (from: Int, to: Int) -> Unit,
    getTransactionCount: (Int) -> Int,
    getTotalAmount: (Int) -> Double
) {
    var showAddEditDialog by remember { mutableStateOf<Category?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Category?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredCategories = categories.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    GlassmorphismScreen {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Categorías",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Gestiona y personaliza tus categorías",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = { showAddEditDialog = Category(0, "", "default", "#4CAF50") }) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar categoría", tint = AccentVibrantStart)
                }
            }

            // Search
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar categoría", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = {
                    Icon(
                        imageVector = LucideIconMapper.Navigation.search,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = GlassWhiteSubtle,
                    unfocusedContainerColor = GlassWhiteSubtle,
                    cursorColor = AccentVibrantStart,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Grid de categorías (versión simplificada sin drag & drop)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredCategories, key = { it.id }) { category ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.05f),
                        cornerRadius = 20.dp,
                        backgroundColor = Color(android.graphics.Color.parseColor(category.color)).copy(alpha = 0.10f),
                        borderColor = Color(android.graphics.Color.parseColor(category.color)).copy(alpha = 0.20f),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            // Menú de 3 puntos en la esquina superior derecha
                            var menuExpanded by remember { mutableStateOf(false) }
                            IconButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    imageVector = LucideIconMapper.Navigation.more,
                                    contentDescription = "Más opciones",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier.background(GlassWhiteStrong)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Editar", color = AccentVibrantStart) },
                                    onClick = {
                                        menuExpanded = false
                                        showAddEditDialog = category
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = LucideIconMapper.Navigation.edit,
                                            contentDescription = null,
                                            tint = AccentVibrantStart
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Eliminar", color = ExpenseRed) },
                                    onClick = {
                                        menuExpanded = false
                                        showDeleteDialog = category
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = LucideIconMapper.Navigation.delete,
                                            contentDescription = null,
                                            tint = ExpenseRed
                                        )
                                    }
                                )
                            }

                            // Contenido de la tarjeta
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 2.dp, bottom = 2.dp),
                                verticalArrangement = Arrangement.SpaceEvenly,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Icono y nombre
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color(android.graphics.Color.parseColor(category.color)).copy(alpha = 0.85f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val iconOption = LucideIconMapper.getAvailableCategoryIcons().find { it.name == category.icon }
                                        val iconVector = if (iconOption != null) LucideIconMapper.getIconFromEmoji(iconOption.icon) else LucideIconMapper.getCategoryIcon(category)
                                        Icon(
                                            imageVector = iconVector,
                                            contentDescription = null,
                                            tint = TextPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = category.name,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }
                                // Info de transacciones y total
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${getTransactionCount(category.id)} transacciones",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "$${String.format("%.2f", getTotalAmount(category.id))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AccentVibrantStart,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Diálogos (sin cambios, pero asegúrate de que usen tus componentes y colores del tema) ---

    // Diálogo funcional para agregar/editar categoría
    showAddEditDialog?.let { category ->
        var name by remember { mutableStateOf(category.name) }
        var color by remember { mutableStateOf(category.color) }
        var icon by remember { mutableStateOf(category.icon) }
        val isEdit = category.id != 0

        // Paleta de colores predefinidos
        val colorOptions = listOf(
            "#4CAF50", "#2196F3", "#FF9800", "#E91E63", "#9C27B0", "#F44336", "#00BCD4", "#FFEB3B"
        )

        // Usar la lista de iconos disponibles de LucideIconMapper
        val iconOptions = LucideIconMapper.getAvailableCategoryIcons()


        var iconMenuExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddEditDialog = null },
            title = { Text(if (isEdit) "Editar Categoría" else "Agregar Categoría") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Selector de color
                    Text("Color", style = MaterialTheme.typography.labelMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        colorOptions.forEach { option ->
                            val selected = color.equals(option, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(option)))
                                    .border(
                                        width = if (selected) 3.dp else 1.dp,
                                        color = if (selected) AccentVibrantStart else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clickable { color = option }
                            )
                        }
                    }
                    // Selector de icono Lucide
                    Text("Icono", style = MaterialTheme.typography.labelMedium)
                    Box {
                        OutlinedButton(
                            onClick = { iconMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val selectedIconOption = iconOptions.find { it.name == icon }
                            if (selectedIconOption != null) {
                                val iconVector = LucideIconMapper.getIconFromEmoji(selectedIconOption.icon)
                                Icon(iconVector, contentDescription = null, tint = AccentVibrantStart)
                                Spacer(Modifier.width(8.dp))
                                Text(selectedIconOption.description)
                            } else {
                                Text("Seleccionar icono")
                            }
                        }
                        DropdownMenu(
                            expanded = iconMenuExpanded,
                            onDismissRequest = { iconMenuExpanded = false },
                            modifier = Modifier.width(260.dp)
                        ) {
                            iconOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.description) },
                                    onClick = {
                                        icon = option.name
                                        iconMenuExpanded = false
                                    },
                                    leadingIcon = {
                                        val iconVector = LucideIconMapper.getIconFromEmoji(option.icon)
                                        Icon(iconVector, contentDescription = null, tint = AccentVibrantStart)
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = category.copy(name = name, color = color, icon = icon)
                        if (isEdit) onEditCategory(updated) else onAddCategory(updated)
                        showAddEditDialog = null
                    },
                    enabled = name.isNotBlank() && color.isNotBlank() && icon.isNotBlank()
                ) { Text(if (isEdit) "Guardar" else "Agregar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddEditDialog = null }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo de eliminación
    showDeleteDialog?.let { category ->
        Dialog(onDismissRequest = { showDeleteDialog = null }) {
            GlassCard(
                modifier = Modifier.padding(24.dp),
                cornerRadius = 24.dp,
                backgroundColor = GlassWhiteStrong
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Eliminar categoría",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "¿Seguro que deseas eliminar la categoría '${category.name}'?",
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteDialog = null },
                            border = BorderStroke(1.dp, TextSecondary)
                        ) {
                            Text("Cancelar", color = TextSecondary)
                        }
                        Button(
                            onClick = {
                                onDeleteCategory(category)
                                showDeleteDialog = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                        ) {
                            Text("Eliminar", color = TextOnAccent)
                        }
                    }
                }
            }
        }
    }
}