package com.anod.car.home.prefs

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.util.ArrayMap
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView

import com.anod.car.home.R
import com.anod.car.home.app.CarWidgetActivity
import com.anod.car.home.incar.Bluetooth
import com.anod.car.home.incar.BluetoothClassHelper
import com.anod.car.home.incar.BroadcastService
import com.anod.car.home.prefs.model.InCarStorage

/**
 * @author alex
 * @date 6/6/14
 */
class BluetoothDeviceActivity : CarWidgetActivity(), AdapterView.OnItemClickListener {

    private var bluetoothReceiver: BroadcastReceiver? = null

    private val devicesList: ListView  by lazy { findViewById<ListView>(android.R.id.list) }

    private val listAdapter: DeviceAdapter by lazy { DeviceAdapter(this) }

    private val isBroadcastServiceRequired: Boolean
        get() {
            val incar = InCarStorage.load(this)
            return BroadcastService.isServiceRequired(incar)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_device)

        devicesList.onItemClickListener = this

        devicesList.divider = ColorDrawable(Color.TRANSPARENT)
        devicesList.dividerHeight = resources.getDimensionPixelOffset(R.dimen.preference_item_margin)

        devicesList.emptyView = findViewById(android.R.id.empty)
        devicesList.adapter = listAdapter

        initSwitch()

        InitBluetoothDevicesTask(listAdapter, this).execute(0)
    }

    public override fun onPause() {
        super.onPause()
        if (bluetoothReceiver != null) {
            unregisterReceiver(bluetoothReceiver)
        }
    }


    public override fun onResume() {
        super.onResume()
        if (bluetoothReceiver != null) {
            registerReceiver(bluetoothReceiver, INTENT_FILTER)
        }
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {

        val device = listAdapter.getItem(position)

        val checkbox = view.findViewById<View>(android.R.id.checkbox) as CheckBox

        val wasChecked = checkbox.isChecked

        if (wasChecked) {
            onDeviceStateChange(device, false)
            checkbox.isChecked = false
        } else {
            onDeviceStateChange(device, true)
            checkbox.isChecked = true
        }
    }

    private fun onDeviceStateChange(device: Device?, newState: Boolean) {
        val prefs = InCarStorage.load(this)
        var devices = prefs.btDevices
        if (newState) {
            if (devices == null) {
                devices = ArrayMap()
            }
            devices[device!!.address] = device.address
        } else {
            if (devices == null) {
                return
            }
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

    class Device(var address: String, var name: String, var btClassName: String, var selected: Boolean) {
        override fun toString(): String {
            return name
        }
    }

    private fun initSwitch() {
        val btSwitch = findViewById<View>(android.R.id.toggle) as Switch
        btSwitch.isChecked = Bluetooth.getState() == BluetoothAdapter.STATE_ON
        btSwitch.setOnClickListener {
            bluetoothReceiver = BluetoothStateReceiver(this)
            registerReceiver(bluetoothReceiver, INTENT_FILTER)
            if (Bluetooth.getState() == BluetoothAdapter.STATE_ON) {
                Bluetooth.switchOff()
                btSwitch.isChecked = false
            } else {
                Bluetooth.switchOn()
                btSwitch.isChecked = true
            }
        }

    }

    private class BluetoothStateReceiver(private val activity: BluetoothDeviceActivity) : BroadcastReceiver() {

        override fun onReceive(paramContext: Context, paramIntent: Intent) {
            val state = paramIntent
                    .getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
            if (state == BluetoothAdapter.STATE_ON) {
                InitBluetoothDevicesTask(activity.listAdapter, activity).execute(0)
            } else if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.ERROR) {

            }
        }

    }

    private class InitBluetoothDevicesTask(
            private val listAdapter: DeviceAdapter,
            private val context: Context) : AsyncTask<Int, Int, List<Device>>() {

        private val btAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }

        override fun onPreExecute() {
            listAdapter.clear()
        }

        override fun onPostExecute(pairedList: List<Device>) {
            listAdapter.clear()
            listAdapter.addAll(pairedList)
        }

        override fun doInBackground(vararg params: Int?): List<Device> {

            if (btAdapter == null) {
                return emptyList()
            }

            // Get a set of currently paired devices
            val pairedDevices = btAdapter!!.bondedDevices
            val devices = InCarStorage.load(context).btDevices
            val pairedList = mutableListOf<Device>()

            val r = context.resources

            // If there are paired devices, add each one to the ArrayAdapter
            if (!pairedDevices.isEmpty()) {
                for (device in pairedDevices) {
                    val addr = device.address
                    val selected = devices?.containsKey(addr) ?: false
                    if (selected) {
                        devices!!.remove(addr)
                    }
                    val btClass = device.bluetoothClass
                    var res = 0
                    if (btClass != null) {
                        res = BluetoothClassHelper.getBtClassString(btClass)
                    }
                    var btClassName = ""
                    if (res > 0) {
                        btClassName = r.getString(res)
                    }
                    val d = Device(device.address, device.name, btClassName, selected)

                    pairedList.add(d)
                }

            }

            if (devices != null && !devices.isEmpty) {
                for (addr in devices.keys) {
                    val d = Device(addr, addr, r.getString(R.string.unavailable_bt_device), true)
                    pairedList.add(d)
                }
            }

            return pairedList
        }
    }


    private class DeviceAdapter(context: Context) : ArrayAdapter<Device>(context, R.layout.bluetooth_device_item, android.R.id.title) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            val d = getItem(position)
            val checkbox = view.findViewById<View>(android.R.id.checkbox) as CheckBox
            checkbox.isChecked = d!!.selected

            val summary = view.findViewById<View>(android.R.id.summary) as TextView
            summary.text = d.btClassName
            return view
        }
    }

    companion object {
        private val INTENT_FILTER = IntentFilter(
                BluetoothAdapter.ACTION_STATE_CHANGED)
    }
}
