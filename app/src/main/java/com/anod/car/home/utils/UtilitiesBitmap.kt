package com.anod.car.home.utils

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.util.DisplayMetrics
import android.util.Log
import com.anod.car.home.R
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.max

object UtilitiesBitmap {
    private const val sizeIcon = 0 // icon with higher density
    private const val sizeSystem = 1 // default system icon size
    private const val sizeMax = 2 // max scale size
    const val maxScale = 3

    private var sIconSize = -1
    private var sIconSystem = -1
    private var sIconMaxScale = -1
    private var sSystemDensity = -1
    private var sIconDensity = -1

    private val sOldBounds = Rect()
    private val sCanvas = Canvas()

    init {
        sCanvas.drawFilter = PaintFlagsDrawFilter(Paint.DITHER_FLAG, Paint.FILTER_BITMAP_FLAG)
    }

    private fun initStatics(paramContext: Context) {
        val r = paramContext.resources

        val metrics = r.displayMetrics
        sSystemDensity = metrics.densityDpi
        sIconDensity = getHigherDensity(sSystemDensity)

        sIconSystem = r.getDimension(R.dimen.icon_size).toInt()
        sIconSize = convertForDensity(sIconSystem, sIconDensity, metrics.densityDpi).toInt()

        sIconMaxScale = sIconSize * maxScale

        val localPaintFlagsDrawFilter = PaintFlagsDrawFilter(4, 2)
        sCanvas.drawFilter = localPaintFlagsDrawFilter
    }

    private fun convertForDensity(value: Int, densityDpi: Int, deviceDensityDpi: Int): Float {
        return if (deviceDensityDpi == densityDpi) {
            value.toFloat()
        } else densityDpi / deviceDensityDpi.toFloat() * value
    }

    fun getSystemIconSize(context: Context): Int {
        synchronized(sCanvas) {
            // we share the statics :-(
            if (sIconSize == -1) {
                initStatics(context)
            }

            return sIconSystem
        }
    }

    fun getIconMaxSize(context: Context): Int {
        synchronized(sCanvas) {
            // we share the statics :-(
            if (sIconMaxScale == -1) {
                initStatics(context)
            }

            return sIconMaxScale
        }
    }

    fun getTargetDensity(context: Context): Int {
        synchronized(sCanvas) {
            // we share the statics :-(
            if (sIconSize == -1) {
                initStatics(context)
            }

            return sIconDensity
        }
    }

    private fun getHigherDensity(deviceDensity: Int): Int {

        if (deviceDensity <= DisplayMetrics.DENSITY_TV) {
            return DisplayMetrics.DENSITY_XXHIGH
        } else if (deviceDensity < DisplayMetrics.DENSITY_XXXHIGH) {
            return DisplayMetrics.DENSITY_XXXHIGH
        }

        return deviceDensity
    }

    fun makeDefaultIcon(manager: PackageManager): Bitmap {
        val d = manager.defaultActivityIcon
        val b = Bitmap.createBitmap(max(d.intrinsicWidth, 1),
                max(d.intrinsicHeight, 1), Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        d.setBounds(0, 0, b.width, b.height)
        d.draw(c)
        return b
    }

    fun createSystemIconBitmap(icon: Drawable, context: Context): Bitmap {
        return createIconBitmapSize(icon, sizeSystem, context)
    }

    fun createHiResIconBitmap(icon: Drawable, context: Context): Bitmap {
        return createIconBitmapSize(icon, sizeIcon, context)
    }

    fun createMaxSizeIcon(icon: Drawable, context: Context): Bitmap {
        return createIconBitmapSize(icon, sizeMax, context)
    }

    /**
     * Returns a bitmap suitable for the all apps view. The bitmap will be a
     * power of two sized ARGB_8888 bitmap that can be used as a gl texture.
     */
    private fun createIconBitmapSize(icon: Drawable, size: Int, context: Context): Bitmap {
        synchronized(sCanvas) {
            // we share the statics :-(
            if (sIconSize == -1) {
                initStatics(context)
            }

            var width: Int
            var height: Int

            when (size) {
                sizeIcon -> {
                    width = sIconSize
                    height = sIconSize
                }
                sizeMax -> {
                    width = sIconMaxScale
                    height = sIconMaxScale
                }
                else -> {
                    width = sIconSystem
                    height = sIconSystem
                }
            }

            if (icon is PaintDrawable) {
                icon.intrinsicWidth = width
                icon.intrinsicHeight = height
            } else if (icon is BitmapDrawable) {
                // Ensure the bitmap has a density.
                val bitmap = icon.bitmap

                if (bitmap.density == Bitmap.DENSITY_NONE) {
                    icon.setTargetDensity(context.resources.displayMetrics)
                }
            }
            val sourceWidth = icon.intrinsicWidth
            val sourceHeight = icon.intrinsicHeight

            if (sourceWidth > 0 && sourceWidth > 0) {
                // There are intrinsic sizes.
                if (width < sourceWidth || height < sourceHeight) {
                    // It's too big, scale it down.
                    val ratio = sourceWidth.toFloat() / sourceHeight
                    if (sourceWidth > sourceHeight) {
                        height = (width / ratio).toInt()
                    } else if (sourceHeight > sourceWidth) {
                        width = (height * ratio).toInt()
                    }
                } else if (width > sourceWidth && height > sourceHeight) {
                    // It's small, use the size they gave us.
                    width = sourceWidth
                    height = sourceHeight
                } else if (width == sourceWidth && height == sourceHeight) {
                    // no processing required
                    if (icon is BitmapDrawable) {
                        return icon.bitmap
                    }
                }
            }

            val bitmapSize = getSize(size, width, height)
            val bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888)
            val canvas = sCanvas
            canvas.setBitmap(bitmap)

            sOldBounds.set(icon.bounds)

            val left = (bitmapSize - width) / 2
            val top = (bitmapSize - height) / 2
            icon.setBounds(left, top, left + width, top + height)

            icon.colorFilter = null
            icon.draw(canvas)
            icon.bounds = sOldBounds
            return bitmap
        }
    }

    private fun getSize(size: Int, width: Int, height: Int): Int {
        return if (size == sizeMax && (width >= sIconMaxScale || height >= sIconMaxScale)) {
            sIconMaxScale
        } else if (size == sizeIcon && (width >= sIconSize || height >= sIconSize)) {
            sIconSize
        } else if (width > sIconSystem || height > sIconSystem) {
            if (width > height) width else height
        } else {
            sIconSystem
        }
    }

    fun canUseForInBitmap(candidate: Bitmap, targetOptions: BitmapFactory.Options): Boolean {
        // From Android 4.4 (KitKat) onward we can re-use if the byte size of
        // the new bitmap is smaller than the reusable bitmap candidate
        // allocation byte count.
        val width = targetOptions.outWidth / targetOptions.inSampleSize
        val height = targetOptions.outHeight / targetOptions.inSampleSize
        val byteCount = width * height * getBytesPerPixel(candidate.config)
        return byteCount <= candidate.allocationByteCount
    }

    private fun getBytesPerPixel(config: Bitmap.Config): Int {
        return when (config) {
            Bitmap.Config.ARGB_8888 -> 4
            Bitmap.Config.RGB_565 -> 2
            Bitmap.Config.ARGB_4444 -> 2
            Bitmap.Config.ALPHA_8 -> 1
            else -> 1
        }
    }

    fun flattenBitmap(bitmap: Bitmap): ByteArray? {
        // Try go guesstimate how much space the icon will take when serialized
        // to avoid unnecessary allocations/copies during the write.
        val size = bitmap.width * bitmap.height * 4
        val out = ByteArrayOutputStream(size)
        return try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            out.toByteArray()
        } catch (e: IOException) {
            Log.w("Favorite", "Could not write icon")
            null
        }

    }
}
