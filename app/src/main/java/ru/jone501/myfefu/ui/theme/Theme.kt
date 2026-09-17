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
    primary = Color(0xFF21262d),
    secondary = Color(0xFF161b22),
    background = Color(0xFF0d1117),
    onBackground = Color(0xFFecf2f8),
    onPrimary = Color(0xFFc6cdd5),
    onSecondary = Color(0xFF89929b),
    onError = Color(0xFFfa7970)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFd1d5da),
    secondary = Color(0xFFf2f3f4),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1f2328),
    onPrimary = Color(0xFF59636e),
    onSecondary = Color(0xFF59636e),
    onError = Color(0xFFfa7970)
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