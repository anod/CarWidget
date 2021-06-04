package com.anod.car.home

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import com.anod.car.home.incar.BroadcastService
import com.anod.car.home.prefs.LookAndFeelActivity
import info.anodsplace.carwidget.MainComposeActivity

/**
 * @author alex
 * @date 5/22/13
 */
class MainActivity : MainComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        BroadcastService.registerBroadcastService(applicationContext)
    }

    override fun onPause() {
        super.onPause()
        BroadcastService.registerBroadcastService(applicationContext)
    }

    override fun startConfigActivity(appWidgetId: Int) {
        val configIntent = Intent(this, LookAndFeelActivity::class.java)
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        startActivity(configIntent)
    }
}