package info.anodsplace.carwidget

import android.app.UiModeManager
import android.content.Context
import androidx.annotation.ColorRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

data class WidgetColorScheme(
    val colorDynamicWidgetBackground: Color,
    val colorDynamicWidgetOnBackground: Color,
    val colorDynamicWidgetPrimary: Color,
    val colorDynamicWidgetPrimaryInverse: Color,
    val colorDynamicWidgetOnWallpaper: Color,
) {
    constructor(
        context: Context,
        colorDynamicWidgetBackground: Int,
        colorDynamicWidgetOnBackground: Int,
        colorDynamicWidgetPrimary: Int,
        colorDynamicWidgetPrimaryInverse: Int,
        colorDynamicWidgetOnWallpaper: Int
    ) : this(
        colorDynamicWidgetBackground = getColor(context, colorDynamicWidgetBackground),
        colorDynamicWidgetOnBackground = getColor(context, colorDynamicWidgetOnBackground),
        colorDynamicWidgetPrimary = getColor(context, colorDynamicWidgetPrimary),
        colorDynamicWidgetPrimaryInverse = getColor(context, colorDynamicWidgetPrimaryInverse),
        colorDynamicWidgetOnWallpaper = getColor(context, colorDynamicWidgetOnWallpaper),
    )

    companion object {
        private fun getColor(context: Context, @ColorRes resId: Int) = Color(context.resources.getColor(resId, context.theme))
    }
}

data class WidgetRadius(
    val background: Float,
    val inner: Float
)

data class WidgetSystemTheme(
    val colorScheme: WidgetColorScheme,
    val radius: WidgetRadius
)

internal val LocalWidgetSystemTheme = compositionLocalOf<WidgetSystemTheme> {
    error("No color scheme provided")
}

@Composable
fun WidgetSystemTheme(uiMode: Int = UiModeManager.MODE_NIGHT_AUTO, content: @Composable (WidgetSystemTheme) -> Unit) {
    val darkTheme = if (uiMode == UiModeManager.MODE_NIGHT_AUTO)
        isSystemInDarkTheme()
    else uiMode == UiModeManager.MODE_NIGHT_YES
    val context = LocalContext.current
    val widgetColorScheme = if (supportsDynamic()) {
        if (darkTheme)
            WidgetColorScheme(
                context = context,
                colorDynamicWidgetBackground = android.R.color.system_accent2_800,
                colorDynamicWidgetOnBackground = android.R.color.system_accent2_100,
                colorDynamicWidgetPrimary = android.R.color.system_accent1_200,
                colorDynamicWidgetPrimaryInverse = android.R.color.system_accent1_600,
                colorDynamicWidgetOnWallpaper = android.R.color.system_accent1_200,
            )
        else
            WidgetColorScheme(
                context = context,
                colorDynamicWidgetBackground = android.R.color.system_accent2_50,
                colorDynamicWidgetOnBackground = android.R.color.system_accent2_900,
                colorDynamicWidgetPrimary = android.R.color.system_accent1_600,
                colorDynamicWidgetPrimaryInverse = android.R.color.system_accent1_200,
                colorDynamicWidgetOnWallpaper = android.R.color.system_accent2_50,
            )
    } else {
        WidgetColorScheme(
            colorDynamicWidgetBackground = MaterialTheme.colorScheme.secondaryContainer,
            colorDynamicWidgetOnBackground = MaterialTheme.colorScheme.onSecondaryContainer,
            colorDynamicWidgetPrimary = MaterialTheme.colorScheme.primary,
            colorDynamicWidgetPrimaryInverse = MaterialTheme.colorScheme.inversePrimary,
            colorDynamicWidgetOnWallpaper = MaterialTheme.colorScheme.secondaryContainer,
        )
    }

    val widgetRadius = if (supportsDynamic()) {
        WidgetRadius(
            background = context.resources.getDimension(android.R.dimen.system_app_widget_background_radius),
            inner = context.resources.getDimension(android.R.dimen.system_app_widget_inner_radius)
        )
    } else {
        val size = with(LocalDensity.current) { 16.dp.toPx() }
        WidgetRadius(
            background = size,
            inner = size
        )
    }

    val widgetSystemTheme = WidgetSystemTheme(
        colorScheme = widgetColorScheme,
        radius = widgetRadius
    )

    content(widgetSystemTheme)
}