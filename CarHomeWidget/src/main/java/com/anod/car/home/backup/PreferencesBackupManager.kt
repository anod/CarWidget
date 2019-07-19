package com.anod.car.home.backup

import android.app.backup.BackupManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.JsonReader
import android.util.JsonWriter
import android.util.SparseArray
import androidx.core.net.toFile

import com.anod.car.home.model.AbstractShortcuts
import com.anod.car.home.model.NotificationShortcutsModel
import com.anod.car.home.model.Shortcut
import com.anod.car.home.model.WidgetShortcutsModel
import com.anod.car.home.prefs.model.InCarSettings
import com.anod.car.home.prefs.model.InCarStorage
import com.anod.car.home.prefs.model.WidgetSettings
import com.anod.car.home.prefs.model.WidgetStorage
import com.anod.car.home.prefs.preferences.ObjectRestoreManager
import info.anodsplace.framework.AppLog
import java.io.*

class PreferencesBackupManager(private val context: Context) {

    val mainBackups: Array<File>
        get() {
            val saveDir = mainBackupDir
            if (!saveDir.isDirectory) {
                return emptyArray()
            }
            val filter = FilenameFilter { _, filename -> filename.endsWith(FILE_EXT_JSON) || filename.endsWith(ObjectRestoreManager.FILE_EXT_DAT) }
            return saveDir.listFiles(filter)
        }

    val incarTime: Long
        get() {
            var dataFile = File(backupDir, FILE_INCAR_JSON)
            if (!dataFile.exists()) {
                dataFile = File(backupDir, ObjectRestoreManager.FILE_INCAR_DAT)
                if (!dataFile.exists()) {
                    return 0
                }
            }
            return dataFile.lastModified()
        }

    val backupIncarFile: File
        get() {
            val saveDir = backupDir
            return File(saveDir, FILE_INCAR_JSON)
        }


    val backupDir: File
        get() {
            val externalPath = Environment.getExternalStorageDirectory()
            return File(externalPath.absolutePath + DIR_BACKUP)
        }

    private val mainBackupDir: File
        get() = File(backupDir.path + File.separator + BACKUP_MAIN_DIRNAME)

    fun doBackupWidgetLocal(uri: Uri, appWidgetId: Int): Int {
        if (!checkMediaWritable()) {
            return ERROR_STORAGE_NOT_AVAILABLE
        }

        val file = uri.toFile()
        val saveDir = uri.toFile().parentFile
        if (!saveDir.exists()) {
            saveDir.mkdirs()
        }

        val result = doBackupWidget(FileOutputStream(file), appWidgetId)

        saveDir.setLastModified(System.currentTimeMillis())
        return result
    }

    fun doBackupWidgetUri(uri: Uri, appWidgetId: Int): Int? {
        val outputStream: OutputStream?
        try {
            outputStream = context.contentResolver.openOutputStream(uri)
            return doBackupWidget(outputStream, appWidgetId)
        } catch (e: FileNotFoundException) {
            AppLog.e(e)
            return ERROR_FILE_READ
        }
    }

    private fun doBackupWidget(outputStream: OutputStream, appWidgetId: Int): Int {
        val model = WidgetShortcutsModel.init(context, appWidgetId)
        val widget = WidgetStorage.load(context, appWidgetId)
        try {
            synchronized(sLock) {
                val writer = JsonWriter(OutputStreamWriter(outputStream))

                writer.beginObject()

                val settingsWriter = writer.name("settings")
                widget.writeJson(settingsWriter)

                val arrayWriter = writer.name("shortcuts").beginArray()
                val shortcutsJsonWriter = ShortcutsJsonWriter()
                shortcutsJsonWriter.writeList(arrayWriter, model.shortcuts, model)
                arrayWriter.endArray()

                writer.endObject()
                writer.close()
            }
        } catch (e: IOException) {
            AppLog.e(e)
            return ERROR_FILE_WRITE
        }
        return RESULT_DONE
    }

    fun doBackupInCarLocal(uri: Uri): Int {
        if (!checkMediaWritable()) {
            return ERROR_STORAGE_NOT_AVAILABLE
        }
        val file = uri.toFile()
        val saveDir = uri.toFile().parentFile
        if (!saveDir.exists()) {
            saveDir.mkdirs()
        }

        val result = doBackupInCar(FileOutputStream(file))

        BackupManager.dataChanged(BACKUP_PACKAGE)
        return result
    }

    fun doBackupInCarUri(uri: Uri): Int {
        val outputStream: OutputStream?
        try {
            outputStream = context.contentResolver.openOutputStream(uri)
            return doBackupInCar(outputStream)
        } catch (e: FileNotFoundException) {
            AppLog.e(e)
            return ERROR_FILE_READ
        }
    }

    private fun doBackupInCar(outputStream: OutputStream): Int {
        val model = NotificationShortcutsModel.init(context)

        val prefs = InCarStorage.load(context)

        try {
            synchronized(sLock) {
                val writer = JsonWriter(OutputStreamWriter(outputStream))
                writer.beginObject()

                val settingsWriter = writer.name("settings")
                prefs.writeJson(settingsWriter)

                val arrayWriter = writer.name("shortcuts").beginArray()
                val shortcutsJsonWriter = ShortcutsJsonWriter()
                shortcutsJsonWriter.writeList(arrayWriter, model.shortcuts, model)
                arrayWriter.endArray()

                writer.endObject()
                writer.close()
            }
        } catch (e: IOException) {
            AppLog.e(e)
            return ERROR_FILE_WRITE
        }

        return RESULT_DONE
    }

    fun getBackupWidgetFile(filename: String): File {
        val saveDir = mainBackupDir
        return File(saveDir, filename + FILE_EXT_JSON)
    }

    fun doRestoreWidgetLocal(uri: Uri, appWidgetId: Int): Int {
        if (!checkMediaReadable()) {
            return ERROR_STORAGE_NOT_AVAILABLE
        }

        val dataFile = uri.toFile()
        if (!dataFile.exists()) {
            return ERROR_FILE_NOT_EXIST
        }
        if (!dataFile.canRead()) {
            return ERROR_FILE_READ
        }

        val inputStream: FileInputStream
        try {
            inputStream = FileInputStream(dataFile)
        } catch (e: FileNotFoundException) {
            AppLog.e(e)
            return ERROR_FILE_READ
        }

        val pos = dataFile.name.lastIndexOf('.')
        if (pos > 0) {
            val extension = dataFile.name.substring(pos)
            if (extension == ObjectRestoreManager.FILE_EXT_DAT) {
                val objectRestore = ObjectRestoreManager(context)
                return objectRestore.doRestoreMain(inputStream, appWidgetId)
            }
        }

        return doRestoreWidget(inputStream, appWidgetId)
    }

    fun doRestoreWidgetUri(uri: Uri, appWidgetId: Int): Int? {
        val inputStream: InputStream?
        try {
            inputStream = context.contentResolver.openInputStream(uri)
        } catch (e: FileNotFoundException) {
            AppLog.e(e)
            return ERROR_FILE_READ
        }

        return doRestoreWidget(inputStream, appWidgetId)
    }

    private fun doRestoreWidget(inputStream: InputStream?, appWidgetId: Int): Int {
        val sharedPrefs = WidgetStorage.getSharedPreferences(context, appWidgetId)
        sharedPrefs.edit().clear().apply()
        val widget = WidgetSettings(sharedPrefs, context.resources)

        val shortcutsJsonReader = ShortcutsJsonReader(context)
        var shortcuts = SparseArray<ShortcutsJsonReader.ShortcutWithIconAndPosition>()

        try {
            synchronized(sLock) {
                val reader = JsonReader(InputStreamReader(BufferedInputStream(inputStream!!)))
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "settings" -> widget.readJson(reader)
                        "shortcuts" -> shortcuts = shortcutsJsonReader.readList(reader)
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
                reader.close()
            }
        } catch (e: IOException) {
            AppLog.e(e)
            return ERROR_FILE_READ
        } catch (e: ClassCastException) {
            AppLog.e(e)
            return ERROR_DESERIALIZE
        }

        widget.apply()
        // small check
        if (shortcuts.size() % 2 == 0) {
            WidgetStorage.saveLaunchComponentNumber(shortcuts.size(), context, appWidgetId)
        }
        val model = WidgetShortcutsModel.init(context, appWidgetId)

        restoreShortcuts(model, shortcuts)

        return RESULT_DONE
    }

    private fun restoreShortcuts(model: AbstractShortcuts, shortcuts: SparseArray<ShortcutsJsonReader.ShortcutWithIconAndPosition>) {
        for (pos in 0 until model.count) {
            model.drop(pos)
            val shortcut = shortcuts.get(pos)
            if (shortcut?.icon != null) {
                val info = Shortcut(Shortcut.idUnknown, shortcut.info)
                model.save(pos, info, shortcut.icon)
            }
        }
    }

    fun doRestoreInCarLocal(uri: Uri): Int {
        if (!checkMediaReadable()) {
            return ERROR_STORAGE_NOT_AVAILABLE
        }

        val dataFile = uri.toFile()
        if (!dataFile.exists()) {
            return ERROR_FILE_NOT_EXIST
        }
        if (!dataFile.canRead()) {
            return ERROR_FILE_READ
        }

        val fis: FileInputStream
        try {
            fis = FileInputStream(dataFile)
        } catch (e: FileNotFoundException) {
            AppLog.e(e)
            return ERROR_FILE_READ
        }

        val pos = dataFile.name.lastIndexOf('.')
        if (pos > 0) {
            val extension = dataFile.name.substring(pos)
            if (extension == ObjectRestoreManager.FILE_EXT_DAT) {
                val objectRestore = ObjectRestoreManager(context)
                return objectRestore.doRestoreInCar(fis)
            }
        }

        return doRestoreInCar(fis)
    }

    fun doRestoreInCarUri(uri: Uri): Int {
        val inputStream: InputStream?
        try {
            inputStream = context.contentResolver.openInputStream(uri)
        } catch (e: FileNotFoundException) {
            AppLog.e(e)
            return ERROR_FILE_READ
        }

        return doRestoreInCar(inputStream)
    }

    private fun doRestoreInCar(inputStream: InputStream?): Int {
        val sharedPrefs = InCarStorage.getSharedPreferences(context)
        sharedPrefs.edit().clear().apply()
        val incar = InCarSettings(sharedPrefs)

        val shortcutsJsonReader = ShortcutsJsonReader(context)
        var shortcuts = SparseArray<ShortcutsJsonReader.ShortcutWithIconAndPosition>()

        try {
            synchronized(sLock) {
                val reader = JsonReader(InputStreamReader(BufferedInputStream(inputStream!!)))
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "settings" -> incar.readJson(reader)
                        "shortcuts" -> shortcuts = shortcutsJsonReader.readList(reader)
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
                reader.close()
            }
        } catch (e: IOException) {
            AppLog.e(e)
            return ERROR_FILE_READ
        } catch (e: ClassCastException) {
            AppLog.e(e)
            return ERROR_DESERIALIZE
        }

        val model = NotificationShortcutsModel.init(context)

        incar.apply()
        restoreShortcuts(model, shortcuts)

        return RESULT_DONE
    }

    private fun checkMediaWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state && Environment.MEDIA_MOUNTED_READ_ONLY != state
    }

    private fun checkMediaReadable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }


    companion object {
        const val TYPE_MAIN = 1
        const val TYPE_INCAR = 2
        private const val BACKUP_PACKAGE = "com.anod.car.home.pro"
        internal const val DIR_BACKUP = "/data/com.anod.car.home/backup"
        const val FILE_EXT_JSON = ".json"
        const val RESULT_DONE = 0
        const val ERROR_STORAGE_NOT_AVAILABLE = 1
        const val ERROR_FILE_NOT_EXIST = 2
        const val ERROR_FILE_READ = 3
        const val ERROR_FILE_WRITE = 4
        const val ERROR_DESERIALIZE = 5
        const val ERROR_UNEXPECTED = 6
        private const val BACKUP_MAIN_DIRNAME = "backup_main"
        const val FILE_INCAR_JSON = "backup_incar.json"
        internal val sLock = arrayOfNulls<Any>(0)
    }


}
