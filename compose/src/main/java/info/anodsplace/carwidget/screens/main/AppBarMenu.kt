package info.anodsplace.carwidget.screens.main

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.carwidget.screens.NavItem
import info.anodsplace.carwidget.screens.UiAction
import info.anodsplace.carwidget.screens.WidgetDialog
import kotlinx.coroutines.launch

sealed interface AppBarTileColor {
    object Hidden : AppBarTileColor
    object Icon : AppBarTileColor
    data class Value(val color: Color) : AppBarTileColor
}

@Composable
fun rememberTileColor(currentSkinValue: String, prefs: WidgetInterface): AppBarTileColor {
    val color by prefs.observe<Int>(WidgetSettings.BUTTON_COLOR).collectAsState(initial = prefs.tileColor)
    val palette by prefs.observe<Boolean>(WidgetSettings.PALETTE_BG).collectAsState(initial = prefs.paletteBackground)
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
fun AppBarMenu(tileColor: AppBarTileColor, appWidgetId: Int, currentSkinValue: String, action: (UiAction) -> Unit, navController: NavHostController) {
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    val isIconsMono = false
    when (tileColor) {
        is AppBarTileColor.Value -> {
            AppBarColorButton(color = tileColor.color, descRes = R.string.choose_color) {
                action(UiAction.ShowDialog(WidgetDialog.ChooseTileColor))
            }
        }
        AppBarTileColor.Icon -> {
            AppBarButton(image = Icons.Filled.FormatColorFill, descRes = R.string.choose_color) {
                action(UiAction.ShowDialog(WidgetDialog.ChooseTileColor))
            }
        }
        AppBarTileColor.Hidden -> { }
    }
    AppBarButton(image = Icons.Filled.Check, descRes = android.R.string.ok) {
        scope.launch {  action(UiAction.ApplyWidget(appWidgetId, currentSkinValue)) }
    }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(id = R.string.more))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(onClick = {
                action(UiAction.ShowDialog(WidgetDialog.ChooseShortcutsNumber))
                expanded = false
            }) { Text(text = stringResource(id = R.string.number)) }
            DropdownMenuItem(onClick = {
                action(UiAction.ShowDialog(WidgetDialog.ChooseBackgroundColor))
                expanded = false
            }) { Text(text = stringResource(id = R.string.pref_bg_color_title)) }
            DropdownMenuItem(onClick = {
                action(UiAction.ShowDialog(WidgetDialog.ChooseIconsTheme))
                expanded = false
            }) { Text(text = stringResource(id = R.string.icons_theme)) }
            DropdownMenuItem(onClick = {
                action(UiAction.ShowDialog(WidgetDialog.SwitchIconsMono(!isIconsMono)))
                expanded = false
            }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(id = R.string.pref_icons_mono_title))
                    Checkbox(checked = isIconsMono, onCheckedChange = {
                        action(UiAction.ShowDialog(WidgetDialog.SwitchIconsMono(!isIconsMono)))
                        expanded = false
                    }, modifier = Modifier.padding(start = 8.dp))
                }
            }
            DropdownMenuItem(onClick = {
                action(UiAction.ShowDialog(WidgetDialog.ChooseIconsScale))
                expanded = false
            }) { Text(text = stringResource(id = R.string.pref_scale_icon)) }
            Divider()
            DropdownMenuItem(onClick = {
                navController.navigate(NavItem.Tab.CurrentWidget.MoreSettings.route)
                expanded = false
            }) { Text(text = stringResource(id = R.string.more)) }
        }
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