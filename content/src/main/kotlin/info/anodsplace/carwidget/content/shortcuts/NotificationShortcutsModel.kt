package info.anodsplace.carwidget.content.shortcuts

import android.content.Context
import android.graphics.Path
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutIconLoader
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.carwidget.content.preferences.InCarStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

import java.util.ArrayList

class NotificationShortcutsModel private constructor(context: Context, database: ShortcutsDatabase) : AbstractShortcuts(context, database) {

    val filledCount: Int
        get() {
            val ids = this.loadIds()
            var count = 0
            for (i in 0 until InCarStorage.NOTIFICATION_COMPONENT_NUMBER) {
                count += if (ids[i] == Shortcut.idUnknown) 0 else 1
            }
            return count
        }

    override suspend fun createDefaultShortcuts() {
        // Nothing
    }

    override fun loadCount(): Int {
        return InCarStorage.NOTIFICATION_COMPONENT_NUMBER
    }

    override suspend fun saveId(position: Int, shortcutId: Long) {
        InCarStorage.saveNotifShortcut(shortcutsDatabase, context, shortcutId, position)
    }

    override fun dropId(position: Int) {
        InCarStorage.dropNotifShortcut(position, context)
    }

    override fun loadIds(): ArrayList<Long> {
        return InCarStorage.getNotifComponents(context)
    }

    override fun countUpdated(count: Int) {

    }

    companion object {

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
