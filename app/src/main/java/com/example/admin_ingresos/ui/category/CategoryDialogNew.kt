
package com.example.admin_ingresos.ui.category

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.data.Category
import com.example.admin_ingresos.ui.components.GlassCard

@Composable
fun CategoryDialogNew(
    category: Category?,
    onDismiss: () -> Unit,
    onSave: (String, String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var icon by remember { mutableStateOf(category?.icon ?: "") }
    var isFavorite by remember { mutableStateOf(category?.isFavorite ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "Añadir Categoría" else "Editar Categoría") },
        text = {
            GlassCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = icon,
                        onValueChange = { icon = it },
                        label = { Text("Icono (Emoji)") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(
                            checked = isFavorite,
                            onCheckedChange = { isFavorite = it }
                        )
                        Text("Favorito")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, icon, isFavorite) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
