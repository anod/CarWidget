package info.anodsplace.carwidget.content.extentions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import info.anodsplace.carwidget.content.graphics.UtilitiesBitmap

fun Drawable?.toBitmap(context: Context): Bitmap? {
    return when {
        this is BitmapDrawable -> this.bitmap
        this != null -> UtilitiesBitmap.createHiResIconBitmap(this, context)
        else -> null
    }
}