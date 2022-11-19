package info.anodsplace.carwidget.screens.main

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.carwidget.screens.WidgetDialogType
import info.anodsplace.carwidget.screens.widget.SkinList

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
    shortcutNumber: Int,
    tileColor: AppBarTileColor,
    appWidgetId: Int,
    skinList: SkinList,
    onEvent: (MainViewEvent) -> Unit,
    showSkinSelector: Boolean = true
) {
    var pickNumber by remember { mutableStateOf(false) }
    var pickSkin by remember { mutableStateOf(false) }

    if (showSkinSelector) {
        OutlinedButton(
            onClick = { pickSkin = true },
            modifier = Modifier.padding(horizontal = 4.dp),
            contentPadding = PaddingValues(4.dp)
        ) {
            Text(
                text = skinList.current.title,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
    }

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

    OutlinedButton(
        onClick = { pickNumber = true },
        modifier = Modifier.width(56.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(
            text = shortcutNumber.toString(),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center
        )
    }
    AppBarButton(image = Icons.Filled.Check, descRes = android.R.string.ok) {
        onEvent(MainViewEvent.ApplyWidget(appWidgetId, skinList.current.value))
    }

    DropdownMenu(expanded = pickNumber, onDismissRequest = { pickNumber = false }) {
        Text(
            text = stringResource(id = R.string.number_shortcuts_title),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
            textAlign = TextAlign.Center
        )
        val boxSize = mapOf(4 to 12.dp, 6 to 10.dp, 8 to 8.dp,10 to 6.dp)
        arrayOf(4,6,8,10).forEach { number ->
            DropdownMenuItem(
                text = { Text(text = number.toString(), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                leadingIcon = { ShortcutsNumber(number = number, current = shortcutNumber, boxSize = boxSize[number]!!)},
                onClick = {
                    onEvent(MainViewEvent.WidgetUpdateShortcuts(number))
                    pickNumber = false
                }
            )
        }
    }

    DropdownMenu(expanded = pickSkin, onDismissRequest = { pickSkin = false }) {
        (0 until skinList.count).forEach { skinPosition ->
            val skinItem = skinList[skinPosition]
            DropdownMenuItem(
                text = { Text(text = skinItem.title, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                leadingIcon = { Icon(
                    imageVector = if (skinList.selectedSkinPosition == skinPosition)
                        Icons.Default.RadioButtonChecked
                    else
                        Icons.Default.RadioButtonUnchecked,
                    contentDescription = null
                ) },
                onClick = {
                    onEvent(MainViewEvent.WidgetUpdateSkin(skinPosition))
                    pickSkin = false
                }
            )
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


@Composable
fun ShortcutsNumber(number: Int, current: Int, boxSize: Dp) {
    val rowsNumber = number / 2
    Box(modifier = Modifier
        .padding(4.dp)) {
        Column(
            modifier = Modifier
                .size(size = 40.dp)
                .border(
                    width = if (current == number) 2.dp else 1.dp,
                    color = if (current == number) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                    shape = RoundedCornerShape(4.dp)
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (row in (0 until rowsNumber)) {
                Row(modifier = Modifier.padding(top = 1.dp)) {
                    Box(modifier = Modifier
                        .size(boxSize)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        )) {
                    }
                    Box(modifier = Modifier
                        .size(boxSize)
                        .padding(start = 1.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        ))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview()
@Composable
fun AppBarActionsPreview() {
    val currentSkin = "windows7"
    val context = LocalContext.current
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        actions = {
            AppBarActions(
                shortcutNumber = 10,
                tileColor = rememberTileColor(currentSkin, WidgetInterface.NoOp()),
                appWidgetId = 1,
                skinList = SkinList(WidgetInterface.SKIN_YOU, context),
                onEvent = { },
            )
        }
    )
}

@Preview()
@Composable
fun NumberPickerPreview() {
        val boxSize = mapOf(4 to 12.dp, 6 to 10.dp, 8 to 8.dp,10 to 6.dp)
    Column {
        Text(
            text = stringResource(id = R.string.number_shortcuts_title),
            modifier = Modifier.padding(2.dp))

        arrayOf(4,6,8,10).forEach { number ->
            DropdownMenuItem(
                text = { Text(text = number.toString(), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                leadingIcon = { ShortcutsNumber(number = number, current = 10, boxSize = boxSize[number]!!)},
                onClick = {  }
            )
        }
    }
}