package info.anodsplace.carwidget.compose

import androidx.compose.runtime.Composable

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.font
import androidx.compose.ui.text.font.fontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.anodsplace.carwidget.R

private val Rubik = fontFamily(
    font(R.font.rubik_regular),
    font(R.font.rubik_light, FontWeight.W300),
    font(R.font.rubik_medium, FontWeight.W500),
)

val CarWidgetTypography = Typography(
        h4 = TextStyle(
                fontFamily = Rubik,
        ),
        h5 = TextStyle(
                fontFamily = Rubik,
        ),
        h6 = TextStyle(
                fontFamily = Rubik,
        ),
        subtitle1 = TextStyle(
                fontFamily = Rubik,
        ),
        subtitle2 = TextStyle(
                fontFamily = Rubik,
        ),
        body1 = TextStyle(
                fontFamily = Rubik,
        ),
        body2 = TextStyle(
                fontFamily = Rubik,
        ),
        button = TextStyle(
                fontFamily = Rubik,
        ),
        caption = TextStyle(
                fontFamily = Rubik,
        ),
        overline = TextStyle(
                fontFamily = Rubik,
        )
)

private val BlueGray600 = Color(0x546E7A)
private val BlueGray800 = Color(0x37474f)
private val BlueGray300 = Color(0x90a4ae)
private val BlueGray100 = Color(0xFFBBDEFB)

private val LightThemeColors = lightColors(
        primary = BlueGray600,
        primaryVariant = BlueGray800,
        secondary = BlueGray300,
        secondaryVariant = BlueGray100,
        background = Color.White,
        surface = Color(0x546E7A),
        error = Color(0xb00020),
        onPrimary = Color.White,
        onSecondary = Color.Black,
        onBackground = Color.Black,
        onSurface = Color.Black,
        onError = Color.White,
)

private val DarkThemeColors = darkColors(
        primary = BlueGray600,
        primaryVariant = BlueGray800,
        secondary = BlueGray300,
        background = Color.Black,
        surface = Color(0x001c26),
        error = Color(0xcf6679),
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