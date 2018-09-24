package com.anod.car.home.model

import android.content.ContentResolver
import android.content.Context

import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler

import java.io.IOException

import com.squareup.picasso.Picasso.LoadedFrom.DISK

/**
 * @author algavris
 * @date 23/08/2016.
 */

class ShortcutIconRequestHandler(context: Context) : RequestHandler() {
    private val authority: String? = LauncherSettings.Favorites.getContentUri(context.packageName).authority
    private val model: ShortcutModel = ShortcutModel(context)

    override fun canHandleRequest(data: Request): Boolean {
        return if (ContentResolver.SCHEME_CONTENT == data.uri.scheme) {
            data.uri.authority == authority
        } else false
    }

    @Throws(IOException::class)
    override fun load(request: Request, networkPolicy: Int): RequestHandler.Result? {
        val icon = model.loadShortcutIcon(request.uri)
        return RequestHandler.Result(icon!!.bitmap, DISK)
    }

}
