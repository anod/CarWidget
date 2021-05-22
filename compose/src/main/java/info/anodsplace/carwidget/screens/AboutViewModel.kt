package info.anodsplace.carwidget.screens

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.content.backup.Backup
import info.anodsplace.carwidget.content.backup.BackupManager
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.preferences.DefaultsResourceProvider
import info.anodsplace.framework.livedata.SingleLiveEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

class AboutScreenState(
    val appWidgetId: Int,
    val themeIndex: Int,
    val themeName: String,
    val musicApp: String,
    val appVersion: String
) {
    val isValidWidget: Boolean
        get() = appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID
}

class AboutViewModel(application: Application): AndroidViewModel(application), KoinComponent {
    var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private val context: Context
        get() = getApplication()
    private val backupManager: BackupManager by lazy {
        BackupManager(
            getApplication(),
            DefaultsResourceProvider(context.resources)
        )
    }

    val screenState = MutableStateFlow<AboutScreenState?>(null)

    private val appSettings: AppSettings
        get() = get()

    private val nightMode: Int
        get() = get(named("NightMode"))

    fun init(appWidgetId: Int) {
        val themeIdx = appSettings.theme
        val themes = context.resources.getStringArray(R.array.app_themes)
        screenState.value = AboutScreenState(
            appWidgetId = appWidgetId,
            themeIndex = themeIdx,
            themeName = themes[themeIdx],
            musicApp = renderMusicApp(),
            appVersion = renderVersion()
        )
    }

    fun changeTheme(themeIdx: Int) {
        val newThemeIdx = if (themeIdx == 0) 1 else 0
        appSettings.theme = newThemeIdx
        appSettings.apply()
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

    val restoreEvent = SingleLiveEvent<Int>()
    val backupEvent = SingleLiveEvent<Int>()

    fun restore(uri: Uri) {
        viewModelScope.launch {
            restoreEvent.value = Backup.NO_RESULT
            restoreEvent.value = backupManager.restore(appWidgetId, uri)
        }
    }

    fun backup(type: Int, uri: Uri) {
        viewModelScope.launch {
            backupEvent.value = Backup.NO_RESULT
            val code = backupManager.backup(type, appWidgetId, uri)
            backupEvent.value = code
        }
    }

}
