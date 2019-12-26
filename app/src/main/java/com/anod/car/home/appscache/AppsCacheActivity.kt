package com.anod.car.home.appscache

import android.content.Intent
import android.os.Bundle
import com.anod.car.home.app.AppsListActivity

abstract class AppsCacheActivity : AppsListActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loader = AppsCacheLoader(this, createQueryIntent())
    }

    abstract fun createQueryIntent(): Intent
}