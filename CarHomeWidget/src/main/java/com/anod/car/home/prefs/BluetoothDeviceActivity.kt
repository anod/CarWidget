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
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.anod.car.home.R
import com.anod.car.home.app.CarWidgetActivity
import com.anod.car.home.incar.Bluetooth
import com.anod.car.home.incar.BluetoothClassHelper
import com.anod.car.home.incar.BroadcastService
import com.anod.car.home.prefs.model.InCarStorage
import info.anodsplace.framework.app.ApplicationContext
import kotlinx.android.synthetic.main.activity_bluetooth_device.*

/**
 * @author alex
 * @date 6/6/14
 */
class BluetoothDeviceActivity : CarWidgetActivity(), AdapterView.OnItemClickListener {
    private var bluetoothReceiver: BroadcastReceiver? = null
    private val listAdapter: DeviceAdapter by lazy { DeviceAdapter(this) }

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

        InitBluetoothDevicesTask(listAdapter, ApplicationContext(this)).execute(0)
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

    private fun onDeviceStateChange(device: Device?, newState: Boolean) {
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

    class Device(var address: String, var name: String, var btClassName: String, var selected: Boolean) {
        override fun toString(): String {
            return name
        }
    }

    private fun initSwitch() {
        val btSwitch = findViewById<Switch>(R.id.switch1)
        btSwitch.isChecked = Bluetooth.state == BluetoothAdapter.STATE_ON
        btSwitch.setOnClickListener {
            bluetoothReceiver = BluetoothStateReceiver(this)
            registerReceiver(bluetoothReceiver, INTENT_FILTER)
            if (Bluetooth.state == BluetoothAdapter.STATE_ON) {
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
                InitBluetoothDevicesTask(activity.listAdapter, ApplicationContext(activity)).execute(0)
            } else if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.ERROR) {

            }
        }

    }

    private class InitBluetoothDevicesTask(
            private val listAdapter: DeviceAdapter,
            private val context: ApplicationContext) : AsyncTask<Int, Int, List<Device>>() {

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
            val devices = InCarStorage.load(context.actual).btDevices
            val pairedList = mutableListOf<Device>()

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
                    val d = Device(device.address, device.name, btClassName, selected)

                    pairedList.add(d)
                }

            }

            if (!devices.isEmpty) {
                for (addr in devices.keys) {
                    val d = Device(addr, addr, context.getString(R.string.unavailable_bt_device), true)
                    pairedList.add(d)
                }
            }

            return pairedList
        }
    }


    private class DeviceAdapter(context: Context) : ArrayAdapter<Device>(context, R.layout.list_item_bluetooth_device, R.id.deviceName) {

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

    companion object {
        private val INTENT_FILTER = IntentFilter(
                BluetoothAdapter.ACTION_STATE_CHANGED)
    }
}
