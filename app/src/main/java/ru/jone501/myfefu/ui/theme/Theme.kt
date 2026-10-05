package ru.jone501.myfefu.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00f7a3),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFEDEDED),
    surfaceTint = Color(0xFFDBDBDB),
    onBackground = Color(0xFF000000),
    onSurface = Color(0xFF333333),
    onSurfaceVariant = Color(0xFF666666),
    onTertiary = Color(0xFF999999)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00f7a3),
    background = Color(0xFF000000),
    surface = Color(0xFF121212),
    surfaceTint = Color(0xFF242424),
    onBackground = Color(0xFFffffff),
    onSurface = Color(0xFFCCCCCC),
    onSurfaceVariant = Color(0xFF999999),
    onTertiary = Color(0xFF666666)
)

private val FefuBlueLightColorScheme = lightColorScheme(
    primary = Color(0xFF3B63AB),
    background = Color(0xFFE6EEFF),
    surface = Color(0xFFCCDDFF),
    surfaceTint = Color(0xFFB3CCFF),
    onBackground = Color(0xFF3B63AB),
    onSurface = Color(0xFF597DCE),
    onSurfaceVariant = Color(0xFF85ABF2),
    onTertiary = Color(0xFFA9C1F1)
)

private val FefuBlueDarkColorScheme = darkColorScheme(
    primary = Color(0xFF3B63AB),
    background = Color(0xFF090F1A),
    surface = Color(0xFF121D33),
    surfaceTint = Color(0xFF1B2C4D),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFCCE0FF),
    onSurfaceVariant = Color(0xFF99B8FF),
    onTertiary = Color(0xFF668AFF)
)

val Default: Pair<ColorScheme, ColorScheme> = LightColorScheme to DarkColorScheme
val FefuBlue: Pair<ColorScheme, ColorScheme> = FefuBlueLightColorScheme to FefuBlueDarkColorScheme

@Composable
fun MyFEFUTheme(
    theme: Pair<ColorScheme, ColorScheme> = Default,
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> theme.second
        else -> theme.first
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}