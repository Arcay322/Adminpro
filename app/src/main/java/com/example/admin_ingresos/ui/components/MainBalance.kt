package com.example.admin_ingresos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import com.example.admin_ingresos.ui.icons.LucideIconMapper
import com.example.admin_ingresos.ui.theme.*
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.platform.LocalContext

@Composable
fun MainBalanceCards(
    currentBalance: Double,
    monthlyIncome: Double,
    monthlyExpenses: Double,
    monthlyTransfers: Double,
    modifier: Modifier = Modifier,
    onViewDetails: () -> Unit = {},
    balanceSubtitle: String? = "Actualizado ahora",
    incomeSubtitle: String? = "Este mes",
    expensesSubtitle: String? = "Este mes",
    transfersSubtitle: String? = "Este mes"
) {
    val context = LocalContext.current
    val prefs = com.example.admin_ingresos.data.PreferencesManager(context)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = modifier) {
        AdvancedBalanceCard(
            title = "Balance Total",
            amount = com.example.admin_ingresos.data.CurrencyUtils.format(currentBalance, context),
            subtitle = balanceSubtitle,
            icon = com.example.admin_ingresos.ui.icons.LucideIconMapper.getNavigationIcon("DollarSign"),
            onClick = onViewDetails,
            modifier = Modifier.fillMaxWidth()
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InsightCard(
                    title = "Ingresos",
                    value = com.example.admin_ingresos.data.CurrencyUtils.format(monthlyIncome, context).replace(" ", "\u00A0"),
                    subtitle = incomeSubtitle ?: "Este mes",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = IncomeGreen,
                    modifier = Modifier.weight(1f)
                )

                InsightCard(
                    title = "Gastos",
                    value = com.example.admin_ingresos.data.CurrencyUtils.format(monthlyExpenses, context).replace(" ", "\u00A0"),
                    subtitle = expensesSubtitle ?: "Este mes",
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    color = ExpenseRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                InsightCard(
                    title = "Ahorro",
                    value = com.example.admin_ingresos.data.CurrencyUtils.format(monthlyTransfers, context).replace(" ", "\u00A0"),
                    subtitle = transfersSubtitle ?: "Este mes",
                    icon = com.example.admin_ingresos.ui.icons.LucideIconMapper.getIconFromCategoryName("ahorros"),
                    color = Color(0xFF42A5F5),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun InsightCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    valueColor: Color? = null,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        backgroundColor = GlassWhiteSubtle,
        cornerRadius = 16.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = valueColor ?: TextPrimary,
                lineHeight = 20.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
