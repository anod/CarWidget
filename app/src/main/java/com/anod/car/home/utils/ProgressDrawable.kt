package com.anod.car.home.utils

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable

fun createProgressDrawable(context: Context) = CircularProgressDrawable(context).apply {
    setStyle(CircularProgressDrawable.DEFAULT)
    setColorSchemeColors(Color.CYAN)

    //bounds definition is required to show drawable correctly
    val size = (centerRadius + strokeWidth).toInt() * 2
    setBounds(0, 0, size, size)
}

fun View.startProgressAnimation() {
    if (this is TextView) {
        val progressDrawable = createProgressDrawable(context).also { drawable ->
            drawable.start()
        }
        setCompoundDrawablesWithIntrinsicBounds(progressDrawable, null, null, null)
    }
}

fun View.stopProgressAnimation() {
    if (this is TextView) {
        (this.compoundDrawables[0] as? CircularProgressDrawable)?.stop()
        setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
    }
}