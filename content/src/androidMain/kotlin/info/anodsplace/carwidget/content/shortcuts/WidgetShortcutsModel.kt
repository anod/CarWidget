package info.anodsplace.carwidget.content.shortcuts

import android.content.Context
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.di.unaryPlus
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.carwidget.content.preferences.WidgetStorage

class WidgetShortcutsModel(
    context: Context,
    database: ShortcutsDatabase,
    private val widgetSettings: WidgetSettings,
    appWidgetIdScope: AppWidgetIdScope,
) : AbstractShortcuts(context, database) {

    override val targetId: Int = +appWidgetIdScope

    override val count: Int
        get() = widgetSettings.shortcutsNumber

    override suspend fun runDbMigration() {
        val ids = WidgetStorage.getMigrateIds(context, targetId)
        shortcutsDatabase.migrateShortcutPosition(targetId, ids)
        WidgetStorage.launcherComponentsMigrated(context, targetId)
    }

    override fun isMigrated(): Boolean {
        return WidgetStorage.isDbMigrated(context, targetId)
    }

    override suspend fun createDefaultShortcuts() {
        init()
        val defaultShortcuts = DefaultShortcuts.load(context)
        defaultShortcuts.forEachIndexed { index, result ->
            save(index, result.info, result.icon)
        }
    }
}