package com.example.admin_ingresos.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.admin_ingresos.ui.components.ThemedAlertDialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.data.Category
import androidx.compose.foundation.border
import com.example.admin_ingresos.ui.theme.AccentVibrantStart

@Composable
fun AddEditCategoryDialogFull(
    initial: Category?,
    allCategories: List<Category>,
    onConfirm: (Category) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var color by remember { mutableStateOf(initial?.color ?: "#4CAF50") }
    var type by remember { mutableStateOf("Ingreso") } // Si no tienes el campo type, usa un valor fijo o elimina
    var icon by remember { mutableStateOf(initial?.icon ?: "default") }
    var error by remember { mutableStateOf<String?>(null) }

    ThemedAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null || initial.id == 0) "Agregar categoría" else "Editar categoría") },
        text = {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = if (allCategories.any { c -> c.name.equals(it, true) && c.id != (initial?.id ?: 0) }) "Ya existe una categoría con ese nombre" else null
                    },
                    label = { Text("Nombre") },
                    isError = error != null,
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Color:", Modifier.width(48.dp))
                    ColorPickerCircle(color = color, onColorSelected = { color = it })
                }
                Spacer(Modifier.height(8.dp))
                // Si tienes el campo type, descomenta esto. Si no, elimina este bloque.
                // Row(verticalAlignment = Alignment.CenterVertically) {
                //     Text("Tipo:", Modifier.width(48.dp))
                //     SegmentedButton(
                //         options = listOf("Ingreso", "Gasto"),
                //         selected = type,
                //         onSelected = { type = it }
                //     )
                // }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Icono:", Modifier.width(48.dp))
                    Icon(Icons.Default.Category, contentDescription = null, tint = Color(android.graphics.Color.parseColor(color)), modifier = Modifier.size(28.dp))
                    // Aquí podrías agregar un selector de iconos personalizado
                }
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && error == null) {
                        onConfirm(Category(initial?.id ?: 0, name.trim(), icon, color))
                    }
                },
                enabled = name.isNotBlank() && error == null,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)) { Text("Cancelar") }
        }
    )
}

@Composable
fun ColorPickerCircle(color: String, onColorSelected: (String) -> Unit) {
    val colors = listOf("#4CAF50", "#E57373", "#FFD600", "#2196F3", "#9C27B0", "#FF9800", "#607D8B")
    Row {
        colors.forEach {
            Box(
                Modifier
                    .size(28.dp)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(it)))
                    .border(
                        width = if (it == color) 2.dp else 1.dp,
                        color = if (it == color) MaterialTheme.colorScheme.primary else Color.Gray,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(it) }
            )
        }
    }
}

@Composable
fun SegmentedButton(options: List<String>, selected: String, onSelected: (String) -> Unit) {
    Row {
        options.forEach { option ->
            Button(
                onClick = { onSelected(option) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (selected == option) Color.White else MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text(option)
            }
        }
    }
}

@Composable
fun CategoryAddEditDialogShared(
    initial: com.example.admin_ingresos.data.Category,
    onConfirm: (com.example.admin_ingresos.data.Category) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var color by remember { mutableStateOf(initial.color) }
    var icon by remember { mutableStateOf(initial.icon) }
    val isEdit = initial.id != 0

    ThemedAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Editar Categoría" else "Agregar Categoría") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("Color", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    val colorOptions = listOf("#4CAF50", "#2196F3", "#FF9800", "#E91E63", "#9C27B0", "#F44336", "#00BCD4", "#FFEB3B")
                    colorOptions.forEach { option ->
                        val selected = color.equals(option, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(option)))
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) com.example.admin_ingresos.ui.theme.AccentVibrantStart else Color.LightGray,
                                    shape = CircleShape
                                )
                                .clickable { color = option }
                        )
                    }
                }
                Text("Icono", style = MaterialTheme.typography.labelMedium)
                var iconMenuExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(
                        onClick = { iconMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        val iconOptions = com.example.admin_ingresos.ui.icons.LucideIconMapper.getAvailableCategoryIcons()
                        val selectedIconOption = iconOptions.find { it.name == icon }
                        if (selectedIconOption != null) {
                            val iconVector = com.example.admin_ingresos.ui.icons.LucideIconMapper.getIconFromEmoji(selectedIconOption.icon)
                            Icon(iconVector, null, tint = MaterialTheme.colorScheme.primary)
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
                            .background(com.example.admin_ingresos.ui.components.resolvedMenuContainerColor())
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        com.example.admin_ingresos.ui.icons.LucideIconMapper.getAvailableCategoryIcons().forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.description) },
                                onClick = {
                                    icon = option.name
                                    iconMenuExpanded = false
                                },
                                leadingIcon = {
                                    val iconVector = com.example.admin_ingresos.ui.icons.LucideIconMapper.getIconFromEmoji(option.icon)
                                    Icon(iconVector, null, tint = MaterialTheme.colorScheme.primary)
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
                    val updatedCategory = initial.copy(name = name, color = color, icon = icon)
                    onConfirm(updatedCategory)
                },
                enabled = name.isNotBlank() && color.isNotBlank() && icon.isNotBlank()
            ) { Text(if (isEdit) "Guardar" else "Agregar") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancelar") } },
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.border(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.2f),
            shape = RoundedCornerShape(28.dp)
        )
    )
}
