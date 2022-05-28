package info.anodsplace.carwidget.screens.incar

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.content.preferences.InCarSettings
import info.anodsplace.framework.bluetooth.Bluetooth
import info.anodsplace.framework.bluetooth.BtClassType
import info.anodsplace.framework.bluetooth.classType
import info.anodsplace.permissions.AppPermission
import info.anodsplace.permissions.AppPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class BluetoothDevice(var address: String, var name: String, var btClassName: String, var selected: Boolean) {
    override fun toString(): String = name
}

sealed class BluetoothDevicesState {
    object Initial: BluetoothDevicesState()
    object SwitchedOff: BluetoothDevicesState()
    object RequiresPermissions: BluetoothDevicesState()
    class Devices(val list: List<BluetoothDevice>): BluetoothDevicesState()
}

class BluetoothDevicesViewModel(
    application: Application,
    private val bluetoothManager: BluetoothManager,
    private val settings: InCarSettings
) : AndroidViewModel(application) {

    class Factory(
        private val application: Application,
        private val bluetoothManager: BluetoothManager,
        private val settings: InCarSettings
    ): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return BluetoothDevicesViewModel(application, bluetoothManager, settings) as T
        }
    }

    private var bluetoothReceiver: BroadcastReceiver? = null
    private val context: Context
        get() = getApplication()

    val requiresPermission = MutableStateFlow(checkPermission())
    val btState = MutableStateFlow(Bluetooth.state)

    @SuppressLint("MissingPermission")
    fun load(): Flow<BluetoothDevicesState> = requiresPermission.combine(
            settings.observe<String?>(InCarSettings.BLUETOOTH_DEVICE_ADDRESSES).onStart { emit(null) },
            transform = { requires, _ -> requires }
    ).map { requires ->
        if (requires) {
            return@map BluetoothDevicesState.RequiresPermissions
        } else {
            if (bluetoothManager.adapter?.isEnabled == true) {
                val list = loadDevices()
                return@map BluetoothDevicesState.Devices(list)
            }
            return@map BluetoothDevicesState.SwitchedOff
        }
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return (
                    !AppPermissions.isGranted(context, AppPermission.BluetoothScan)
                    || !AppPermissions.isGranted(context, AppPermission.BluetoothConnect))
        }
        return false
    }

    override fun onCleared() {
        if (bluetoothReceiver != null) {
            context.unregisterReceiver(bluetoothReceiver)
        }
    }

    fun registerBroadcastReceiver() {
        if (bluetoothReceiver != null) {
            context.unregisterReceiver(bluetoothReceiver)
            bluetoothReceiver = null
        }
        bluetoothReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val state = intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    ?: Bluetooth.state
                btState.value = state
                if (state == BluetoothAdapter.STATE_ON) {
                    requiresPermission.value = false
                }/* else if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.ERROR) {

                }*/
            }
        }

        context.registerReceiver(bluetoothReceiver, INTENT_FILTER)
    }

    @SuppressLint("MissingPermission")
    private suspend fun loadDevices(): List<BluetoothDevice> = withContext(Dispatchers.Default) {
        val btAdapter = bluetoothManager.adapter ?: return@withContext emptyList<BluetoothDevice>()

        // Get a set of currently paired devices
        val pairedDevices = btAdapter.bondedDevices
        val devices = settings.btDevices
        val pairedList = mutableListOf<BluetoothDevice>()

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.isNotEmpty()) {
            for (device in pairedDevices) {
                val addr = device.address
                val selected = devices.containsKey(addr)
                if (selected) {
                    devices.remove(addr)
                }
                val res = when (device.bluetoothClass?.classType) {
                        BtClassType.COMPUTER -> R.string.bluetooth_device_laptop
                        BtClassType.PHONE -> R.string.bluetooth_device_cellphone
                        BtClassType.HEADPHONES -> R.string.bluetooth_device_headphones
                        BtClassType.HEADSET -> R.string.bluetooth_device_headset
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

        if (!devices.isEmpty) {
            for (addr in devices.keys) {
                val d = BluetoothDevice(addr, addr, context.getString(R.string.unavailable_bt_device), true)
                pairedList.add(d)
            }
        }

        return@withContext pairedList
    }

    fun updateDevice(device: BluetoothDevice, checked: Boolean) {
        val devices = settings.btDevices
        if (checked) {
            devices[device.address] = device.address
        } else {
            devices.remove(device.address)
        }
        settings.btDevices = devices
    }

    companion object {
        private val INTENT_FILTER = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
    }
}