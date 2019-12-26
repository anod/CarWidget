// Copyright (c) 2019. Alex Gavrishev
package com.anod.car.home.backup.ui

import android.app.Application
import android.appwidget.AppWidgetManager
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.anod.car.home.backup.BackupTask
import com.anod.car.home.backup.PreferencesBackupManager
import com.anod.car.home.backup.RestoreTask
import com.anod.car.home.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

open class RestoreViewModel(application: Application) : AndroidViewModel(application) {
    var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    var type = 0
    lateinit var backupManager: PreferencesBackupManager

    val restoreEvent = SingleLiveEvent<Int>()
    val deleteEvent = SingleLiveEvent<Boolean>()
    val backupEvent = SingleLiveEvent<Int>()

    val uploadEvent = SingleLiveEvent<Pair<String, File>>()
    val files = MutableLiveData<List<File>>(emptyList())

    fun restore(uri: Uri) {
        viewModelScope.launch {
            restoreEvent.value = PreferencesBackupManager.NO_RESULT
            restoreEvent.value = RestoreTask(type, backupManager, appWidgetId, uri).execute()
        }
    }

    fun delete(file: File) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { file.delete() }
            if (result) {
                files.value = refreshFiles()
            }
            deleteEvent.value = result
        }
    }

    fun upload(name: String, file: File) {
        uploadEvent.value = Pair("car-$name", file)
    }

    fun loadFiles() {
        viewModelScope.launch {
            files.value = refreshFiles()
        }
    }

    fun backup(uri: Uri) {
        viewModelScope.launch {
            backupEvent.value = PreferencesBackupManager.NO_RESULT
            val code = BackupTask(backupManager, appWidgetId, uri).execute()
            if (code == PreferencesBackupManager.RESULT_DONE) {
                files.value = refreshFiles()
            }
            backupEvent.value = code
        }
    }

    open suspend fun refreshFiles() = withContext(Dispatchers.IO) { emptyList<File>() }
}