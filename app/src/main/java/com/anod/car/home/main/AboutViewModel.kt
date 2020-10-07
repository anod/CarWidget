package com.anod.car.home.main

import android.app.Application
import android.appwidget.AppWidgetManager
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anod.car.home.backup.Backup
import com.anod.car.home.backup.BackupManager
import info.anodsplace.framework.livedata.SingleLiveEvent
import kotlinx.coroutines.launch

class AboutViewModel(application: Application): AndroidViewModel(application) {
    var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private val backupManager: BackupManager by lazy { BackupManager(getApplication()) }

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
