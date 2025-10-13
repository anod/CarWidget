package com.anod.car.home.utils

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import info.anodsplace.carwidget.content.PlayServicesAvailability
import info.anodsplace.carwidget.content.R

class GoogleServicesAvailability(private val context: Context): PlayServicesAvailability {
    override val availabilityMessage: String?
        get() {
            val errorCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
            if (errorCode == ConnectionResult.SUCCESS) {
                return null
            }
            if (errorCode == ConnectionResult.SERVICE_MISSING) {
                return context.getString(R.string.gms_service_missing)
            }
            if (errorCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
                return context.getString(R.string.gms_service_update_required)
            }
            if (errorCode == ConnectionResult.SERVICE_DISABLED) {
                return context.getString(R.string.gms_service_disabled)
            }
            if (errorCode == ConnectionResult.SERVICE_INVALID) {
                return context.getString(R.string.gms_service_invalid)
            }
            return GoogleApiAvailability.getInstance().getErrorString(errorCode)
        }
}