package info.anodsplace.carwidget.content.db

import android.content.Context
import android.graphics.drawable.AdaptiveIconDrawable
import info.anodsplace.applog.AppLog
import info.anodsplace.graphics.AdaptiveIcon

class ShortcutIconLoader(
    private val db: ShortcutsDatabase,
    private val context: Context
) {
    suspend fun load(shortcut: Shortcut, adaptiveIconStyle: String): ShortcutIcon {
        if (shortcut.isApp && !shortcut.isCustomIcon) {

            if (adaptiveIconStyle.isEmpty()) {
                return ShortcutsDatabase.loadIconFromDatabase(shortcut.id, context, db)
            }

            val maskPath = AdaptiveIcon.maskToPath(adaptiveIconStyle)
            if (maskPath.isEmpty) {
                return ShortcutsDatabase.loadIconFromDatabase(shortcut.id, context, db)
            }

            try {
                val activityIcon = context.packageManager.getActivityIcon(shortcut.intent)
                if (activityIcon is AdaptiveIconDrawable) {
                    val bitmap = AdaptiveIcon(context, maskPath).fromDrawable(activityIcon)
                    return ShortcutIcon.forActivity(shortcut.id, bitmap)
                }
            } catch (e: Exception) {
                AppLog.e(e)
            }
        }

        return ShortcutsDatabase.loadIconFromDatabase(shortcut.id, context, db)
    }
}