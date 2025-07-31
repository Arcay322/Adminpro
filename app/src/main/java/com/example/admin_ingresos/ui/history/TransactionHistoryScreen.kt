package com.example.admin_ingresos.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.R
import com.example.admin_ingresos.data.Transaction

@Composable
fun TransactionHistoryScreen(transactions: List<Transaction>) {
    var search by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Todos") }
    var selectedCategory by remember { mutableStateOf("") }
    val filtered = transactions.filter {
        (selectedType == "Todos" || it.type == selectedType) &&
        (selectedCategory.isBlank() || it.description.contains(selectedCategory, ignoreCase = true)) &&
        (search.isBlank() || it.description.contains(search, ignoreCase = true))
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.bg_slate_50))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Historial de Transacciones", style = MaterialTheme.typography.titleLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Buscar") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            DropdownMenuBox(
                options = listOf("Todos", "Ingreso", "Gasto"),
                selected = selectedType,
                onSelected = { selectedType = it },
                label = "Tipo"
            )
        }
        filtered.forEach { tx ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = tx.type, color = if (tx.type == "Ingreso") colorResource(id = R.color.green_600) else colorResource(id = R.color.red_500), style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = tx.description, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = tx.amount.toString(), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun DropdownMenuBox(options: List<String>, selected: String, onSelected: (String) -> Unit, label: String) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text("$label: $selected")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = {
                    onSelected(it)
                    expanded = false
                })
            }
        }
    }
}
