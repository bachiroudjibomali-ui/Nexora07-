package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LotoDarkColorScheme = darkColorScheme(
    primary = LotoOrange,
    onPrimary = LotoBackground,
    primaryContainer = LotoSurfaceVariant,
    onPrimaryContainer = LotoText,
    secondary = LotoGreen,
    onSecondary = LotoBackground,
    secondaryContainer = LotoSurfaceVariant,
    onSecondaryContainer = LotoText,
    tertiary = LotoBlue,
    onTertiary = LotoBackground,
    background = LotoBackground,
    onBackground = LotoText,
    surface = LotoSurface,
    onSurface = LotoText,
    surfaceVariant = LotoSurfaceVariant,
    onSurfaceVariant = LotoTextMuted,
    outline = LotoBorder,
    outlineVariant = LotoBorder
)

val LotoShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LotoDarkColorScheme,
        typography = Typography,
        shapes = LotoShapes,
        content = content
    )
}

