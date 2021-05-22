package com.anod.car.home.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import info.anodsplace.carwidget.utils.UtilitiesBitmap

fun Drawable?.toBitmap(context: Context): Bitmap? {
    return when {
        this is BitmapDrawable -> this.bitmap
        this != null -> UtilitiesBitmap.createHiResIconBitmap(this, context)
        else -> null
    }
}