package com.example.bio.presentation.common.component.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.core.view.WindowCompat
import com.example.bio.R


val vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular),
    Font(R.font.vazirmatn_bold)
)


data class CustomColors(
    val appGrey: Color,
    val anotherCustomColor: Color,
    val lightBlue: Color
)

val LocalCustomColors = staticCompositionLocalOf {
    CustomColors(
        appGrey = Color.Gray,
        anotherCustomColor = Color.Magenta,
        lightBlue = Color(0xFF3369FF)
    )
}

val MaterialTheme.customColors: CustomColors
    @Composable
    @ReadOnlyComposable
    get() = LocalCustomColors.current

private val DarkColorScheme = darkColorScheme(
    primary = CyanNeon,
    secondary = DarkNavy,
    tertiary = ElectricPurple,
    background = BackgroundColor,
    surface = BackgroundColor,
    onPrimary = DarkNavy,
    onSecondary = TextColor,
    onTertiary = TextColor,
    onBackground = TextColor,
    onSurface = TextColor
)

private val LightColorScheme = lightColorScheme(
    primary = CyanNeon,
    secondary = DarkNavy,
    tertiary = ElectricPurple,
    background = BackgroundColor,
    surface = BackgroundColor,
    onPrimary = DarkNavy,
    onSecondary = TextColor,
    onTertiary = TextColor,
    onBackground = TextColor,
    onSurface = TextColor
)

@Composable
fun BioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
