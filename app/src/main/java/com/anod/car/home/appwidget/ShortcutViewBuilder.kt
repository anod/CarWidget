package com.anod.car.home.appwidget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.appwidget.PendingIntentFactory
import info.anodsplace.carwidget.content.IconBackgroundProcessor
import info.anodsplace.carwidget.content.IconTheme
import info.anodsplace.carwidget.content.SkinProperties
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutIconLoader
import info.anodsplace.carwidget.content.db.Shortcuts
import info.anodsplace.carwidget.content.graphics.BitmapTransform
import info.anodsplace.carwidget.content.graphics.UtilitiesBitmap
import info.anodsplace.carwidget.content.preferences.WidgetInterface

interface ShortcutViewBuilderInstance {
    var bitmapMemoryCache: LruCache<String, Bitmap>?
    suspend fun fill(views: RemoteViews, position: Int, resBtn: Int, resText: Int)
}

/**
 * @author alex
 * @date 1/4/14
 */
class ShortcutViewBuilder(
        private val context: Context,
        private val appWidgetId: Int,
        private val pendingIntentFactory: PendingIntentFactory,
        private val iconLoader: ShortcutIconLoader,
): ShortcutViewBuilderInstance {
    private var intance: Instance? = null

    fun init(scaledDensity: Float, skinProperties: SkinProperties,
             iconTheme: IconTheme?, prefs: WidgetInterface, shortcuts: Shortcuts,
             bitmapTransform: BitmapTransform
    ) {
        this.intance = Instance(
             context = context,
             appWidgetId = appWidgetId,
             pendingIntentFactory = pendingIntentFactory,
             scaledDensity = scaledDensity,
             skinProperties = skinProperties,
             iconTheme = iconTheme,
             prefs = prefs,
             shortcuts = shortcuts,
             bitmapTransform = bitmapTransform,
             iconLoader = iconLoader,
        )
    }

    override var bitmapMemoryCache: LruCache<String, Bitmap>?
        get() = this.intance?.bitmapMemoryCache
        set(value) {
            this.intance?.bitmapMemoryCache = value
        }

    override suspend fun fill(views: RemoteViews, position: Int, resBtn: Int, resText: Int) {
        this.intance!!.fill(views, position, resBtn, resText)
    }

    class Instance(
        private val context: Context,
        private val appWidgetId: Int,
        private val pendingIntentFactory: PendingIntentFactory,
        private val skinProperties: SkinProperties,
        private val iconTheme: IconTheme?,
        private val prefs: WidgetInterface,
        private val shortcuts: Shortcuts,
        private val bitmapTransform: BitmapTransform,
        private val scaledDensity: Float,
        private val iconLoader: ShortcutIconLoader,
    ) : ShortcutViewBuilderInstance {

        override var bitmapMemoryCache: LruCache<String, Bitmap>? = null

        private val backgroundProcessor: IconBackgroundProcessor? = skinProperties.backgroundProcessor
        private val adaptiveIconPath: Path = prefs.adaptiveIconPath

        override suspend fun fill(views: RemoteViews, position: Int, resBtn: Int, resText: Int) {
            val info = shortcuts.get(position)

            var icon: Bitmap? = null
            if (info == null) {
                setNoShortcut(resBtn, resText, views, position, skinProperties)
            } else {
                AppLog.i("Shortcut: " + (info.intent.component?.toShortString()
                        ?: info.intent.toString()))
                icon = applyShortcut(resBtn, resText, info, views, position, iconTheme)
            }
            if (prefs.isTitlesHide) {
                views.setViewVisibility(resText, View.GONE)
            } else {
                setFont(resText, scaledDensity, views)
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

        private fun setNoShortcut(res: Int, resText: Int, views: RemoteViews, cellId: Int,
                                  skinProp: SkinProperties) {
            views.setImageViewResource(res, skinProp.setShortcutRes)

            if (!prefs.isTitlesHide) {
                val title = context.resources.getString(skinProp.setShortcutText)
                views.setTextViewText(resText, title)
            }
            val configIntent = pendingIntentFactory.createNew(appWidgetId, cellId)
            views.setOnClickPendingIntent(res, configIntent)
            views.setOnClickPendingIntent(resText, configIntent)
        }

        private fun setFont(resText: Int, scaledDensity: Float, views: RemoteViews) {
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
                    /*
                     * Limitation of RemoteViews to use setTextSize with only one
                     * argument (without providing scale unit) size already in
                     * scaled pixel format so we revert it to pixels to get properly
                     * converted after re-applying setTextSize function
                     */
                    val cSize = prefs.fontSize.toFloat() / scaledDensity

                    views.setFloat(resText, "setTextSize", cSize)
                    views.setViewVisibility(resText, View.VISIBLE)
                }
            }
        }

        private suspend fun applyShortcut(res: Int, resText: Int, info: Shortcut, views: RemoteViews, cellId: Int, themeIcons: IconTheme?): Bitmap {

            val themePackage = themeIcons?.packageName ?: "null"
            val transformKey = bitmapTransform.cacheKey
            val imageKey = info.id.toString() + ":" + themePackage + ":" + transformKey

            var icon = getBitmapFromMemCache(imageKey)
            if (icon == null) {
                icon = shortcutBitmap(info, themeIcons)
                icon = bitmapTransform.transform(icon)
                addBitmapToMemCache(imageKey, icon)
            }

            views.setImageViewBitmap(res, icon)

            if (!prefs.isTitlesHide) {
                val title = info.title.toString()
                views.setTextViewText(resText, title)
            }
            pendingIntentFactory.createShortcut(info.intent, appWidgetId, cellId, info.id)?.let {
                views.setOnClickPendingIntent(res, it)
                views.setOnClickPendingIntent(resText, it)
            }
            return icon
        }

        private fun addBitmapToMemCache(key: String, bitmap: Bitmap?) {
            if (bitmapMemoryCache == null) {
                return
            }
            synchronized(sBitmapCacheLock) {
                if (getBitmapFromMemCache(key) == null) {
                    bitmapMemoryCache!!.put(key, bitmap)
                }
            }
        }

        private fun getBitmapFromMemCache(key: String): Bitmap? {
            if (bitmapMemoryCache == null) {
                return null
            }
            synchronized(sBitmapCacheLock) {
                return bitmapMemoryCache!!.get(key)
            }
        }

        private suspend fun shortcutBitmap(info: Shortcut, themeIcons: IconTheme?): Bitmap {
            if (themeIcons == null || !info.isApp || info.isCustomIcon) {
                val icon = iconLoader.load(info, adaptiveIconPath)
                return icon.bitmap
            }

            val resourceId = themeIcons.getIcon(info.intent.component!!.className)
            var iconDrawable: Drawable? = null
            if (resourceId != 0) {
                iconDrawable = themeIcons.getDrawable(resourceId)
            }
            if (iconDrawable is BitmapDrawable) {
                return iconDrawable.bitmap
            }
            if (iconDrawable != null) {
                return UtilitiesBitmap.createHiResIconBitmap(iconDrawable, context)
            }
            val icon = iconLoader.load(info, adaptiveIconPath)
            return icon.bitmap
        }
    }

    companion object {
        private val sBitmapCacheLock = Any()
    }

}