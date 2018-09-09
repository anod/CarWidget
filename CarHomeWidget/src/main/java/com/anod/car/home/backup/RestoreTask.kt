package com.anod.car.home.backup

import android.content.ContentResolver
import android.net.Uri
import android.os.AsyncTask

/**
 * @author alex
 * @date 12/30/13
 */
class RestoreTask(private val type: Int, private val backupManager: PreferencesBackupManager, private val appWidgetId: Int,
                  private val listener: RestoreTaskListener) : AsyncTask<Uri, Void, Int>() {

    interface RestoreTaskListener {
        fun onRestorePreExecute(type: Int)
        fun onRestoreFinish(type: Int, code: Int)
    }

    override fun onPreExecute() {
        listener.onRestorePreExecute(type)
    }

    override fun doInBackground(vararg uris: Uri): Int? {
        val uri = uris[0]
        if (type == PreferencesBackupManager.TYPE_INCAR) {

            return if (ContentResolver.SCHEME_FILE == uri.scheme) {
                backupManager.doRestoreInCarLocal(uri.path!!)
            } else backupManager.doRestoreInCarUri(uri)
        }

        return if (ContentResolver.SCHEME_FILE == uri.scheme) {
            backupManager.doRestoreWidgetLocal(uri.path!!, appWidgetId)
        } else backupManager.doRestoreWidgetUri(uri, appWidgetId)
    }

    override fun onPostExecute(result: Int?) {
        listener.onRestoreFinish(type, result!!)
    }

}