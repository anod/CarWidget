package com.anod.car.home.backup

import android.os.Environment
import com.anod.car.home.BuildConfig
import com.anod.car.home.R
import java.io.File

object Backup {
    const val TYPE_MAIN = 1
    const val TYPE_INCAR = 2

    const val FILE_EXT_JSON = ".json"
    const val NO_RESULT = -1
    const val RESULT_DONE = 0
    const val ERROR_FILE_READ = 3
    const val ERROR_FILE_WRITE = 4
    const val ERROR_DESERIALIZE = 5
    const val ERROR_UNEXPECTED = 6
    const val ERROR_INCORRECT_FORMAT = 7
    const val FILE_INCAR_JSON = "backup_incar.json"
    const val LEGACY_PATH = "/data/com.anod.car.home/backup"
    const val AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider"

    val legacyBackupDir: File
        get() {
            val externalPath = Environment.getExternalStorageDirectory()
            return File(externalPath, LEGACY_PATH)
        }

    fun renderBackupCode(code: Int): Int {
        if (code == RESULT_DONE) {
            return R.string.backup_done
        }
        return if (code == ERROR_FILE_WRITE) {
            R.string.failed_to_write_file
        } else R.string.unexpected_error
    }

    fun renderRestoreCode(code: Int): Int {
        if (code == RESULT_DONE) {
            return R.string.restore_done
        }
        return when (code) {
            ERROR_DESERIALIZE -> R.string.restore_deserialize_failed
            ERROR_FILE_READ -> R.string.failed_to_read_file
            ERROR_UNEXPECTED -> R.string.unexpected_error
            ERROR_INCORRECT_FORMAT -> R.string.backup_unknown_format
            else -> R.string.unexpected_error
        }
    }
}