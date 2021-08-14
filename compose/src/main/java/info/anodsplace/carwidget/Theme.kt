package info.anodsplace.carwidget

import android.app.UiModeManager
import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.StyleRes
import androidx.compose.runtime.Composable

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.dp
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.use
import androidx.core.graphics.ColorUtils
import info.anodsplace.applog.AppLog
import info.anodsplace.compose.toColorHex

private val Rubik = FontFamily(
        Font(R.font.rubik_regular),
        Font(R.font.rubik_light, FontWeight.W300),
        // Font(R.font.rubik_medium, FontWeight.W500)
)

val typography = Typography()
val CarWidgetTypography = Typography(
        h4 = typography.h4.merge(TextStyle(fontFamily = Rubik)),
        h5 = typography.h5.merge(TextStyle(fontFamily = Rubik)),
        h6 = typography.h6.merge(TextStyle(fontFamily = Rubik)),
        subtitle1 = typography.subtitle1.merge(TextStyle(fontFamily = Rubik)),
        subtitle2 = typography.subtitle2.merge(TextStyle(fontFamily = Rubik)),
        body1 = typography.body1.merge(TextStyle(fontFamily = Rubik)),
        body2 = typography.body2.merge(TextStyle(fontFamily = Rubik)),
        button = typography.button.merge(TextStyle(fontFamily = Rubik)),
        caption = typography.caption.merge(TextStyle(fontFamily = Rubik)),
        overline = typography.overline.merge(TextStyle(fontFamily = Rubik))
)

val WarningColor = Color(0xfff4511e)
val CarWidgetShapes = Shapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(4.dp),
        large = RoundedCornerShape(8.dp)
)

@Composable
fun CarWidgetTheme(
    context: Context = LocalContext.current,
    nightMode: Int = UiModeManager.MODE_NIGHT_AUTO,
    content: @Composable () -> Unit
) {
    val darkTheme = if (nightMode == UiModeManager.MODE_NIGHT_AUTO)
            isSystemInDarkTheme()
        else
            nightMode == UiModeManager.MODE_NIGHT_YES
    val defaultColors = if (darkTheme) darkColors() else lightColors()
    val colors = readColors(context, defaultColors)

    MaterialTheme(
            colors = colors,
            typography = CarWidgetTypography,
            shapes = CarWidgetShapes,
            content = content
    )
}

private fun TypedArray.getComposeColor(
        index: Int,
        fallbackColor: Color = Color.Unspecified
): Color = if (hasValue(index)) Color(getColorOrThrow(index)) else fallbackColor

private fun Color.calculateContrastForForeground(foreground: Color): Double {
    return ColorUtils.calculateContrast(foreground.toArgb(), copy(alpha = 1.0f).toArgb())
}

private const val MINIMUM_CONTRAST = 4.5

private fun Color.calculateOnColorWithTextColorPrimary(textColorPrimary: Color): Color {
    if (textColorPrimary != Color.Unspecified &&
            calculateContrastForForeground(textColorPrimary) >= MINIMUM_CONTRAST
    ) {
        return textColorPrimary
    }
    return calculateOnColor()
}

private fun Color.calculateOnColor(): Color {
    val contrastForBlack = calculateContrastForForeground(Color.Black)
    val contrastForWhite = calculateContrastForForeground(Color.White)
    return if (contrastForBlack > contrastForWhite) Color.Black else Color.White
}

fun readColors(
        context: Context,
        defaultColors: Colors
): Colors {
    @StyleRes val device = android.R.style.Theme_DeviceDefault_DayNight
    return context.obtainStyledAttributes(device, intArrayOf(
            android.R.attr.colorPrimary,
            android.R.attr.colorPrimaryDark,
            android.R.attr.colorAccent,
            android.R.attr.textColorPrimary,
            android.R.attr.colorBackground,
            android.R.attr.colorError,
    )).use { ta ->
        /* First we'll read the Material color palette */
        val primary = ta.getComposeColor(index = 0)
        // colorPrimaryDark is roughly equivalent to primaryVariant
        val primaryVariant = ta.getComposeColor(index = 1)
        val onPrimary = primary.calculateOnColor()

        // colorAccent is roughly equivalent to secondary
        val secondary = ta.getComposeColor(index = 2)
        // We don't have a secondaryVariant, so just use the secondary
        val onSecondary = secondary.calculateOnColor()

        // We try and use the android:textColorPrimary value (with forced 100% alpha) for the
        // onSurface and onBackground colors
        val textColorPrimary = ta.getComposeColor(index = 3).let { color ->
            // We only force the alpha value if it's not Unspecified
            if (color != Color.Unspecified) color.copy(alpha = 1f) else color
        }

        val surface = defaultColors.surface
        val onSurface = surface.calculateOnColorWithTextColorPrimary(textColorPrimary)

        val background = ta.getComposeColor(index = 4)
        val onBackground = background.calculateOnColorWithTextColorPrimary(textColorPrimary)

        val error = ta.getComposeColor(index = 5)
        val onError = error.calculateOnColor()

        AppLog.d("primary: ${primary.toColorHex()}, " +
                "secondary: ${secondary.toColorHex()}, " +
                "background: ${background.toColorHex()}, "
        )

        defaultColors.copy(
                primary = primary,
                primaryVariant = primaryVariant,
                onPrimary = onPrimary,
                secondary = secondary,
                secondaryVariant = secondary,
                onSecondary = onSecondary,
                surface = surface,
                onSurface = onSurface,
                background = background,
                onBackground = onBackground,
                error = error,
                onError = onError
        )
    }
}