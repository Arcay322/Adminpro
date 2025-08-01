package com.example.admin_ingresos.ui.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import com.example.admin_ingresos.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: String,
    val backgroundColor: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    onSkip: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "¡Bienvenido a CashFlow!",
            description = "La aplicación inteligente que te ayudará a tomar control total de tus finanzas personales de manera fácil y eficiente.",
            icon = "💰",
            backgroundColor = CashFlowPrimary
        ),
        OnboardingPage(
            title = "Registra tus Transacciones",
            description = "Lleva un registro detallado de todos tus ingresos y gastos. Categoriza, filtra y organiza tu dinero como nunca antes.",
            icon = "�",
            backgroundColor = CashFlowSecondary
        ),
        OnboardingPage(
            title = "Presupuestos Inteligentes",
            description = "Crea presupuestos personalizados y recibe alertas inteligentes cuando te acerques a tus límites de gasto.",
            icon = "🎯",
            backgroundColor = TertiaryPurple
        ),
        OnboardingPage(
            title = "Análisis y Reportes",
            description = "Visualiza tendencias, patrones de gasto y obtén insights detallados sobre tu salud financiera con gráficos interactivos.",
            icon = "📈",
            backgroundColor = Success
        ),
        OnboardingPage(
            title = "Exporta y Comparte",
            description = "Genera reportes profesionales en PDF y Excel. Mantén respaldos seguros y comparte información cuando la necesites.",
            icon = "📄",
            backgroundColor = Warning
        )
    )
            val pages = listOf(
        OnboardingPage(
            title = "¡Bienvenido a CashFlow!",
            description = "Tu compañero financiero inteligente que te ayuda a tomar control total de tus finanzas personales con herramientas avanzadas.",
            icon = "💰",
            backgroundColor = CashFlowPrimary
        ),
        OnboardingPage(
            title = "Gestión Inteligente",
            description = "Registra tus ingresos y gastos de manera fácil y rápida. Categoriza automáticamente y obtén insights sobre tus patrones de gasto.",
            icon = "📱",
            backgroundColor = CashFlowSecondary
        ),
        OnboardingPage(
            title = "Presupuestos Dinámicos",
            description = "Crea presupuestos personalizados con alertas inteligentes. Recibe notificaciones cuando te acerques a los límites.",
            icon = "🎯",
            backgroundColor = TertiaryPurple
        ),
        OnboardingPage(
            title = "Análisis Avanzado",
            description = "Visualiza tendencias, patrones de gasto y obtén reportes profesionales con gráficos interactivos y análisis detallados.",
            icon = "📈",
            backgroundColor = Success
        ),
        OnboardingPage(
            title = "Reportes Profesionales",
            description = "Genera reportes detallados en PDF y Excel. Exporta y comparte tu información financiera de manera segura y profesional.",
            icon = "📄",
            backgroundColor = Warning
        )
    )
        )
    )
    
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPageContent(
                page = pages[page],
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Skip button
        TextButton(
            onClick = onSkip,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text(
                text = "Saltar",
                color = Color.White,
                fontSize = 16.sp
            )
        }
        
        // Bottom section with indicators and navigation
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous button
                if (pagerState.currentPage > 0) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
                    ) {
                        Text("Anterior")
                    }
                } else {
                    Spacer(modifier = Modifier.width(80.dp))
                }
                
                // Next/Finish button
                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onOnboardingComplete()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = pages[pagerState.currentPage].backgroundColor
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage < pages.size - 1) "Siguiente" else "Comenzar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }
    
    val iconScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "iconScale"
    )
    
    val contentAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            delayMillis = 300
        ),
        label = "contentAlpha"
    )
    
    LaunchedEffect(Unit) {
        startAnimation = true
    }
    
    Box(
        modifier = modifier.background(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    page.backgroundColor,
                    page.backgroundColor.copy(alpha = 0.8f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Card(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    },
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.2f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = page.icon,
                        fontSize = 48.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Title
            Text(
                text = page.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { alpha = contentAlpha }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description
            Text(
                text = page.description,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier
                    .graphicsLayer { alpha = contentAlpha }
                    .padding(horizontal = 16.dp)
            )
        }
    }
}