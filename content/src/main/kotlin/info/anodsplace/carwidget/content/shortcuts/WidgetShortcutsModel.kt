package info.anodsplace.carwidget.content.shortcuts

import android.content.Context
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutIcon
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

    private val appWidgetId = +appWidgetIdScope

    override fun loadCount(): Int {
        return widgetSettings.shortcutsNumber
    }

    override suspend fun loadShortcuts(): Map<Int, Shortcut?> {
        return shortcutsDatabase.loadTarget(appWidgetId)
    }

    override suspend fun dropShortcut(position: Int) {
        shortcutsDatabase.deleteTargetPosition(appWidgetId, position)
    }

    override suspend fun saveShortcut(position: Int, shortcut: Shortcut, icon: ShortcutIcon) {
        shortcutsDatabase.addItem(appWidgetId, position, shortcut, icon)
    }

    override suspend fun moveShortcut(from: Int, to: Int) {
        shortcutsDatabase.moveShortcut(appWidgetId, from, to)
    }

    override suspend fun runDbMigration() {
        val ids = WidgetStorage.getMigrateIds(context, appWidgetId)
        shortcutsDatabase.migrateShortcutPosition(appWidgetId, ids)
        WidgetStorage.launcherComponentsMigrated(context, appWidgetId)
    }

    override fun isMigrated(): Boolean {
        return WidgetStorage.isDbMigrated(context, appWidgetId)
    }

    override suspend fun createDefaultShortcuts() {
        init()
        val defaultShortcuts = DefaultShortcuts.load(context)
        defaultShortcuts.forEachIndexed { index, result ->
            saveShortcut(index, result.info!!, result.icon!!)
        }
    }
}