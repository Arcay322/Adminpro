package com.example.admin_ingresos.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController

// Animation specifications
object AnimationSpecs {
    val fastTransition = tween<Float>(300, easing = FastOutSlowInEasing)
    val mediumTransition = tween<Float>(500, easing = FastOutSlowInEasing)
    val slowTransition = tween<Float>(700, easing = FastOutSlowInEasing)
    
    val springTransition = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val bounceTransition = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )
}

// Screen transition types
enum class TransitionType {
    SLIDE_HORIZONTAL,
    SLIDE_VERTICAL,
    FADE,
    SCALE,
    SLIDE_UP,
    SLIDE_DOWN,
    FADE_THROUGH,
    SHARED_AXIS_X,
    SHARED_AXIS_Y,
    SHARED_AXIS_Z
}

// Navigation transitions
@OptIn(ExperimentalAnimationApi::class)
fun getEnterTransition(
    transitionType: TransitionType = TransitionType.SLIDE_HORIZONTAL
): EnterTransition {
    return when (transitionType) {
        TransitionType.SLIDE_HORIZONTAL -> slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeIn(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.SLIDE_VERTICAL -> slideInVertically(
            initialOffsetY = { it },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeIn(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.FADE -> fadeIn(animationSpec = AnimationSpecs.mediumTransition)
        
        TransitionType.SCALE -> scaleIn(
            initialScale = 0.8f,
            transformOrigin = TransformOrigin.Center,
            animationSpec = AnimationSpecs.springTransition
        ) + fadeIn(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.SLIDE_UP -> slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeIn(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.SLIDE_DOWN -> slideInVertically(
            initialOffsetY = { -it / 2 },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeIn(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.FADE_THROUGH -> fadeIn(
            animationSpec = tween(300, delayMillis = 150)
        ) + scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(300, delayMillis = 150)
        )
        
        TransitionType.SHARED_AXIS_X -> slideInHorizontally(
            initialOffsetX = { it / 3 },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeIn(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.SHARED_AXIS_Y -> slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeIn(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.SHARED_AXIS_Z -> scaleIn(
            initialScale = 1.1f,
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeIn(animationSpec = AnimationSpecs.fastTransition)
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun getExitTransition(
    transitionType: TransitionType = TransitionType.SLIDE_HORIZONTAL
): ExitTransition {
    return when (transitionType) {
        TransitionType.SLIDE_HORIZONTAL -> slideOutHorizontally(
            targetOffsetX = { -it / 3 },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeOut(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.SLIDE_VERTICAL -> slideOutVertically(
            targetOffsetY = { -it / 3 },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeOut(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.FADE -> fadeOut(animationSpec = AnimationSpecs.mediumTransition)
        
        TransitionType.SCALE -> scaleOut(
            targetScale = 0.8f,
            transformOrigin = TransformOrigin.Center,
            animationSpec = AnimationSpecs.springTransition
        ) + fadeOut(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.SLIDE_UP -> slideOutVertically(
            targetOffsetY = { -it / 2 },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeOut(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.SLIDE_DOWN -> slideOutVertically(
            targetOffsetY = { it / 2 },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeOut(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.FADE_THROUGH -> fadeOut(
            animationSpec = tween(150)
        ) + scaleOut(
            targetScale = 0.92f,
            animationSpec = tween(150)
        )
        
        TransitionType.SHARED_AXIS_X -> slideOutHorizontally(
            targetOffsetX = { -it / 3 },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeOut(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.SHARED_AXIS_Y -> slideOutVertically(
            targetOffsetY = { -it / 3 },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeOut(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.SHARED_AXIS_Z -> scaleOut(
            targetScale = 0.9f,
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeOut(animationSpec = AnimationSpecs.fastTransition)
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun getPopEnterTransition(
    transitionType: TransitionType = TransitionType.SLIDE_HORIZONTAL
): EnterTransition {
    return when (transitionType) {
        TransitionType.SLIDE_HORIZONTAL -> slideInHorizontally(
            initialOffsetX = { -it / 3 },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeIn(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.SLIDE_VERTICAL -> slideInVertically(
            initialOffsetY = { -it / 3 },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeIn(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.FADE -> fadeIn(animationSpec = AnimationSpecs.mediumTransition)
        
        TransitionType.SCALE -> scaleIn(
            initialScale = 0.8f,
            transformOrigin = TransformOrigin.Center,
            animationSpec = AnimationSpecs.springTransition
        ) + fadeIn(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.SLIDE_UP -> slideInVertically(
            initialOffsetY = { -it / 2 },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeIn(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.SLIDE_DOWN -> slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeIn(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.FADE_THROUGH -> fadeIn(
            animationSpec = tween(300, delayMillis = 150)
        ) + scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(300, delayMillis = 150)
        )
        
        TransitionType.SHARED_AXIS_X -> slideInHorizontally(
            initialOffsetX = { -it / 3 },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeIn(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.SHARED_AXIS_Y -> slideInVertically(
            initialOffsetY = { -it / 3 },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeIn(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.SHARED_AXIS_Z -> scaleIn(
            initialScale = 0.9f,
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeIn(animationSpec = AnimationSpecs.fastTransition)
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun getPopExitTransition(
    transitionType: TransitionType = TransitionType.SLIDE_HORIZONTAL
): ExitTransition {
    return when (transitionType) {
        TransitionType.SLIDE_HORIZONTAL -> slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeOut(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.SLIDE_VERTICAL -> slideOutVertically(
            targetOffsetY = { it },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeOut(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.FADE -> fadeOut(animationSpec = AnimationSpecs.mediumTransition)
        
        TransitionType.SCALE -> scaleOut(
            targetScale = 1.2f,
            transformOrigin = TransformOrigin.Center,
            animationSpec = AnimationSpecs.springTransition
        ) + fadeOut(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.SLIDE_UP -> slideOutVertically(
            targetOffsetY = { it / 2 },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeOut(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.SLIDE_DOWN -> slideOutVertically(
            targetOffsetY = { -it / 2 },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeOut(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.FADE_THROUGH -> fadeOut(
            animationSpec = tween(150)
        ) + scaleOut(
            targetScale = 0.92f,
            animationSpec = tween(150)
        )
        
        TransitionType.SHARED_AXIS_X -> slideOutHorizontally(
            targetOffsetX = { it / 3 },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeOut(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.SHARED_AXIS_Y -> slideOutVertically(
            targetOffsetY = { it / 3 },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeOut(animationSpec = AnimationSpecs.fastTransition)
        
        TransitionType.SHARED_AXIS_Z -> scaleOut(
            targetScale = 1.1f,
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeOut(animationSpec = AnimationSpecs.fastTransition)
    }
}

// Route-specific transition configurations
fun getTransitionForRoute(route: String): TransitionType {
    return when (route) {
        "dashboard" -> TransitionType.FADE_THROUGH
        "addTransaction" -> TransitionType.SLIDE_UP
        "history" -> TransitionType.SHARED_AXIS_X
        "reports" -> TransitionType.SHARED_AXIS_Y
        "budget" -> TransitionType.SHARED_AXIS_X
        "settings" -> TransitionType.SLIDE_HORIZONTAL
        else -> TransitionType.SLIDE_HORIZONTAL
    }
}

// Shared element transition helpers
@Composable
fun SharedElementTransition(
    key: String,
    content: @Composable () -> Unit
) {
    // TODO: Implement shared element transitions when available in Compose Navigation
    // For now, we'll use regular content
    content()
}

// Custom transition composables
@Composable
fun SlideInFromBottom(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = AnimationSpecs.springTransition
        ) + fadeIn(animationSpec = AnimationSpecs.fastTransition),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeOut(animationSpec = AnimationSpecs.fastTransition)
    ) {
        content()
    }
}

@Composable
fun SlideInFromTop(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = AnimationSpecs.springTransition
        ) + fadeIn(animationSpec = AnimationSpecs.fastTransition),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeOut(animationSpec = AnimationSpecs.fastTransition)
    ) {
        content()
    }
}

@Composable
fun SlideInFromRight(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = AnimationSpecs.springTransition
        ) + fadeIn(animationSpec = AnimationSpecs.fastTransition),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeOut(animationSpec = AnimationSpecs.fastTransition)
    ) {
        content()
    }
}

@Composable
fun SlideInFromLeft(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = AnimationSpecs.springTransition
        ) + fadeIn(animationSpec = AnimationSpecs.fastTransition),
        exit = slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeOut(animationSpec = AnimationSpecs.fastTransition)
    ) {
        content()
    }
}

@Composable
fun ScaleInTransition(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.8f,
            transformOrigin = TransformOrigin.Center,
            animationSpec = AnimationSpecs.bounceTransition
        ) + fadeIn(animationSpec = AnimationSpecs.fastTransition),
        exit = scaleOut(
            targetScale = 0.8f,
            transformOrigin = TransformOrigin.Center,
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeOut(animationSpec = AnimationSpecs.fastTransition)
    ) {
        content()
    }
}

@Composable
fun FadeTransition(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = AnimationSpecs.mediumTransition),
        exit = fadeOut(animationSpec = AnimationSpecs.mediumTransition)
    ) {
        content()
    }
}

// Contextual animations for specific UI elements
@Composable
fun ExpandableCard(
    expanded: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically(
            animationSpec = AnimationSpecs.springTransition
        ) + fadeIn(animationSpec = AnimationSpecs.fastTransition),
        exit = shrinkVertically(
            animationSpec = AnimationSpecs.fastTransition
        ) + fadeOut(animationSpec = AnimationSpecs.fastTransition)
    ) {
        content()
    }
}

@Composable
fun StaggeredListAnimation(
    visible: Boolean,
    itemCount: Int,
    content: @Composable (Int) -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = AnimationSpecs.fastTransition)
    ) {
        repeat(itemCount) { index ->
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(
                        durationMillis = 300,
                        delayMillis = index * 50,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 300,
                        delayMillis = index * 50
                    )
                )
            ) {
                content(index)
            }
        }
    }
}