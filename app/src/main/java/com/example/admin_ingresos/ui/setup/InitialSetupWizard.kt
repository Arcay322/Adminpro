package com.example.admin_ingresos.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

data class Currency(
    val code: String,
    val name: String,
    val symbol: String,
    val flag: String
)

data class SetupStep(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isCompleted: Boolean = false
)

@Composable
fun InitialSetupWizard(
    onSetupComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var selectedCurrency by remember { mutableStateOf<Currency?>(null) }
    var importSampleData by remember { mutableStateOf(true) }
    var enableNotifications by remember { mutableStateOf(true) }
    var selectedTheme by remember { mutableStateOf("system") }
    
    val scope = rememberCoroutineScope()
    
    val steps = listOf(
        SetupStep("Bienvenido", "Configuremos tu aplicación de finanzas", Icons.Default.Waving),
        SetupStep("Moneda", "Selecciona tu moneda principal", Icons.Default.AttachMoney),
        SetupStep("Datos", "¿Importar datos de ejemplo?", Icons.Default.DataUsage),
        SetupStep("Preferencias", "Configura tus preferencias", Icons.Default.Settings),
        SetupStep("¡Listo!", "Tu aplicación está configurada", Icons.Default.CheckCircle)
    )
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        // Progress indicator
        SetupProgressIndicator(
            currentStep = currentStep,
            totalSteps = steps.size,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Step content
        when (currentStep) {
            0 -> WelcomeStep(
                onNext = { currentStep = 1 }
            )
            1 -> CurrencySelectionStep(
                selectedCurrency = selectedCurrency,
                onCurrencySelected = { selectedCurrency = it },
                onNext = { currentStep = 2 },
                onBack = { currentStep = 0 }
            )
            2 -> SampleDataStep(
                importSampleData = importSampleData,
                onToggleImport = { importSampleData = it },
                onNext = { currentStep = 3 },
                onBack = { currentStep = 1 }
            )
            3 -> PreferencesStep(
                enableNotifications = enableNotifications,
                selectedTheme = selectedTheme,
                onNotificationsToggle = { enableNotifications = it },
                onThemeSelected = { selectedTheme = it },
                onNext = { currentStep = 4 },
                onBack = { currentStep = 2 }
            )
            4 -> CompletionStep(
                onFinish = {
                    scope.launch {
                        // Save all preferences
                        saveSetupPreferences(
                            currency = selectedCurrency,
                            importSampleData = importSampleData,
                            enableNotifications = enableNotifications,
                            theme = selectedTheme
                        )
                        onSetupComplete()
                    }
                }
            )
        }
    }
}

@Composable
fun SetupProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(totalSteps) { step ->
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (step <= currentStep) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = { (currentStep + 1).toFloat() / totalSteps },
            modifier = Modifier.fillMaxWidth(),
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Paso ${currentStep + 1} de $totalSteps",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun WelcomeStep(
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.AccountBalance,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "¡Bienvenido a Admin Ingresos!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Tu aplicación personal para gestionar finanzas de manera inteligente y sencilla.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Características principales:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                FeatureItem(
                    icon = Icons.Default.TrendingUp,
                    text = "Seguimiento de ingresos y gastos"
                )
                FeatureItem(
                    icon = Icons.Default.PieChart,
                    text = "Análisis visual con gráficos"
                )
                FeatureItem(
                    icon = Icons.Default.Category,
                    text = "Organización por categorías"
                )
                FeatureItem(
                    icon = Icons.Default.Savings,
                    text = "Gestión de presupuestos"
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Comenzar configuración")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun FeatureItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun CurrencySelectionStep(
    selectedCurrency: Currency?,
    onCurrencySelected: (Currency) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencies = remember {
        listOf(
            Currency("USD", "Dólar Estadounidense", "$", "🇺🇸"),
            Currency("EUR", "Euro", "€", "🇪🇺"),
            Currency("MXN", "Peso Mexicano", "$", "🇲🇽"),
            Currency("COP", "Peso Colombiano", "$", "🇨🇴"),
            Currency("ARS", "Peso Argentino", "$", "🇦🇷"),
            Currency("CLP", "Peso Chileno", "$", "🇨🇱"),
            Currency("PEN", "Sol Peruano", "S/", "🇵🇪"),
            Currency("BRL", "Real Brasileño", "R$", "🇧🇷")
        )
    }
    
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Selecciona tu moneda",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Esta será la moneda principal para mostrar tus transacciones y reportes.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(currencies) { currency ->
                CurrencyItem(
                    currency = currency,
                    isSelected = selectedCurrency?.code == currency.code,
                    onClick = { onCurrencySelected(currency) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Atrás")
            }
            
            Button(
                onClick = onNext,
                enabled = selectedCurrency != null,
                modifier = Modifier.weight(1f)
            ) {
                Text("Continuar")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun CurrencyItem(
    currency: Currency,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currency.flag,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currency.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${currency.code} • ${currency.symbol}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Seleccionado",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SampleDataStep(
    importSampleData: Boolean,
    onToggleImport: (Boolean) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Datos de ejemplo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "¿Te gustaría importar algunos datos de ejemplo para explorar las funciones de la aplicación?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (importSampleData) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Importar datos de ejemplo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Incluye transacciones, categorías y presupuestos de muestra",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Switch(
                        checked = importSampleData,
                        onCheckedChange = onToggleImport
                    )
                }
                
                if (importSampleData) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Se incluirán:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    FeatureItem(
                        icon = Icons.Default.Receipt,
                        text = "50+ transacciones de ejemplo"
                    )
                    FeatureItem(
                        icon = Icons.Default.Category,
                        text = "Categorías predefinidas"
                    )
                    FeatureItem(
                        icon = Icons.Default.CreditCard,
                        text = "Métodos de pago comunes"
                    )
                    FeatureItem(
                        icon = Icons.Default.Savings,
                        text = "Presupuestos de muestra"
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Atrás")
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f)
            ) {
                Text("Continuar")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun PreferencesStep(
    enableNotifications: Boolean,
    selectedTheme: String,
    onNotificationsToggle: (Boolean) -> Unit,
    onThemeSelected: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Preferencias",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Configura las preferencias básicas de la aplicación.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Notifications preference
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Notificaciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Recibe alertas de presupuestos y recordatorios",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = enableNotifications,
                    onCheckedChange = onNotificationsToggle
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Theme preference
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Tema de la aplicación",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                val themes = listOf(
                    "system" to "Seguir sistema",
                    "light" to "Claro",
                    "dark" to "Oscuro"
                )
                
                themes.forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(value) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedTheme == value,
                            onClick = { onThemeSelected(value) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Atrás")
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f)
            ) {
                Text("Continuar")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun CompletionStep(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "¡Todo listo!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Tu aplicación está configurada y lista para usar. ¡Comienza a gestionar tus finanzas de manera inteligente!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Próximos pasos:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                FeatureItem(
                    icon = Icons.Default.Add,
                    text = "Agrega tu primera transacción"
                )
                FeatureItem(
                    icon = Icons.Default.Category,
                    text = "Explora las categorías"
                )
                FeatureItem(
                    icon = Icons.Default.Assessment,
                    text = "Revisa los reportes"
                )
                FeatureItem(
                    icon = Icons.Default.Savings,
                    text = "Configura tus presupuestos"
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Comenzar a usar la aplicación")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Launch,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// Helper function to save setup preferences
suspend fun saveSetupPreferences(
    currency: Currency?,
    importSampleData: Boolean,
    enableNotifications: Boolean,
    theme: String
) {
    // In a real app, this would save to SharedPreferences or Room database
    // For now, we'll just simulate the save operation
    kotlinx.coroutines.delay(500) // Simulate async operation
    
    // TODO: Implement actual preference saving
    // - Save currency preference
    // - Import sample data if requested
    // - Set notification preferences
    // - Apply theme preference
}