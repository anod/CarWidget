package com.anod.car.home.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

fun Drawable?.toBitmap(context: Context): Bitmap? {
    return when {
        this is BitmapDrawable -> this.bitmap
        this != null -> UtilitiesBitmap.createHiResIconBitmap(this, context)
        else -> null
    }
}