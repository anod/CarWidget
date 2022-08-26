package info.anodsplace.carwidget.content.graphics

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.core.graphics.drawable.toDrawable
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import info.anodsplace.carwidget.content.db.LauncherSettings
import info.anodsplace.carwidget.content.db.ShortcutIconLoader
import info.anodsplace.carwidget.content.db.ShortcutsDatabase

/**
 * @author algavris
 * @date 23/08/2016.
 */

class ShortcutIconRequestHandler(
    private val db: ShortcutsDatabase,
    private val iconLoader: ShortcutIconLoader,
    private val data: Uri,
    private val options: Options
) : Fetcher {

    class Factory(
        private val context: Context,
        private val db: ShortcutsDatabase,
        private val iconLoader: ShortcutIconLoader
    ) : Fetcher.Factory<Uri> {
        private val authority: String? = LauncherSettings.Favorites.getContentUri(context.packageName).authority

        override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
            return if (data.scheme == ContentResolver.SCHEME_CONTENT && data.authority == authority) ShortcutIconRequestHandler(db, iconLoader, data, options) else null
        }
    }

    override suspend fun fetch(): FetchResult? {
        val shortcutId = data.lastPathSegment?.toLong() ?: return null
        val shortcut = db.loadShortcut(shortcutId) ?: return null
        val adaptiveIconStyle = data.getQueryParameter("adaptiveIconStyle") ?: ""
        val icon = iconLoader.load(shortcut, adaptiveIconStyle)
        return DrawableResult(
            drawable = icon.bitmap.toDrawable(options.context.resources),
            isSampled = false,
            dataSource = DataSource.DISK
        )
    }
}