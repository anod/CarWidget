package com.anod.car.home.incar

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class ChangeBrightnessActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bt = intent.getFloatExtra(EXTRA_BRIGHT_LEVEL, 1.0f)

        val lp = window.attributes
        lp.screenBrightness = bt
        window.attributes = lp
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({ this@ChangeBrightnessActivity.finish() }, 500)
    }

    companion object {
        internal const val EXTRA_BRIGHT_LEVEL = "bright_level"
    }
}
