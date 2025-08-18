package com.example.admin_ingresos.ui.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Small process-wide theme state holder so multiple viewmodels/composables
 * can observe background color changes immediately.
 */
object AppThemeManager {
    // store ARGB int for easy persistence interop
    private val _backgroundColor = MutableStateFlow(Color(0xFF000000).toArgb())
    val backgroundColor: StateFlow<Int> = _backgroundColor

    // allow forcing a light mode independent of color luminance
    private val _forceLight = MutableStateFlow(false)
    val forceLight: StateFlow<Boolean> = _forceLight

    fun setBackgroundColor(argb: Int) {
        _backgroundColor.value = argb
    }

    fun setForceLight(value: Boolean) {
        _forceLight.value = value
    }
}
