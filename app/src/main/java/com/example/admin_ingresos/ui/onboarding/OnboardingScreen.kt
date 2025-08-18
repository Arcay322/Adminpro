package com.example.admin_ingresos.ui.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin_ingresos.ui.icons.LucideIconMapper
import com.example.admin_ingresos.ui.theme.*
import com.example.admin_ingresos.ui.theme.GlassmorphicCard

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    onSkip: () -> Unit
) {
    // pages: icon name, title, subtitle
    val pages = listOf(
        Triple(LucideIconMapper.Navigation.home, "Bienvenido a CashFlow", "Tu panel de control para entender y mejorar tu salud financiera."),
        Triple(LucideIconMapper.Navigation.reports, "Informes y Gráficos", "Visualiza gastos por categoría y tendencias mensuales con gráficos claros."),
        Triple(LucideIconMapper.Navigation.transactions, "Registra movimientos", "Añade transacciones rápido, adjunta recibos y organiza por categoría.")
    )

    var pageIndex by remember { mutableStateOf(0) }
    val page = pages[pageIndex]

    // subtle scale animation for the icon
    val scale by animateFloatAsState(targetValue = if (pageIndex % 2 == 0) 1.02f else 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.background))
            )
            .padding(20.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    // dragAmount > 0 -> dragging right (previous), < 0 -> dragging left (next)
                    if (dragAmount > 150f) {
                        if (pageIndex > 0) pageIndex = pageIndex - 1
                    } else if (dragAmount < -150f) {
                        if (pageIndex < pages.lastIndex) pageIndex = pageIndex + 1
                    }
                }
            }
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Spacer(modifier = Modifier.height(6.dp))

            // Top: Skip
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onSkip() }) {
                    Text(text = "Saltar", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f))
                }
            }

            // Middle: content card (limit height so bottom controls remain visible)
            GlassmorphicCard(modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp)) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // icon badge
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(AccentVibrantStart, AccentVibrantEnd)))
                            .shadow(elevation = 8.dp, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = rememberVectorPainter(image = page.first),
                            contentDescription = null,
                            tint = TextOnAccent,
                            modifier = Modifier.size((56 * scale).dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = page.second,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = page.third,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // indicators
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pages.forEachIndexed { idx, _ ->
                            val active = idx == pageIndex
                            Box(
                                modifier = Modifier
                                    .height(if (active) 10.dp else 8.dp)
                                    .width(if (active) 28.dp else 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) Brush.linearGradient(listOf(AccentVibrantStart, AccentVibrantEnd)).toBrushColor() else Color.White.copy(alpha = 0.12f))
                            ) {}
                        }
                    }
                }
            }

            // Bottom controls
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    if (pageIndex > 0) {
                        TextButton(onClick = { pageIndex = (pageIndex - 1).coerceAtLeast(0) }) {
                            Text(text = "Anterior", color = MaterialTheme.colorScheme.onBackground)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Row {
                        if (pageIndex < pages.lastIndex) {
                            OutlinedButton(onClick = { pageIndex = (pageIndex + 1).coerceAtMost(pages.lastIndex) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)) {
                                Text("Siguiente")
                            }
                        } else {
                            Button(onClick = { onFinish() }) {
                                Text("Comenzar")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// helper extension to convert Brush to a Color fallback for simple backgrounds
private fun Brush.toBrushColor(): Color {
    // choose primary accent as approximation
    return AccentVibrantStart
}

