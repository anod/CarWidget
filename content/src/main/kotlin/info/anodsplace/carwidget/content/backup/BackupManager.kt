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
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.carwidget.content.shortcuts.NotificationShortcutsModel
import info.anodsplace.carwidget.content.shortcuts.WidgetShortcutsModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import java.io.*

sealed interface BackupCheckResult {
    class Error(val errorCode: Int) : BackupCheckResult
    class Success(val hasInCar: Boolean) : BackupCheckResult
}

class BackupManager(
    private val context: Context,
    private val database: ShortcutsDatabase,
    private val inCarSettings: InCarSettings
) {

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun backup(appWidgetIdScope: AppWidgetIdScope, uri: Uri): Int = withContext(newSingleThreadContext("BackupWidget")) {
        return@withContext try {
            val outputStream = context.contentResolver.openOutputStream(uri) ?: return@withContext Backup.ERROR_UNEXPECTED
            writeBackup(outputStream, appWidgetIdScope)
        } catch (e: Exception) {
            AppLog.e(e)
            Backup.ERROR_FILE_READ
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun checkSrcUri(srcUri: Uri): BackupCheckResult = withContext(newSingleThreadContext("RestoreWidget")) {
        return@withContext try {
            val inputStream: InputStream = context.contentResolver.openInputStream(srcUri) ?: return@withContext BackupCheckResult.Error(errorCode = Backup.ERROR_FILE_READ)
            checkBackup(inputStream)
        } catch (e: Exception) {
            AppLog.e(e)
            return@withContext BackupCheckResult.Error(errorCode = Backup.ERROR_FILE_READ)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun restore(appWidgetIdScope: AppWidgetIdScope, uri: Uri, restoreInCar: Boolean): Int = withContext(newSingleThreadContext("RestoreWidget")) {
        return@withContext  try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext Backup.ERROR_FILE_READ
            doRestoreWidget(inputStream, appWidgetIdScope, restoreInCar)
        } catch (e: FileNotFoundException) {
            AppLog.e(e)
           Backup.ERROR_FILE_READ
        }
    }

    private suspend fun doRestoreWidget(inputStream: InputStream, appWidgetIdScope: AppWidgetIdScope, restoreInCar: Boolean): Int {
        val widget = appWidgetIdScope.scope.get<WidgetSettings>()

        val shortcutsJsonReader = ShortcutsJsonReader(context)
        var shortcuts = SparseArray<ShortcutWithIcon>()
        var notificationShortcuts = SparseArray<ShortcutWithIcon>()
        var found = 0
        try {
                val reader = JsonReader(inputStream.bufferedReader())
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "settings" -> {
                            found = widget.readJson(reader)
                        }
                        "incar" -> {
                            inCarSettings.readJson(reader)
                        }
                        "shortcuts" -> {
                            shortcuts = shortcutsJsonReader.readList(reader)
                        }
                        "notificationShortcuts" -> {
                            notificationShortcuts = shortcutsJsonReader.readList(reader)
                        }
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
                reader.close()
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

        if (restoreInCar) {
            inCarSettings.clear()
            inCarSettings.applyPending()

            database.restoreTarget(NotificationShortcutsModel.notificationTargetId, notificationShortcuts)
        }

        return Backup.RESULT_DONE
    }

    private suspend fun writeBackup(outputStream: OutputStream, appWidgetIdScope: AppWidgetIdScope): Int {
        val widgetShortcuts = appWidgetIdScope.scope.get<WidgetShortcutsModel>().apply {
            init()
        }
        val notificationShortcuts = NotificationShortcutsModel.init(context, database)
        val widgetSettings = appWidgetIdScope.scope.get<WidgetSettings>()

        try {
            val writer = JsonWriter(OutputStreamWriter(outputStream))
            writer.beginObject()

            val settingsWriter = writer.name("settings")
            widgetSettings.writeJson(settingsWriter)

            val inCarWriter = writer.name("incar")
            inCarSettings.writeJson(inCarWriter)

            val arrayWriter = writer.name("shortcuts").beginArray()
            val shortcutsJsonWriter = ShortcutsJsonWriter()
            shortcutsJsonWriter.writeList(arrayWriter, widgetShortcuts.shortcuts, database, context)
            arrayWriter.endArray()

            val arrayNotificationWriter = writer.name("notificationShortcuts").beginArray()
            ShortcutsJsonWriter().writeList(arrayNotificationWriter, notificationShortcuts.shortcuts, database, context)
            arrayNotificationWriter.endArray()

            writer.endObject()
            writer.close()
        } catch (e: IOException) {
            AppLog.e(e)
            return Backup.ERROR_FILE_WRITE
        }
        return Backup.RESULT_DONE
    }

    private fun checkBackup(inputStream: InputStream): BackupCheckResult {
        var hasInCar = false
        var hasSettings = false
        val result  = try {
            val reader = JsonReader(inputStream.bufferedReader())
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "settings" -> {
                        hasSettings = true
                        reader.skipValue()
                    }
                    "incar" -> {
                        hasInCar = true
                        reader.skipValue()
                    }
                    "shortcuts" -> {
                        reader.skipValue()
                    }
                    "notificationShortcuts" -> {
                        reader.skipValue()
                    }
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
            reader.close()
            Backup.RESULT_DONE
        } catch (e: IOException) {
            AppLog.e(e)
            Backup.ERROR_FILE_READ
        } catch (e: ClassCastException) {
            AppLog.e(e)
            Backup.ERROR_DESERIALIZE
        }

        if (result != Backup.RESULT_DONE) {
            return BackupCheckResult.Error(errorCode = result)
        }

        if (!hasSettings) {
            return BackupCheckResult.Error(errorCode = Backup.ERROR_INCORRECT_FORMAT)
        }

        return BackupCheckResult.Success(hasInCar = hasInCar)
    }
}