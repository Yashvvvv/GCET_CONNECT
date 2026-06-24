package com.example.collegebot.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary            = LightPrimary,
    onPrimary          = LightOnPrimary,
    primaryContainer   = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,

    secondary            = LightSecondary,
    onSecondary          = LightOnSecondary,
    secondaryContainer   = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,

    background       = LightBackground,
    surface          = LightSurface,
    surfaceVariant   = LightSurfaceVariant,
    onBackground     = LightOnSurface,
    onSurface        = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline          = LightOutline,
    outlineVariant   = LightOutlineVariant,

    error   = Error40,
    onError = LightOnPrimary,
    errorContainer   = Error90,
    onErrorContainer = Error10,
)

private val DarkColorScheme = darkColorScheme(
    primary            = DarkPrimary,
    onPrimary          = DarkOnPrimary,
    primaryContainer   = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,

    secondary            = DarkSecondary,
    onSecondary          = DarkOnSecondary,
    secondaryContainer   = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,

    background       = DarkBackground,
    surface          = DarkSurface,
    surfaceVariant   = DarkSurfaceVariant,
    onBackground     = DarkOnSurface,
    onSurface        = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline          = DarkOutline,
    outlineVariant   = DarkOutlineVariant,

    error   = Error80,
    onError = Error10,
    errorContainer   = Error40,
    onErrorContainer = Error90,
)

@Composable
fun CollegeBotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content
    )
}
