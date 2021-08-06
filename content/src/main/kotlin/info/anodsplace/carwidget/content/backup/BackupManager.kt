package info.anodsplace.carwidget.content.backup

import android.content.Context
import android.net.Uri
import android.util.JsonReader
import android.util.JsonWriter
import android.util.SparseArray
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.model.AbstractShortcuts
import info.anodsplace.carwidget.content.model.NotificationShortcutsModel
import info.anodsplace.carwidget.content.model.WidgetShortcutsModel
import info.anodsplace.carwidget.content.preferences.InCarSettings
import info.anodsplace.carwidget.content.preferences.InCarStorage
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.carwidget.content.preferences.WidgetStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*

class BackupManager(private val context: Context, private val resourceDefaults: WidgetSettings.DefaultsProvider) {

    interface OnRestore {
        fun restoreCompleted()
    }

    suspend fun backup(type: Int, appWidgetId: Int, uri: Uri): Int = withContext(Dispatchers.IO) {
        if (type == Backup.TYPE_INCAR) {
            return@withContext doBackupInCarUri(uri)
        }
        return@withContext doBackupWidgetUri(uri, appWidgetId)
    }

    suspend fun restore(type: Int, appWidgetId: Int, uri: Uri): Int = withContext(Dispatchers.IO) {
        if (type == Backup.TYPE_INCAR) {
            return@withContext doRestoreInCarUri(uri)
        }

        return@withContext doRestoreWidgetUri(uri, appWidgetId)
    }

    suspend fun restore(appWidgetId: Int, uri: Uri): Int = withContext(Dispatchers.IO) {
        val code = if (appWidgetId > 0)
            doRestoreWidgetUri(uri, appWidgetId)
        else Backup.ERROR_INCORRECT_FORMAT
        if (code != Backup.RESULT_DONE) {
            return@withContext doRestoreInCarUri(uri)
        }
        return@withContext code
    }

    private fun doRestoreWidgetUri(uri: Uri, appWidgetId: Int): Int {
        val inputStream: InputStream?
        try {
            inputStream = context.contentResolver.openInputStream(uri)
        } catch (e: FileNotFoundException) {
            AppLog.e(e)
            return Backup.ERROR_FILE_READ
        }

        return doRestoreWidget(inputStream, appWidgetId)
    }

    private fun doRestoreWidget(inputStream: InputStream?, appWidgetId: Int): Int {
        val widget = WidgetStorage.load(context, resourceDefaults, appWidgetId)

        val shortcutsJsonReader = ShortcutsJsonReader(context)
        var shortcuts = SparseArray<ShortcutsJsonReader.ShortcutWithIconAndPosition>()
        var found = 0

        try {
            synchronized(sLock) {
                val reader = JsonReader(InputStreamReader(BufferedInputStream(inputStream!!)))
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "settings" -> {
                            found = widget.readJson(reader)
                        }
                        "shortcuts" -> shortcuts = shortcutsJsonReader.readList(reader)
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
                reader.close()
            }
        } catch (e: IOException) {
            AppLog.e(e)
            return Backup.ERROR_FILE_READ
        } catch (e: ClassCastException) {
            AppLog.e(e)
            return Backup.ERROR_DESERIALIZE
        }

        if (found == 0) {
            return Backup.ERROR_INCORRECT_FORMAT
        }

        widget.clear()
        widget.applyPending()
        // small check
        if (shortcuts.size() % 2 == 0) {
            widget.shortcutsNumber = shortcuts.size()
        }
        val model = WidgetShortcutsModel.init(context, resourceDefaults, appWidgetId)

        restoreShortcuts(model, shortcuts)

        return Backup.RESULT_DONE
    }

    private fun doBackupWidgetUri(uri: Uri, appWidgetId: Int): Int {
        val outputStream: OutputStream?
        return try {
            outputStream = context.contentResolver.openOutputStream(uri) ?: return Backup.ERROR_UNEXPECTED
            doBackupWidget(outputStream, appWidgetId)
        } catch (e: FileNotFoundException) {
            AppLog.e(e)
            Backup.ERROR_FILE_READ
        }
    }

    private fun doBackupWidget(outputStream: OutputStream, appWidgetId: Int): Int {
        val model = WidgetShortcutsModel.init(context, resourceDefaults, appWidgetId)
        val widget = WidgetStorage.load(context, resourceDefaults, appWidgetId)
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
            return Backup.ERROR_FILE_WRITE
        }
        return Backup.RESULT_DONE
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

    private fun doRestoreInCarUri(uri: Uri): Int {
        val inputStream: InputStream?
        try {
            inputStream = context.contentResolver.openInputStream(uri)
        } catch (e: FileNotFoundException) {
            AppLog.e(e)
            return Backup.ERROR_FILE_READ
        }

        return doRestoreInCar(inputStream)
    }

    private fun doRestoreInCar(inputStream: InputStream?): Int {
        val sharedPrefs = InCarStorage.getSharedPreferences(context)
        val incar = InCarSettings(sharedPrefs)

        val shortcutsJsonReader = ShortcutsJsonReader(context)
        var shortcuts = SparseArray<ShortcutsJsonReader.ShortcutWithIconAndPosition>()
        var found = 0
        try {
            synchronized(sLock) {
                val reader = JsonReader(InputStreamReader(BufferedInputStream(inputStream!!)))
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "settings" -> {
                            found = incar.readJson(reader)
                        }
                        "shortcuts" -> shortcuts = shortcutsJsonReader.readList(reader)
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
                reader.close()
            }
        } catch (e: IOException) {
            AppLog.e(e)
            return Backup.ERROR_FILE_READ
        } catch (e: ClassCastException) {
            AppLog.e(e)
            return Backup.ERROR_DESERIALIZE
        }

        if (found == 0) {
            return Backup.ERROR_INCORRECT_FORMAT
        }

        val model = NotificationShortcutsModel.init(context)

        sharedPrefs.edit().clear().apply()
        incar.applyPending()
        restoreShortcuts(model, shortcuts)

        return Backup.RESULT_DONE
    }

    private fun doBackupInCarUri(uri: Uri): Int {
        val outputStream: OutputStream?
        return try {
            outputStream = context.contentResolver.openOutputStream(uri) ?: return Backup.ERROR_UNEXPECTED
            doBackupInCar(outputStream)
        } catch (e: FileNotFoundException) {
            AppLog.e(e)
            Backup.ERROR_FILE_READ
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
            return Backup.ERROR_FILE_WRITE
        }

        return Backup.RESULT_DONE
    }

    companion object {
        internal val sLock = arrayOfNulls<Any>(0)
    }
}