package info.anodsplace.carwidget.screens.main

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.screens.UiAction
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@Composable
fun AppBarMenu(showColor: Boolean, appWidgetId: Int, currentSkinValue: String, action: MutableSharedFlow<UiAction>) {
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    val isIconsMono = false
    if (showColor) {
        AppBarButton(image = Icons.Filled.SmartDisplay, descRes = R.string.choose_color) {
            scope.launch {  action.emit(UiAction.ChooseTileColor) }
        }
    }
    AppBarButton(image = Icons.Filled.Check, descRes = android.R.string.ok) {
        scope.launch {  action.emit(UiAction.ApplyWidget(appWidgetId, currentSkinValue)) }
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
                scope.launch {  action.emit(UiAction.ChooseShortcutsNumber) }
                expanded = false
            }) { Text(text = stringResource(id = R.string.number)) }
            DropdownMenuItem(onClick = {
                scope.launch {  action.emit(UiAction.ChooseBackgroundColor) }
                expanded = false
            }) { Text(text = stringResource(id = R.string.pref_bg_color_title)) }
            DropdownMenuItem(onClick = {
                scope.launch { action.emit(UiAction.ChooseIconsTheme) }
                expanded = false
            }) { Text(text = stringResource(id = R.string.icons_theme)) }
            DropdownMenuItem(onClick = {
                scope.launch {  action.emit(UiAction.SwitchIconsMono(!isIconsMono)) }
                expanded = false
            }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(id = R.string.pref_icons_mono_title))
                    Checkbox(checked = isIconsMono, onCheckedChange = {
                        scope.launch {  action.emit(UiAction.SwitchIconsMono(!isIconsMono)) }
                        expanded = false
                    }, modifier = Modifier.padding(start = 8.dp))
                }
            }
            DropdownMenuItem(onClick = {
                scope.launch {  action.emit(UiAction.ChooseIconsScale) }
                expanded = false
            }) { Text(text = stringResource(id = R.string.pref_scale_icon)) }
            Divider()
            DropdownMenuItem(onClick = { scope.launch {
                action.emit(UiAction.ShowMoreSettings) }
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