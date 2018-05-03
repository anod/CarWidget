package com.anod.car.home.backup

import android.app.Activity
import android.os.AsyncTask
import com.anod.car.home.utils.AppPermissions

/**
 * @author alex
 * @date 12/30/13
 */

class BackupTask(private val type: Int, private val backupManager: PreferencesBackupManager, private val appWidgetId: Int,
                 private val listener: BackupTaskListener) : AsyncTask<String, Void, Int>() {

    interface BackupTaskListener {
        fun onBackupPreExecute(type: Int)
        fun onBackupFinish(type: Int, code: Int)
    }

    override fun onPreExecute() {
        listener.onBackupPreExecute(type)
    }

    override fun doInBackground(vararg filenames: String): Int? {
        if (type == PreferencesBackupManager.TYPE_INCAR) {
            return backupManager.doBackupInCar()
        }
        val filename = filenames[0]
        return backupManager.doBackupWidget(filename, appWidgetId)
    }

    override fun onPostExecute(result: Int?) {
        listener.onBackupFinish(type, result!!)
    }
}
