package info.anodsplace.carwidget.content.backup

import android.content.Context
import android.net.Uri
import android.util.JsonReader
import android.util.JsonWriter
import android.util.SparseArray
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.db.ShortcutWithIcon
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.di.unaryPlus
import info.anodsplace.carwidget.content.preferences.InCarSettings
import info.anodsplace.carwidget.content.preferences.InCarStorage
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.carwidget.content.shortcuts.NotificationShortcutsModel
import info.anodsplace.carwidget.content.shortcuts.WidgetShortcutsModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.*

class BackupManager(
    private val context: Context,
    private val database: ShortcutsDatabase,
    private val inCarSettings: InCarSettings
) {

    suspend fun backup(appWidgetIdScope: AppWidgetIdScope?, uri: Uri): Int {
        if (appWidgetIdScope == null) {
            return doBackupInCarUri(uri)
        }
        return doBackupWidgetUri(uri, appWidgetIdScope)
    }

    suspend fun restore(appWidgetIdScope: AppWidgetIdScope?, uri: Uri): Int {
        val code = if (appWidgetIdScope != null)
            doRestoreWidgetUri(uri, appWidgetIdScope)
        else Backup.ERROR_INCORRECT_FORMAT
        if (code != Backup.RESULT_DONE) {
            return doRestoreInCarUri(uri)
        }
        return code
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun doRestoreWidgetUri(uri: Uri, appWidgetIdScope: AppWidgetIdScope): Int = withContext(newSingleThreadContext("RestoreWidget")) {
        val inputStream: InputStream?
        try {
            inputStream = context.contentResolver.openInputStream(uri)
        } catch (e: FileNotFoundException) {
            AppLog.e(e)
            return@withContext Backup.ERROR_FILE_READ
        }

        return@withContext doRestoreWidget(inputStream!!, appWidgetIdScope)
    }

    private suspend fun doRestoreWidget(inputStream: InputStream, appWidgetIdScope: AppWidgetIdScope): Int {
        val widget = appWidgetIdScope.scope.get<WidgetSettings>()

        val shortcutsJsonReader = ShortcutsJsonReader(context)
        var shortcuts = SparseArray<ShortcutWithIcon>()
        var found = 0

        try {
            mutex.withLock {
                val reader = JsonReader(inputStream.bufferedReader())
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

        database.restoreTarget(+appWidgetIdScope, shortcuts)

        return Backup.RESULT_DONE
    }

    private suspend fun doBackupWidgetUri(uri: Uri, appWidgetIdScope: AppWidgetIdScope): Int = withContext(newSingleThreadContext("BackupWidget")) {
        val outputStream: OutputStream?
        return@withContext try {
            outputStream = context.contentResolver.openOutputStream(uri)
                ?: return@withContext Backup.ERROR_UNEXPECTED
            doBackupWidget(outputStream, appWidgetIdScope)
        } catch (e: FileNotFoundException) {
            AppLog.e(e)
            Backup.ERROR_FILE_READ
        }
    }

    private suspend fun doBackupWidget(outputStream: OutputStream, appWidgetIdScope: AppWidgetIdScope): Int {
        val model = appWidgetIdScope.scope.get<WidgetShortcutsModel>().apply {
            init()
        }
        val widget = appWidgetIdScope.scope.get<WidgetSettings>()

        try {
            mutex.withLock {
                val writer = JsonWriter(OutputStreamWriter(outputStream))

                writer.beginObject()

                val settingsWriter = writer.name("settings")
                widget.writeJson(settingsWriter)

                val arrayWriter = writer.name("shortcuts").beginArray()
                val shortcutsJsonWriter = ShortcutsJsonWriter()
                shortcutsJsonWriter.writeList(arrayWriter, model.shortcuts, database, context)
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

    private suspend fun doRestoreInCarUri(uri: Uri): Int = withContext(newSingleThreadContext("RestoreInCar")) {
        val inputStream: InputStream?
        try {
            inputStream = context.contentResolver.openInputStream(uri)
        } catch (e: FileNotFoundException) {
            AppLog.e(e)
            return@withContext Backup.ERROR_FILE_READ
        }

        return@withContext doRestoreInCar(inputStream!!)
    }

    private suspend fun doRestoreInCar(inputStream: InputStream): Int {
        val sharedPrefs = InCarStorage.getSharedPreferences(context)

        val shortcutsJsonReader = ShortcutsJsonReader(context)
        var shortcuts = SparseArray<ShortcutWithIcon>()
        var found = 0
        try {
            mutex.withLock {
                val reader = JsonReader(inputStream.bufferedReader())
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "settings" -> {
                            found = inCarSettings.readJson(reader)
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

        database.restoreTarget(NotificationShortcutsModel.notificationTargetId, shortcuts)

        sharedPrefs.edit().clear().apply()
        inCarSettings.applyPending()

        return Backup.RESULT_DONE
    }

    private suspend fun doBackupInCarUri(uri: Uri): Int = withContext(newSingleThreadContext("BackupInCar")) {
        val outputStream: OutputStream?
        return@withContext try {
            outputStream = context.contentResolver.openOutputStream(uri)
                ?: return@withContext Backup.ERROR_UNEXPECTED
            doBackupInCar(outputStream)
        } catch (e: FileNotFoundException) {
            AppLog.e(e)
            Backup.ERROR_FILE_READ
        }
    }

    private suspend fun doBackupInCar(outputStream: OutputStream): Int {
        val model = NotificationShortcutsModel.init(context, database)

        try {
            mutex.withLock {
                val writer = JsonWriter(outputStream.writer())
                writer.beginObject()

                val settingsWriter = writer.name("settings")
                inCarSettings.writeJson(settingsWriter)

                val arrayWriter = writer.name("shortcuts").beginArray()
                ShortcutsJsonWriter().writeList(arrayWriter, model.shortcuts, database, context)
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
        internal val mutex = Mutex()
    }
}