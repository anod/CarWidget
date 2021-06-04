package info.anodsplace.carwidget.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.chooser.ChooserDialog
import info.anodsplace.carwidget.chooser.ChooserEntry
import info.anodsplace.carwidget.chooser.Header
import info.anodsplace.carwidget.chooser.MediaListLoader
import info.anodsplace.carwidget.compose.BackgroundSurface
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.carwidget.compose.PreferenceCategory
import info.anodsplace.carwidget.compose.PreferenceItem
import info.anodsplace.carwidget.content.backup.Backup
import info.anodsplace.framework.content.CreateDocument
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun AboutButton(
    @StringRes titleRes: Int = 0,
    title: String = "",
    subtitle: String = "",
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    loader: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colors.onSurface
        ),
        enabled = enabled
    ) {
        if (loader) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = if (titleRes != 0) {
                stringResource(id = titleRes).toUpperCase(Locale.getDefault())
            } else title.toUpperCase(Locale.getDefault()))
            if (subtitle.isNotEmpty()) {
                Text(text = subtitle.toUpperCase(Locale.getDefault()),
                    style = MaterialTheme.typography.caption.copy(
                        fontSize = 8.sp
                    ))
            }
        }
    }
}

@Composable
fun AboutTitle(@StringRes titleRes: Int) {
    PreferenceCategory(
        PreferenceItem.Category(titleRes = titleRes)
    )
}

@Composable
fun AboutScreen(screenState: AboutScreenState, action: MutableSharedFlow<AboutUiAction>, modifier: Modifier = Modifier) {
    val coroutinesScope = rememberCoroutineScope()
    var restoreAnimation by remember { mutableStateOf(false) }
    var backupInCarAnimation by remember { mutableStateOf(false) }
    var backupWidgetAnimation by remember { mutableStateOf(false) }
    var showOpenCarDock by remember { mutableStateOf(false) }
    var showMusicAppDialog by remember { mutableStateOf(false) }

    val openDocumentLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { destUri ->
        if (destUri == null) {
            restoreAnimation = false
        } else {
            coroutinesScope.launch { action.emit(AboutUiAction.Restore(destUri)) }
        }
    }

    val createDocumentLauncherInCar = rememberLauncherForActivityResult(contract = CreateDocument()) { destUri ->
        if (destUri == null) {
            backupInCarAnimation = false
        } else {
            coroutinesScope.launch { action.emit(AboutUiAction.BackupInCar(destUri)) }
        }
    }

    val createDocumentLauncherWidget = rememberLauncherForActivityResult(contract = CreateDocument()) { destUri ->
        if (destUri == null) {
            backupWidgetAnimation = false
        } else {
            coroutinesScope.launch { action.emit(AboutUiAction.BackupWidget(destUri)) }
        }
    }

    if (screenState.restoreStatus != Backup.NO_RESULT) {
        restoreAnimation = false
    }

    if (screenState.backupStatus != Backup.NO_RESULT) {
        backupInCarAnimation = false
        backupWidgetAnimation = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        AboutTitle(titleRes = R.string.app_preferences)
        AboutButton(titleRes = R.string.app_theme, subtitle = screenState.themeName, onClick = {
            coroutinesScope.launch { action.emit(AboutUiAction.ChangeTheme) }
        })
        AboutButton(titleRes = R.string.music_app, subtitle = screenState.musicApp, onClick = {
            showMusicAppDialog = true
        })
        AboutButton(titleRes = R.string.default_car_dock_app, onClick = { showOpenCarDock = true })
        AboutTitle(titleRes = R.string.pref_backup_title)
        AboutButton(titleRes = R.string.backup_current_widget, loader = backupWidgetAnimation, enabled = screenState.isValidWidget, onClick = {
            try {
                backupWidgetAnimation = true
                createDocumentLauncherWidget.launch(CreateDocument.Args( "application/json", "carwidget-${screenState.appWidgetId}" + Backup.FILE_EXT_JSON))
            } catch (e: Exception) {
                AppLog.e(e)
                coroutinesScope.launch { action.emit(AboutUiAction.ShowToast("Cannot start activity: ACTION_CREATE_DOCUMENT")) }
            }
        })
        AboutButton(titleRes = R.string.backup_incar_settings, loader = backupInCarAnimation, onClick = {
            try {
                backupInCarAnimation = true
                createDocumentLauncherInCar.launch(CreateDocument.Args( "application/json", Backup.FILE_INCAR_JSON))
            } catch (e: Exception) {
                AppLog.e(e)
                coroutinesScope.launch { action.emit(AboutUiAction.ShowToast("Cannot start activity: ACTION_CREATE_DOCUMENT")) }
            }
        })
        AboutButton(titleRes = R.string.restore, loader = restoreAnimation, onClick = {
            restoreAnimation = true
            openDocumentLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
        })
        AboutTitle(titleRes = R.string.information_title)
        AboutButton(title = screenState.appVersion, subtitle = stringResource(id = R.string.version_summary), onClick = {
            coroutinesScope.launch { action.emit(AboutUiAction.OpenPlayStoreDetails) }
        })
    }

    if (showOpenCarDock) {
        AlertDialog(
            title = { Text(text = stringResource(id = R.string.default_car_dock_app)) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = stringResource(id = R.string.cardock_text1))
                    Button(
                        modifier = Modifier.padding(16.dp),
                        onClick = { coroutinesScope.launch { action.emit(AboutUiAction.OpenDefaultCarDock) } }
                    ) { Text(text = stringResource(id = R.string.cardock_btn_1)) }
                    Text(text = stringResource(id = R.string.cardock_text2))
                }
            },
            buttons = { },
            onDismissRequest = { showOpenCarDock = false })
    }

    if (showMusicAppDialog) {
        val context = LocalContext.current
        val loader by remember { mutableStateOf(MediaListLoader(context)) }
        ChooserDialog(
            headers = listOf(
                Header(stringResource(R.string.show_choice), iconVector = Icons.Filled.List)
            ),
            appsLoader = loader,
            onDismissRequest = { showMusicAppDialog = false },
            onClick = { entry ->
                coroutinesScope.launch {
                    action.emit(AboutUiAction.ChangeMusicApp(entry.componentName))
                }
                showMusicAppDialog = false
            })
    }
}

@Preview("About Screen Light")
@Composable
fun PreviewAboutScreenLight() {
    CarWidgetTheme(darkTheme = false) {
        BackgroundSurface {
            AboutScreen(
                screenState = AboutScreenState(0, 0, "Light", "CHOICE", "DUMMY"),
                action = MutableSharedFlow()
            )
        }
    }
}

@Preview("About Screen Dark")
@Composable
fun PreviewAboutScreenDark() {
    CarWidgetTheme(darkTheme = true) {
        BackgroundSurface {
            AboutScreen(
                screenState = AboutScreenState(1, 0, "Dark", "Yandex", "v123"),
                action = MutableSharedFlow()
            )
        }
    }
}