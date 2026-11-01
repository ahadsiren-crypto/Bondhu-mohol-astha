package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryDarkGreen,
    onPrimary = PureWhite,
    primaryContainer = PaleGreenBg,
    onPrimaryContainer = PrimaryDarkGreen,
    secondary = PrimaryGreen,
    onSecondary = PureWhite,
    secondaryContainer = PaleGreenBg,
    onSecondaryContainer = PrimaryDarkGreen,
    tertiary = AccentBlue,
    onTertiary = PureWhite,
    tertiaryContainer = SoftBlueBg,
    onTertiaryContainer = AccentBlue,
    background = OffWhite,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = PaleGreenBg,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
    error = ErrorRed,
    onError = PureWhite,
    errorContainer = ErrorSoftBg,
    onErrorContainer = ErrorRed
)

private val DarkColorScheme = darkColorScheme(
    primary = LightGreen,
    onPrimary = DarkBackground,
    primaryContainer = PrimaryDarkGreen,
    onPrimaryContainer = PureWhite,
    secondary = SoftGreen,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurface,
    onSecondaryContainer = LightGreen,
    tertiary = AccentBlue,
    onTertiary = PureWhite,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCardSurface,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkCardSurface,
    error = ErrorRed,
    onError = PureWhite
)

@Composable
fun BondhuMoholTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SomitiTypography,
        content = content
    )
}
