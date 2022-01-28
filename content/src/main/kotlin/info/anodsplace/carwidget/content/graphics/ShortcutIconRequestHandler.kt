package info.anodsplace.carwidget.content.graphics

import android.content.ContentResolver
import android.content.Context
import android.graphics.Path

import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler

import java.io.IOException

import com.squareup.picasso.Picasso.LoadedFrom.DISK
import info.anodsplace.carwidget.content.db.LauncherSettings
import info.anodsplace.carwidget.content.db.ShortcutIconLoader
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.graphics.PathParser
import kotlinx.coroutines.runBlocking

/**
 * @author algavris
 * @date 23/08/2016.
 */

class ShortcutIconRequestHandler(context: Context, private val db: ShortcutsDatabase, private val iconLoader: ShortcutIconLoader) : RequestHandler() {
    private val authority: String? = LauncherSettings.Favorites.getContentUri(context.packageName).authority

    override fun canHandleRequest(data: Request): Boolean {
        return if (ContentResolver.SCHEME_CONTENT == data.uri.scheme) {
            data.uri.authority == authority
        } else false
    }

    @Throws(IOException::class)
    override fun load(request: Request, networkPolicy: Int): Result? = runBlocking {
        val shortcutId = request.uri.lastPathSegment?.toLong() ?: return@runBlocking null
        val shortcut = db.loadShortcut(shortcutId) ?: return@runBlocking null
        val adaptiveIconStyle = request.uri.getQueryParameter("adaptiveIconStyle") ?: ""
        val adaptiveIconPath = if (adaptiveIconStyle.isNotBlank()) PathParser.createPathFromPathData(adaptiveIconStyle) else Path()
        val icon = iconLoader.load(shortcut, adaptiveIconPath)
        return@runBlocking Result(icon.bitmap, DISK)
    }

}
