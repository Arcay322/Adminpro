package com.example.admin_ingresos.ui.transaction

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.ui.components.CashFlowCard

@Composable
fun EnhancedCategorySelector(
    categories: List<com.example.admin_ingresos.data.Category>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int) -> Unit,
    error: String?
) {
    CashFlowCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Categoría",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (error != null) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(categories) { category ->
                    CategoryChip(
                        category = category,
                        isSelected = selectedCategoryId == category.id,
                        onClick = { onCategorySelected(category.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: com.example.admin_ingresos.data.Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .height(40.dp)
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = getCategoryIcon(category.name),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                ),
                color = contentColor
            )
        }
    }
}

@Composable
fun EnhancedPaymentMethodSelector(
    paymentMethods: List<com.example.admin_ingresos.data.PaymentMethod>,
    selectedPaymentMethodId: Int?,
    onPaymentMethodSelected: (Int?) -> Unit
) {
    CashFlowCard {
        Column {
            Text(
                text = "Método de Pago (Opcional)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                // None option
                item {
                    PaymentMethodChip(
                        name = "Ninguno",
                        icon = Icons.Default.Money,
                        isSelected = selectedPaymentMethodId == null,
                        onClick = { onPaymentMethodSelected(null) }
                    )
                }
                
                items(paymentMethods) { paymentMethod ->
                    PaymentMethodChip(
                        name = paymentMethod.name,
                        icon = getPaymentMethodIcon(paymentMethod.name),
                        isSelected = selectedPaymentMethodId == paymentMethod.id,
                        onClick = { onPaymentMethodSelected(paymentMethod.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodChip(
    name: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onSecondary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .height(40.dp)
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                ),
                color = contentColor
            )
        }
    }
}

// Helper functions for icons
private fun getCategoryIcon(categoryName: String): ImageVector {
    return when (categoryName.lowercase()) {
        "comida", "alimentación", "supermercado" -> Icons.Default.Restaurant
        "transporte" -> Icons.Default.DirectionsCar
        "entretenimiento", "ocio" -> Icons.Default.Movie
        "salud" -> Icons.Default.LocalHospital
        "ropa", "vestimenta" -> Icons.Default.Checkroom
        "hogar", "casa" -> Icons.Default.Home
        "educación", "estudio" -> Icons.Default.School
        "trabajo", "salario" -> Icons.Default.Work
        "servicios" -> Icons.Default.Build
        "otros" -> Icons.Default.Category
        else -> Icons.Default.Category
    }
}

private fun getPaymentMethodIcon(paymentMethodName: String): ImageVector {
    return when (paymentMethodName.lowercase()) {
        "efectivo" -> Icons.Default.Money
        "tarjeta de crédito", "crédito" -> Icons.Default.CreditCard
        "tarjeta de débito", "débito" -> Icons.Default.Payment
        "transferencia", "banco" -> Icons.Default.AccountBalance
        "nequi", "daviplata", "digital" -> Icons.Default.PhoneAndroid
        else -> Icons.Default.Payment
    }
}
