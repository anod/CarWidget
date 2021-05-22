package info.anodsplace.carwidget.compose

import androidx.compose.runtime.Composable

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.R

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

private val BlueGray600 = Color(0xFF546E7A)
private val BlueGray800 = Color(0xFF37474f)
private val BlueGray300 = Color(0xFF90a4ae)
private val BlueGray100 = Color(0xFFBBDEFB)

private val LightThemeColors = lightColors(
        primary = BlueGray600,
        primaryVariant = BlueGray800,
        secondary = BlueGray300,
        secondaryVariant = BlueGray100,
        background = Color.White,
        surface = BlueGray600,
        error = Color(0xbFF00020),
        onPrimary = Color.White,
        onSecondary = Color.Black,
        onBackground = Color.Black,
        onSurface = Color.White,
        onError = Color.White,
)

private val DarkThemeColors = darkColors(
        primary = BlueGray600,
        primaryVariant = BlueGray800,
        secondary = BlueGray300,
        background = Color.Black,
        surface = Color(0xFF001c26),
        error = Color(0xFFcf6679),
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White,
        onError = Color.Black
)

val CarWidgetShapes = Shapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(4.dp),
        large = RoundedCornerShape(8.dp)
)

@Composable
fun CarWidgetTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        content: @Composable () -> Unit) {
    MaterialTheme(
            colors = if (darkTheme) DarkThemeColors else LightThemeColors,
            typography = CarWidgetTypography,
            shapes = CarWidgetShapes,
            content = content
    )
}