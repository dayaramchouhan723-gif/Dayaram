package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val EmpireColorScheme = darkColorScheme(
    primary = NeonRed,
    onPrimary = TextWhite,
    primaryContainer = NeonRedDark,
    onPrimaryContainer = NeonRedGlow,
    secondary = ImperialGold,
    onSecondary = EmpireBlack,
    secondaryContainer = ImperialGoldDark,
    onSecondaryContainer = ImperialGoldGlow,
    tertiary = NeonRedGlow,
    background = EmpireBlack,
    onBackground = TextOffWhite,
    surface = EmpireCardBg,
    onSurface = TextOffWhite,
    surfaceVariant = EmpireCardElevated,
    onSurfaceVariant = TextMuted,
    outline = EmpireCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Always enforce CHOUHAN EMPIRE visual branding
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EmpireColorScheme,
        typography = Typography,
        content = content
    )
}
