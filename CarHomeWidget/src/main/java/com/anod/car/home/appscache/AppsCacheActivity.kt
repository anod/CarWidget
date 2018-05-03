package com.anod.car.home.appscache

import com.anod.car.home.app.AppsListActivity
import com.anod.car.home.model.AppsList

import android.content.Loader
import android.os.Bundle

abstract class AppsCacheActivity : AppsListActivity(), AppsCacheLoader.Callback {

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<AppsList.Entry>> {
        return AppsCacheLoader(this, this, appsList)
    }

}