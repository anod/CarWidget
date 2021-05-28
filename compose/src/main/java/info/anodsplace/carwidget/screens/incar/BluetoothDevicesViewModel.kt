package info.anodsplace.carwidget.screens.incar

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.AndroidViewModel
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.content.preferences.InCarStorage
import info.anodsplace.framework.bluetooth.BtClassType
import info.anodsplace.framework.bluetooth.classType
import info.anodsplace.framework.permissions.AppPermissions
import info.anodsplace.framework.permissions.BluetoothConnect
import info.anodsplace.framework.permissions.BluetoothScan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class BluetoothDevice(var address: String, var name: String, var btClassName: String, var selected: Boolean) {
    override fun toString(): String = name
}

sealed class BluetoothDevicesState {
    object Initial: BluetoothDevicesState()
    object RequiresPermissions: BluetoothDevicesState()
    class Devices(val list: List<BluetoothDevice>): BluetoothDevicesState()
}

class BluetoothDevicesViewModel(application: Application) : AndroidViewModel(application) {
    private val btAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }
    private var bluetoothReceiver: BroadcastReceiver? = null
    private val context: Context
        get() = getApplication()

    val requiresPermission = MutableStateFlow(checkPermission())

    fun load(): Flow<BluetoothDevicesState> = requiresPermission.map { requires ->
        if (requires) {
            return@map BluetoothDevicesState.RequiresPermissions
        } else {
            val list = loadDevices()
            return@map BluetoothDevicesState.Devices(list)
        }
    }

    private fun checkPermission(): Boolean {
        return (!AppPermissions.isGranted(context, BluetoothScan) || !AppPermissions.isGranted(context, BluetoothConnect))
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
                if (state == BluetoothAdapter.STATE_ON) {
                    load()
                }/* else if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.ERROR) {

                }*/
            }
        }

        if (bluetoothReceiver != null) {
            context.registerReceiver(bluetoothReceiver, INTENT_FILTER)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun loadDevices(): List<BluetoothDevice> = withContext(Dispatchers.Default) {

        if (btAdapter == null) {
            return@withContext emptyList<BluetoothDevice>()
        }

        // Get a set of currently paired devices
        val pairedDevices = btAdapter!!.bondedDevices
        val devices = InCarStorage.load(context).btDevices
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

    companion object {
        private val INTENT_FILTER = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
    }
}