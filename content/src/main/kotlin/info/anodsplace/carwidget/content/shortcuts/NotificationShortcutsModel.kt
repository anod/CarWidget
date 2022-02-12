package info.anodsplace.carwidget.content.shortcuts

import android.content.Context
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutIcon
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.carwidget.content.preferences.InCarStorage
import info.anodsplace.carwidget.content.preferences.WidgetStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

import java.util.ArrayList

class NotificationShortcutsModel private constructor(context: Context, database: ShortcutsDatabase) : AbstractShortcuts(context, database) {

    val filledCount: Int
        get() {
            var result = 0
            for (i in 0 until InCarStorage.NOTIFICATION_COMPONENT_NUMBER) {
                if (get(i) != null) {
                    result++
                }
            }
            return result
        }

    override suspend fun createDefaultShortcuts() {
        // Nothing
    }

    override fun loadCount(): Int {
        return InCarStorage.NOTIFICATION_COMPONENT_NUMBER
    }

    override suspend fun loadShortcuts(): Map<Int, Shortcut?> {
        return shortcutsDatabase.loadTarget(notificationTargetId)
    }

    override suspend fun dropShortcut(position: Int) {
        shortcutsDatabase.deleteTargetPosition(notificationTargetId, position)
    }

    override suspend fun saveShortcut(position: Int, shortcut: Shortcut, icon: ShortcutIcon) {
        shortcutsDatabase.addItem(notificationTargetId, position, shortcut, icon)
    }

    override suspend fun moveShortcut(from: Int, to: Int) {
        shortcutsDatabase.moveShortcut(notificationTargetId, from, to)
    }

    override suspend fun runDbMigration() {
        val ids = InCarStorage.getMigrateIds(context)
        shortcutsDatabase.migrateShortcutPosition(notificationTargetId, ids)
        InCarStorage.launcherComponentsMigrated(context)
    }

    override fun isMigrated(): Boolean = InCarStorage.isDbMigrated(context)

    override fun countUpdated(count: Int) {

    }

    companion object {
        const val notificationTargetId = -1
        fun request(context: Context, database: ShortcutsDatabase): Flow<NotificationShortcutsModel> = flow {
            emit(init(context, database))
        }

        suspend fun init(context: Context, database: ShortcutsDatabase): NotificationShortcutsModel {
            val model = NotificationShortcutsModel(context, database)
            model.init()
            return model
        }
    }
}
