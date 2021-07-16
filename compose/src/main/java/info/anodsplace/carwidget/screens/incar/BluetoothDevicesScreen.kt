package info.anodsplace.carwidget.screens.incar

import android.bluetooth.BluetoothAdapter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.R
import info.anodsplace.compose.BackgroundSurface
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.compose.PreferenceCheckbox
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.framework.bluetooth.Bluetooth
import info.anodsplace.framework.permissions.BluetoothConnect
import info.anodsplace.framework.permissions.BluetoothScan
import info.anodsplace.framework.permissions.RequestMultiplePermissions

private fun btSwitchRequest(newState: Boolean, viewModel: BluetoothDevicesViewModel) {
    if (newState) {
        viewModel.registerBroadcastReceiver()
        Bluetooth.switchOn()
    } else {
        Bluetooth.switchOff()
    }
}

@Composable
fun BluetoothDevicesScreen(viewModel: BluetoothDevicesViewModel, modifier: Modifier) {
    val screenState by viewModel.load().collectAsState(initial = BluetoothDevicesState.Initial)
    val btState by viewModel.btState.collectAsState(initial = Bluetooth.state)
    val screenModifier = modifier.padding(16.dp)
    BackgroundSurface(modifier = modifier.fillMaxSize()) {
        when (screenState) {
            BluetoothDevicesState.Initial -> {
                CircularProgressIndicator()
            }
            BluetoothDevicesState.RequiresPermissions -> {
                BluetoothPermissions(viewModel, modifier = screenModifier)
            }
            is BluetoothDevicesState.Devices -> {
                BluetoothDeviceList(
                    list = (screenState as BluetoothDevicesState.Devices).list,
                    btState = btState,
                    onSwitchRequest = { btSwitchRequest(it, viewModel) },
                    modifier = screenModifier)
            }
            BluetoothDevicesState.SwitchedOff -> {
                BluetoothDeviceEmpty(
                    btState = btState,
                    modifier = screenModifier,
                    onSwitchRequest = { btSwitchRequest(it, viewModel) }
                )
            }
        }
    }
}

@Composable
fun BluetoothPermissions(viewModel: BluetoothDevicesViewModel, modifier: Modifier = Modifier) {
    val bluetoothPermissions = rememberLauncherForActivityResult(contract = RequestMultiplePermissions(listOf(BluetoothScan, BluetoothConnect))) { (allGranted, _) ->
        viewModel.requiresPermission.value = allGranted
    }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = stringResource(id = R.string.bluetooth_device_category_title))
        Text(text = stringResource(id = R.string.allow_bluetooth_summary))
        Button(modifier = Modifier.padding(16.dp), onClick = {
            bluetoothPermissions.launch(null)
        }) {
            Text(text = stringResource(id = R.string.allow_bluetooth))
        }
    }
}

@Composable
fun BluetoothDeviceList(list: List<BluetoothDevice>, btState: Int, onSwitchRequest: (newState: Boolean) -> Unit, modifier: Modifier = Modifier) {

    if (list.isEmpty()) {
        BluetoothDeviceEmpty(btState, onSwitchRequest, modifier = modifier)
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
fun BluetoothDeviceEmpty(btState: Int, onSwitchRequest: (newState: Boolean) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = stringResource(id = R.string.bluetooth_device_category_title))
        Text(
            text = stringResource(id = R.string.no_paired_devices_found_summary),
            modifier = Modifier.padding(top = 16.dp)
        )
        if (btState != BluetoothAdapter.STATE_ON) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.turn_on_bluetooth_summary),
                )
                Switch(
                    modifier = Modifier.padding(start = 16.dp),
                    checked = btState == BluetoothAdapter.STATE_ON,
                    onCheckedChange = { onSwitchRequest(it) },
                    enabled = btState == BluetoothAdapter.STATE_OFF
                )
            }
        }
    }
}

@Preview("BluetoothDeviceEmptyScreen")
@Composable
fun BluetoothDeviceEmptyScreen() {
    CarWidgetTheme(darkTheme = true) {
        BackgroundSurface {
            BluetoothDeviceEmpty(
                btState = BluetoothAdapter.STATE_OFF,
                onSwitchRequest = { },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}