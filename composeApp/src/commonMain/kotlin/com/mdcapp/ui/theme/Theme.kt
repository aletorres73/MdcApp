package com.mdcapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = OnPrimaryBlue,
    primaryContainer = PrimaryContainerBlue,
    onPrimaryContainer = OnPrimaryContainerBlue,
    secondary = SecondaryGreen,
    onSecondary = OnSecondaryGreen,
    secondaryContainer = SecondaryContainerGreen,
    onSecondaryContainer = OnSecondaryContainerGreen,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    error = Error,
    onError = OnError,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
)

// Opcional: Esquema oscuro (simplificado para ahora)
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryGreen,
    tertiary = Tertiary
)

@Composable
fun AppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
