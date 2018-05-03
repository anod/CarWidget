package com.anod.car.home.backup

import android.app.backup.BackupAgentHelper
import android.app.backup.BackupDataInput
import android.app.backup.BackupDataOutput
import android.app.backup.FileBackupHelper
import android.content.Context
import android.os.ParcelFileDescriptor

import java.io.File
import java.io.IOException


class BackupFileHelperAgent : BackupAgentHelper() {

    private val manager: PreferencesBackupManager by lazy { PreferencesBackupManager(this) }

    /**
     * The [FileBackupHelper][android.app.backup.FileBackupHelper] class
     * does nearly all of the work for our use case:  backup and restore of a
     * file stored within our application's getFilesDir() location.  It will
     * also handle files stored at any subpath within that location.  All we
     * need to do is a bit of one-time configuration: installing the helper
     * when this agent object is created.
     */
    override fun onCreate() {
        val helper = createFileBackupHelper(this, manager.backupDir,
                PreferencesBackupManager.FILE_INCAR_JSON)
        addHelper(FILE_HELPER_KEY, helper)
    }

    /**
     * We want to ensure that the UI is not trying to rewrite the data file
     * while we're reading it for backup, so we override this method to
     * supply the necessary locking.
     */
    @Throws(IOException::class)
    override fun onBackup(oldState: ParcelFileDescriptor, data: BackupDataOutput,
                          newState: ParcelFileDescriptor) {
        // Hold the lock while the FileBackupHelper performs the backup operation
        synchronized(PreferencesBackupManager.sLock) {
            super.onBackup(oldState, data, newState)
        }
    }

    /**
     * Adding locking around the file rewrite that happens during restore is
     * similarly straightforward.
     */
    @Throws(IOException::class)
    override fun onRestore(data: BackupDataInput, appVersionCode: Int,
                           newState: ParcelFileDescriptor) {
        // Hold the lock while the FileBackupHelper restores the file from
        // the data provided here.
        synchronized(PreferencesBackupManager.sLock) {
            super.onRestore(data, appVersionCode, newState)
            manager.doRestoreInCarLocal(manager.backupIncarFile.path)
        }
    }

    companion object {

        /**
         * The "key" string passed when adding a helper is a token used to
         * disambiguate between entities supplied by multiple different helper
         * objects.  They only need to be unique among the helpers within this
         * one agent class, not globally unique.
         */
        internal const val FILE_HELPER_KEY = "backup_incar.json"

        private fun createFileBackupHelper(context: Context, path: File,
                                           file: String): FileBackupHelper {
            val filesDir = context.filesDir.absolutePath
            val absPath = path.absolutePath

            val relPath = createRelativePath(filesDir)

            val filePathBuilder = StringBuilder(relPath)
            filePathBuilder.append(absPath)
            filePathBuilder.append(File.separatorChar)
            filePathBuilder.append(file)

            return FileBackupHelper(context, filePathBuilder.toString())
        }

        private fun createRelativePath(path: String): String {
            val parts = path.split(File.separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val relative = StringBuilder("..")
            for (i in 0 until parts.size - 2) {
                relative.append(File.separatorChar)
                relative.append("..")
            }
            return relative.toString()
        }
    }
}
