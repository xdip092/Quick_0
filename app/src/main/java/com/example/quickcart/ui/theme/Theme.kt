package com.example.quickcart.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors: ColorScheme = lightColorScheme(
    primary = BlinkitGreen,
    secondary = BlinkitYellow,
    background = BlinkitBackground,
    surface = BlinkitSurface
)
private val DarkColors: ColorScheme = darkColorScheme()

@Composable
fun QuickCartTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}
