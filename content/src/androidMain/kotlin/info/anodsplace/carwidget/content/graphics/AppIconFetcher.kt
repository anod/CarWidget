package info.anodsplace.carwidget.content.graphics

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import info.anodsplace.applog.AppLog
import info.anodsplace.graphics.toByteArray
import info.anodsplace.ktx.SCHEME_APPLICATION_ICON
import okio.buffer
import okio.source

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
        } catch (_: PackageManager.NameNotFoundException) {
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
        return SourceFetchResult(
            source = ImageSource(
                source = source.inputStream().source().buffer(),
                fileSystem = options.fileSystem,
                metadata = null
            ),
            mimeType = null,
            dataSource = DataSource.DISK
        )
    }
}