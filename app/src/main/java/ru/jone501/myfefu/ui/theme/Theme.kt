package ru.jone501.myfefu.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00f7a3),
    background = Color(0xFF000000),
    surface = Color(0xFF121212),
    surfaceTint = Color(0xFF242424),
    onBackground = Color(0xFFffffff),
    onSurface = Color(0xFFCCCCCC),
    onSurfaceVariant = Color(0xFF999999),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00f7a3),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFEDEDED),
    surfaceTint = Color(0xFFDBDBDB),
    onBackground = Color(0xFF000000),
    onSurface = Color(0xFF333333),
    onSurfaceVariant = Color(0xFF666666)
)

@Composable
fun MyFEFUTheme(
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

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}