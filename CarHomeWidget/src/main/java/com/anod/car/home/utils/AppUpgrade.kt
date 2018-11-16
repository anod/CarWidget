package com.anod.car.home.utils

import android.content.Context
import android.preference.PreferenceManager
import com.anod.car.home.BuildConfig

object AppUpgrade {
    private var upgradeCode = -1

    fun isUpgraded(context: Context): Boolean {
        if (upgradeCode == -1) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            upgradeCode = sharedPreferences.getInt("UPGRADE_CODE", -1)
            sharedPreferences.edit().putInt("UPGRADE_CODE", BuildConfig.VERSION_CODE).apply()
        }

        return (upgradeCode != BuildConfig.VERSION_CODE)
    }
}