package com.example.admin_ingresos.ui.savings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.ui.icons.LucideIconMapper
import com.example.admin_ingresos.ui.theme.*
import com.example.admin_ingresos.ui.components.ThemedAlertDialog

// Lista de las claves de los iconos para las metas
private val savingsIconKeys: List<String> = listOf(
    "emergency", "car", "house", "travel", "education", "retirement",
    "tech", "wedding", "business", "investment", "repairs", "gift",
    "lock", "other"
)

// Mapa para traducir las claves a español
private val iconSpanishNames: Map<String, String> = mapOf(
    "emergency" to "Fondo de Emergencia",
    "car" to "Comprar Auto",
    "house" to "Inicial de Vivienda",
    "travel" to "Viajes y Vacaciones",
    "education" to "Educación",
    "retirement" to "Jubilación / Retiro",
    "tech" to "Tecnología",
    "wedding" to "Boda / Evento Especial",
    "business" to "Iniciar Negocio",
    "investment" to "Inversiones",
    "repairs" to "Reparaciones",
    "gift" to "Regalo Especial",
    "lock" to "Caja Fuerte / Ahorro Seguro",
    "other" to "Otra Meta"
)

@Composable
fun AddEditSavingsGoalDialog(
    initialName: String = "",
    initialAmount: String = "",
    initialColor: String = "#4CAF50",
    initialIcon: String = "",
    onConfirm: (name: String, amount: Double, icon: String, color: String) -> Unit,
    onDismiss: () -> Unit
) {
    val isEdit = initialName.isNotBlank()

    var name by remember { mutableStateOf(initialName) }
    var amount by remember { mutableStateOf(initialAmount) }
    var color by remember { mutableStateOf(initialColor) }
    var iconKey by remember { mutableStateOf(initialIcon) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }

    ThemedAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Editar meta de ahorro" else "Agregar meta de ahorro", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text("Nombre de la meta") },
                    isError = nameError != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        if (it.matches(Regex("^\\d*\\.?\\d*\$"))) {
                            amount = it; amountError = null
                        }
                    },
                    label = { Text("Monto objetivo") },
                    leadingIcon = { Text("$", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = amountError != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Color", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val colorOptions = listOf("#4CAF50", "#2196F3", "#FF9800", "#E91E63", "#9C27B0", "#F44336", "#00BCD4", "#FFEB3B")
                    items(colorOptions) { option ->
                        val selected = color.equals(option, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(option)))
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) AccentVibrantStart else Color.White.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .clickable { color = option }
                        )
                    }
                }
                Text("Icono", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                var iconMenuExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(
                        onClick = { iconMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        if (iconKey.isNotBlank()) {
                            // CAMBIO AQUÍ: Usamos la nueva función
                            Icon(LucideIconMapper.getSavingsGoalIcon(iconKey), null, tint = AccentVibrantStart)
                            Spacer(Modifier.width(8.dp))
                            Text(iconSpanishNames[iconKey] ?: iconKey)
                        } else {
                            Text("Seleccionar icono")
                        }
                    }
                    DropdownMenu(
                        expanded = iconMenuExpanded,
                        onDismissRequest = { iconMenuExpanded = false },
                        modifier = Modifier.width(260.dp).background(Color(0xFF33363B))
                    ) {
                        savingsIconKeys.forEach { key ->
                            DropdownMenuItem(
                                text = { Text(iconSpanishNames[key] ?: key, color = TextPrimary) },
                                onClick = {
                                    iconKey = key
                                    iconMenuExpanded = false
                                },
                                leadingIcon = {
                                    // CAMBIO AQUÍ: Usamos la nueva función
                                    Icon(LucideIconMapper.getSavingsGoalIcon(key), null, tint = AccentVibrantStart)
                                }
                            )
                        }
                    }
                }
                if (nameError != null) {
                    Text(nameError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                if (amountError != null) {
                    Text(amountError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
    },
        confirmButton = {
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (name.isBlank()) {
                        nameError = "El nombre no puede estar vacío."
                    } else if (amount.isBlank() || amountValue == null || amountValue <= 0) {
                        amountError = "Ingresa un monto válido."
                    } else if (iconKey.isBlank()) {
                        // Opcional: Podrías mostrar un error si no se selecciona un ícono
                    } else {
                        onConfirm(name.trim(), amountValue, iconKey, color)
                    }
                },
                enabled = name.isNotBlank() && amount.isNotBlank() && iconKey.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentVibrantStart)
            ) {
                Text(if (isEdit) "Guardar" else "Agregar")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Text("Cancelar", color = TextPrimary)
            }
        },
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.border(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.2f),
            shape = RoundedCornerShape(28.dp)
        )
    )
}