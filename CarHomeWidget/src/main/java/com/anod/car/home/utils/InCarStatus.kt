package com.anod.car.home.utils

import android.content.Context

import com.anod.car.home.R
import com.anod.car.home.appwidget.WidgetHelper
import com.anod.car.home.prefs.model.InCarSettings
import com.anod.car.home.prefs.model.InCarStorage

class InCarStatus(widgetsCount: Int, version: Version, settings: InCarSettings) {

    constructor(widgetsCount: Int, version: Version, context: Context)
        : this(widgetsCount, version, InCarStorage.load(context))

    constructor(context: Context) : this(
            WidgetHelper.getLargeWidgetIds(context).size,
            Version(context),
            context)

    val value = calc(widgetsCount, version, settings)
    val isEnabled = value == ENABLED

    companion object {
        const val NOT_ACTIVE = 0
        const val ENABLED = 1
        const val DISABLED = 2

        private fun calc(widgetsCount: Int, version: Version, settings: InCarSettings): Int {
            if (widgetsCount == 0) {
                return NOT_ACTIVE
            }
            return if (version.isProOrTrial) {
                if (settings.isInCarEnabled) {
                    ENABLED
                } else {
                    DISABLED
                }
            } else DISABLED
        }
    }

    val resId: Int
        get() {
            if (value == NOT_ACTIVE) {
                return R.string.not_active
            }
            return if (value == ENABLED) {
                R.string.enabled
            } else {
                R.string.disabled
            }
        }
}
