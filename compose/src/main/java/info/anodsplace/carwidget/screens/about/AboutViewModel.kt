package info.anodsplace.carwidget.screens.about

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.content.AppCoroutineScope
import info.anodsplace.carwidget.content.backup.Backup
import info.anodsplace.carwidget.content.backup.BackupManager
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.di.unaryPlus
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.extensions.openDefaultCarDock
import info.anodsplace.carwidget.extensions.openPlayStoreDetails
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
    val backupStatus: Int = Backup.NO_RESULT,
    val restoreStatus: Int = Backup.NO_RESULT,
) {
    val isValidWidget: Boolean
        get() = appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID
}

sealed interface AboutScreenStateEvent {
    object ChangeTheme: AboutScreenStateEvent
    object OpenPlayStoreDetails: AboutScreenStateEvent
    object OpenDefaultCarDock: AboutScreenStateEvent
    class BackupWidget(val dstUri: Uri) : AboutScreenStateEvent
    class Restore(val srcUri: Uri) : AboutScreenStateEvent
    class ShowToast(val text: String) : AboutScreenStateEvent
    class ChangeMusicApp(val component: ComponentName?) : AboutScreenStateEvent
}

sealed interface AboutScreenStateAction

class AboutViewModel(private val appWidgetIdScope: AppWidgetIdScope?): BaseFlowViewModel<AboutScreenState, AboutScreenStateEvent, AboutScreenStateAction>(), KoinComponent {

    class Factory(private val appWidgetIdScope: AppWidgetIdScope?): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AboutViewModel(appWidgetIdScope) as T
    }

    private val context: Context by inject()
    private val backupManager: BackupManager by inject()
    private val themes = context.resources.getStringArray(R.array.app_themes)
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
            is AboutScreenStateEvent.BackupWidget -> {
                appScope.launch {
                    val code = backupManager.backup(appWidgetIdScope, event.dstUri)
                    viewState = viewState.copy(backupStatus = code)
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
                appScope.launch {
                    val code = backupManager.restore(appWidgetIdScope, event.srcUri)
                    viewState = viewState.copy(restoreStatus = code)
                }
            }
            AboutScreenStateEvent.OpenPlayStoreDetails -> {
                context.openPlayStoreDetails(context.packageName)
            }
            AboutScreenStateEvent.OpenDefaultCarDock -> {
                context.openDefaultCarDock()
            }
            is AboutScreenStateEvent.ShowToast -> {
                Toast.makeText(context, event.text, Toast.LENGTH_SHORT).show()
            }
        }
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