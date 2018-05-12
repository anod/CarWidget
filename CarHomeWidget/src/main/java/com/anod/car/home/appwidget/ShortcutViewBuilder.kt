package com.anod.car.home.appwidget

import com.anod.car.home.model.LauncherSettings
import com.anod.car.home.model.Shortcut
import com.anod.car.home.model.WidgetShortcutsModel
import com.anod.car.home.prefs.model.WidgetSettings
import com.anod.car.home.prefs.preferences.Main
import com.anod.car.home.skin.SkinProperties
import com.anod.car.home.skin.icon.BackgroundProcessor
import info.anodsplace.framework.AppLog
import com.anod.car.home.utils.BitmapTransform
import com.anod.car.home.utils.IconTheme
import com.anod.car.home.utils.UtilitiesBitmap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import android.view.View
import android.widget.RemoteViews

/**
 * @author alex
 * @date 1/4/14
 */
class ShortcutViewBuilder(private val mContext: Context, private val mAppWidgetId: Int,
                          private val mPendingIntentFactory: WidgetViewBuilder.PendingIntentFactory) {

    private var mScaledDensity: Float = 0.toFloat()

    private var mSkinProperties: SkinProperties? = null

    private var mIconTheme: IconTheme? = null

    private var mPrefs: WidgetSettings? = null

    private var mShortcuts: WidgetShortcutsModel? = null

    private var mBitmapMemoryCache: LruCache<String, Bitmap>? = null

    private var mBitmapTransform: BitmapTransform? = null

    private var mBackgroundProcessor: BackgroundProcessor? = null

    fun init(scaledDensity: Float, skinProperties: SkinProperties,
             iconTheme: IconTheme?, prefs: WidgetSettings, smodel: WidgetShortcutsModel,
             bitmapTransform: BitmapTransform) {
        mScaledDensity = scaledDensity
        mSkinProperties = skinProperties
        mIconTheme = iconTheme
        mPrefs = prefs
        mShortcuts = smodel
        mBitmapTransform = bitmapTransform
        mBackgroundProcessor = mSkinProperties!!.backgroundProcessor
    }

    fun fill(views: RemoteViews, position: Int, resBtn: Int, resText: Int) {
        val info = mShortcuts!!.getShortcut(position)

        var icon: Bitmap? = null
        if (info == null) {
            setNoShortcut(resBtn, resText, views, position, mSkinProperties!!)
        } else {
            AppLog.d("Shortcut:" + info.intent.toString())
            icon = setShortcut(resBtn, resText, info, views, position, mIconTheme)
        }
        if (mPrefs!!.isTitlesHide) {
            views.setViewVisibility(resText, View.GONE)
        } else {
            setFont(resText, mScaledDensity, views)
        }
        if (mBackgroundProcessor != null) {
            setIconBackground(icon, resBtn, views)
        }

    }

    private fun setIconBackground(icon: Bitmap?, res: Int, views: RemoteViews) {
        val color = mBackgroundProcessor!!.getColor(mPrefs, icon)
        if (Color.alpha(color) == 0) {
            views.setViewVisibility(res, View.GONE)
        } else {
            views.setViewVisibility(res, View.VISIBLE)
            views.setInt(res, "setBackgroundColor", color)
        }
    }

    private fun setNoShortcut(res: Int, resText: Int, views: RemoteViews, cellId: Int,
                              skinProp: SkinProperties) {
        views.setImageViewResource(res, skinProp.setShortcutRes)

        if (!mPrefs!!.isTitlesHide) {
            val title = mContext.resources.getString(skinProp.setShortcutText)
            views.setTextViewText(resText, title)
        }
        val configIntent = mPendingIntentFactory.createNew(mAppWidgetId, cellId)
        views.setOnClickPendingIntent(res, configIntent)
        views.setOnClickPendingIntent(resText, configIntent)
    }

    private fun setFont(resText: Int, scaledDensity: Float, views: RemoteViews) {
        views.setTextColor(resText, mPrefs!!.fontColor)
        if (mPrefs!!.fontSize != Main.FONT_SIZE_UNDEFINED) {
            if (mPrefs!!.fontSize == 0) {
                views.setViewVisibility(resText, View.GONE)
            } else {
                /*
                 * Limitation of RemoteViews to use setTextSize with only one
                 * argument (without providing scale unit) size already in
                 * scaled pixel format so we revert it to pixels to get properly
                 * converted after re-applying setTextSize function
                 */
                val cSize = mPrefs!!.fontSize.toFloat() / scaledDensity

                views.setFloat(resText, "setTextSize", cSize)
                views.setViewVisibility(resText, View.VISIBLE)
            }
        }

    }

    private fun setShortcut(res: Int, resText: Int, info: Shortcut, views: RemoteViews, cellId: Int, themeIcons: IconTheme?): Bitmap {

        val themePackage = themeIcons?.packageName ?: "null"
        val transformKey = mBitmapTransform!!.cacheKey
        val imageKey = info.id.toString() + ":" + themePackage + ":" + transformKey

        var icon = getBitmapFromMemCache(imageKey)
        if (icon == null) {
            icon = getShortcutIcon(info, themeIcons)
            icon = mBitmapTransform!!.transform(icon)
            addBitmapToMemCache(imageKey, icon)
        }

        views.setImageViewBitmap(res, icon)

        if (!mPrefs!!.isTitlesHide) {
            val title = info.title.toString()
            views.setTextViewText(resText, title)
        }
        val shortcutIntent = mPendingIntentFactory
                .createShortcut(info.intent, mAppWidgetId, cellId, info.id)
        views.setOnClickPendingIntent(res, shortcutIntent)
        views.setOnClickPendingIntent(resText, shortcutIntent)

        return icon!!
    }

    private fun addBitmapToMemCache(key: String, bitmap: Bitmap?) {
        if (mBitmapMemoryCache == null) {
            return
        }
        synchronized(sBitmapCacheLock) {
            if (getBitmapFromMemCache(key) == null) {
                mBitmapMemoryCache!!.put(key, bitmap)
            }
        }
    }

    private fun getBitmapFromMemCache(key: String): Bitmap? {
        if (mBitmapMemoryCache == null) {
            return null
        }
        synchronized(sBitmapCacheLock) {
            return mBitmapMemoryCache!!.get(key)
        }
    }

    private fun getShortcutIcon(info: Shortcut, themeIcons: IconTheme?): Bitmap {
        if (themeIcons == null || info.itemType != LauncherSettings.Favorites.ITEM_TYPE_APPLICATION || info.isCustomIcon) {
            val icon = mShortcuts!!.loadIcon(info.id)
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
            return UtilitiesBitmap.createHiResIconBitmap(iconDrawable, mContext)
        }
        val icon = mShortcuts!!.loadIcon(info.id)
        return icon.bitmap
    }

    fun setBitmapMemoryCache(bitmapMemoryCache: LruCache<String, Bitmap>) {
        mBitmapMemoryCache = bitmapMemoryCache
    }

    companion object {

        private val sBitmapCacheLock = Any()
    }
}
