package com.anod.car.home.utils

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v4.content.res.ResourcesCompat
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log

import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

/**
 * @author algavris
 * @date 14/04/2017.
 */

class DrawableUri(private val mContext: Context) {

    class OpenResourceIdResult(val r: Resources, val id: Int)

    /**
     * Source android.widget.ImageView
     */
    fun resolve(uri: Uri): Drawable? {
        var d: Drawable? = null
        val scheme = uri.scheme
        if (ContentResolver.SCHEME_ANDROID_RESOURCE == scheme) {
            d = getDrawableByUri(uri)
        } else if (ContentResolver.SCHEME_CONTENT == scheme || ContentResolver.SCHEME_FILE == scheme) {
            try {
                val maxIconSize = UtilitiesBitmap.getIconMaxSize(mContext)
                val bmp = decodeSampledBitmapFromStream(uri, maxIconSize, maxIconSize)
                val dm = mContext.resources.displayMetrics
                bmp.density = dm.densityDpi
                d = BitmapDrawable(mContext.resources, bmp)
            } catch (e: Exception) {
                Log.w("ShortcutEditActivity", "Unable to open content: $uri", e)
            }

        } else {
            d = Drawable.createFromPath(uri.toString())
        }

        return d
    }

    private fun getDrawableByUri(uri: Uri): Drawable? {
        var d: Drawable? = null
        try {
            // Load drawable through Resources, to get the source density information
            val r = getResourceId(uri)
            d = ResourcesCompat.getDrawableForDensity(r.r, r.id, UtilitiesBitmap.getTargetDensity(mContext), null)
        } catch (e: Exception) {
            Log.w("ShortcutEditActivity", "Unable to open content: $uri", e)
        }

        return d
    }

    @Throws(FileNotFoundException::class)
    fun decodeSampledBitmapFromStream(uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap {

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        var `is` = mContext.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(`is`, null, options)
        closeStream(`is`)
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false

        `is` = mContext.contentResolver.openInputStream(uri)
        val bmp = BitmapFactory.decodeStream(`is`, null, options)
        closeStream(`is`)

        return bmp
    }

    /**
     * From android.content.ContentResolver
     */
    @Throws(FileNotFoundException::class)
    fun getResourceId(uri: Uri): OpenResourceIdResult {
        val authority = uri.authority
        val r: Resources
        if (TextUtils.isEmpty(authority)) {
            throw FileNotFoundException("No authority: $uri")
        } else {
            try {
                r = mContext.packageManager.getResourcesForApplication(authority)
            } catch (ex: PackageManager.NameNotFoundException) {
                throw FileNotFoundException("No package found for authority: $uri")
            }

        }
        val path = uri.pathSegments ?: throw FileNotFoundException("No path: $uri")
        val len = path.size
        val id: Int
        if (len == 1) {
            try {
                id = Integer.parseInt(path[0])
            } catch (e: NumberFormatException) {
                throw FileNotFoundException("Single path segment is not a resource ID: $uri")
            }

        } else if (len == 2) {
            id = r.getIdentifier(path[1], path[0], authority)
        } else {
            throw FileNotFoundException("More than two path segments: $uri")
        }
        if (id == 0) {
            throw FileNotFoundException("No resource found for: $uri")
        }
        return OpenResourceIdResult(r, id)
    }

    companion object {

        internal fun closeStream(`is`: InputStream?) {
            if (`is` != null) {
                try {
                    `is`.close()
                } catch (ignored: IOException) {
                }

            }
        }

        fun calculateInSampleSize(
                options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
            // Raw height and width of image
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {

                val halfHeight = height / 2
                val halfWidth = width / 2

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                    inSampleSize *= 2
                }
            }

            return inSampleSize
        }
    }

}
