package com.anod.car.home.backup

import android.content.ContentResolver
import android.net.Uri
import android.os.AsyncTask

/**
 * @author alex
 * @date 12/30/13
 */
class RestoreTask(
        private val type: Int,
        private val backupManager: PreferencesBackupManager,
        private val appWidgetId: Int,
        private val uri: Uri,
        private val listener: RestoreTaskListener) : AsyncTask<Void, Void, Int>() {

    constructor(backupManager: PreferencesBackupManager, appWidgetId: Int, uri: Uri, listener: RestoreTaskListener)
        : this(PreferencesBackupManager.TYPE_MAIN, backupManager, appWidgetId, uri, listener)

    constructor(backupManager: PreferencesBackupManager, uri: Uri, listener: RestoreTaskListener)
            : this(PreferencesBackupManager.TYPE_INCAR, backupManager, 0, uri, listener)

    interface RestoreTaskListener {
        fun onRestorePreExecute(type: Int)
        fun onRestoreFinish(type: Int, code: Int)
    }

    override fun onPreExecute() {
        listener.onRestorePreExecute(type)
    }

    override fun doInBackground(vararg params: Void): Int? {
        if (type == PreferencesBackupManager.TYPE_INCAR) {

            return if (ContentResolver.SCHEME_FILE == uri.scheme) {
                backupManager.doRestoreInCarLocal(uri)
            } else backupManager.doRestoreInCarUri(uri)
        }

        return if (ContentResolver.SCHEME_FILE == uri.scheme) {
            backupManager.doRestoreWidgetLocal(uri, appWidgetId)
        } else backupManager.doRestoreWidgetUri(uri, appWidgetId)
    }

    override fun onPostExecute(result: Int?) {
        listener.onRestoreFinish(type, result!!)
    }

}