package com.anod.car.home.model

import android.content.Context
import android.graphics.Path
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import com.anod.car.home.prefs.model.WidgetStorage
import com.anod.car.home.utils.AdaptiveIcon
import info.anodsplace.framework.AppLog
import java.lang.Exception

class ShortcutIconLoader(
        private val db: ShortcutsDatabase,
        private val adaptiveIconPath: Path,
        private val context: Context) {

    constructor(db: ShortcutsDatabase, appWidgetId: Int, context: Context)
        : this(db, WidgetStorage.load(context, appWidgetId).adaptiveIconPath, context)

    fun load(shortcut: Shortcut): ShortcutIcon {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && shortcut.isApp && !shortcut.isCustomIcon) {
            if (adaptiveIconPath.isEmpty) {
                val shortcutUri = LauncherSettings.Favorites.getContentUri(context.packageName, shortcut.id)
                return db.loadShortcutIcon(shortcutUri)
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

        val shortcutUri = LauncherSettings.Favorites.getContentUri(context.packageName, shortcut.id)
        return db.loadShortcutIcon(shortcutUri)
    }

}