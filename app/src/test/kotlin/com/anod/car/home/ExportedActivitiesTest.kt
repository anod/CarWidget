// Copyright (c) CarWidget contributors. Licensed under the project license.
package com.anod.car.home

import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Security regression guard: activities that are only launched via internal PendingIntents must
 * stay android:exported="false" so other apps cannot start them. SwitchInCarActivity is the one
 * exception in debug builds, where a manifest overlay re-exports it for the adb switch script.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31], application = android.app.Application::class)
class ExportedActivitiesTest {

    private fun exportedByActivityName(): Map<String, Boolean> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val info = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_ACTIVITIES
        )
        return info.activities.orEmpty().associate { it.name to it.exported }
    }

    @Test
    fun pendingIntentOnlyActivitiesAreNotExported() {
        val exported = exportedByActivityName()
        assertEquals(false, exported["com.anod.car.home.OverlayActivity"])
        assertEquals(false, exported["com.anod.car.home.ShortcutActivity"])
    }

    @Test
    fun switchInCarActivityExportedOnlyInDebugBuilds() {
        val exported = exportedByActivityName()
        // Debug re-exports it for the adb switch script; release keeps it exported=false.
        assertEquals(BuildConfig.DEBUG, exported["com.anod.car.home.incar.SwitchInCarActivity"])
    }
}
