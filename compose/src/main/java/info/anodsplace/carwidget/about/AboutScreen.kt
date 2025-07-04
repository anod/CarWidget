package info.anodsplace.carwidget.about

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import info.anodsplace.carwidget.chooser.ChooserDialog
import info.anodsplace.carwidget.chooser.Header
import info.anodsplace.carwidget.chooser.MediaListLoader
import info.anodsplace.carwidget.content.R
import info.anodsplace.carwidget.content.backup.Backup
import info.anodsplace.carwidget.permissions.RequestPermissionsDialog
import info.anodsplace.compose.PreferenceCategory
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.framework.content.CreateDocument
import info.anodsplace.framework.content.ScreenCommonAction
import info.anodsplace.framework.content.showToast
import java.util.Locale

@Composable
fun AboutScreen(
    screenState: AboutScreenState,
    onEvent: (AboutScreenStateEvent) -> Unit,
    imageLoader: ImageLoader,
    innerPadding: PaddingValues = PaddingValues(0.dp)
) {
    var showOpenCarDock by remember { mutableStateOf(false) }
    var showMusicAppDialog by remember { mutableStateOf(false) }

    val openDocumentLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { destUri ->
        if (destUri != null) {
            onEvent(AboutScreenStateEvent.Restore(destUri))
        }
    }

    val createDocumentLauncherWidget = rememberLauncherForActivityResult(contract = CreateDocument("application/json")) { destUri ->
        if (destUri != null) {
            onEvent(AboutScreenStateEvent.Backup(destUri))
        }
    }

    val context = LocalContext.current

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
            if (screenState.isValidWidget) {
                AboutTitle(titleRes = R.string.pref_backup_title)
                AboutButton(
                    titleRes = R.string.backup_current_widget,
                    loader = screenState.backupProgress,
                    enabled = screenState.isValidWidget,
                    onClick = {
                        try {
                            createDocumentLauncherWidget.launch(
                                CreateDocument.Args("carwidget-${screenState.appWidgetId}" + Backup.FILE_EXT_JSON)
                            )
                        } catch (e: Exception) {
                            AppLog.e(e)
                            context.showToast(ScreenCommonAction.ShowToast(text = "Error - cannot perform ACTION_CREATE_DOCUMENT"))
                        }
                    })
                AboutButton(
                    titleRes = R.string.restore,
                    loader = screenState.restoreProgress,
                    enabled = screenState.isValidWidget,
                    onClick = {
                        try {
                            openDocumentLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                        } catch (e: Exception) {
                            AppLog.e(e)
                            context.showToast(ScreenCommonAction.ShowToast(text = "Error - cannot perform ACTION_OPEN_DOCUMENT"))
                        }
                    }
                )
            }
            AboutTitle(titleRes = R.string.information_title)
            AboutButton(title = screenState.appVersion, subtitle = stringResource(id = R.string.version_summary), onClick = {
                onEvent(AboutScreenStateEvent.OpenPlayStoreDetails)
            })
        }
    }

    if (screenState.restoreInCarDialog != null) {
        AlertDialog(
            modifier = Modifier.padding(16.dp),
            title = { Text(text = stringResource(id = R.string.update_incar_settings)) },
            text = {
                Text(text = stringResource(id = R.string.incar_backup_question))
            },
            confirmButton = {
                Button(
                    onClick = { onEvent(
                        AboutScreenStateEvent.RestoreInCar(
                            srcUri = screenState.restoreInCarDialog,
                            restoreInCar = true
                        )
                    ) }
                ) { Text(text = stringResource(id = R.string.update)) }
            },
            dismissButton = {
                Button(
                    onClick = { onEvent(
                        AboutScreenStateEvent.RestoreInCar(
                            srcUri = screenState.restoreInCarDialog,
                            restoreInCar = false
                        )
                    ) }
                ) { Text(text = stringResource(id = R.string.skip)) }
            },
            onDismissRequest = { })
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
                Header(0, stringResource(R.string.show_choice), iconVector = Icons.AutoMirrored.Filled.List)
            ),
            loader = loader,
            onDismissRequest = {
                showMusicAppDialog = false
            },
            onClick = { entry ->
                onEvent(AboutScreenStateEvent.ChangeMusicApp(entry.componentName))
                showMusicAppDialog = false
            },
            imageLoader = imageLoader
        )
    }

    if (screenState.missingPermissionsDialog.isNotEmpty()) {
        RequestPermissionsDialog(
            missingPermissions = screenState.missingPermissionsDialog,
            onResult = { _, ex -> onEvent(AboutScreenStateEvent.RequestPermissionResult(ex)) }
        )
    }
}


@Composable
private fun AboutButton(
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
private fun AboutTitle(@StringRes titleRes: Int) {
    PreferenceCategory(
        PreferenceItem.Category(titleRes = titleRes)
    )
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