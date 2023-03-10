package info.anodsplace.carwidget.screens.about

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.chooser.ChooserDialog
import info.anodsplace.carwidget.chooser.Header
import info.anodsplace.carwidget.chooser.MediaListLoader
import info.anodsplace.carwidget.content.backup.Backup
import info.anodsplace.compose.PreferenceCategory
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.framework.content.CreateDocument
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
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            disabledContentColor = MaterialTheme.colorScheme.onSecondary
                .copy(alpha = 0.38f),
            disabledContainerColor =  MaterialTheme.colorScheme.secondary,
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
                stringResource(id = titleRes).uppercase(Locale.getDefault())
            } else title.uppercase(Locale.getDefault()))

            if (subtitle.isNotEmpty()) {
                Text(text = subtitle.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.bodySmall.copy(
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
fun AboutScreen(screenState: AboutScreenState, onEvent: (AboutScreenStateEvent) -> Unit, imageLoader: ImageLoader, innerPadding: PaddingValues = PaddingValues(0.dp)) {
    var restoreAnimation by remember { mutableStateOf(false) }
    var backupWidgetAnimation by remember { mutableStateOf(false) }
    var showOpenCarDock by remember { mutableStateOf(false) }
    var showMusicAppDialog by remember { mutableStateOf(false) }

    val openDocumentLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { destUri ->
        if (destUri == null) {
            restoreAnimation = false
        } else {
            onEvent(AboutScreenStateEvent.Restore(destUri))
        }
    }

    val createDocumentLauncherWidget = rememberLauncherForActivityResult(contract = CreateDocument("application/json")) { destUri ->
        if (destUri == null) {
            backupWidgetAnimation = false
        } else {
            onEvent(AboutScreenStateEvent.BackupWidget(destUri))
        }
    }

    if (screenState.restoreStatus != Backup.NO_RESULT) {
        restoreAnimation = false
    }

    if (screenState.backupStatus != Backup.NO_RESULT) {
        backupWidgetAnimation = false
    }

    Surface {
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            AboutTitle(titleRes = R.string.app_preferences)
            AboutButton(titleRes = R.string.app_theme, subtitle = screenState.themeName, onClick = {
                onEvent(AboutScreenStateEvent.ChangeTheme)
            })
            AboutButton(titleRes = R.string.music_app, subtitle = screenState.musicApp, onClick = {
                showMusicAppDialog = true
            })
            AboutButton(titleRes = R.string.default_car_dock_app, onClick = { showOpenCarDock = true })
            AboutTitle(titleRes = R.string.pref_backup_title)
            AboutButton(
                titleRes = R.string.backup_current_widget,
                loader = backupWidgetAnimation,
                enabled = screenState.isValidWidget,
                onClick = {
                    try {
                        backupWidgetAnimation = true
                        createDocumentLauncherWidget.launch(
                            CreateDocument.Args("carwidget-${screenState.appWidgetId}" + Backup.FILE_EXT_JSON)
                        )
                    } catch (e: Exception) {
                        AppLog.e(e)
                        onEvent(AboutScreenStateEvent.ShowToast("Cannot start activity: ACTION_CREATE_DOCUMENT"))
                    }
                })
            AboutButton(titleRes = R.string.restore, loader = restoreAnimation, onClick = {
                restoreAnimation = true
                openDocumentLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
            })
            AboutTitle(titleRes = R.string.information_title)
            AboutButton(title = screenState.appVersion, subtitle = stringResource(id = R.string.version_summary), onClick = {
                onEvent(AboutScreenStateEvent.OpenPlayStoreDetails)
            })
        }
    }

    if (showOpenCarDock) {
        AlertDialog(
            modifier = Modifier.padding(16.dp),
            title = { Text(text = stringResource(id = R.string.default_car_dock_app)) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = stringResource(id = R.string.cardock_text1))
                    Button(
                        modifier = Modifier.padding(16.dp),
                        onClick = { onEvent(AboutScreenStateEvent.OpenDefaultCarDock) }
                    ) { Text(text = stringResource(id = R.string.cardock_btn_1)) }
                    Text(text = stringResource(id = R.string.cardock_text2))
                }
            },
            confirmButton = { },
            onDismissRequest = { showOpenCarDock = false })
    }

    if (showMusicAppDialog) {
        val context = LocalContext.current
        val loader = remember { MediaListLoader(context) }
        ChooserDialog(
            modifier = Modifier.padding(16.dp),
            headers = listOf(
                Header(0, stringResource(R.string.show_choice), iconVector = Icons.Filled.List)
            ),
            loader = loader,
            onDismissRequest = { showMusicAppDialog = false },
            onClick = { entry ->
                onEvent(AboutScreenStateEvent.ChangeMusicApp(entry.componentName))
                showMusicAppDialog = false
            },
            imageLoader = imageLoader
        )
    }
}

@Preview("About Screen Light")
@Composable
fun PreviewAboutScreenLight() {
    CarWidgetTheme {
            AboutScreen(
                screenState = AboutScreenState(0, 0, "Light", "CHOICE", "DUMMY"),
                onEvent = { },
                imageLoader = ImageLoader.Builder(LocalContext.current).build()
            )
    }
}