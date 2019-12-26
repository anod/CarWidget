package com.anod.car.home.skin.icon

import android.graphics.Bitmap

interface IconProcessor {
    val id: String
    val sizeDiff: Float
    fun process(icon: Bitmap): Bitmap
}
