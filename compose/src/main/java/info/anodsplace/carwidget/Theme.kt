package info.anodsplace.carwidget

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val Rubik = FontFamily(
        Font(R.font.rubik_regular),
        Font(R.font.rubik_light, FontWeight.W300),
        // Font(R.font.rubik_medium, FontWeight.W500)
)

val typography = Typography()
val CarWidgetTypography = Typography(
    displayLarge = typography.displayLarge.merge(TextStyle(fontFamily = Rubik)),
    displayMedium = typography.displayMedium.merge(TextStyle(fontFamily = Rubik)),
    displaySmall = typography.displaySmall.merge(TextStyle(fontFamily = Rubik)),
    headlineLarge = typography.headlineLarge.merge(TextStyle(fontFamily = Rubik)),
    headlineMedium = typography.headlineMedium.merge(TextStyle(fontFamily = Rubik)),
    headlineSmall = typography.headlineSmall.merge(TextStyle(fontFamily = Rubik)),
    titleLarge = typography.titleLarge.merge(TextStyle(fontFamily = Rubik)),
    titleMedium = typography.titleMedium.merge(TextStyle(fontFamily = Rubik)),
    titleSmall = typography.titleSmall.merge(TextStyle(fontFamily = Rubik)),
    bodyLarge = typography.bodyLarge.merge(TextStyle(fontFamily = Rubik)),
    bodyMedium = typography.bodyMedium.merge(TextStyle(fontFamily = Rubik)),
    bodySmall = typography.bodySmall.merge(TextStyle(fontFamily = Rubik)),
    labelLarge = typography.labelLarge.merge(TextStyle(fontFamily = Rubik)),
    labelMedium = typography.labelMedium.merge(TextStyle(fontFamily = Rubik)),
    labelSmall  = typography.labelSmall.merge(TextStyle(fontFamily = Rubik))
)

val WarningColor = Color(0xfff4511e)
val CarWidgetShapes = Shapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(4.dp),
        large = RoundedCornerShape(8.dp)
)

val md_theme_light_primary = Color(0xFF006782)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFBBE9FF)
val md_theme_light_onPrimaryContainer = Color(0xFF001F29)
val md_theme_light_secondary = Color(0xFF006684)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFBDE9FF)
val md_theme_light_onSecondaryContainer = Color(0xFF001F2A)
val md_theme_light_tertiary = Color(0xFF006782)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFBBE9FF)
val md_theme_light_onTertiaryContainer = Color(0xFF001F29)
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_onErrorContainer = Color(0xFF410002)
val md_theme_light_background = Color(0xFFFCFCFF)
val md_theme_light_onBackground = Color(0xFF001E2F)
val md_theme_light_surface = Color(0xFFFCFCFF)
val md_theme_light_onSurface = Color(0xFF001E2F)
val md_theme_light_surfaceVariant = Color(0xFFDCE4E9)
val md_theme_light_onSurfaceVariant = Color(0xFF40484C)
val md_theme_light_outline = Color(0xFF70787D)
val md_theme_light_inverseOnSurface = Color(0xFFE6F2FF)
val md_theme_light_inverseSurface = Color(0xFF00344E)
val md_theme_light_inversePrimary = Color(0xFF61D4FF)
val md_theme_light_shadow = Color(0xFF000000)
val md_theme_light_surfaceTint = Color(0xFF006782)

val md_theme_dark_primary = Color(0xFF61D4FF)
val md_theme_dark_onPrimary = Color(0xFF003545)
val md_theme_dark_primaryContainer = Color(0xFF004D63)
val md_theme_dark_onPrimaryContainer = Color(0xFFBBE9FF)
val md_theme_dark_secondary = Color(0xFF67D3FF)
val md_theme_dark_onSecondary = Color(0xFF003546)
val md_theme_dark_secondaryContainer = Color(0xFF004D64)
val md_theme_dark_onSecondaryContainer = Color(0xFFBDE9FF)
val md_theme_dark_tertiary = Color(0xFF61D4FF)
val md_theme_dark_onTertiary = Color(0xFF003545)
val md_theme_dark_tertiaryContainer = Color(0xFF004D63)
val md_theme_dark_onTertiaryContainer = Color(0xFFBBE9FF)
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_background = Color(0xFF001E2F)
val md_theme_dark_onBackground = Color(0xFFC9E6FF)
val md_theme_dark_surface = Color(0xFF001E2F)
val md_theme_dark_onSurface = Color(0xFFC9E6FF)
val md_theme_dark_surfaceVariant = Color(0xFF40484C)
val md_theme_dark_onSurfaceVariant = Color(0xFFC0C8CC)
val md_theme_dark_outline = Color(0xFF8A9296)
val md_theme_dark_inverseOnSurface = Color(0xFF001E2F)
val md_theme_dark_inverseSurface = Color(0xFFC9E6FF)
val md_theme_dark_inversePrimary = Color(0xFF006782)
val md_theme_dark_shadow = Color(0xFF000000)
val md_theme_dark_surfaceTint = Color(0xFF61D4FF)

private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    errorContainer = md_theme_light_errorContainer,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseSurface = md_theme_light_inverseSurface,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
)


private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    errorContainer = md_theme_dark_errorContainer,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseSurface = md_theme_dark_inverseSurface,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
)

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
fun supportsDynamic(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
fun CarWidgetTheme(
    uiMode: Int = UiModeManager.MODE_NIGHT_AUTO,
    content: @Composable () -> Unit
) {
    val darkTheme = if (uiMode == UiModeManager.MODE_NIGHT_AUTO)
            isSystemInDarkTheme()
        else uiMode == UiModeManager.MODE_NIGHT_YES
    val colorScheme = if (supportsDynamic()) {
        if (darkTheme) dynamicDarkColorScheme(LocalContext.current) else dynamicLightColorScheme(LocalContext.current)
    } else {
        if (darkTheme) DarkColors else LightColors
    }

    MaterialTheme(
            colorScheme = colorScheme,
            typography = CarWidgetTypography,
            shapes = CarWidgetShapes
    ) {
        WidgetSystemTheme { widgetSystemTheme ->
            CompositionLocalProvider(LocalWidgetSystemTheme provides widgetSystemTheme) {
                content()
            }
        }
    }
}