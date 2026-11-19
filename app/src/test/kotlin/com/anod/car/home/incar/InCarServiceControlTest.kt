package com.anod.car.home.incar

import android.app.Application
import android.app.ForegroundServiceStartNotAllowedException
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.anod.car.home.appwidget.ShortcutPendingIntent
import com.anod.car.home.notifications.Channels
import com.anod.car.home.notifications.InCarStartNotification
import com.anod.car.home.utils.WidgetShortcutResource
import info.anodsplace.carwidget.content.AppCoroutineScope
import info.anodsplace.carwidget.content.BroadcastServiceManager
import info.anodsplace.carwidget.content.preferences.InCarSettings
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31], application = Application::class)
class InCarServiceControlTest {
    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val appScope = AppCoroutineScope()
    private lateinit var settings: InCarSettings
    private var monitorRegistrations = 0

    @Before
    fun setUp() {
        Channels.register(context)
        stopKoin()
        settings = InCarSettings(context, appScope).apply { isInCarEnabled = true }
        startKoin {
            modules(module {
                single { settings }
                single<BroadcastServiceManager> {
                    object : BroadcastServiceManager {
                        override val isServiceRequired = true
                        override val isServiceRunning = false
                        override fun registerBroadcastService() { monitorRegistrations++ }
                        override fun startService() = Unit
                        override fun stopService() = Unit
                    }
                }
            })
        }
    }

    @After
    fun tearDown() {
        stopKoin()
        appScope.cancel()
        ModeService.sInCarMode = false
    }

    @Test
    fun activationUsesForegroundStartAndPreservesForcedStateRequest() {
        val recording = RecordingContext(context)

        ModeService.start(recording, forceState = true)

        assertEquals(ModeService::class.java.name, recording.started!!.component!!.className)
        assertTrue(recording.started!!.getBooleanExtra(ModeService.EXTRA_FORCE_STATE, false))
    }

    @Test
    fun rejectedLaunchOffersRecoveryInsteadOfThrowing() {
        val recording = RecordingContext(context).apply { rejectStart = true }

        ModeService.start(recording)

        assertEquals(1, shadowOf(context.getSystemService(NotificationManager::class.java)).size())
    }

    @Test
    fun stopDoesNotStartServiceAndClearsPendingActivationRecovery() {
        val recording = RecordingContext(context)
        InCarStartNotification.show(context, activate = true)

        ModeService.stop(recording)

        assertNull(recording.started)
        assertEquals(ModeService::class.java.name, recording.stopped!!.component!!.className)
        assertEquals(0, shadowOf(context.getSystemService(NotificationManager::class.java)).size())
    }

    @Test
    fun widgetOnUsesForegroundServiceAndOffUsesExplicitStopActivity() {
        val factory = ShortcutPendingIntent(context, WidgetShortcutResource())

        val on = shadowOf(factory.createInCar(true, 0))
        val off = shadowOf(factory.createInCar(false, 0))

        assertTrue(on.isForegroundService)
        assertTrue(off.isActivity)
        assertEquals(SwitchInCarActivity::class.java.name, off.savedIntent.component!!.className)
        assertEquals(ModeService.MODE_SWITCH_OFF, off.savedIntent.getIntExtra(ModeService.EXTRA_MODE, -1))
    }

    @Test
    fun blockedNotificationPermissionDoesNotCauseAnotherFailure() {
        val manager = context.getSystemService(NotificationManager::class.java)
        shadowOf(manager).setNotificationsEnabled(false)

        ModeService.start(RecordingContext(context).apply { rejectStart = true })

        assertTrue(shadowOf(manager).allNotifications.isEmpty())
    }

    @Test
    fun explicitOffActionStopsEvenWhenSettingsAreDisabledAndDoesNotRestartMonitoring() {
        settings.isInCarEnabled = false
        val intent = shadowOf(ModeService.createStopPendingIntent(context)).savedIntent

        Robolectric.buildActivity(SwitchInCarActivity::class.java, intent).create().destroy()

        assertEquals(ModeService::class.java.name, shadowOf(context).nextStoppedService.component!!.className)
        assertNull(shadowOf(context).nextStartedService)
        assertEquals(0, monitorRegistrations)
    }

    @Test
    fun recoveryActionAlwaysActivatesRatherThanTogglingRunningModeOff() {
        ModeService.sInCarMode = true
        InCarStartNotification.show(context, activate = true)
        val notification = shadowOf(context.getSystemService(NotificationManager::class.java)).allNotifications.single()
        val intent = shadowOf(notification.contentIntent).savedIntent

        Robolectric.buildActivity(SwitchInCarActivity::class.java, intent).create().destroy()

        val started = shadowOf(context).nextStartedService
        assertEquals(ModeService.MODE_SWITCH_ON, started.getIntExtra(ModeService.EXTRA_MODE, -1))
        assertNull(shadowOf(context).nextStoppedService)
        assertEquals(1, monitorRegistrations)
    }

    private class RecordingContext(base: Context) : ContextWrapper(base) {
        var rejectStart = false
        var started: Intent? = null
        var stopped: Intent? = null

        override fun startForegroundService(service: Intent): ComponentName? {
            if (rejectStart) {
                throw ForegroundServiceStartNotAllowedException("blocked")
            }
            started = service
            return service.component
        }

        override fun stopService(service: Intent): Boolean {
            stopped = service
            return true
        }
    }
}
