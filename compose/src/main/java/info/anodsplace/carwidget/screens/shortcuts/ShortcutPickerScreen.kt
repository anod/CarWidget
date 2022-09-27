package info.anodsplace.carwidget.screens.shortcuts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.chooser.*
import info.anodsplace.carwidget.content.shortcuts.CreateShortcutResult
import info.anodsplace.carwidget.content.shortcuts.ShortcutResources
import info.anodsplace.carwidget.utils.forPickShortcutLocal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

sealed class ShortcutPickerState {
    object Initial: ShortcutPickerState()
    object Apps: ShortcutPickerState()
    object CarWidget: ShortcutPickerState()
}

@Composable
fun ShortcutPickerScreen(viewActions: Flow<ShortcutPickerViewAction> = emptyFlow(), onEvent: (ShortcutPickerViewEvent) -> Unit, onDismissRequest: () -> Unit, shortcutResources: ShortcutResources, imageLoader: ImageLoader) {
    val context = LocalContext.current
    var screenState by remember { mutableStateOf<ShortcutPickerState>(ShortcutPickerState.Initial) }
    val activityLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult(), onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                onEvent(ShortcutPickerViewEvent.Save(result.data!!, isApp = false))
            }
            onDismissRequest()
    })

    LaunchedEffect(true) {
        viewActions.collect { action ->
            when (action) {
                is ShortcutPickerViewAction.CreateResult -> {
                    makeToast(action.result, context)
                }
            }
        }
    }

    when (screenState) {
        ShortcutPickerState.Initial -> CreateShortcutChooser(
            onNewState = { screenState = it },
            onIntent = { activityLauncher.launch(it) },
            onDismissRequest = onDismissRequest,
            imageLoader = imageLoader
        )
        ShortcutPickerState.Apps -> AppChooser(
            onChoose = { entry ->
                onEvent(ShortcutPickerViewEvent.Save(entry.getIntent(baseIntent = null), isApp = false))
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
    val loader by remember { mutableStateOf(QueryIntentLoader(context, baseIntent)) }
    ChooserDialog(
        modifier = Modifier.padding(horizontal = 16.dp),
        loader = loader,
        headers = listOf(
            Header(0, stringResource(R.string.applications), iconVector = Icons.Filled.List),
            Header(1, stringResource(R.string.car_widget_shortcuts), iconVector = Icons.Filled.List),
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
    val titles = stringArrayResource(id = R.array.carwidget_shortcuts)
    val loader = remember { createCarWidgetShortcuts(titles, context, shortcutResources) }
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

private fun createCarWidgetShortcuts(titles: Array<String>, context: Context, shortcutResources: ShortcutResources): StaticChooserLoader {
    val icons = shortcutResources.internalShortcuts.icons
    val list = titles.mapIndexed { i, title ->
        val intent = Intent().forPickShortcutLocal(i, titles[i], icons[i], context, shortcutResources)
        ChooserEntry(componentName = null, title = title, intent = intent, iconRes = icons[i])
    }
    return StaticChooserLoader(list)
}

private fun makeToast(resultCode: CreateShortcutResult, context: Context) {
    if (resultCode == CreateShortcutResult.SuccessAppShortcut) {
        Toast.makeText(context, R.string.app_shortcuts_limited, Toast.LENGTH_SHORT).show()
    } else if (resultCode == CreateShortcutResult.FailedAppShortcut) {
        Toast.makeText(context, R.string.app_shortcuts_limited, Toast.LENGTH_LONG).show()
    }
}