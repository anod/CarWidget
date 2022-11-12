package info.anodsplace.carwidget.screens.main

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.carwidget.screens.WidgetDialogType

sealed interface AppBarTileColor {
    object Hidden : AppBarTileColor
    object Icon : AppBarTileColor
    data class Value(val color: Color) : AppBarTileColor
}

@Composable
fun rememberTileColor(currentSkinValue: String, prefs: WidgetInterface): AppBarTileColor {
    val color by prefs.observe<Int>(WidgetSettings.BUTTON_COLOR)
        .collectAsState(initial = prefs.tileColor)
    val palette by prefs.observe<Boolean>(WidgetSettings.PALETTE_BG)
        .collectAsState(initial = prefs.paletteBackground)
    return remember(currentSkinValue, color, palette) {
        if (currentSkinValue == WidgetInterface.SKIN_WINDOWS7) {
            if (palette) {
                AppBarTileColor.Icon
            }
            AppBarTileColor.Value(color = Color(color))
        }
        AppBarTileColor.Hidden
    }
}

@Composable
fun AppBarActions(
    tileColor: AppBarTileColor,
    appWidgetId: Int,
    currentSkinValue: String,
    onEvent: (MainViewEvent) -> Unit,
) {
    when (tileColor) {
        is AppBarTileColor.Value -> {
            AppBarColorButton(color = tileColor.color, descRes = R.string.choose_color) {
                onEvent(MainViewEvent.ShowDialog(WidgetDialogType.ChooseTileColor))
            }
        }
        AppBarTileColor.Icon -> {
            AppBarButton(image = Icons.Filled.FormatColorFill, descRes = R.string.choose_color) {
                onEvent(MainViewEvent.ShowDialog(WidgetDialogType.ChooseTileColor))
            }
        }
        AppBarTileColor.Hidden -> {}
    }
    AppBarButton(image = Icons.Filled.Check, descRes = android.R.string.ok) {
        onEvent(MainViewEvent.ApplyWidget(appWidgetId, currentSkinValue))
    }
}

@Composable
fun AppBarButton(image: ImageVector, @StringRes descRes: Int, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(imageVector = image, contentDescription = stringResource(id = descRes))
    }
}

@Composable
fun AppBarColorButton(color: Color, @StringRes descRes: Int, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            modifier = Modifier.size(48.dp),
            painter = ColorPainter(color),
            contentDescription = stringResource(id = descRes)
        )
    }
}