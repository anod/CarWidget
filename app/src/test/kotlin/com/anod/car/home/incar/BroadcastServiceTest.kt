package com.anod.car.home.incar

import android.app.Application
import android.app.ForegroundServiceStartNotAllowedException
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.anod.car.home.MainActivity
import com.anod.car.home.notifications.Channels
import info.anodsplace.carwidget.content.AppCoroutineScope
import info.anodsplace.carwidget.content.preferences.InCarSettings
import kotlinx.coroutines.cancel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31], application = Application::class)
class BroadcastServiceTest {
    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun rejectedPromotionOffersMonitoringRecoveryAndStops() {
        Channels.register(context)
        val controller = Robolectric.buildService(BroadcastService::class.java).create()
        val service = controller.get()
        shadowOf(service).setThrowInStartForeground(ForegroundServiceStartNotAllowedException("blocked"))

        val result = service.onStartCommand(Intent(), 0, 1)

        assertEquals(Service.START_NOT_STICKY, result)
        assertTrue(shadowOf(service).isStoppedBySelf)
        val notification = shadowOf(context.getSystemService(NotificationManager::class.java)).allNotifications.single()
        assertEquals(MainActivity::class.java.name, shadowOf(notification.contentIntent).savedIntent.component!!.className)
        controller.destroy()
    }

    @Test
    fun incomingBroadcastDoesNotStartUnneededMonitoringService() {
        val scope = AppCoroutineScope()
        try {
            val settings = InCarSettings(context, scope).apply { isInCarEnabled = false }

            BroadcastService.Manager(context, settings).startService()

            assertNull(shadowOf(context).nextStartedService)
        } finally {
            scope.cancel()
        }
    }
}
