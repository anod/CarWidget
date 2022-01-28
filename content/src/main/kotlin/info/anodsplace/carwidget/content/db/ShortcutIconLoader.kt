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
    private val context: Context
) {
    suspend fun load(shortcut: Shortcut, adaptiveIconPath: Path): ShortcutIcon {
        if (shortcut.isApp && !shortcut.isCustomIcon) {
            if (adaptiveIconPath.isEmpty) {
                return ShortcutsDatabase.loadIconFromDatabase(shortcut.id, context, db)
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

        return ShortcutsDatabase.loadIconFromDatabase(shortcut.id, context, db)
    }
}