package info.anodsplace.carwidget.screens.incar

import android.bluetooth.BluetoothAdapter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.isVisible
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.compose.PreferenceCheckbox
import info.anodsplace.carwidget.compose.PreferenceItem
import info.anodsplace.framework.bluetooth.Bluetooth
import info.anodsplace.framework.permissions.BluetoothConnect
import info.anodsplace.framework.permissions.BluetoothScan
import info.anodsplace.framework.permissions.RequestMultiplePermissions

@Composable
fun BluetoothDevices(viewModel: BluetoothDevicesViewModel, modifier: Modifier) {
    val state by viewModel.load().collectAsState(initial = BluetoothDevicesState.Initial)
    val screenModifier = modifier.padding(16.dp)
    when (state) {
        BluetoothDevicesState.Initial -> {
            CircularProgressIndicator()
        }
        BluetoothDevicesState.RequiresPermissions -> {
            BluetoothPermissions(viewModel, modifier = screenModifier)
        }
        is BluetoothDevicesState.Devices -> {
            BluetoothDeviceList((state as BluetoothDevicesState.Devices).list, viewModel, modifier = screenModifier)
        }
    }
}


@Composable
fun BluetoothPermissions(viewModel: BluetoothDevicesViewModel, modifier: Modifier = Modifier) {
    val bluetoothPermissions = rememberLauncherForActivityResult(contract = RequestMultiplePermissions(listOf(BluetoothScan, BluetoothConnect))) { (allGranted, _) ->
        viewModel.requiresPermission.value = allGranted
    }
    Column(modifier = modifier) {
        Text(text = stringResource(id = R.string.bluetooth_device_category_title))
        Text(text = stringResource(id = R.string.allow_bluetooth_summary))
        Button(onClick = {
            bluetoothPermissions.launch(null)
        }) {
            Text(text = stringResource(id = R.string.allow_bluetooth))
        }
    }
}

@Composable
fun BluetoothDeviceList(list: List<BluetoothDevice>, viewModel: BluetoothDevicesViewModel, modifier: Modifier = Modifier) {

    if (list.isEmpty()) {
        BluetoothDeviceEmpty(viewModel, modifier = modifier)
    } else {
        LazyColumn(modifier = modifier) {
            item {
                Text(
                    modifier = Modifier.padding(vertical = 16.dp),
                    text = stringResource(id = R.string.bluetooth_device_category_title)
                )
            }
            items(list.size) { index ->
                val device = list[index]
                PreferenceCheckbox(
                    checked = device.selected, item = PreferenceItem.Text(
                        title = if (device.name.isEmpty()) device.address else device.name,
                        summary = device.btClassName
                    )
                ) {

                }
            }
        }
    }
}

@Composable
fun BluetoothDeviceEmpty(viewModel: BluetoothDevicesViewModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = stringResource(id = R.string.bluetooth_device_category_title))

        var btSwitch by remember { mutableStateOf(Bluetooth.state == BluetoothAdapter.STATE_ON) }
        if (btSwitch) {
            Text(text = stringResource(id = R.string.no_paired_devices_found_summary))
        } else {
            Text(text = stringResource(id = R.string.turn_on_bluetooth_summary))
            Switch(checked = btSwitch, onCheckedChange = {
                viewModel.registerBroadcastReceiver()
                btSwitch = if (Bluetooth.state == BluetoothAdapter.STATE_ON) {
                    Bluetooth.switchOff()
                    false
                } else {
                    Bluetooth.switchOn()
                    true
                }
            })
        }
    }
}
