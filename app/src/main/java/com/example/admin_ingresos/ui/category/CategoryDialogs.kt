package com.example.admin_ingresos.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
                enabled = name.isNotBlank() && error == null
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
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
