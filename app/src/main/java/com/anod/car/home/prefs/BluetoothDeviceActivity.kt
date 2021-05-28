package com.anod.car.home.prefs

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.anod.car.home.R
import com.anod.car.home.app.CarWidgetActivity
import com.anod.car.home.databinding.ActivityBluetoothDeviceBinding
import info.anodsplace.framework.bluetooth.Bluetooth
import com.anod.car.home.incar.BroadcastService
import info.anodsplace.carwidget.content.preferences.InCarStorage
import info.anodsplace.carwidget.screens.incar.BluetoothDevice
import info.anodsplace.carwidget.screens.incar.BluetoothDevicesState
import info.anodsplace.carwidget.screens.incar.BluetoothDevicesViewModel
import info.anodsplace.framework.permissions.AppPermissions
import info.anodsplace.framework.permissions.BluetoothConnect
import info.anodsplace.framework.permissions.BluetoothScan
import kotlinx.coroutines.flow.collect

/**
 * @author alex
 * @date 6/6/14
 */
class BluetoothDeviceActivity : CarWidgetActivity(), AdapterView.OnItemClickListener {
    private lateinit var bluetoothPermissions: ActivityResultLauncher<Void>
    private lateinit var binding: ActivityBluetoothDeviceBinding
    private val listAdapter: DeviceAdapter by lazy { DeviceAdapter(this) }
    private val viewModel: BluetoothDevicesViewModel by viewModels()
    private val isBroadcastServiceRequired: Boolean
        get() {
            val incar = InCarStorage.load(this)
            return BroadcastService.isServiceRequired(incar)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bluetoothPermissions = AppPermissions.register(this, listOf(BluetoothScan, BluetoothConnect)) { (allGranted, _) ->
            if (allGranted) {
                initSwitch()
                viewModel.load()
            }
        }

        binding.deviceList.onItemClickListener = this

        binding.deviceList.divider = ColorDrawable(Color.TRANSPARENT)
        binding.deviceList.dividerHeight = resources.getDimensionPixelOffset(R.dimen.preference_item_margin)

        binding.deviceList.emptyView = binding.empty
        binding.deviceList.adapter = listAdapter

        lifecycleScope.launchWhenResumed {
            viewModel.load().collect { state ->
                when (state) {
                    BluetoothDevicesState.RequiresPermissions -> {
                        binding.requestPermission.setOnClickListener {
                            bluetoothPermissions.launch(null)
                        }
                        binding.emptyText.setText(R.string.allow_bluetooth_summary)
                        binding.requestPermission.isVisible = true
                        binding.btSwitch.isVisible = false
                    }
                    is BluetoothDevicesState.Devices -> {
                        initSwitch()
                        listAdapter.clear()
                        listAdapter.addAll(state.list)
                    }
                    else -> { }
                }
            }
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
        binding.emptyText.setText(R.string.turn_on_bluetooth_summary)
        binding.requestPermission.isVisible = false
        binding.btSwitch.isVisible = true
        binding.btSwitch.isChecked = Bluetooth.state == BluetoothAdapter.STATE_ON
        binding.btSwitch.setOnClickListener {
            viewModel.registerBroadcastReceiver()
            if (Bluetooth.state == BluetoothAdapter.STATE_ON) {
                Bluetooth.switchOff()
                binding.btSwitch.isChecked = false
            } else {
                Bluetooth.switchOn()
                binding.btSwitch.isChecked = true
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
