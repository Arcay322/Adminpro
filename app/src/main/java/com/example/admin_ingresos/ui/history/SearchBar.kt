package com.example.admin_ingresos.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.data.TransactionFilter
import com.example.admin_ingresos.data.FilterPreset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    searchSuggestions: List<String>,
    currentFilter: TransactionFilter,
    filterPresets: List<FilterPreset>,
    onFilterClick: () -> Unit,
    onClearFilters: () -> Unit,
    onPresetSelected: (FilterPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearchActive by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
    Column(modifier = modifier) {
        // Search input with filter button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text("Buscar transacciones...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(
                            onClick = {
                                onSearchQueryChanged("")
                                focusManager.clearFocus()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar búsqueda"
                            )
                        }
                    }
                } else null,
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { focusState ->
                        isSearchActive = focusState.isFocused && searchSuggestions.isNotEmpty()
                    }
            )
            
            // Filter button with badge
            Box {
                IconButton(
                    onClick = onFilterClick
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filtros",
                        tint = if (currentFilter.getActiveFilterCount() > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                // Filter count badge
                if (currentFilter.getActiveFilterCount() > 0) {
                    Badge(
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = currentFilter.getActiveFilterCount().toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
        
        // Active filters chips
        if (currentFilter.getActiveFilterCount() > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                // Date range chip
                currentFilter.dateRange?.let { dateRange ->
                    item {
                        FilterChip(
                            onClick = { },
                            label = { Text("Fecha personalizada") },
                            selected = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        onClearFilters()
                                    },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Quitar filtro",
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        )
                    }
                }
                
                // Transaction types chips
                currentFilter.transactionTypes.forEach { type ->
                    item {
                        FilterChip(
                            onClick = { },
                            label = { Text(type) },
                            selected = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        // Remove this specific filter
                                    },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Quitar filtro",
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        )
                    }
                }
                
                // Categories count chip
                if (currentFilter.categories.isNotEmpty()) {
                    item {
                        FilterChip(
                            onClick = { },
                            label = { Text("${currentFilter.categories.size} categoría(s)") },
                            selected = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        // Remove categories filter
                                    },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Quitar filtro",
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        )
                    }
                }
                
                // Payment methods count chip
                if (currentFilter.paymentMethods.isNotEmpty()) {
                    item {
                        FilterChip(
                            onClick = { },
                            label = { Text("${currentFilter.paymentMethods.size} método(s) de pago") },
                            selected = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        // Remove payment methods filter
                                    },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Quitar filtro",
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        )
                    }
                }
                
                // Amount range chip
                currentFilter.amountRange?.let { amountRange ->
                    item {
                        FilterChip(
                            onClick = { },
                            label = { 
                                Text("$${String.format("%.0f", amountRange.minAmount)} - $${String.format("%.0f", amountRange.maxAmount)}") 
                            },
                            selected = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        // Remove amount range filter
                                    },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Quitar filtro",
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        )
                    }
                }
                
                // Clear all filters chip
                item {
                    AssistChip(
                        onClick = onClearFilters,
                        label = { Text("Limpiar todo") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
        
        // Filter presets
        if (currentFilter.isEmpty() && searchQuery.isBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Filtros rápidos",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(filterPresets) { preset ->
                    AssistChip(
                        onClick = { onPresetSelected(preset) },
                        label = { Text(preset.name) }
                    )
                }
            }
        }
        
        // Search suggestions dropdown
        if (isSearchActive && searchSuggestions.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Sugerencias",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    
                    searchSuggestions.take(5).forEach { suggestion ->
                        TextButton(
                            onClick = {
                                onSearchQueryChanged(suggestion)
                                focusManager.clearFocus()
                                isSearchActive = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}