package info.anodsplace.carwidget.shortcut

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import info.anodsplace.carwidget.chooser.AllAppsIntentLoader
import info.anodsplace.carwidget.chooser.ChooserDialog
import info.anodsplace.carwidget.chooser.ChooserEntry
import info.anodsplace.carwidget.chooser.Header
import info.anodsplace.carwidget.chooser.QueryIntentLoader
import info.anodsplace.carwidget.chooser.StaticChooserLoader
import info.anodsplace.carwidget.content.shortcuts.InternalShortcut
import info.anodsplace.carwidget.content.shortcuts.ShortcutResources
import info.anodsplace.carwidget.utils.forPickShortcutLocal

sealed interface ShortcutPickerState {
    data object Initial: ShortcutPickerState
    data object Apps: ShortcutPickerState
    data object CarWidget: ShortcutPickerState
}

@Composable
fun ShortcutPickerScreen(
    onEvent: (ShortcutPickerViewEvent) -> Unit,
    onDismissRequest: () -> Unit,
    shortcutResources: ShortcutResources,
    imageLoader: ImageLoader
) {
    var screenState by remember { mutableStateOf<ShortcutPickerState>(ShortcutPickerState.Initial) }
    val activityLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult(), onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                onEvent(ShortcutPickerViewEvent.Save(result.data!!, isApp = false))
            }
            onDismissRequest()
    })

    when (screenState) {
        ShortcutPickerState.Initial -> CreateShortcutChooser(
            onNewState = { screenState = it },
            onIntent = {
                try {
                    activityLauncher.launch(it)
                } catch (e: Exception) {
                    onEvent(ShortcutPickerViewEvent.LaunchShortcutError(exception = e))
                }
            },
            onDismissRequest = onDismissRequest,
            imageLoader = imageLoader
        )
        ShortcutPickerState.Apps -> AppChooser(
            onChoose = { entry ->
                onEvent(ShortcutPickerViewEvent.Save(entry.getIntent(baseIntent = null), isApp = true))
                onDismissRequest()
            },
            onDismissRequest = { screenState = ShortcutPickerState.Initial },
            imageLoader = imageLoader
        )
        ShortcutPickerState.CarWidget -> CarWidgetChooser(
            shortcutResources,
            onChoose = { entry ->
                onEvent(ShortcutPickerViewEvent.Save(entry.getIntent(baseIntent = null), isApp = false))
                onDismissRequest()
            },
            onDismissRequest = { screenState = ShortcutPickerState.Initial },
            imageLoader = imageLoader
        )
    }
}

@Composable
fun CreateShortcutChooser(onNewState: (ShortcutPickerState) -> Unit, onIntent: (Intent) -> Unit, onDismissRequest: () -> Unit, imageLoader: ImageLoader) {
    val context = LocalContext.current
    val baseIntent = remember { Intent(Intent.ACTION_CREATE_SHORTCUT) }
    val loader = remember { QueryIntentLoader(context, baseIntent) }
    ChooserDialog(
        modifier = Modifier.padding(horizontal = 16.dp),
        loader = loader,
        headers = listOf(
            Header(0, stringResource(info.anodsplace.carwidget.content.R.string.applications), iconVector = Icons.AutoMirrored.Filled.List),
            Header(1, stringResource(info.anodsplace.carwidget.content.R.string.car_widget_shortcuts), iconVector = Icons.AutoMirrored.Filled.List),
        ),
        onDismissRequest = onDismissRequest,
        onClick = { entry ->
            when(entry) {
                is Header -> {
                    if (entry.headerId == 0) {
                        onNewState(ShortcutPickerState.Apps)
                    } else if (entry.headerId == 1) {
                        onNewState(ShortcutPickerState.CarWidget)
                    }
                }
                else -> onIntent(entry.getIntent(baseIntent = baseIntent))
            }
        },
        imageLoader = imageLoader
    )
}

@Composable
fun AppChooser(onChoose: (ChooserEntry) -> Unit, onDismissRequest: () -> Unit, imageLoader: ImageLoader) {
    val context = LocalContext.current
    val loader = remember { AllAppsIntentLoader(context) }
    ChooserDialog(
        modifier = Modifier.padding(horizontal = 16.dp),
        loader = loader,
        onDismissRequest = onDismissRequest,
        onClick = onChoose,
        imageLoader = imageLoader
    )
}

@Composable
fun CarWidgetChooser(shortcutResources: ShortcutResources, onChoose: (ChooserEntry) -> Unit, onDismissRequest: () -> Unit, imageLoader: ImageLoader) {
    val context = LocalContext.current
    val loader = remember { createCarWidgetShortcuts(context, shortcutResources) }
    ChooserDialog(
        modifier = Modifier.padding(horizontal = 16.dp),
        loader = loader,
        onDismissRequest = onDismissRequest,
        onClick = onChoose,
        imageLoader = imageLoader
    )
}

@Composable
fun IntentChooser(intent: Intent, onChoose: (ChooserEntry) -> Unit, onDismissRequest: () -> Unit, imageLoader: ImageLoader) {
    val context = LocalContext.current
    val loader = remember { QueryIntentLoader(context, intent) }
    ChooserDialog(
        modifier = Modifier.padding(horizontal = 16.dp),
        loader = loader,
        onDismissRequest = onDismissRequest,
        onClick = onChoose,
        imageLoader = imageLoader
    )
}

private fun createCarWidgetShortcuts(context: Context, shortcutResources: ShortcutResources): StaticChooserLoader {
    val shortcuts = InternalShortcut.all
    val titles = InternalShortcut.titles(context)
    val list = shortcuts.map { shortcut ->
        val title = titles[shortcut.index]
        val icon = shortcutResources.internalShortcuts.icons[shortcut.index]
        val intent = Intent().forPickShortcutLocal(shortcut, title, icon, context, shortcutResources)
        ChooserEntry(componentName = null, title = title, intent = intent, iconRes = icon)
    }
    return StaticChooserLoader(list)
}
