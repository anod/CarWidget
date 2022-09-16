package info.anodsplace.carwidget.content.db

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.AdaptiveIconDrawable
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.graphics.UtilitiesBitmap
import info.anodsplace.graphics.AdaptiveIcon

interface ShortcutIconLoader {
    suspend fun load(shortcut: Shortcut, adaptiveIconStyle: String): ShortcutIcon

    class AppOnly(private val context: Context) : ShortcutIconLoader {
        override suspend fun load(shortcut: Shortcut, adaptiveIconStyle: String): ShortcutIcon {
            if (shortcut.isApp) {
               val maskPath = AdaptiveIcon.maskToPath(adaptiveIconStyle)
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

            val bitmap = UtilitiesBitmap.makeDefaultIcon(context.packageManager)
            return ShortcutIcon.forFallbackIcon(shortcut.id, bitmap)
        }
    }
}

class DbShortcutIconLoader(
    private val db: ShortcutsDatabase,
    private val context: Context
) : ShortcutIconLoader {
    override suspend fun load(shortcut: Shortcut, adaptiveIconStyle: String): ShortcutIcon {
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