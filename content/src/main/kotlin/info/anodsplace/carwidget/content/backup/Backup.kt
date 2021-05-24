package info.anodsplace.carwidget.content.backup

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
}