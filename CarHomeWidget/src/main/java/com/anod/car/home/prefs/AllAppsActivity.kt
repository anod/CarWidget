package com.anod.car.home.prefs

import android.app.Activity
import com.anod.car.home.app.App
import com.anod.car.home.appscache.AppsCacheActivity
import com.anod.car.home.model.AppsList

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle

class AllAppsActivity : AppsCacheActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.appsList = App.provide(this).appListCache
    }

    override fun onEntryClick(position: Int, entry: AppsList.Entry) {
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

    override fun createQueryIntent(): Intent {
        val intent = Intent()
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        return intent
    }

}
