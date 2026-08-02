package com.example.ui.theme

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

// Define Sophisticated text color locally for theme fallbacks
private val SophisticatedOnBg = Color(0xFFE2E2E6)

private val DarkColorScheme =
  darkColorScheme(
    primary = SophisticatedPrimary,
    secondary = SophisticatedSecondary,
    tertiary = Amber500,
    background = SophisticatedBgBase,
    surface = SophisticatedSurface,
    onPrimary = Color(0xFF1A1C1E), // Primary background is dark, so text on primary should be dark
    onSecondary = Color(0xFF1A1C1E),
    onBackground = SophisticatedOnBg,
    onSurface = SophisticatedOnBg
  )

private val LightColorScheme =
  darkColorScheme( // We prefer a consistent, beautiful ambient dark mode for the map app
    primary = SophisticatedPrimary,
    secondary = SophisticatedSecondary,
    tertiary = Amber500,
    background = SophisticatedBgBase,
    surface = SophisticatedSurface,
    onPrimary = Color(0xFF1A1C1E),
    onSecondary = Color(0xFF1A1C1E),
    onBackground = SophisticatedOnBg,
    onSurface = SophisticatedOnBg
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark mode as standard for the Sophisticated Dark theme
  dynamicColor: Boolean = false, // Disable system dynamic color override to preserve specific theme
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

