package com.anod.car.home.backup

import android.content.ContentResolver
import android.net.Uri
import android.os.AsyncTask

/**
 * @author alex
 * @date 12/30/13
 */

class BackupTask(
        private val type: Int,
        private val backupManager: PreferencesBackupManager,
        private val appWidgetId: Int,
        private val uri: Uri,
        private val listener: BackupTaskListener) : AsyncTask<Void, Void, Int>() {

    constructor(backupManager: PreferencesBackupManager, appWidgetId: Int, uri: Uri, listener: BackupTaskListener)
            : this(PreferencesBackupManager.TYPE_MAIN, backupManager, appWidgetId, uri, listener)

    constructor(backupManager: PreferencesBackupManager, uri: Uri, listener: BackupTaskListener)
            : this(PreferencesBackupManager.TYPE_INCAR, backupManager, 0, uri, listener)


    interface BackupTaskListener {
        fun onBackupPreExecute(type: Int)
        fun onBackupFinish(type: Int, code: Int)
    }

    override fun onPreExecute() {
        listener.onBackupPreExecute(type)
    }

    override fun doInBackground(vararg params: Void): Int? {

        if (type == PreferencesBackupManager.TYPE_INCAR) {
            if (ContentResolver.SCHEME_FILE == uri.scheme) {
                return backupManager.doBackupInCarLocal(uri)
            }
            return backupManager.doBackupInCarUri(uri)
        }

        return if (ContentResolver.SCHEME_FILE == uri.scheme) {
            backupManager.doBackupWidgetLocal(uri, appWidgetId)
        } else backupManager.doBackupWidgetUri(uri, appWidgetId)
    }

    override fun onPostExecute(result: Int?) {
        listener.onBackupFinish(type, result!!)
    }
}
