package com.anod.car.home.backup;

import com.anod.car.home.R;

/**
 * @author alex
 * @date 12/30/13
 */
public class RestoreCodeRender {

    public static int render(int code) {
        if (code == PreferencesBackupManager.RESULT_DONE) {
            return R.string.restore_done;
        }
        switch (code) {
            case PreferencesBackupManager.ERROR_STORAGE_NOT_AVAILABLE:
                return R.string.external_storage_not_available;
            case PreferencesBackupManager.ERROR_DESERIALIZE:
                return R.string.restore_deserialize_failed;
            case PreferencesBackupManager.ERROR_FILE_READ:
                return R.string.failed_to_read_file;
            case PreferencesBackupManager.ERROR_FILE_NOT_EXIST:
                return R.string.backup_not_exist;
            case PreferencesBackupManager.ERROR_UNEXPECTED:
            default:
                return R.string.unexpected_error;
        }
    }

}
