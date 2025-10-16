package com.example.a122mm.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RedPrimary,
    onPrimary = Color.White,
    secondary = RedDark,
    background = BlackBackground,
    surface = DarkGray,
    onSurface = Color.White,
    onBackground = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = RedPrimary,
    onPrimary = Color.White,
    secondary = RedDark,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary
)


@Composable
fun A122mmTheme(
    darkTheme: Boolean = true, // default to dark
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        content = content
    )
}