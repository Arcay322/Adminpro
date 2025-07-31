# Design Document

## Overview

Este diseño aborda la refactorización completa de la interfaz de usuario de Admin_ingresos para crear una experiencia móvil nativa, moderna y funcional. El enfoque principal es corregir errores críticos, implementar Material Design 3 correctamente, y optimizar la aplicación para dispositivos Android.

## Architecture

### Design System
- **Material Design 3**: Implementación completa con Dynamic Color support
- **Compose UI**: Refactorización de componentes para mejor rendimiento
- **Responsive Design**: Adaptación a diferentes tamaños de pantalla Android
- **Accessibility**: Cumplimiento con estándares de accesibilidad móvil

### Navigation Architecture
- **Bottom Navigation Bar**: Navegación principal con 4 tabs (Dashboard, Transacciones, Historial, Reportes)
- **Modal Bottom Sheets**: Para formularios y selecciones
- **Floating Action Button**: Acción principal de agregar transacción
- **Top App Bar**: Con título contextual y acciones secundarias

## Components and Interfaces

### 1. Theme System Refactoring
```kotlin
// Nuevo sistema de colores Material 3
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6366F1),        // Indigo 500
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF), // Indigo 100
    secondary = Color(0xFF16A34A),       // Green 600
    error = Color(0xFFEF4444),          // Red 500
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F)
)
```

### 2. Navigation Component
```kotlin
@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = { AddTransactionFAB() }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(paddingValues)
        )
    }
}
```

### 3. Dashboard Screen Redesign
- **Header Section**: Balance total con indicadores visuales
- **Quick Stats Cards**: Ingresos/Gastos del mes en cards horizontales
- **Chart Section**: Gráfico de dona mejorado con leyenda
- **Recent Transactions**: Lista optimizada con lazy loading
- **Quick Actions**: Botones de acceso rápido

### 4. Form Components
```kotlin
@Composable
fun TransactionForm() {
    // Campos con validación en tiempo real
    // Selectors con Material 3 ExposedDropdownMenu
    // Date picker nativo de Android
    // Teclado numérico para montos
}
```

### 5. Chart Integration
- **MPAndroidChart Wrapper**: Componente Compose personalizado
- **Responsive Sizing**: Adaptación automática a pantalla
- **Touch Interactions**: Gestos optimizados para móvil
- **Color Theming**: Integración con Material 3 colors

## Data Models

### UI State Management
```kotlin
data class DashboardUiState(
    val balance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpenses: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val expensesByCategory: Map<String, Double> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```

### Form Validation
```kotlin
data class TransactionFormState(
    val amount: String = "",
    val amountError: String? = null,
    val description: String = "",
    val selectedCategory: Category? = null,
    val selectedPaymentMethod: PaymentMethod? = null,
    val date: LocalDate = LocalDate.now(),
    val type: TransactionType = TransactionType.EXPENSE,
    val isValid: Boolean = false
)
```

## Error Handling

### Code Issues Resolution
1. **Duplicate UI Elements**: Eliminar código duplicado en DashboardScreen
2. **Resource References**: Corregir referencias a recursos inexistentes
3. **State Management**: Implementar StateFlow correctamente
4. **Memory Leaks**: Proper lifecycle management en ViewModels

### User Experience Errors
1. **Form Validation**: Validación en tiempo real con feedback visual
2. **Loading States**: Indicadores de carga apropiados
3. **Error Messages**: Mensajes de error claros y accionables
4. **Empty States**: Pantallas vacías con call-to-action

## Testing Strategy

### Unit Tests
- ViewModel logic testing
- Form validation testing
- Data transformation testing
- Repository pattern testing

### UI Tests
- Navigation flow testing
- Form interaction testing
- Chart rendering testing
- Accessibility testing

### Integration Tests
- Database operations
- End-to-end user flows
- Performance testing
- Memory usage testing

## Implementation Phases

### Phase 1: Foundation
- Fix critical code errors
- Implement proper theme system
- Set up navigation architecture

### Phase 2: Core UI
- Redesign Dashboard screen
- Implement form components
- Add proper state management

### Phase 3: Polish
- Chart integration
- Animations and transitions
- Accessibility improvements
- Performance optimization

## Mobile Optimization Specifics

### Touch Targets
- Minimum 48dp for all interactive elements
- Proper spacing between touch targets
- Swipe gestures for list interactions

### Screen Adaptation
- Support for different screen densities
- Landscape orientation handling
- Foldable device considerations

### Performance
- Lazy loading for large lists
- Image optimization
- Efficient recomposition
- Memory management

### Accessibility
- Content descriptions
- Semantic markup
- Screen reader support
- High contrast support