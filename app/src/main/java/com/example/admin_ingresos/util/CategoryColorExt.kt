package com.example.admin_ingresos.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.admin_ingresos.data.Category

val Category.composeColor: Color
    @Composable
    get() = Color(android.graphics.Color.parseColor(this.color))
