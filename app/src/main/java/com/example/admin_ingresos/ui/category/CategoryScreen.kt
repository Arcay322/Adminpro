package com.example.admin_ingresos.ui.category

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.data.Category
import com.example.admin_ingresos.data.CategoryType
import com.example.admin_ingresos.ui.components.GlassCard
import com.example.admin_ingresos.ui.components.GlassmorphismScreen
import com.example.admin_ingresos.ui.icons.LucideIconMapper
import com.example.admin_ingresos.ui.theme.AccentVibrantStart
import com.example.admin_ingresos.ui.theme.ExpenseRed
import com.example.admin_ingresos.ui.theme.GlassWhiteSubtle
import com.example.admin_ingresos.ui.theme.TextPrimary
import com.example.admin_ingresos.ui.theme.TextSecondary
import com.example.admin_ingresos.ui.dashboard.SavingsGoalCard
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.admin_ingresos.AppDatabaseProvider
import com.example.admin_ingresos.viewmodel.SavingsGoalViewModel
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    viewModel: CategoryViewModel,
    // Parámetro para manejar la navegación al tocar una categoría
    onNavigateToCategoryDetail: (Int) -> Unit,
    onNavigateToSavingsDetail: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvents.collectLatest { msg ->
            snackbarHostState.showSnackbar(message = msg)
        }
    }

    GlassmorphismScreen {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                val layoutDir = androidx.compose.ui.platform.LocalLayoutDirection.current

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = paddingValues.calculateStartPadding(layoutDir),
                            end = paddingValues.calculateEndPadding(layoutDir),
                            bottom = paddingValues.calculateBottomPadding()
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Header (styled to match Reports title)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = LucideIconMapper.getNavigationIcon("list"),
                                contentDescription = "Categorías",
                                tint = AccentVibrantStart,
                                modifier = Modifier.size(28.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                                Text(
                                    text = "Categorías",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Mantén presionado para reordenar",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.showAddEditDialog(Category(name = "", type = uiState.selectedTab)) }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Agregar categoría",
                                tint = AccentVibrantStart
                            )
                        }
                    }

                    // Buscador
                    TextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        label = { Text("Buscar categoría", color = TextSecondary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
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
                    Spacer(Modifier.height(8.dp))

                    // Pestañas Estilo Píldora con Colores + botón Ahorros
                    val context = LocalContext.current
                    val db = remember { AppDatabaseProvider.getDatabase(context) }
                    val savingsGoalViewModel: SavingsGoalViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return SavingsGoalViewModel(db) as T
                        }
                    })

                    var showSavingsSection by remember { mutableStateOf(false) }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val tabs = listOf(CategoryType.GASTO, CategoryType.INGRESO)
                        val selectedTabIndex = tabs.indexOf(uiState.selectedTab)

                        TabRow(
                            selectedTabIndex = selectedTabIndex,
                            modifier = Modifier.weight(1f),
                            containerColor = Color.Transparent,
                            indicator = {}, // Sin indicador
                            divider = {} // Sin divisor
                        ) {
                            tabs.forEachIndexed { index, tabType ->
                                val selected = selectedTabIndex == index && !showSavingsSection
                                val pillColor = if (tabType == CategoryType.GASTO) ExpenseRed else AccentVibrantStart

                                Tab(
                                    selected = selected,
                                    onClick = {
                                        showSavingsSection = false
                                        viewModel.onTabSelected(tabType)
                                    },
                                    text = {
                                        val title = if (tabType == CategoryType.GASTO) "Gastos" else "Ingresos"
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .background(
                                                    if (selected) pillColor else Color.Transparent
                                                )
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = title,
                                                color = if (selected) TextPrimary else TextSecondary
                                            )
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Blue savings button
                        Button(
                            onClick = { showSavingsSection = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688)),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Icon(LucideIconMapper.getNavigationIcon("PiggyBank"), contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Ahorros", color = Color.White)
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    // Contenido principal
                    Column(modifier = Modifier.weight(1f)) {
                        if (showSavingsSection) {
                            SavingsGoalsSummary(savingsGoalViewModel = savingsGoalViewModel, onOpen = { goalId -> onNavigateToSavingsDetail(goalId) })
                        } else if (uiState.categories.isEmpty() && uiState.searchQuery.isBlank()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = LucideIconMapper.getNavigationIcon("Archive"),
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = TextSecondary
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Text("No hay categorías aquí.", color = TextSecondary)
                                    Text("¡Crea una nueva para empezar!", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        } else {
                            val state = rememberReorderableLazyGridState(
                                onMove = { from, to ->
                                    val reordered = uiState.categories.toMutableList().apply {
                                        add(to.index, removeAt(from.index))
                                    }
                                    viewModel.reorderCategories(reordered)
                                }
                            )
                            LazyVerticalGrid(
                                state = state.gridState,
                                columns = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .reorderable(state),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(uiState.categories, key = { it.id }) { category ->
                                    ReorderableItem(state, key = category.id) { isDragging ->
                                        val scale by animateFloatAsState(if (isDragging) 1.07f else 1f, label = "")
                                        val elevation by animateDpAsState(if (isDragging) 16.dp else 2.dp, label = "")

                                        val cardColor by remember(category.color) {
                                            mutableStateOf(Color(android.graphics.Color.parseColor(category.color)))
                                        }
                                        val iconVector by remember(category.icon) {
                                            val iconOption = LucideIconMapper.getAvailableCategoryIcons().find { it.name == category.icon }
                                            val vector = if (iconOption != null) {
                                                LucideIconMapper.getIconFromEmoji(iconOption.icon)
                                            } else {
                                                LucideIconMapper.getCategoryIcon(category)
                                            }
                                            mutableStateOf(vector)
                                        }

                                        // Make the category card border slightly thicker and brighter
                                        val brightBorder = androidx.compose.ui.graphics.lerp(cardColor.copy(alpha = 0.34f), Color.White, 0.12f)
                                        GlassCard(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1.05f)
                                                .graphicsLayer {
                                                    scaleX = scale
                                                    scaleY = scale
                                                }
                                                .detectReorderAfterLongPress(state)
                                                .clickable { onNavigateToCategoryDetail(category.id) },
                                            cornerRadius = 20.dp,
                                            backgroundColor = cardColor.copy(alpha = 0.10f),
                                            borderColor = brightBorder,
                                            borderWidth = 1.5.dp,
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize()) {

                                                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                                                    var menuExpanded by remember { mutableStateOf(false) }

                                                    IconButton(onClick = { menuExpanded = true }) {
                                                        Icon(LucideIconMapper.Navigation.more, "Más opciones", tint = TextSecondary, modifier = Modifier.size(20.dp))
                                                    }

                                                    DropdownMenu(
                                                        expanded = menuExpanded,
                                                        onDismissRequest = { menuExpanded = false },
                                                        modifier = Modifier
                                                            .background(Color(0xFF2A2D32))
                                                            .clip(RoundedCornerShape(12.dp))
                                                    ) {
                                                        DropdownMenuItem(
                                                            text = { Text("Editar", color = AccentVibrantStart) },
                                                            onClick = {
                                                                menuExpanded = false
                                                                viewModel.showAddEditDialog(category)
                                                            },
                                                            leadingIcon = { Icon(LucideIconMapper.Navigation.edit, null, tint = AccentVibrantStart) }
                                                        )
                                                        DropdownMenuItem(
                                                            text = { Text("Archivar", color = ExpenseRed) },
                                                            onClick = {
                                                                menuExpanded = false
                                                                viewModel.showArchiveDialog(category)
                                                            },
                                                            leadingIcon = { Icon(LucideIconMapper.getNavigationIcon("Archive"), null, tint = ExpenseRed) }
                                                        )
                                                    }
                                                }

                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(12.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    // Use the category's stored color for background and derive an icon tint
                                                    val bgColor = try { Color(android.graphics.Color.parseColor(category.color)) } catch (_: Exception) { com.example.admin_ingresos.ui.theme.getCategoryColor(category.name) }
                                                    // Use full category color for the icon to match dashboard intensity
                                                    val iconTint = bgColor

                                                    Box(
                                                        Modifier
                                                            .size(42.dp)
                                                            .clip(CircleShape)
                                                            .background(bgColor.copy(alpha = 0.18f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(iconVector, null, tint = iconTint, modifier = Modifier.size(22.dp))
                                                    }
                                                    Spacer(Modifier.height(8.dp))
                                                    Text(
                                                        text = category.name,
                                                        fontWeight = FontWeight.Bold,
                                                        color = com.example.admin_ingresos.ui.theme.TextPrimary,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier.padding(horizontal = 4.dp)
                                                    )

                                                    Spacer(Modifier.weight(1f))

                                                    val stats = uiState.statsMap[category.id]
                                                    Text(
                                                        text = "${stats?.transactionCount ?: 0} transacciones",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = com.example.admin_ingresos.ui.theme.TextSecondary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )

                                                    val amountColor = if (category.type == CategoryType.GASTO) ExpenseRed else AccentVibrantStart
                                                    Text(
                                                        text = "$${String.format("%.2f", stats?.totalAmount ?: 0.0)}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = amountColor, // Color aplicado
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

                
                }

                // Diálogos con Scrim y Estilo Mejorado
                if (uiState.showAddEditDialog != null) {
                    DialogScrim(onDismiss = { viewModel.hideAddEditDialog() }) {
                        CategoryAddEditDialog(
                            category = uiState.showAddEditDialog!!,
                            viewModel = viewModel
                        )
                    }
                }

                if (uiState.showArchiveDialog != null) {
                    val categoryToArchive = uiState.showArchiveDialog!!
                    DialogScrim(onDismiss = { viewModel.hideArchiveDialog() }) {
                        AlertDialog(
                            onDismissRequest = { viewModel.hideArchiveDialog() },
                            title = { Text("Archivar categoría") },
                            text = { Text("¿Seguro que deseas archivar la categoría '${categoryToArchive.name}'?") },
                            confirmButton = {
                                Button(
                                    onClick = { viewModel.archiveCategory(categoryToArchive) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                                ) { Text("Archivar") }
                            },
                            dismissButton = { OutlinedButton(onClick = { viewModel.hideArchiveDialog() }) { Text("Cancelar") } },
                            shape = RoundedCornerShape(28.dp),
                            containerColor = Color(0xFF2A2D32),
                            modifier = Modifier.border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(28.dp)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogScrim(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )
        content()
    }
}

@Composable
private fun CategoryAddEditDialog(category: Category, viewModel: CategoryViewModel) {
    var name by remember(category.id) { mutableStateOf(category.name) }
    var color by remember(category.id) { mutableStateOf(category.color) }
    var icon by remember(category.id) { mutableStateOf(category.icon) }
    val isEdit = category.id != 0

    AlertDialog(
        onDismissRequest = { viewModel.hideAddEditDialog() },
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
                                    color = if (selected) AccentVibrantStart else Color.LightGray,
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val iconOptions = LucideIconMapper.getAvailableCategoryIcons()
                        val selectedIconOption = iconOptions.find { it.name == icon }
                        if (selectedIconOption != null) {
                            val iconVector = LucideIconMapper.getIconFromEmoji(selectedIconOption.icon)
                            Icon(iconVector, null, tint = AccentVibrantStart)
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
                        LucideIconMapper.getAvailableCategoryIcons().forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.description) },
                                onClick = {
                                    icon = option.name
                                    iconMenuExpanded = false
                                },
                                leadingIcon = {
                                    val iconVector = LucideIconMapper.getIconFromEmoji(option.icon)
                                    Icon(iconVector, null, tint = AccentVibrantStart)
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
                    val updatedCategory = category.copy(name = name, color = color, icon = icon)
                    if (isEdit) viewModel.updateCategory(updatedCategory) else viewModel.addCategory(updatedCategory)
                    viewModel.hideAddEditDialog()
                },
                enabled = name.isNotBlank() && color.isNotBlank() && icon.isNotBlank()
            ) { Text(if (isEdit) "Guardar" else "Agregar") }
        },
        dismissButton = { OutlinedButton(onClick = { viewModel.hideAddEditDialog() }) { Text("Cancelar") } },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color(0xFF2A2D32),
        modifier = Modifier.border(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.2f),
            shape = RoundedCornerShape(28.dp)
        )
    )
}

@Composable
private fun SavingsGoalsSummary(savingsGoalViewModel: SavingsGoalViewModel, onOpen: (Long) -> Unit) {
    val savingsGoals by savingsGoalViewModel.savingsGoals.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    // Obtain database for transaction queries
    val context = LocalContext.current
    val db = remember { AppDatabaseProvider.getDatabase(context) }

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Metas de Ahorro", fontWeight = FontWeight.Bold, color = TextPrimary)
            Button(onClick = { showAdd = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688))) {
                Icon(LucideIconMapper.Navigation.add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nueva", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (savingsGoals.isEmpty()) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No tienes metas de ahorro", color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Crea una meta para empezar a ahorrar", color = TextSecondary)
            }
        } else {
            // Show savings goals in a two-column grid so cards match size of other category cards
            val gridState = rememberReorderableLazyGridState(onMove = { _, _ -> })
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState.gridState,
                modifier = Modifier
                    .fillMaxSize()
                    .reorderable(gridState),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(savingsGoals) { goal ->
                    val cardColor = try { Color(android.graphics.Color.parseColor(goal.color)) } catch (_: Exception) { com.example.admin_ingresos.ui.theme.getCategoryColor(goal.name) }
                    val brightBorder = androidx.compose.ui.graphics.lerp(cardColor.copy(alpha = 0.34f), Color.White, 0.12f)

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.05f)
                            .clickable { onOpen(goal.id) },
                        cornerRadius = 20.dp,
                        backgroundColor = cardColor.copy(alpha = 0.10f),
                        borderColor = brightBorder,
                        borderWidth = 1.5.dp,
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                                var menuExpanded by remember { mutableStateOf(false) }

                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(LucideIconMapper.Navigation.more, "Más opciones", tint = TextSecondary, modifier = Modifier.size(20.dp))
                                }

                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false },
                                    modifier = Modifier
                                        .background(Color(0xFF2A2D32))
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Editar", color = AccentVibrantStart) },
                                        onClick = {
                                            menuExpanded = false
                                            // Could open an edit savings dialog here
                                        },
                                        leadingIcon = { Icon(LucideIconMapper.Navigation.edit, null, tint = AccentVibrantStart) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Eliminar", color = ExpenseRed) },
                                        onClick = {
                                            menuExpanded = false
                                            savingsGoalViewModel.deleteSavingsGoal(goal)
                                        },
                                        leadingIcon = { Icon(LucideIconMapper.Navigation.delete, null, tint = ExpenseRed) }
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(cardColor.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(LucideIconMapper.getSavingsGoalIcon(goal.emoji), null, tint = cardColor, modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = goal.name,
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.admin_ingresos.ui.theme.TextPrimary,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )

                                Spacer(Modifier.weight(1f))

                                // Collect transactions for this goal to show accurate count and sum
                                val txDao = db.transactionDao()
                                val txList by txDao.getTransactionsByGoalIdFlow(goal.id).collectAsState(initial = emptyList())
                                val txCount = txList.size
                                val txSum = txList.sumOf { it.amount }

                                Text(
                                    text = "${txCount} transacciones",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = com.example.admin_ingresos.ui.theme.TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = "$${String.format("%.2f", txSum)}",
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

        if (showAdd) {
            com.example.admin_ingresos.ui.savings.AddEditSavingsGoalDialog(
                onConfirm = { name, amount, icon, color ->
                    savingsGoalViewModel.addSavingsGoal(name = name, targetAmount = amount, emoji = icon, color = color)
                    showAdd = false
                },
                onDismiss = { showAdd = false }
            )
        }
    }
}