package com.anod.car.home.backup

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author alex
 * @date 12/30/13
 */
class RestoreTask(
        private val type: Int,
        private val backupManager: PreferencesBackupManager,
        private val appWidgetId: Int,
        private val uri: Uri) {

    constructor(backupManager: PreferencesBackupManager, appWidgetId: Int, uri: Uri)
            : this(PreferencesBackupManager.TYPE_MAIN, backupManager, appWidgetId, uri)

    constructor(backupManager: PreferencesBackupManager, uri: Uri)
            : this(PreferencesBackupManager.TYPE_INCAR, backupManager, 0, uri)

    suspend fun execute(): Int = withContext(Dispatchers.IO) {
        if (type == PreferencesBackupManager.TYPE_INCAR) {
            return@withContext if (ContentResolver.SCHEME_FILE == uri.scheme) {
                backupManager.doRestoreInCarLocal(uri)
            } else backupManager.doRestoreInCarUri(uri)
        }

        return@withContext if (ContentResolver.SCHEME_FILE == uri.scheme) {
            backupManager.doRestoreWidgetLocal(uri, appWidgetId)
        } else backupManager.doRestoreWidgetUri(uri, appWidgetId)
    }

}