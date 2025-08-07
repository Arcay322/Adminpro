package com.example.admin_ingresos.ui.reports.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin_ingresos.ui.components.GlassCard
import com.example.admin_ingresos.ui.icons.LucideIconMapper
import com.example.admin_ingresos.ui.reports.ExportFormat
import com.example.admin_ingresos.ui.theme.*

@Composable
fun ExportOptionsCard(
    selectedFormat: ExportFormat,
    onFormatSelected: (ExportFormat) -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = GlassWhite,
        cornerRadius = 16.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Exportar Reporte",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Icon(
                    imageVector = LucideIconMapper.getNavigationIcon("download"),
                    contentDescription = "Exportar",
                    tint = AccentVibrantStart,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                text = "Selecciona el formato de exportación",
                fontSize = 14.sp,
                color = TextSecondary
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ExportFormat.values()) { format ->
                    ExportFormatCard(
                        format = format,
                        isSelected = format == selectedFormat,
                        onClick = { onFormatSelected(format) }
                    )
                }
            }
            
            Button(
                onClick = onExport,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentVibrantStart
                )
            ) {
                Icon(
                    imageVector = LucideIconMapper.getNavigationIcon("download"),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exportar ${selectedFormat.label}")
            }
        }
    }
}

@Composable
private fun ExportFormatCard(
    format: ExportFormat,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (format) {
        ExportFormat.PDF -> Icons.Default.PictureAsPdf
        ExportFormat.CSV -> Icons.Default.TableChart  
        ExportFormat.EXCEL -> Icons.Default.GridOn
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier.size(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AccentVibrantStart.copy(alpha = 0.2f) else GlassWhiteSubtle
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = format.label,
                tint = if (isSelected) AccentVibrantStart else TextSecondary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = format.label,
                fontSize = 12.sp,
                color = if (isSelected) AccentVibrantStart else TextSecondary,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}
