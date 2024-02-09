package info.anodsplace.carwidget.incar

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import androidx.collection.ArrayMap
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import info.anodsplace.carwidget.content.R
import info.anodsplace.carwidget.content.preferences.InCarSettings
import info.anodsplace.framework.bluetooth.BtClassType
import info.anodsplace.framework.bluetooth.classType
import info.anodsplace.ktx.broadcastReceiver
import info.anodsplace.permissions.AppPermission
import info.anodsplace.permissions.AppPermissions
import info.anodsplace.viewmodel.BaseFlowViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BluetoothDevice(var address: String, var name: String, var btClassName: String, var selected: Boolean) {
    override fun toString(): String = name
}

sealed class BluetoothDevicesListState {
    data object Initial: BluetoothDevicesListState()
    data object SwitchedOff: BluetoothDevicesListState()
    data object RequiresPermissions: BluetoothDevicesListState()
    @Immutable
    class Devices(val list: List<BluetoothDevice>): BluetoothDevicesListState()
}

@Immutable
data class BluetoothDevicesViewState(
    val btAdapterState: Int,
    val listState: BluetoothDevicesListState,
)

sealed interface BluetoothDevicesViewEvent {
    class UpdateDevice(val device: BluetoothDevice, val checked: Boolean) :
        BluetoothDevicesViewEvent
    class PermissionResult(val requires: Boolean) : BluetoothDevicesViewEvent

    data object LoadDevices : BluetoothDevicesViewEvent
}

sealed interface BluetoothDevicesViewAction

class BluetoothDevicesViewModel(
    private val bluetoothManager: BluetoothManager,
    private val settings: InCarSettings
) : BaseFlowViewModel<BluetoothDevicesViewState, BluetoothDevicesViewEvent, BluetoothDevicesViewAction>(), KoinComponent {

    class Factory(
        private val bluetoothManager: BluetoothManager,
        private val settings: InCarSettings
    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return BluetoothDevicesViewModel(bluetoothManager, settings) as T
        }
    }

    private val context: Context by inject()
    private val isBluetoothEnabled: Boolean
        get() = bluetoothManager.adapter?.isEnabled == true

    init {
        viewState = BluetoothDevicesViewState(
            btAdapterState = bluetoothManager.adapter?.state ?: BluetoothAdapter.STATE_OFF,
            listState = if (checkPermission()) {
                BluetoothDevicesListState.RequiresPermissions
            } else {
                if (isBluetoothEnabled) BluetoothDevicesListState.Initial else BluetoothDevicesListState.SwitchedOff
            }
        )
        if (viewState.listState is BluetoothDevicesListState.Initial) {
            handleEvent(BluetoothDevicesViewEvent.LoadDevices)
        }

        viewModelScope.launch {
            context.broadcastReceiver(filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
                .collect { intent ->
                    val state = intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR) ?: BluetoothAdapter.ERROR
                    viewState = viewState.copy(btAdapterState = state)
                    if (state == BluetoothAdapter.STATE_ON) {
                        handleEvent(BluetoothDevicesViewEvent.LoadDevices)
                    }
                }
        }
    }

    override fun handleEvent(event: BluetoothDevicesViewEvent) {
        when (event) {
            is BluetoothDevicesViewEvent.LoadDevices -> {
                viewModelScope.launch {
                    val list = loadDevices(settings.btDevices)
                    viewState = viewState.copy(listState = BluetoothDevicesListState.Devices(list))
                }
            }
            is BluetoothDevicesViewEvent.UpdateDevice -> updateDevice(event.device, event.checked)
            is BluetoothDevicesViewEvent.PermissionResult -> {
                if (event.requires) {
                    viewState = viewState.copy(listState = BluetoothDevicesListState.RequiresPermissions)
                } else {
                    handleEvent(BluetoothDevicesViewEvent.LoadDevices)
                }
            }
        }
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return (!AppPermissions.isGranted(context, AppPermission.BluetoothScan)
                    || !AppPermissions.isGranted(context, AppPermission.BluetoothConnect))
        }
        return false
    }

    @SuppressLint("MissingPermission")
    private suspend fun loadDevices(selectedDevices: ArrayMap<String, String>): List<BluetoothDevice> = withContext(Dispatchers.Default) {
        val btAdapter = bluetoothManager.adapter ?: return@withContext emptyList<BluetoothDevice>()

        // Get a set of currently paired devices
        val pairedDevices = btAdapter.bondedDevices
        val pairedList = mutableListOf<BluetoothDevice>()

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.isNotEmpty()) {
            for (device in pairedDevices) {
                val addr = device.address
                val selected = selectedDevices.containsKey(addr)
                if (selected) {
                    selectedDevices.remove(addr)
                }
                val res = when (device.bluetoothClass?.classType) {
                        BtClassType.COMPUTER -> info.anodsplace.carwidget.content.R.string.bluetooth_device_laptop
                        BtClassType.PHONE -> info.anodsplace.carwidget.content.R.string.bluetooth_device_cellphone
                        BtClassType.HEADPHONES -> info.anodsplace.carwidget.content.R.string.bluetooth_device_headphones
                        BtClassType.HEADSET -> info.anodsplace.carwidget.content.R.string.bluetooth_device_headset
                        null -> 0
                }
                var btClassName = ""
                if (res > 0) {
                    btClassName = context.getString(res)
                }
                if (!device.address.isNullOrEmpty()) {
                    val d = BluetoothDevice(device.address, device.name
                            ?: "Unknown", btClassName, selected)

                    pairedList.add(d)
                }
            }
        }

        if (selectedDevices.isNotEmpty()) {
            for (addr in selectedDevices.keys) {
                val d = BluetoothDevice(addr, addr, context.getString(R.string.unavailable_bt_device), true)
                pairedList.add(d)
            }
        }

        return@withContext pairedList
    }

    private fun updateDevice(device: BluetoothDevice, checked: Boolean) {
        val devices = settings.btDevices
        if (checked) {
            devices[device.address] = device.address
        } else {
            devices.remove(device.address)
        }
        settings.btDevices = devices
        handleEvent(BluetoothDevicesViewEvent.LoadDevices)
    }
}