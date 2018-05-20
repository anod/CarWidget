package com.anod.car.home.backup

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.support.v4.content.res.ResourcesCompat
import android.text.TextUtils
import android.util.JsonReader
import android.util.SparseArray

import com.anod.car.home.model.LauncherSettings
import com.anod.car.home.model.Shortcut
import com.anod.car.home.model.ShortcutIcon
import info.anodsplace.framework.AppLog

import com.anod.car.home.utils.SoftReferenceThreadLocal
import com.anod.car.home.utils.UtilitiesBitmap

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.ref.SoftReference
import java.net.URISyntaxException
import java.util.ArrayList

/**
 * @author algavris
 * @date 08/04/2016.
 */
class ShortcutsJsonReader(private val mContext: Context) {

    private val mIconBitmapSize: Int = UtilitiesBitmap.getIconMaxSize(mContext)
    private val mUnusedBitmaps: ArrayList<SoftReference<Bitmap>> = ArrayList()

    private val mCachedIconCanvas = object : SoftReferenceThreadLocal<Canvas>() {
        override fun initialValue(): Canvas {
            return Canvas()
        }
    }

    private val mCachedBitmapFactoryOptions = object : SoftReferenceThreadLocal<BitmapFactory.Options>() {
        override fun initialValue(): BitmapFactory.Options {
            return BitmapFactory.Options()
        }
    }

    @Throws(IOException::class)
    fun readList(reader: JsonReader): SparseArray<ShortcutWithIconAndPosition> {
        val shortcuts = SparseArray<ShortcutWithIconAndPosition>()
        reader.beginArray()

        while (reader.hasNext()) {
            var unusedBitmap: Bitmap? = null
            synchronized(sLock) {
                // not in cache; we need to load it from the db
                while ((unusedBitmap == null || !unusedBitmap!!.isMutable ||
                                unusedBitmap!!.width != mIconBitmapSize ||
                                unusedBitmap!!.height != mIconBitmapSize) && mUnusedBitmaps.size > 0) {
                    unusedBitmap = mUnusedBitmaps.removeAt(0).get()
                }
                if (unusedBitmap != null) {
                    val canvas = mCachedIconCanvas.get()
                    canvas.setBitmap(unusedBitmap)
                    canvas.drawColor(0, PorterDuff.Mode.CLEAR)
                    canvas.setBitmap(null)
                }

                if (unusedBitmap == null) {
                    unusedBitmap = Bitmap.createBitmap(mIconBitmapSize, mIconBitmapSize, Bitmap.Config.ARGB_8888)
                }
            }

            val shortcut = readShortcut(reader, unusedBitmap!!)
            shortcuts.put(shortcut.pos, shortcut)
        }

        reader.endArray()
        return shortcuts
    }

    @Throws(IOException::class)
    private fun readShortcut(reader: JsonReader, unusedBitmap: Bitmap): ShortcutWithIconAndPosition {
        reader.beginObject()

        var pos = -1
        var iconType = 0
        var itemType = 0
        var iconData: ByteArray? = null
        var iconPackageName = ""
        var iconResourceName = ""
        var title: CharSequence = ""
        var isCustomIcon = false
        var intent: Intent? = null

        while (reader.hasNext()) {
            val name = reader.nextName()
            if (name == "pos") {
                pos = reader.nextInt()
            } else if (name == LauncherSettings.Favorites.ITEM_TYPE) {
                itemType = reader.nextInt()
            } else if (name == LauncherSettings.Favorites.TITLE) {
                title = reader.nextString()
            } else if (name == LauncherSettings.Favorites.INTENT) {
                val intentDescription = reader.nextString()
                if (!TextUtils.isEmpty(intentDescription)) {
                    try {
                        intent = Intent.parseUri(intentDescription, 0)
                    } catch (e: URISyntaxException) {
                        AppLog.e(e)
                    }

                }
            } else if (name == LauncherSettings.Favorites.ICON_TYPE) {
                iconType = reader.nextInt()
            } else if (name == LauncherSettings.Favorites.ICON) {
                val baos = ByteArrayOutputStream()
                reader.beginArray()
                while (reader.hasNext()) {
                    baos.write(reader.nextInt())
                }
                reader.endArray()
                iconData = baos.toByteArray()
            } else if (name == LauncherSettings.Favorites.ICON_PACKAGE) {
                iconPackageName = reader.nextString()
            } else if (name == LauncherSettings.Favorites.ICON_RESOURCE) {
                iconResourceName = reader.nextString()
            } else if (name == LauncherSettings.Favorites.IS_CUSTOM_ICON) {
                isCustomIcon = reader.nextInt() == 1
            }
        }

        val info = Shortcut(Shortcut.idUnknown, itemType, title, isCustomIcon, intent ?: Intent())

        var bitmap: Bitmap? = null
        var icon: ShortcutIcon? = null
        if (itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
            bitmap = decodeIcon(iconData, unusedBitmap)
            if (isCustomIcon) {
                icon = ShortcutIcon.forCustomIcon(Shortcut.idUnknown.toLong(), bitmap)
            } else {
                icon = ShortcutIcon.forActivity(Shortcut.idUnknown.toLong(), bitmap)
            }
        } else {
            if (iconType == LauncherSettings.Favorites.ICON_TYPE_RESOURCE) {
                val iconResource = Intent.ShortcutIconResource()
                iconResource.packageName = iconPackageName
                iconResource.resourceName = iconResourceName
                // the resource
                try {
                    val resources = mContext.packageManager
                            .getResourcesForApplication(iconPackageName)
                    if (resources != null) {
                        val resId = resources.getIdentifier(iconResourceName, null, null)
                        if (resId > 0) {
                            bitmap = UtilitiesBitmap.createHiResIconBitmap(ResourcesCompat.getDrawable(resources, resId, null), mContext)
                        }
                    }
                } catch (e: PackageManager.NameNotFoundException) {
                    // drop this. we have other places to look for icons
                    AppLog.e(e)
                } catch (e: Resources.NotFoundException) {
                    AppLog.e(e)
                }

                // the db
                if (bitmap == null) {
                    bitmap = decodeIcon(iconData, unusedBitmap)
                }
                if (bitmap != null) {
                    icon = ShortcutIcon.forIconResource(Shortcut.idUnknown.toLong(), bitmap, iconResource)
                }
            } else if (iconType == LauncherSettings.Favorites.ICON_TYPE_BITMAP) {
                bitmap = decodeIcon(iconData, unusedBitmap)
                if (bitmap != null) {
                    icon = ShortcutIcon.forCustomIcon(Shortcut.idUnknown.toLong(), bitmap)
                }
            }
        }

        if (bitmap == null) {
            bitmap = UtilitiesBitmap.makeDefaultIcon(mContext.packageManager)
            icon = ShortcutIcon.forFallbackIcon(Shortcut.idUnknown.toLong(), bitmap)
        }

        reader.endObject()
        return ShortcutWithIconAndPosition(info, icon, pos)
    }

    class ShortcutWithIconAndPosition(var info: Shortcut, var icon: ShortcutIcon?, var pos: Int)

    private fun decodeIcon(data: ByteArray?, unusedBitmap: Bitmap): Bitmap? {
        if (data == null || data.isEmpty()) {
            return null
        }
        val opts = mCachedBitmapFactoryOptions.get()
        opts.outWidth = mIconBitmapSize
        opts.outHeight = mIconBitmapSize
        opts.inSampleSize = 1
        //        opts.inMutable = true;
        if (UtilitiesBitmap.canUseForInBitmap(unusedBitmap, opts)) {
            opts.inBitmap = unusedBitmap
        }
        try {
            return BitmapFactory.decodeByteArray(data, 0, data.size, opts)
        } catch (e: Exception) {
            AppLog.e(e)
            // throw new RuntimeException(e.getMessage(), e);
            return null
        }

    }

    companion object {
        private val sLock = Any()
    }
}
