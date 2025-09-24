package info.anodsplace.carwidget.content.shortcuts

import android.content.Context
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.carwidget.content.preferences.InCarStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NotificationShortcutsModel(context: Context, database: ShortcutsDatabase) : AbstractShortcuts(context, database) {

    override val targetId: Int = NOTIFICATION_TARGET_ID
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

    override val count: Int = InCarStorage.NOTIFICATION_COMPONENT_NUMBER

    override suspend fun createDefaultShortcuts() {
        // Nothing
    }



    override suspend fun runDbMigration() {
        val ids = InCarStorage.getMigrateIds(context)
        shortcutsDatabase.migrateShortcutPosition(targetId, ids)
        InCarStorage.launcherComponentsMigrated(context)
    }

    override fun isMigrated(): Boolean = InCarStorage.isDbMigrated(context)

    companion object {
        const val NOTIFICATION_TARGET_ID = -1
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