package info.anodsplace.carwidget.content.graphics

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable

import info.anodsplace.applog.AppLog
import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler

import java.io.IOException

import com.squareup.picasso.Picasso.LoadedFrom.DISK

class PackageIconRequestHandler(private val context: Context) : RequestHandler() {
    private val packageManager: PackageManager = context.packageManager

    override fun canHandleRequest(data: Request): Boolean {
        return SCHEME_APPLICATION_ICON == data.uri.scheme
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


    companion object {
        const val SCHEME_APPLICATION_ICON = "application.icon"
    }
}
