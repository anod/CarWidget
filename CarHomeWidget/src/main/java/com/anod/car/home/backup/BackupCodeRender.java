package com.anod.car.home.backup;

import com.anod.car.home.R;

/**
 * @author alex
 * @date 12/30/13
 */
public class BackupCodeRender {

    public static int render(int code) {
        if (code == PreferencesBackupManager.RESULT_DONE) {
            return R.string.backup_done;
        }
        if (code == PreferencesBackupManager.ERROR_STORAGE_NOT_AVAILABLE) {
            return R.string.external_storage_not_available;
        }
        if (code == PreferencesBackupManager.ERROR_FILE_WRITE) {
            return R.string.failed_to_write_file;
        }
        return R.string.unexpected_error;
    }

}
