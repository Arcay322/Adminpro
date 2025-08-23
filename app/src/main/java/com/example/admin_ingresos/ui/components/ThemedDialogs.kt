package com.example.admin_ingresos.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Surface
import androidx.compose.material3.CardDefaults

/**
 * Wrapper to ensure all alert dialogs use the MaterialTheme background (surface) so they
 * correctly adapt to light/dark mode. This intentionally only changes the containerColor.
 * Use everywhere instead of calling AlertDialog directly when you want theme-consistent backgrounds.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemedAlertDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    confirmButton: (@Composable () -> Unit)? = null,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    // Use nullable colors and provide fallbacks when forwarding to AlertDialog/Surface
    containerColor: Color? = MaterialTheme.colorScheme.surface,
    iconContentColor: Color? = MaterialTheme.colorScheme.onSurface,
    titleContentColor: Color? = MaterialTheme.colorScheme.onSurface,
    textContentColor: Color? = MaterialTheme.colorScheme.onSurface,
    tonalElevation: Dp = 1.dp,
    shape: Shape = MaterialTheme.shapes.medium,
    borderStroke: BorderStroke? = null,
    // Optional full-content lambda used by many call sites that pass a trailing lambda
    content: (@Composable () -> Unit)? = null
) {
    // Resolve a final container color: if the active theme's surface is translucent
    // (used by the app's glassmorphism dark scheme), pick a solid dark background
    // color when the app is in dark mode so dialogs don't appear as light/white.
    // For light mode we preserve the caller hue but make it fully opaque.
    val baseContainer = containerColor ?: MaterialTheme.colorScheme.surface
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val resolvedContainer = if (baseContainer.alpha < 1f) {
        if (isDarkTheme) {
            // Use a slightly lightened background (blend toward surface) so the dialog
            // is solid but not too heavy in dark mode.
            lerp(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surface, 0.08f)
        } else {
            // In light mode, preserve hue but make fully opaque
            baseContainer.copy(alpha = 1f)
        }
    } else {
        baseContainer
    }

    // Determine default border when caller doesn't provide one: in dark mode use a
    // subtle onSurface stroke; in light mode do not apply a border by default
    // (caller may pass one via `borderStroke`).
    val finalBorder = borderStroke ?: if (isDarkTheme) BorderStroke(
        1.dp,
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    ) else null

    val finalModifier = if (finalBorder != null) modifier.then(Modifier.border(finalBorder, shape)) else modifier

    if (content == null) {
        // Use the standard AlertDialog API when content isn't provided. confirmButton is required
        // by AlertDialog, so provide an empty composable if caller didn't supply one.
        AlertDialog(
            onDismissRequest = onDismissRequest,
            modifier = finalModifier,
            title = title,
            text = text,
            confirmButton = confirmButton ?: {},
            dismissButton = dismissButton,
            icon = icon,
            containerColor = resolvedContainer,
            iconContentColor = iconContentColor ?: MaterialTheme.colorScheme.onSurface,
            titleContentColor = titleContentColor ?: MaterialTheme.colorScheme.onSurface,
            textContentColor = textContentColor ?: MaterialTheme.colorScheme.onSurface,
            tonalElevation = tonalElevation,
            shape = shape
        )
    } else {
        // Many existing dialogs in the project use a trailing lambda to provide arbitrary
        // content (Card/Column/etc.). Support that pattern by hosting the content inside
        // a Dialog whose Surface uses the MaterialTheme surface color.
        Dialog(onDismissRequest = onDismissRequest) {
            Surface(
                modifier = finalModifier,
                color = resolvedContainer,
                tonalElevation = tonalElevation,
                shape = shape
            ) {
                content()
            }
        }
    }
}
