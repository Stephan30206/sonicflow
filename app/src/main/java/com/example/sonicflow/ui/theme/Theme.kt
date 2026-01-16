package com.example.sonicflow.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AccentOrange,
    onPrimary = TextBlack,
    primaryContainer = AccentOrangeDark,
    onPrimaryContainer = TextPrimary,

    secondary = AccentOrangeLight,
    onSecondary = TextBlack,
    secondaryContainer = Color(0xFFFFE5B4),
    onSecondaryContainer = TextBlack,

    tertiary = GradientEnd,
    onTertiary = TextPrimary,

    background = DarkBackground,
    onBackground = TextPrimary,

    surface = DarkSurface,
    onSurface = TextPrimary,

    surfaceVariant = DarkInput,
    onSurfaceVariant = TextSecondary,

    error = ErrorColor,
    onError = TextPrimary,

    outline = TextTertiary,
    outlineVariant = Color(0xFF404040),

    inverseSurface = TextPrimary,
    inverseOnSurface = DarkBackground,
    inversePrimary = AccentOrangeDark
)

private val LightColorScheme = lightColorScheme(
    primary = AccentOrange,
    onPrimary = TextBlack,
    primaryContainer = AccentOrangeLight,
    onPrimaryContainer = TextBlack,

    secondary = AccentOrangeDark,
    onSecondary = TextPrimary,
    secondaryContainer = Color(0xFFFFE5B4),
    onSecondaryContainer = TextBlack,

    tertiary = GradientStart,
    onTertiary = TextBlack,

    background = LightBackground,
    onBackground = TextBlack,

    surface = LightSurface,
    onSurface = TextBlack,

    surfaceVariant = LightInput,
    onSurfaceVariant = Color(0xFF666666),

    error = ErrorColor,
    onError = TextPrimary,

    outline = Color(0xFFCCCCCC),
    outlineVariant = Color(0xFFE0E0E0),

    inverseSurface = TextBlack,
    inverseOnSurface = LightBackground,
    inversePrimary = AccentOrange
)

@Composable
fun SonicFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color est disponible sur Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()

            val windowInsetsController = WindowCompat.getInsetsController(window, view)
            windowInsetsController.isAppearanceLightStatusBars = !darkTheme
            windowInsetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}