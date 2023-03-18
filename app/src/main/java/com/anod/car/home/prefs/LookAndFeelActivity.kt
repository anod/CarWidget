package com.anod.car.home.prefs

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import com.anod.car.home.R
import com.anod.car.home.appwidget.Provider
import info.anodsplace.carwidget.MainComposeActivity
import info.anodsplace.carwidget.content.BroadcastServiceManager
import org.koin.core.component.get

class LookAndFeelActivity : MainComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Transparent)
        super.onCreate(savedInstanceState)
        getKoin().get<BroadcastServiceManager>().registerBroadcastService()
    }

    override fun onPause() {
        super.onPause()
        getKoin().get<BroadcastServiceManager>().registerBroadcastService()
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