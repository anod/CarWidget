package com.anod.car.home.model

import android.content.ContentResolver
import android.content.Context
import android.graphics.Path

import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler

import java.io.IOException

import com.squareup.picasso.Picasso.LoadedFrom.DISK
import info.anodsplace.carwidget.db.LauncherSettings
import info.anodsplace.carwidget.db.ShortcutIconLoader
import info.anodsplace.carwidget.db.ShortcutsDatabase
import info.anodsplace.framework.graphics.PathParser

/**
 * @author algavris
 * @date 23/08/2016.
 */

class ShortcutIconRequestHandler(private val context: Context) : RequestHandler() {
    private val authority: String? = LauncherSettings.Favorites.getContentUri(context.packageName).authority
    private val db: ShortcutsDatabase = ShortcutsDatabase(context)

    override fun canHandleRequest(data: Request): Boolean {
        return if (ContentResolver.SCHEME_CONTENT == data.uri.scheme) {
            data.uri.authority == authority
        } else false
    }

    @Throws(IOException::class)
    override fun load(request: Request, networkPolicy: Int): Result? {
        val shortcutId = request.uri.lastPathSegment?.toLong() ?: return null
        val shortcut = db.loadShortcut(shortcutId) ?: return null
        val adaptiveIconStyle = request.uri.getQueryParameter("adaptiveIconStyle") ?: ""
        val adaptiveIconPath = if (adaptiveIconStyle.isNotBlank()) PathParser.createPathFromPathData(adaptiveIconStyle) else Path()
        val icon = ShortcutIconLoader(db, adaptiveIconPath, context.applicationContext).load(shortcut)
        return Result(icon.bitmap, DISK)
    }

}
