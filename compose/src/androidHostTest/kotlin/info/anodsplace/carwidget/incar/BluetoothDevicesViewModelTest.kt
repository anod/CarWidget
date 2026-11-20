package info.anodsplace.carwidget.incar

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Looper
import androidx.lifecycle.ViewModelStore
import info.anodsplace.carwidget.content.AppCoroutineScope
import info.anodsplace.carwidget.content.preferences.InCarSettings
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowBluetoothDevice
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.fail

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31], application = Application::class)
class BluetoothDevicesViewModelTest {
    private val application = RuntimeEnvironment.getApplication()
    private val manager = application.getSystemService(BluetoothManager::class.java)
    private val appScope = AppCoroutineScope()
    private val store = ViewModelStore()
    private lateinit var settings: InCarSettings

    @Before
    fun setUp() {
        stopKoin()
        startKoin { modules(module { single<Context> { application } }) }
        settings = InCarSettings(application, appScope)
        shadowOf(application).grantPermissions(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
        shadowOf(manager.adapter).setEnabled(true)
    }

    @After
    fun tearDown() {
        store.clear()
        shadowOf(Looper.getMainLooper()).idle()
        appScope.cancel()
        stopKoin()
    }

    @Test
    fun loadsPairedDeviceDetailsWhenPermissionIsGranted() {
        val device = manager.adapter.getRemoteDevice("00:11:22:33:44:55")
        shadowOf(device).setName("Car stereo")
        shadowOf(manager.adapter).setBondedDevices(setOf(device))
        val model = createModel()

        val state = awaitLoadedState(model)

        val devices = assertIs<BluetoothDevicesListState.Devices>(state).list
        assertEquals("Car stereo", devices.single().name)
        assertEquals(device.address, devices.single().address)
    }

    @Test
    @Config(shadows = [RevokedBluetoothDeviceShadow::class])
    fun revokedPermissionDuringDevicePropertyReadShowsPermissionRequest() {
        val device = manager.adapter.getRemoteDevice("00:11:22:33:44:55")
        shadowOf(manager.adapter).setBondedDevices(setOf(device))
        val model = createModel()

        assertIs<BluetoothDevicesListState.RequiresPermissions>(awaitLoadedState(model))
    }

    @Implements(android.bluetooth.BluetoothDevice::class)
    class RevokedBluetoothDeviceShadow : ShadowBluetoothDevice() {
        @Implementation
        override fun getName(): String {
            throw SecurityException("Bluetooth permission revoked after device enumeration")
        }
    }

    @Test
    fun updatingDeviceKeepsLoadedListVisible() {
        val device = manager.adapter.getRemoteDevice("00:11:22:33:44:55")
        shadowOf(manager.adapter).setBondedDevices(setOf(device))
        val model = createModel()
        val devices = assertIs<BluetoothDevicesListState.Devices>(awaitLoadedState(model))

        model.handleEvent(BluetoothDevicesViewEvent.UpdateDevice(devices.list.single(), checked = true))

        assertIs<BluetoothDevicesListState.Devices>(model.viewState.listState)
        awaitState(model) {
            (it.listState as? BluetoothDevicesListState.Devices)?.list?.singleOrNull()?.selected == true
        }
    }

    @Test
    fun missingPermissionDoesNotMasqueradeAsEmptyDeviceList() {
        shadowOf(application).denyPermissions(Manifest.permission.BLUETOOTH_CONNECT)
        val model = createModel()

        assertIs<BluetoothDevicesListState.RequiresPermissions>(model.viewState.listState)
    }

    @Test
    fun permissionGrantRefreshesOnAdapterAndLoadsDevices() {
        shadowOf(application).denyPermissions(Manifest.permission.BLUETOOTH_CONNECT)
        val model = createModel()
        assertEquals(BluetoothAdapter.STATE_OFF, model.viewState.btAdapterState)
        shadowOf(application).grantPermissions(Manifest.permission.BLUETOOTH_CONNECT)

        model.handleEvent(BluetoothDevicesViewEvent.PermissionResult(requires = false))

        awaitState(model) { it.listState is BluetoothDevicesListState.Devices }
        assertEquals(BluetoothAdapter.STATE_ON, model.viewState.btAdapterState)
    }

    @Test
    fun permissionGrantWithAdapterOffDoesNotLoadDevices() {
        shadowOf(application).denyPermissions(Manifest.permission.BLUETOOTH_CONNECT)
        shadowOf(manager.adapter).setEnabled(false)
        val model = createModel()
        shadowOf(application).grantPermissions(Manifest.permission.BLUETOOTH_CONNECT)

        model.handleEvent(BluetoothDevicesViewEvent.PermissionResult(requires = false))

        awaitState(model) { it.listState is BluetoothDevicesListState.SwitchedOff }
        assertEquals(BluetoothAdapter.STATE_OFF, model.viewState.btAdapterState)
    }

    private fun createModel() = BluetoothDevicesViewModel(manager, settings).also { store.put("bluetooth", it) }

    private fun awaitLoadedState(model: BluetoothDevicesViewModel): BluetoothDevicesListState {
        return awaitState(model) { it.listState !is BluetoothDevicesListState.Initial }.listState
    }

    private fun awaitState(
        model: BluetoothDevicesViewModel,
        predicate: (BluetoothDevicesViewState) -> Boolean
    ): BluetoothDevicesViewState {
        val deadline = System.nanoTime() + 5_000_000_000L
        while (System.nanoTime() < deadline) {
            shadowOf(Looper.getMainLooper()).idle()
            val state = model.viewState
            if (predicate(state)) {
                return state
            }
            Thread.sleep(10)
        }
        fail("Bluetooth device loading did not complete")
    }
}
