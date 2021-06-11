package info.anodsplace.carwidget.screens.about

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.content.backup.Backup
import info.anodsplace.carwidget.content.backup.BackupManager
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.extensions.openDefaultCarDock
import info.anodsplace.carwidget.extensions.openPlayStoreDetails
import info.anodsplace.carwidget.preferences.DefaultsResourceProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

sealed class AboutUiAction {
    object ChangeTheme: AboutUiAction()
    object OpenPlayStoreDetails: AboutUiAction()
    object  OpenDefaultCarDock: AboutUiAction()
    class BackupWidget(val dstUri: Uri) : AboutUiAction()
    class BackupInCar(val dstUri: Uri) : AboutUiAction()
    class Restore(val srcUri: Uri) : AboutUiAction()
    class ShowToast(val text: String) : AboutUiAction()
    class ChangeMusicApp(val component: ComponentName?) : AboutUiAction()
}

data class AboutScreenState(
    val appWidgetId: Int,
    val themeIndex: Int,
    val themeName: String,
    val musicApp: String,
    val appVersion: String,
    val backupStatus: Int = Backup.NO_RESULT,
    val restoreStatus: Int = Backup.NO_RESULT,
) {
    val isValidWidget: Boolean
        get() = appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID
}

class AboutViewModel(application: Application): AndroidViewModel(application), KoinComponent {
    var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    val uiAction = MutableSharedFlow<AboutUiAction>()

    private val context: Context
        get() = getApplication()
    private val backupManager: BackupManager by lazy {
        BackupManager(
            getApplication(),
            DefaultsResourceProvider(context.resources)
        )
    }

    private lateinit var screenState: MutableStateFlow<AboutScreenState>

    private val appSettings: AppSettings
        get() = get()

    private val nightMode: Int
        get() = get(named("NightMode"))

    private var job: Job? = null

    fun initScreenState(appWidgetId: Int): StateFlow<AboutScreenState> {
        val themeIdx = appSettings.theme
        val themes = context.resources.getStringArray(R.array.app_themes)
        screenState = MutableStateFlow(
            AboutScreenState(
            appWidgetId = appWidgetId,
            themeIndex = themeIdx,
            themeName = themes[themeIdx],
            musicApp = renderMusicApp(),
            appVersion = renderVersion(),
        )
        )
        job?.cancel()
        job = viewModelScope.launch {
            uiAction.collect {
                when (it) {
                    is AboutUiAction.BackupInCar -> {
                        val code = backupManager.backup(Backup.TYPE_INCAR, appWidgetId, it.dstUri)
                        screenState.value = screenState.value.copy(backupStatus = code)
                    }
                    is AboutUiAction.BackupWidget -> {
                        val code = backupManager.backup(Backup.TYPE_MAIN, appWidgetId, it.dstUri)
                        screenState.value = screenState.value.copy(backupStatus = code)
                    }
                    AboutUiAction.ChangeTheme -> {
                        val newThemeIdx = if (screenState.value.themeIndex == 0) 1 else 0
                        appSettings.theme = newThemeIdx
                        appSettings.apply()
                        screenState.value = screenState.value.copy(themeIndex = appSettings.theme, themeName = themes[newThemeIdx])
                    }
                    is AboutUiAction.ChangeMusicApp -> {
                        appSettings.musicApp = it.component
                        screenState.value = screenState.value.copy(musicApp = renderMusicApp())
                    }
                    is AboutUiAction.Restore -> {
                        val code = backupManager.restore(appWidgetId, it.srcUri)
                        screenState.value = screenState.value.copy(restoreStatus = code)
                    }
                    AboutUiAction.OpenPlayStoreDetails -> {
                        context.openPlayStoreDetails(context.packageName)
                    }
                    AboutUiAction.OpenDefaultCarDock -> {
                        context.openDefaultCarDock()
                    }
                    is AboutUiAction.ShowToast -> {
                        Toast.makeText(context, it.text, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return screenState
    }

    private fun renderMusicApp(): String {
        val musicAppCmp = appSettings.musicApp
        return if (musicAppCmp == null) {
            context.getString(R.string.show_choice)
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
        val appName = context.getString(R.string.app_name)
        var versionName = ""
        try {
            versionName = context.packageManager
                .getPackageInfo(context.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            AppLog.e(e)
        }

        return context.getString(R.string.version_title, appName, versionName)
    }

}