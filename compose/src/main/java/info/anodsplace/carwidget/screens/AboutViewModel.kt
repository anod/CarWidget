package info.anodsplace.carwidget.screens

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import info.anodsplace.carwidget.content.backup.Backup
import info.anodsplace.carwidget.content.backup.BackupManager
import info.anodsplace.carwidget.preferences.DefaultsResourceProvider
import info.anodsplace.framework.livedata.SingleLiveEvent
import kotlinx.coroutines.launch

class ScreenState {

}

class AboutViewModel(application: Application): AndroidViewModel(application) {
    var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private val context: Context
        get() = getApplication()
    private val backupManager: BackupManager by lazy {
        BackupManager(
            getApplication(),
            DefaultsResourceProvider(context.resources)
        )
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
