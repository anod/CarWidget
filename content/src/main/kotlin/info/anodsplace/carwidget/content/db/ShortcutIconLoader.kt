package info.anodsplace.carwidget.content.db

import android.content.Context
import android.graphics.Path
import android.graphics.drawable.AdaptiveIconDrawable
import info.anodsplace.carwidget.content.preferences.WidgetStorage
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.graphics.AdaptiveIcon
import java.lang.Exception

class ShortcutIconLoader(
    private val db: ShortcutsDatabase,
    private val adaptiveIconPath: Path,
    private val context: Context) {

    constructor(db: ShortcutsDatabase, defaultsProvider: WidgetSettings.DefaultsProvider, appWidgetId: Int, context: Context)
        : this(db, WidgetStorage.load(context, defaultsProvider, appWidgetId).adaptiveIconPath, context)

    fun load(shortcut: Shortcut): ShortcutIcon {
        if (shortcut.isApp && !shortcut.isCustomIcon) {
            if (adaptiveIconPath.isEmpty) {
                return loadFromDatabase(shortcut.id)
            }

            try {
                val activityIcon = context.packageManager.getActivityIcon(shortcut.intent)
                if (activityIcon is AdaptiveIconDrawable) {
                    val bitmap = AdaptiveIcon(activityIcon, adaptiveIconPath, context).toBitmap()
                    return ShortcutIcon.forActivity(shortcut.id, bitmap)
                }
            } catch (e: Exception) {
                AppLog.e(e)
            }
        }

        return loadFromDatabase(shortcut.id)
    }

    fun loadFromDatabase(shortcutId: Long): ShortcutIcon {
        val shortcutUri = LauncherSettings.Favorites.getContentUri(context.packageName, shortcutId)
        return db.loadShortcutIcon(shortcutUri)
    }
}