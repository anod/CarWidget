package com.anod.car.home.skin.icon

import android.graphics.Color
import androidx.palette.graphics.Palette

val Palette.cardBackground: Int
    get() {
        if (mutedSwatch != null) {
            return this.mutedSwatch!!.rgb
        }
        if (darkVibrantSwatch != null) {
            return this.darkVibrantSwatch!!.rgb
        }
        return vibrantSwatch?.rgb ?: Color.DKGRAY
    }