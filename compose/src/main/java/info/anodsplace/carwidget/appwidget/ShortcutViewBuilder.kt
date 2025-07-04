package info.anodsplace.carwidget.appwidget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.LruCache
import android.util.TypedValue.COMPLEX_UNIT_PX
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.IconBackgroundProcessor
import info.anodsplace.carwidget.content.IconTheme
import info.anodsplace.carwidget.content.SkinProperties
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutIcon
import info.anodsplace.carwidget.content.db.ShortcutIconLoader
import info.anodsplace.carwidget.content.graphics.BitmapTransform
import info.anodsplace.carwidget.content.graphics.UtilitiesBitmap
import info.anodsplace.carwidget.content.preferences.WidgetInterface

/**
 * @author alex
 * @date 1/4/14
 */
class ShortcutViewBuilder(
    private val skinProperties: SkinProperties,
    private val context: Context,
    private val appWidgetId: Int,
    private val pendingIntentFactory: PendingIntentFactory,
    private val iconLoader: ShortcutIconLoader,
    private val prefs: WidgetInterface,
    private val shortcuts: Map<Int, Shortcut?>,
    private val bitmapTransform: BitmapTransform,
    private val bitmapMemoryCache: LruCache<String, Bitmap>? = null
) : ShortcutViewBuilderInstance {
    private val backgroundProcessor: IconBackgroundProcessor? = skinProperties.backgroundProcessor

    override var iconTheme: IconTheme? = null

    override suspend fun fill(views: RemoteViews, position: Int, resBtn: Int, resText: Int) {
        val info = shortcuts.getOrDefault(position, null)

        var icon: Bitmap? = null
        if (info == null) {
            setNoShortcut(resBtn, resText, views, position, skinProperties)
        } else {
            AppLog.i(info.intent.toString(), tag = "ShortcutViewBuilder")
            icon = applyShortcut(resBtn, resText, info, views, position, iconTheme)
        }
        if (prefs.isTitlesHide) {
            views.setViewVisibility(resText, View.GONE)
        } else {
            setFont(resText, views)
        }
        if (backgroundProcessor != null) {
            setIconBackground(icon, resBtn, views)
        }
    }

    private fun setIconBackground(icon: Bitmap?, res: Int, views: RemoteViews) {
        val color = backgroundProcessor!!.getColor(prefs, icon)
        views.setViewVisibility(res, View.VISIBLE)
        views.setInt(res, "setBackgroundColor", color)
    }

    private fun setNoShortcut(
        res: Int, resText: Int, views: RemoteViews, cellId: Int,
        skinProp: SkinProperties
    ) {
        views.setImageViewResource(res, skinProp.setShortcutRes)

        if (!prefs.isTitlesHide) {
            val title = context.resources.getString(skinProp.setShortcutText)
            views.setTextViewText(resText, title)
        }
        val configIntent = pendingIntentFactory.createNew(appWidgetId, cellId)
        views.setOnClickPendingIntent(res, configIntent)
        views.setOnClickPendingIntent(resText, configIntent)
    }

    private fun setFont(resText: Int, views: RemoteViews) {
        if (prefs.fontColor != null) {
            views.setTextColor(resText, prefs.fontColor!!)
        } else if (skinProperties.fontColorRes != 0) {
            val fontColor = ContextCompat.getColor(context, skinProperties.fontColorRes)
            views.setTextColor(resText, fontColor)
        }
        if (prefs.fontSize != WidgetInterface.FONT_SIZE_UNDEFINED) {
            if (prefs.fontSize == 0) {
                views.setViewVisibility(resText, View.GONE)
            } else {
                views.setTextViewTextSize(resText, COMPLEX_UNIT_PX, prefs.fontSize.toFloat())
                views.setViewVisibility(resText, View.VISIBLE)
            }
        }
    }

    private suspend fun applyShortcut(res: Int, resText: Int, shortcut: Shortcut, views: RemoteViews, cellId: Int, themeIcons: IconTheme?): Bitmap {

        val themePackage = themeIcons?.packageName ?: "null"
        val transformKey = bitmapTransform.cacheKey
        val imageKey = shortcut.id.toString() + ":" + themePackage + ":" + transformKey

        var iconBitmap = getBitmapFromMemCache(imageKey)
        var iconResource = 0
        if (iconBitmap == null) {
            val icon = shortcutIcon(shortcut, themeIcons)
            iconResource = skinProperties.iconResourceTint(icon.resource)
            iconBitmap = bitmapTransform.transform(icon.bitmap)
            if (iconResource != 0) { // TODO: Cache colorStateList
                addBitmapToMemCache(imageKey, iconBitmap)
            }
        }

        views.setImageViewBitmap(res, iconBitmap)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (iconResource != 0) {
                views.setColorStateList(res, "setImageTintList", iconResource)
            } else {
                views.setColorStateList(res, "setImageTintList", null)
            }
        }

        if (!prefs.isTitlesHide) {
            val title = shortcut.title.toString()
            views.setTextViewText(resText, title)
        }
        pendingIntentFactory.createShortcut(shortcut.intent, appWidgetId, cellId, shortcut.id)?.let {
            views.setOnClickPendingIntent(res, it)
            views.setOnClickPendingIntent(resText, it)
        }
        return iconBitmap
    }

    private fun addBitmapToMemCache(key: String, bitmap: Bitmap?) {
        if (bitmapMemoryCache != null && bitmapMemoryCache[key] != null) {
            bitmapMemoryCache.put(key, bitmap)
        }
    }

    private fun getBitmapFromMemCache(key: String): Bitmap? {
        return bitmapMemoryCache?.get(key)
    }

    private suspend fun shortcutIcon(shortcut: Shortcut, themeIcons: IconTheme?): ShortcutIcon {
        if (themeIcons == null || !shortcut.isApp || shortcut.isCustomIcon) {
            return iconLoader.load(shortcut, prefs.adaptiveIconStyle)
        }

        val resourceId = themeIcons.getIcon(shortcut.intent.component!!.className)
        var iconDrawable: Drawable? = null
        if (resourceId != 0) {
            iconDrawable = themeIcons.getDrawable(resourceId)
        }
        if (iconDrawable is BitmapDrawable) {
            return ShortcutIcon.forCustomIcon(shortcut.id, iconDrawable.bitmap)
        }
        if (iconDrawable != null) {
            val bitmap = UtilitiesBitmap.createHiResIconBitmap(iconDrawable, context)
            return ShortcutIcon.forCustomIcon(shortcut.id, bitmap)
        }
        return iconLoader.load(shortcut, prefs.adaptiveIconStyle)
    }
}