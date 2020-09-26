package com.anod.car.home.prefs

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.anod.car.home.R
import com.anod.car.home.app.CarWidgetActivity
import com.anod.car.home.incar.Bluetooth
import com.anod.car.home.incar.BluetoothClassHelper
import com.anod.car.home.incar.BroadcastService
import com.anod.car.home.prefs.model.InCarStorage
import kotlinx.android.synthetic.main.activity_bluetooth_device.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.set

class BluetoothDevice(var address: String, var name: String, var btClassName: String, var selected: Boolean) {
    override fun toString(): String {
        return name
    }
}

class BluetoothDevicesViewModel(application: Application) : AndroidViewModel(application) {
    private val btAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }
    private var bluetoothReceiver: BroadcastReceiver? = null
    val devices = MutableLiveData<List<BluetoothDevice>>(emptyList())
    private val context: Context
        get() = getApplication()

    fun load() {
        viewModelScope.launch {
            val list = loadDevices()
            devices.value = list
        }
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
                val btClass = device.bluetoothClass
                var res = 0
                if (btClass != null) {
                    res = BluetoothClassHelper.getBtClassString(btClass)
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
        private val INTENT_FILTER = IntentFilter(
                BluetoothAdapter.ACTION_STATE_CHANGED)
    }
}

/**
 * @author alex
 * @date 6/6/14
 */
class BluetoothDeviceActivity : CarWidgetActivity(), AdapterView.OnItemClickListener {
    private val listAdapter: DeviceAdapter by lazy { DeviceAdapter(this) }
    private val viewModel: BluetoothDevicesViewModel by viewModels()
    private val isBroadcastServiceRequired: Boolean
        get() {
            val incar = InCarStorage.load(this)
            return BroadcastService.isServiceRequired(incar)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_device)

        deviceList.onItemClickListener = this

        deviceList.divider = ColorDrawable(Color.TRANSPARENT)
        deviceList.dividerHeight = resources.getDimensionPixelOffset(R.dimen.preference_item_margin)

        deviceList.emptyView = empty
        deviceList.adapter = listAdapter

        initSwitch()

        viewModel.devices.observe(this, Observer {
            listAdapter.clear()
            listAdapter.addAll(it)
        })
        viewModel.load()
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {

        val device = listAdapter.getItem(position)
        val checkbox = view.findViewById<CheckBox>(R.id.checkBox)
        val wasChecked = checkbox.isChecked

        if (wasChecked) {
            onDeviceStateChange(device, false)
            checkbox.isChecked = false
        } else {
            onDeviceStateChange(device, true)
            checkbox.isChecked = true
        }
    }

    private fun onDeviceStateChange(device: BluetoothDevice?, newState: Boolean) {
        val prefs = InCarStorage.load(this)
        val devices = prefs.btDevices
        if (newState) {
            devices[device!!.address] = device.address
        } else {
            devices.remove(device!!.address)
        }
        prefs.btDevices = devices
        prefs.apply()

        if (newState || isBroadcastServiceRequired) {
            BroadcastService.startService(this)
        } else {
            BroadcastService.stopService(this)
        }
    }

    private fun initSwitch() {
        val btSwitch = findViewById<Switch>(R.id.switch1)
        btSwitch.isChecked = Bluetooth.state == BluetoothAdapter.STATE_ON
        btSwitch.setOnClickListener {
            viewModel.registerBroadcastReceiver()
            if (Bluetooth.state == BluetoothAdapter.STATE_ON) {
                Bluetooth.switchOff()
                btSwitch.isChecked = false
            } else {
                Bluetooth.switchOn()
                btSwitch.isChecked = true
            }
        }
    }

    private class DeviceAdapter(context: Context) : ArrayAdapter<BluetoothDevice>(context, R.layout.list_item_bluetooth_device, R.id.deviceName) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            val d = getItem(position)
            val checkbox = view.findViewById<CheckBox>(R.id.checkBox)
            checkbox.isChecked = d!!.selected

            val summary = view.findViewById<View>(R.id.deviceType) as TextView
            summary.text = d.btClassName
            return view
        }
    }

}
