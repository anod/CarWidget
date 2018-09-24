package com.anod.car.home.skin.icon

import android.graphics.Bitmap

class BBBIconProcessor : IconProcessor {

    override fun process(icon: Bitmap): Bitmap {
        val x = (icon.width * sizeDiff).toInt()
        val w = icon.width - x
        val y = (icon.height * sizeDiff).toInt()
        val h = icon.height - y

        return Bitmap.createBitmap(icon, x, 0, w, h)
    }

    override val sizeDiff = .3f
    override val id = "bbb"

}
