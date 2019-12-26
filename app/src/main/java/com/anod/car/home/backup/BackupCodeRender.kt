package com.anod.car.home.backup

import com.anod.car.home.R

/**
 * @author alex
 * @date 12/30/13
 */
object BackupCodeRender {

    fun render(code: Int): Int {
        if (code == PreferencesBackupManager.RESULT_DONE) {
            return R.string.backup_done
        }
        if (code == PreferencesBackupManager.ERROR_STORAGE_NOT_AVAILABLE) {
            return R.string.external_storage_not_available
        }
        return if (code == PreferencesBackupManager.ERROR_FILE_WRITE) {
            R.string.failed_to_write_file
        } else R.string.unexpected_error
    }

}
