package com.anod.car.home.appscache

import android.content.Intent
import com.anod.car.home.app.AppsListActivity
import android.os.Bundle

abstract class AppsCacheActivity : AppsListActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loaderFactory = { context, callback -> AppsCacheLoader(context, createQueryIntent(), callback) }
    }

    abstract fun createQueryIntent(): Intent
}