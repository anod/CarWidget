package com.anod.car.home

import info.anodsplace.framework.AppLog

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class CarHomeActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLog.d(" --- CarHomeActivity::onCreate ---")
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
        finish()
    }

}
