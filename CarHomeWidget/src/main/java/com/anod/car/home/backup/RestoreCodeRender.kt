package com.anod.car.home.backup

import com.anod.car.home.R

/**
 * @author alex
 * @date 12/30/13
 */
object RestoreCodeRender {

    fun render(code: Int): Int {
        if (code == PreferencesBackupManager.RESULT_DONE) {
            return R.string.restore_done
        }
        when (code) {
            PreferencesBackupManager.ERROR_STORAGE_NOT_AVAILABLE -> return R.string.external_storage_not_available
            PreferencesBackupManager.ERROR_DESERIALIZE -> return R.string.restore_deserialize_failed
            PreferencesBackupManager.ERROR_FILE_READ -> return R.string.failed_to_read_file
            PreferencesBackupManager.ERROR_FILE_NOT_EXIST -> return R.string.backup_not_exist
            PreferencesBackupManager.ERROR_UNEXPECTED -> return R.string.unexpected_error
            else -> return R.string.unexpected_error
        }
    }

}
