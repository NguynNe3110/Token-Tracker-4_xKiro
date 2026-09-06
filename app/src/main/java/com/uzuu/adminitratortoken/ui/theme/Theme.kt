package com.uzuu.adminitratortoken.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Brand colors
val IndigoPrimary = Color(0xFF5B5BD6)
val IndigoOnPrimary = Color(0xFFFFFFFF)
val IndigoPrimaryContainer = Color(0xFFE0E0FF)
val IndigoOnPrimaryContainer = Color(0xFF1A1A6E)
val IndigoSecondary = Color(0xFF7C3AED)
val IndigoTertiary = Color(0xFFEC4899)

// Status colors
val StatusSuccess = Color(0xFF22C55E)
val StatusWarning = Color(0xFFF59E0B)
val StatusError = Color(0xFFEF4444)

// Dark variants
val IndigoPrimaryDark = Color(0xFF9F9FFF)
val IndigoOnPrimaryDark = Color(0xFF1A1A4A)
val IndigoPrimaryContainerDark = Color(0xFF3A3A8C)
val IndigoOnPrimaryContainerDark = Color(0xFFE0E0FF)

private val DarkColorScheme = darkColorScheme(
    primary = IndigoPrimaryDark,
    onPrimary = IndigoOnPrimaryDark,
    primaryContainer = IndigoPrimaryContainerDark,
    onPrimaryContainer = IndigoOnPrimaryContainerDark,
    secondary = IndigoSecondary,
    tertiary = IndigoTertiary,
    error = StatusError
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = IndigoOnPrimary,
    primaryContainer = IndigoPrimaryContainer,
    onPrimaryContainer = IndigoOnPrimaryContainer,
    secondary = IndigoSecondary,
    tertiary = IndigoTertiary,
    error = StatusError
)

@Composable
fun TokenTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
