package com.example.admin_ingresos.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*

// Simple screen transition functions
fun getEnterTransition(transitionType: String): EnterTransition {
    return when (transitionType) {
        "slide" -> slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300))
        "fade" -> fadeIn(animationSpec = tween(300))
        else -> fadeIn(animationSpec = tween(300))
    }
}

fun getExitTransition(transitionType: String): ExitTransition {
    return when (transitionType) {
        "slide" -> slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300))
        "fade" -> fadeOut(animationSpec = tween(300))
        else -> fadeOut(animationSpec = tween(300))
    }
}

fun getPopEnterTransition(transitionType: String): EnterTransition {
    return when (transitionType) {
        "slide" -> slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300))
        "fade" -> fadeIn(animationSpec = tween(300))
        else -> fadeIn(animationSpec = tween(300))
    }
}

fun getPopExitTransition(transitionType: String): ExitTransition {
    return when (transitionType) {
        "slide" -> slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300))
        "fade" -> fadeOut(animationSpec = tween(300))
        else -> fadeOut(animationSpec = tween(300))
    }
}

fun getTransitionForRoute(route: String): String {
    return when (route) {
        "dashboard" -> "fade"
        "addTransaction" -> "slide"
        "history" -> "slide"
        "reports" -> "slide"
        "budget" -> "slide"
        else -> "fade"
    }
}