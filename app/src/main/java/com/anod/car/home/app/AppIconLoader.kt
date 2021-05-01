package com.anod.car.home.app

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable

import info.anodsplace.framework.AppLog
import com.anod.car.home.utils.UtilitiesBitmap
import com.squareup.picasso.Picasso
import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler

import java.io.IOException

import com.squareup.picasso.Picasso.LoadedFrom.DISK


class AppIconLoader(context: Context) {
    private val context: Context = context.applicationContext
    private var picasso: Picasso? = null

    internal class PackageIconRequestHandler(private val context: Context) : RequestHandler() {
        private val packageManager: PackageManager = context.packageManager

        override fun canHandleRequest(data: Request): Boolean {
            return SCHEME == data.uri.scheme
        }

        @Throws(IOException::class)
        override fun load(request: Request, networkPolicy: Int): Result? {
            var d: Drawable? = null

            val part = request.uri.schemeSpecificPart
            AppLog.d("Get Activity Info: $part")
            val cmp = ComponentName.unflattenFromString(part)
            try {
                if (cmp != null) {
                    d = packageManager.getActivityIcon(cmp)
                }
            } catch (ignored: PackageManager.NameNotFoundException) {
            }

            if (d == null) {
                try {
                    d = packageManager.getApplicationIcon(cmp!!.packageName)
                } catch (e1: PackageManager.NameNotFoundException) {
                    AppLog.e(e1)
                    return null
                }

            }
            val icon: Bitmap = UtilitiesBitmap.createSystemIconBitmap(d, context)
            return Result(icon, DISK)
        }

    }

    fun picasso(): Picasso {
        if (picasso == null) {
            picasso = Picasso.Builder(context)
                    .addRequestHandler(PackageIconRequestHandler(context))
                    .build()
        }
        return picasso!!
    }

    fun shutdown() {
        if (picasso != null) {
            picasso!!.shutdown()
            picasso = null
        }
    }

    companion object {
        const val SCHEME = "application.icon"
    }
}
