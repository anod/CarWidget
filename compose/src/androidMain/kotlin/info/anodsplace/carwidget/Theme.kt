package info.anodsplace.carwidget

import android.app.UiModeManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

private val Rubik = FontFamily(
        Font(R.font.rubik_regular),
        Font(R.font.rubik_light, FontWeight.W300),
)

private val typography = Typography()
private val CarWidgetTypography = Typography(
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

@Composable
fun CarWidgetTheme(
    uiMode: Int = UiModeManager.MODE_NIGHT_AUTO,
    content: @Composable () -> Unit
) {
    val darkTheme = if (uiMode == UiModeManager.MODE_NIGHT_AUTO)
            isSystemInDarkTheme()
        else uiMode == UiModeManager.MODE_NIGHT_YES
    val context = LocalContext.current
    val colorScheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CarWidgetTypography,
    ) {
        WidgetSystemTheme { widgetSystemTheme ->
            CompositionLocalProvider(LocalWidgetSystemTheme provides widgetSystemTheme) {
                content()
            }
        }
    }
}