package com.anod.car.home.incar

import android.app.Application
import android.app.ForegroundServiceStartNotAllowedException
import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.Context
import android.view.WindowManager
import androidx.test.core.app.ApplicationProvider
import com.anod.car.home.notifications.Channels
import com.anod.car.home.notifications.InCarStartNotification
import info.anodsplace.carwidget.content.AppCoroutineScope
import info.anodsplace.carwidget.content.BroadcastServiceManager
import info.anodsplace.carwidget.content.preferences.InCarSettings
import info.anodsplace.carwidget.incar.ScreenOnAlert
import info.anodsplace.carwidget.incar.ScreenOrientation
import info.anodsplace.framework.app.AlertWindow
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
import org.robolectric.android.controller.ServiceController
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31], application = Application::class)
class ModeServiceTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val appScope = AppCoroutineScope()
    private lateinit var settings: InCarSettings
    private var controller: ServiceController<ModeService>? = null
    private var handlerResolutions = 0

    @Before
    fun setUp() {
        stopKoin()
        ModeService.sInCarMode = false
        Channels.register(context)
        settings = InCarSettings(context, appScope).apply {
            isInCarEnabled = true
            isDisableScreenTimeout = false
            isPowerRequired = true
        }
        startKoin {
            modules(module {
                single { settings }
                single { context.getSystemService(AppWidgetManager::class.java) }
                single<BroadcastServiceManager> {
                    object : BroadcastServiceManager {
                        override val isServiceRequired = true
                        override val isServiceRunning = false
                        override fun registerBroadcastService() = Unit
                        override fun startService() = Unit
                        override fun stopService() = Unit
                    }
                }
                factory {
                    handlerResolutions++
                    ModeHandler(
                        context,
                        ScreenOrientation(context, context.getSystemService(WindowManager::class.java)),
                        ScreenOnAlert(context, settings, AlertWindow(context))
                    )
                }
            })
        }
        controller = Robolectric.buildService(ModeService::class.java).create()
    }

    @After
    fun tearDown() {
        destroyService()
        ModeDetector.forceState(settings, false)
        appScope.cancel()
        stopKoin()
        ModeService.sInCarMode = false
    }

    @Test
    fun rejectedPromotionStopsWithoutApplyingOrRestoringSettings() {
        val service = controller!!.get()
        shadowOf(service).setThrowInStartForeground(ForegroundServiceStartNotAllowedException("blocked"))

        val result = service.onStartCommand(
            ModeService.createStartIntent(context, ModeService.MODE_SWITCH_ON)
                .putExtra(ModeService.EXTRA_FORCE_STATE, true),
            0, 1
        )
        destroyService()

        assertEquals(Service.START_NOT_STICKY, result)
        assertTrue(shadowOf(service).isStoppedBySelf)
        assertFalse(ModeService.sInCarMode)
        assertEquals(0, handlerResolutions)
        val notification = shadowOf(context.getSystemService(NotificationManager::class.java)).allNotifications.single()
        val recoveryIntent = shadowOf(notification.contentIntent).savedIntent
        assertEquals(SwitchInCarActivity::class.java.name, recoveryIntent.component!!.className)
        assertEquals(ModeService.MODE_SWITCH_ON, recoveryIntent.getIntExtra(ModeService.EXTRA_MODE, -1))
        assertTrue(recoveryIntent.getBooleanExtra(ModeService.EXTRA_FORCE_STATE, false))
    }

    @Test
    fun rejectedAutomaticPromotionDoesNotForceRecovery() {
        val service = controller!!.get()
        shadowOf(service).setThrowInStartForeground(ForegroundServiceStartNotAllowedException("blocked"))

        service.onStartCommand(ModeService.createStartIntent(context, ModeService.MODE_SWITCH_ON), 0, 1)

        val notification = shadowOf(context.getSystemService(NotificationManager::class.java)).allNotifications.single()
        val recoveryIntent = shadowOf(notification.contentIntent).savedIntent
        assertFalse(recoveryIntent.getBooleanExtra(ModeService.EXTRA_FORCE_STATE, true))
    }

    @Test
    fun recoveredAutomaticActivationPreservesPowerStateOnStop() {
        ModeDetector.forceState(settings, true)
        InCarStartNotification.show(context, activate = true)
        val notification = shadowOf(context.getSystemService(NotificationManager::class.java)).allNotifications.single()
        val recoveryIntent = shadowOf(notification.contentIntent).savedIntent
        Robolectric.buildActivity(SwitchInCarActivity::class.java, recoveryIntent).create().destroy()
        val application = ApplicationProvider.getApplicationContext<Application>()
        val start = shadowOf(application).nextStartedService

        controller!!.get().onStartCommand(start, 0, 1)
        destroyService()

        assertFalse(ModeService.sInCarMode)
        assertTrue(ModeDetector.eventsState()[0].active)
    }

    @Test
    fun successfulActivationAndRepeatedStartPreserveForcedStateUntilStop() {
        val service = controller!!.get()
        val start = ModeService.createStartIntent(context, ModeService.MODE_SWITCH_ON)
            .putExtra(ModeService.EXTRA_FORCE_STATE, true)

        assertEquals(Service.START_NOT_STICKY, service.onStartCommand(start, 0, 1))
        assertTrue(ModeService.sInCarMode)
        assertTrue(ModeDetector.eventsState().first().active)

        service.onStartCommand(ModeService.createStartIntent(context, ModeService.MODE_SWITCH_ON), 0, 2)
        destroyService()

        assertFalse(ModeService.sInCarMode)
        assertFalse(ModeDetector.eventsState().first().active)
        assertEquals(1, handlerResolutions)
    }

    @Test
    fun legacyStopCommandDoesNotPromoteOrApplySettings() {
        val service = controller!!.get()
        shadowOf(service).setThrowInStartForeground(ForegroundServiceStartNotAllowedException("must not promote"))

        service.onStartCommand(ModeService.createStartIntent(context, ModeService.MODE_SWITCH_OFF), 0, 1)
        destroyService()

        assertTrue(shadowOf(service).isStoppedBySelf)
        assertEquals(0, handlerResolutions)
        assertTrue(shadowOf(context.getSystemService(NotificationManager::class.java)).allNotifications.isEmpty())
    }

    @Test
    fun missingDependencyContainerStopsWithoutRunningTeardownActions() {
        stopKoin()
        val service = controller!!.get()

        assertEquals(
            Service.START_NOT_STICKY,
            service.onStartCommand(ModeService.createStartIntent(context, ModeService.MODE_SWITCH_ON), 0, 1)
        )
        destroyService()

        assertTrue(shadowOf(service).isStoppedBySelf)
        assertFalse(ModeService.sInCarMode)
        assertEquals(0, handlerResolutions)
    }

    private fun destroyService() {
        controller?.destroy()
        controller = null
    }
}
