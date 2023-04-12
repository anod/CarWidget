package info.anodsplace.carwidget.content.graphics

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import info.anodsplace.applog.AppLog
import info.anodsplace.graphics.toByteArray
import okio.Buffer

class AppIconFetcher(private val context: Context, private val data: Uri, private val options: Options) : Fetcher {

    class Factory(private val context: Context) : Fetcher.Factory<Uri> {
        override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
            return if (data.scheme == SCHEME_APPLICATION_ICON) AppIconFetcher(context, data, options) else null
        }
    }

    private val packageManager: PackageManager = context.packageManager

    override suspend fun fetch(): FetchResult? {
        var d: Drawable? = null

        val part = data.schemeSpecificPart
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
        val icon: Bitmap = UtilitiesBitmap.createHiResIconBitmap(d, context)
        val source = icon.toByteArray() ?: return null
        return SourceResult(
            source = ImageSource(Buffer().apply { write(source) }, options.context),
            mimeType = null,
            dataSource = DataSource.DISK
        )
    }

    companion object {
        const val SCHEME_APPLICATION_ICON = "application.icon"
    }

}