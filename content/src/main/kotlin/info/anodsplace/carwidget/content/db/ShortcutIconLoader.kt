package info.anodsplace.carwidget.content.db

import android.content.Context
import android.graphics.drawable.AdaptiveIconDrawable
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.graphics.UtilitiesBitmap
import info.anodsplace.graphics.AdaptiveIcon

interface ShortcutIconLoader {
    suspend fun load(shortcut: Shortcut, adaptiveIconStyle: String, converter: ShortcutIconConverter? = null): ShortcutIcon

    class Activity(
            private val context: Context,
            private val allowEmptyMask: Boolean = true,
            private val throwOnError: Boolean = false
    ) : ShortcutIconLoader {
        override suspend fun load(shortcut: Shortcut, adaptiveIconStyle: String, converter: ShortcutIconConverter?): ShortcutIcon {
            if (shortcut.isApp) {
                if (!allowEmptyMask && adaptiveIconStyle.isEmpty()) {
                    throw IllegalStateException("Empty mask")
                }
                val maskPath = AdaptiveIcon.maskToPath(adaptiveIconStyle)
                if (!allowEmptyMask && maskPath.isEmpty) {
                    throw IllegalStateException("Empty mask")
                }
                try {
                    val activityIcon = context.packageManager.getActivityIcon(shortcut.intent)
                    if (activityIcon is AdaptiveIconDrawable) {
                        val bitmap = AdaptiveIcon(context, maskPath).fromDrawable(activityIcon)
                        return ShortcutIcon.forActivity(shortcut.id, bitmap)
                    }
                } catch (e: Exception) {
                    AppLog.e(e)
                    if (throwOnError) {
                        throw e
                    }
                }
            }

            if (throwOnError) {
                throw IllegalStateException("No icon found")
            }

            val bitmap = UtilitiesBitmap.makeDefaultIcon(context.packageManager)
            return ShortcutIcon.forFallbackIcon(shortcut.id, bitmap)
        }
    }
}

class DbShortcutIconLoader(
        private val db: ShortcutsDatabase,
        context: Context
) : ShortcutIconLoader {
    private val iconConverter: ShortcutIconConverter by lazy { ShortcutIconConverter.Default(context) }

    private val activityIconLoader: ShortcutIconLoader = ShortcutIconLoader.Activity(
            context = context,
            allowEmptyMask = false,
            throwOnError = true
    )

    override suspend fun load(shortcut: Shortcut, adaptiveIconStyle: String, converter: ShortcutIconConverter?): ShortcutIcon {
        if (shortcut.isApp && !shortcut.isCustomIcon) {
            try {
                return activityIconLoader.load(shortcut, adaptiveIconStyle, converter)
            } catch (_: IllegalStateException) {
                // Fallback to DB
            } catch (e: Exception) {
                AppLog.e(e)
            }
        }

        val dbShortcut = db.loadByShortcutId(shortcut.id)
        val actualConverter = converter ?: iconConverter
        return actualConverter.convert(shortcut.id, dbShortcut)
    }
}