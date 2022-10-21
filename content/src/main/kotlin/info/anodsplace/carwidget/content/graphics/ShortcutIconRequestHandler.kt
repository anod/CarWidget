package info.anodsplace.carwidget.content.graphics

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import info.anodsplace.carwidget.content.SkinProperties
import info.anodsplace.carwidget.content.db.LauncherSettings
import info.anodsplace.carwidget.content.db.ShortcutIcon
import info.anodsplace.carwidget.content.db.ShortcutIconLoader
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.graphics.toByteArray
import okio.Buffer

/**
 * @author algavris
 * @date 23/08/2016.
 */
class ShortcutIconRequestHandler(
    private val db: ShortcutsDatabase,
    private val iconLoader: ShortcutIconLoader,
    private val skinPropertiesFactory: SkinProperties.Factory,
    private val data: Uri,
    private val options: Options
) : Fetcher {

    class Factory(
        context: Context,
        private val db: ShortcutsDatabase,
        private val iconLoader: ShortcutIconLoader,
        private val skinPropertiesFactory: SkinProperties.Factory,
    ) : Fetcher.Factory<Uri> {
        private val authority: String? = LauncherSettings.Favorites.getContentUri(context.packageName).authority

        override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
            return if (data.scheme == ContentResolver.SCHEME_CONTENT && data.authority == authority)
                ShortcutIconRequestHandler(db, iconLoader, skinPropertiesFactory, data, options)
            else null
        }
    }

    override suspend fun fetch(): FetchResult? {
        val shortcutId = data.lastPathSegment?.toLong() ?: return null
        val shortcut = db.loadShortcut(shortcutId) ?: return null
        val adaptiveIconStyle = data.getQueryParameter("adaptiveIconStyle") ?: ""
        val icon = iconLoader.load(shortcut, adaptiveIconStyle)
        val iconTintColor = getIconTint(icon = icon, skinName = data.getQueryParameter("skin"))

        var bitmap = icon.bitmap
        if (iconTintColor != null) {
            val transform = BitmapTransform(options.context).apply {
                applyGrayFilter = true
                tintColor = iconTintColor
            }
            bitmap = transform.transform(icon.bitmap)
        }

        val source = bitmap.toByteArray() ?: return null
        return SourceResult(
            source = ImageSource(
                source = Buffer().apply { write(source) },
                context = options.context
            ),
            mimeType = null,
            dataSource = DataSource.DISK
        )

    }

    private fun getIconTint(icon: ShortcutIcon, skinName: String?): Int? {
        val skin = skinName ?: return null
        if (skin.isEmpty()) return null

        val colorRes = skinPropertiesFactory.create(skinName = skin).iconResourceTint(icon.resource)
        if (colorRes == 0) return null

        return options.context.resources.getColorStateList(colorRes, options.context.theme).defaultColor
    }
}