package com.anod.car.home.utils

import com.anod.car.home.R
import info.anodsplace.carwidget.content.backup.Backup

fun renderBackupCode(code: Int): Int {
    if (code == Backup.RESULT_DONE) {
        return R.string.backup_done
    }
    return if (code == Backup.ERROR_FILE_WRITE) {
        R.string.failed_to_write_file
    } else R.string.unexpected_error
}

fun renderRestoreCode(code: Int): Int {
    if (code == Backup.RESULT_DONE) {
        return R.string.restore_done
    }
    return when (code) {
        Backup.ERROR_DESERIALIZE -> R.string.restore_deserialize_failed
        Backup.ERROR_FILE_READ -> R.string.failed_to_read_file
        Backup.ERROR_UNEXPECTED -> R.string.unexpected_error
        Backup.ERROR_INCORRECT_FORMAT -> R.string.backup_unknown_format
        else -> R.string.unexpected_error
    }
}