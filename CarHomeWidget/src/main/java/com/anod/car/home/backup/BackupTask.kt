package com.anod.car.home.backup

import android.content.ContentResolver
import android.net.Uri
import android.os.AsyncTask

/**
 * @author alex
 * @date 12/30/13
 */

class BackupTask(private val type: Int, private val backupManager: PreferencesBackupManager, private val appWidgetId: Int,
                 private val listener: BackupTaskListener) : AsyncTask<Uri, Void, Int>() {

    interface BackupTaskListener {
        fun onBackupPreExecute(type: Int)
        fun onBackupFinish(type: Int, code: Int)
    }

    override fun onPreExecute() {
        listener.onBackupPreExecute(type)
    }

    override fun doInBackground(vararg uris: Uri): Int? {
        val uri = uris[0]

        if (type == PreferencesBackupManager.TYPE_INCAR) {
            if (ContentResolver.SCHEME_FILE == uri.scheme) {
                return backupManager.doBackupInCarLocal()
            }
            return backupManager.doBackupInCarUri(uri)
        }

        return if (ContentResolver.SCHEME_FILE == uri.scheme) {
            backupManager.doBackupWidgetLocal(uri.path, appWidgetId)
        } else backupManager.doBackupWidgetUri(uri, appWidgetId)
    }

    override fun onPostExecute(result: Int?) {
        listener.onBackupFinish(type, result!!)
    }
}
