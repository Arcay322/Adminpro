package com.example.admin_ingresos.ui.savings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.admin_ingresos.viewmodel.SavingsGoalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingsGoalScreen(
    onGoalAdded: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { com.example.admin_ingresos.AppDatabaseProvider.getDatabase(context) }
    val viewModel: SavingsGoalViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SavingsGoalViewModel(db) as T
        }
    })

    var name by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var selectedIconName by remember { mutableStateOf("Landmark") }
    var color by remember { mutableStateOf("#4CAF50") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(0) }
    var showError by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Nueva Meta de Ahorro", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = targetAmount,
            onValueChange = { targetAmount = it },
            label = { Text("Monto objetivo") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Text("Icono:", style = MaterialTheme.typography.bodyMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val iconNames = listOf(
                "Landmark", "House", "Briefcase", "GraduationCap", "HeartPulse", "Film", "Shirt", "Gift", "Luggage", "Zap", "ShoppingCart", "Dog", "Dumbbell", "Music", "Book", "Coffee", "Gamepad2", "Shield"
            )
            iconNames.forEach { iconName ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (selectedIconName == iconName) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { selectedIconName = iconName },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = com.example.admin_ingresos.ui.icons.LucideIconMapper::class.java.getMethod("getNavigationIcon", String::class.java).invoke(com.example.admin_ingresos.ui.icons.LucideIconMapper, iconName) as androidx.compose.ui.graphics.vector.ImageVector, contentDescription = iconName, modifier = Modifier.size(28.dp))
                    }
                    Text(iconName, fontSize = 10.sp)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Color:", style = MaterialTheme.typography.bodyMedium)
        com.example.admin_ingresos.ui.category.ColorPickerCircle(color = color, onColorSelected = { color = it })
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Prioridad:", modifier = Modifier.padding(end = 8.dp))
            DropdownMenu(
                expanded = false,
                onDismissRequest = {},
                modifier = Modifier.width(120.dp)
            ) {
                DropdownMenuItem(text = { Text("Baja") }, onClick = { priority = 0 })
                DropdownMenuItem(text = { Text("Media") }, onClick = { priority = 1 })
                DropdownMenuItem(text = { Text("Alta") }, onClick = { priority = 2 })
            }
        }
        if (showError) {
            Text("Completa todos los campos y usa un monto válido.", color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = {
                val amount = targetAmount.toDoubleOrNull()
                if (name.isNotBlank() && amount != null && selectedIconName.isNotBlank()) {
                    viewModel.addSavingsGoal(
                        name = name,
                        targetAmount = amount,
                        emoji = selectedIconName,
                        color = color,
                        description = if (description.isBlank()) null else description,
                        priority = priority
                    )
                    showError = false
                } else {
                    showError = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar Meta")
        }
        if (uiState.message != null) {
            Text(uiState.message!!, color = MaterialTheme.colorScheme.primary)
            LaunchedEffect(uiState.message) {
                onGoalAdded()
                viewModel.clearMessage()
            }
        }
        if (uiState.error != null) {
            Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
        }
    }
}
