package com.anod.car.home.prefs

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import com.anod.car.home.R
import com.anod.car.home.appwidget.Provider
import com.anod.car.home.incar.BroadcastService
import info.anodsplace.carwidget.MainComposeActivity
import org.koin.core.component.get

class LookAndFeelActivity : MainComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Transparent)
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

    override fun requestWidgetUpdate(appWidgetId: Int) {
        Provider.requestUpdate(this, intArrayOf(appWidgetId), appWidgetManager = get())
    }
}