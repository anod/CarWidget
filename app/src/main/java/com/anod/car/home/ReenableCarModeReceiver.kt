package com.anod.car.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ReenableCarModeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("CarHomeWidget", "onReceive car mode re-enable: $intent")
    }

}