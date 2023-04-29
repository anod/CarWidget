package info.anodsplace.carwidget.about

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.AppCoroutineScope
import info.anodsplace.carwidget.content.backup.Backup
import info.anodsplace.carwidget.content.backup.BackupCheckResult
import info.anodsplace.carwidget.content.backup.BackupManager
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.di.unaryPlus
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.framework.content.CommonActivityAction
import info.anodsplace.framework.content.forApplicationDetails
import info.anodsplace.framework.content.playStoreDetails
import info.anodsplace.framework.content.resolveDefaultCarDock
import info.anodsplace.framework.content.startActivitySafely
import info.anodsplace.viewmodel.BaseFlowViewModel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

data class AboutScreenState(
    val appWidgetId: Int,
    val themeIndex: Int,
    val themeName: String,
    val musicApp: String,
    val appVersion: String,
    val backupProgress: Boolean = false,
    val restoreProgress: Boolean = false,
    val restoreInCarDialog: Uri? = null
) {
    val isValidWidget: Boolean
        get() = appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID
}

sealed interface AboutScreenStateEvent {
    object ChangeTheme: AboutScreenStateEvent
    object OpenPlayStoreDetails: AboutScreenStateEvent
    object OpenDefaultCarDock: AboutScreenStateEvent
    class Backup(val dstUri: Uri) : AboutScreenStateEvent
    class Restore(val srcUri: Uri) : AboutScreenStateEvent
    class ChangeMusicApp(val component: ComponentName?) : AboutScreenStateEvent
    class RestoreInCar(val srcUri: Uri, val restoreInCar: Boolean) : AboutScreenStateEvent
}

class AboutViewModel(private val appWidgetIdScope: AppWidgetIdScope?): BaseFlowViewModel<AboutScreenState, AboutScreenStateEvent, CommonActivityAction>(), KoinComponent {

    class Factory(private val appWidgetIdScope: AppWidgetIdScope?): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AboutViewModel(appWidgetIdScope) as T
    }

    private val context: Context by inject()
    private val backupManager: BackupManager by inject()
    private val themes = context.resources.getStringArray(info.anodsplace.carwidget.content.R.array.app_themes)
    private val appSettings: AppSettings
        get() = get()

    private val appScope: AppCoroutineScope by inject()
    init {
        viewState =  AboutScreenState(
            appWidgetId = +appWidgetIdScope,
            themeIndex = appSettings.theme,
            themeName = themes[appSettings.theme],
            musicApp = renderMusicApp(),
            appVersion = renderVersion(),
        )
    }

    override fun handleEvent(event: AboutScreenStateEvent) {
        when (event) {
            is AboutScreenStateEvent.Backup -> {
                viewState = viewState.copy(backupProgress = true)
                appScope.launch {
                    val code = backupManager.backup(appWidgetIdScope!!, event.dstUri)
                    viewState = viewState.copy(backupProgress = false)
                    if (code != Backup.RESULT_DONE) {
                        emitAction(CommonActivityAction.ShowToast(resId = renderBackupCode(code), length = Toast.LENGTH_LONG))
                    }
                }
            }
            AboutScreenStateEvent.ChangeTheme -> {
                val newThemeIdx = when (viewState.themeIndex) {
                    AppSettings.system -> AppSettings.light
                    AppSettings.light -> AppSettings.dark
                    AppSettings.dark -> AppSettings.system
                    else -> AppSettings.system
                }
                appSettings.theme = newThemeIdx
                appSettings.applyPending()
                viewState = viewState.copy(themeIndex = appSettings.theme, themeName = themes[newThemeIdx])
            }
            is AboutScreenStateEvent.ChangeMusicApp -> {
                appSettings.musicApp = event.component
                viewState = viewState.copy(musicApp = renderMusicApp())
            }
            is AboutScreenStateEvent.Restore -> {
                viewState = viewState.copy(restoreProgress = true)
                appScope.launch {
                    when (val checkResult = backupManager.checkSrcUri(event.srcUri)) {
                        is BackupCheckResult.Error -> {
                            viewState = viewState.copy(restoreProgress = false)
                            emitAction(CommonActivityAction.ShowToast(resId = renderRestoreCode(checkResult.errorCode), length = Toast.LENGTH_LONG))
                        }
                        is BackupCheckResult.Success -> {
                            if (checkResult.hasInCar) {
                                viewState = viewState.copy(restoreInCarDialog = event.srcUri, restoreProgress = false)
                            } else {
                                val code = backupManager.restore(appWidgetIdScope!!, event.srcUri, restoreInCar = false)
                                viewState = viewState.copy(restoreProgress = false)
                                emitAction(CommonActivityAction.ShowToast(resId = renderRestoreCode(code), length = Toast.LENGTH_LONG))
                            }
                        }
                    }
                }
            }
            is AboutScreenStateEvent.RestoreInCar -> {
                viewState = viewState.copy(restoreInCarDialog = null, restoreProgress = true)
                appScope.launch {
                    val code = backupManager.restore(appWidgetIdScope!!, event.srcUri, restoreInCar = event.restoreInCar)
                    viewState = viewState.copy(restoreProgress = false)
                    emitAction(CommonActivityAction.ShowToast(resId = renderRestoreCode(code), length = Toast.LENGTH_LONG))
                }
            }
            AboutScreenStateEvent.OpenPlayStoreDetails -> {
                context.startActivitySafely(Intent().playStoreDetails(context.packageName))
            }
            AboutScreenStateEvent.OpenDefaultCarDock -> {
                context.resolveDefaultCarDock()?.also { packageName ->
                    context.startActivitySafely(Intent().forApplicationDetails(packageName))
                }
            }
        }
    }

    private fun renderMusicApp(): String {
        val musicAppCmp = appSettings.musicApp
        return if (musicAppCmp == null) {
            context.getString(info.anodsplace.carwidget.content.R.string.show_choice)
        } else {
            try {
                val info = context.packageManager
                    .getApplicationInfo(musicAppCmp.packageName, 0)
                info.loadLabel(context.packageManager).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                AppLog.e(e)
                musicAppCmp.flattenToShortString()
            }
        }
    }

    private fun renderVersion(): String {
        val appName = context.getString(info.anodsplace.carwidget.content.R.string.app_name)
        var versionName = ""
        try {
            versionName = context.packageManager
                .getPackageInfo(context.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            AppLog.e(e)
        }

        return context.getString(info.anodsplace.carwidget.content.R.string.version_title, appName, versionName)
    }

    private fun renderBackupCode(code: Int): Int {
        if (code == Backup.RESULT_DONE) {
            return info.anodsplace.carwidget.content.R.string.backup_done
        }
        return if (code == Backup.ERROR_FILE_WRITE) {
            info.anodsplace.carwidget.content.R.string.failed_to_write_file
        } else info.anodsplace.carwidget.content.R.string.unexpected_error
    }

    private fun renderRestoreCode(code: Int): Int {
        if (code == Backup.RESULT_DONE) {
            return info.anodsplace.carwidget.content.R.string.restore_done
        }
        return when (code) {
            Backup.ERROR_DESERIALIZE -> info.anodsplace.carwidget.content.R.string.restore_deserialize_failed
            Backup.ERROR_FILE_READ -> info.anodsplace.carwidget.content.R.string.failed_to_read_file
            Backup.ERROR_UNEXPECTED -> info.anodsplace.carwidget.content.R.string.unexpected_error
            Backup.ERROR_INCORRECT_FORMAT -> info.anodsplace.carwidget.content.R.string.backup_unknown_format
            else -> info.anodsplace.carwidget.content.R.string.unexpected_error
        }
    }
}