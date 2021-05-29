package com.anod.car.home.prefs

import android.app.Activity
import com.anod.car.home.app.App

import android.content.ComponentName
import android.content.Intent
import com.anod.car.home.app.AppsListActivity
import info.anodsplace.carwidget.chooser.AppsListViewModel
import info.anodsplace.carwidget.chooser.AppsPackageLoader
import info.anodsplace.carwidget.chooser.ChooserEntry
import info.anodsplace.framework.content.forLauncher

class AllAppsActivity : AppsListActivity() {

    override fun viewModelFactory(): AppsListViewModel.Factory {
        return AppsListViewModel.Factory(App.get(applicationContext), AppsPackageLoader(this, Intent().forLauncher()))
    }

    override fun onEntryClick(position: Int, entry: ChooserEntry) {
        val intent = createActivityIntent(entry.componentName)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun createActivityIntent(componentName: ComponentName?): Intent {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.component = componentName
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        return intent
    }
}
