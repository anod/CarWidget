package info.anodsplace.carwidget.content.graphics

import android.content.ContentResolver
import coil3.ImageLoader
import coil3.Uri
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.pathSegments
import coil3.request.Options
import coil3.toAndroidUri
import info.anodsplace.carwidget.content.BuildProperties
import info.anodsplace.carwidget.content.SkinProperties
import info.anodsplace.carwidget.content.db.LauncherSettings
import info.anodsplace.carwidget.content.db.ShortcutIcon
import info.anodsplace.carwidget.content.db.ShortcutIconLoader
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.graphics.toByteArray
import okio.buffer
import okio.source

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
        private val db: ShortcutsDatabase,
        private val iconLoader: ShortcutIconLoader,
        private val skinPropertiesFactory: SkinProperties.Factory,
        private val buildProps: BuildProperties
    ) : Fetcher.Factory<Uri> {

        override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
            return if (data.scheme == ContentResolver.SCHEME_CONTENT && data.authority == LauncherSettings.Favorites.authority(buildProps.isDebug))
                ShortcutIconRequestHandler(db, iconLoader, skinPropertiesFactory, data, options)
            else null
        }
    }

    override suspend fun fetch(): FetchResult? {
        val shortcutId = data.pathSegments.lastOrNull()?.toLong() ?: return null
        val shortcut = db.loadShortcut(shortcutId) ?: return null
        val androidUri = data.toAndroidUri()
        val adaptiveIconStyle = androidUri.getQueryParameter("adaptiveIconStyle") ?: ""
        val icon = iconLoader.load(shortcut, adaptiveIconStyle)
        val iconTintColor = getIconTint(icon = icon, skinName = androidUri.getQueryParameter("skin"))

        var bitmap = icon.bitmap
        if (iconTintColor != null) {
            val transform = BitmapTransform(options.context).apply {
                applyGrayFilter = true
                tintColor = iconTintColor
            }
            bitmap = transform.transform(icon.bitmap)
        }

        val source = bitmap.toByteArray() ?: return null
        return SourceFetchResult(
            source = ImageSource(
                // Not resolved in AGP 9.0.0 inputStream() ,source()
                source = source.inputStream().source().buffer(),
                fileSystem = options.fileSystem,
                metadata = null
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