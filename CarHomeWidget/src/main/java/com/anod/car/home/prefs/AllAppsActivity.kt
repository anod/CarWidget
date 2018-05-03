package com.anod.car.home.prefs

import android.app.Activity
import com.anod.car.home.R
import com.anod.car.home.app.App
import com.anod.car.home.appscache.AppsCacheActivity
import com.anod.car.home.model.AppsList

import android.content.ComponentName
import android.content.Context
import android.content.Intent

class AllAppsActivity : AppsCacheActivity() {

    override fun onEntryClick(position: Int, entry: AppsList.Entry) {
        val intent = createActivityIntent(entry.componentName)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun createActivityIntent(className: ComponentName): Intent {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.component = className
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        return intent
    }

    override fun onIntentFilterInit(intent: Intent) {
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
    }


    override fun createAppList(context: Context): AppsList {
        return App.provide(context).appListCache
    }

}
