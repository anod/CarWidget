package info.anodsplace.carwidget.incar

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.compose.PreferenceCheckbox
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.permissions.AppPermission

@Composable
fun BluetoothDevicesScreen(screenState: BluetoothDevicesViewState, onEvent: (BluetoothDevicesViewEvent) -> Unit, innerPadding: PaddingValues = PaddingValues(0.dp),) {
    val screenModifier = Modifier.padding(innerPadding).padding(16.dp)
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        when (val listState = screenState.listState) {
            BluetoothDevicesListState.Initial -> {
                CircularProgressIndicator()
            }
            BluetoothDevicesListState.RequiresPermissions -> {
                BluetoothPermissions(onEvent = onEvent, modifier = screenModifier)
            }
            is BluetoothDevicesListState.Devices -> {
                BluetoothDeviceList(
                    list = listState.list,
                    btState = screenState.btAdapterState,
                    onChecked = { device, checked -> onEvent(BluetoothDevicesViewEvent.UpdateDevice(device, checked)) },
                    modifier = screenModifier)
            }
            BluetoothDevicesListState.SwitchedOff -> {
                BluetoothDeviceEmpty(
                    btState = screenState.btAdapterState,
                    modifier = screenModifier,
                )
            }
        }
    }
}

@Composable
fun BluetoothPermissions(onEvent: (BluetoothDevicesViewEvent) -> Unit, modifier: Modifier = Modifier) {
    val bluetoothPermissions = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) { results ->
        val requiresPermission = results.values.any { !it }
        onEvent(BluetoothDevicesViewEvent.PermissionResult(requires = requiresPermission))
    }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = stringResource(id = info.anodsplace.carwidget.content.R.string.bluetooth_device_category_title))
        Text(text = stringResource(id = info.anodsplace.carwidget.content.R.string.allow_bluetooth_summary))
        Button(modifier = Modifier.padding(16.dp), onClick = {
            bluetoothPermissions.launch(arrayOf(AppPermission.BluetoothScan.value, AppPermission.BluetoothConnect.value))
        }) {
            Text(text = stringResource(id = info.anodsplace.carwidget.content.R.string.allow_bluetooth))
        }
    }
}

@Composable
fun BluetoothDeviceList(list: List<BluetoothDevice>, btState: Int, onChecked: (device: BluetoothDevice, checked: Boolean) -> Unit, modifier: Modifier = Modifier) {

    if (list.isEmpty()) {
        BluetoothDeviceEmpty(btState, modifier = modifier)
    } else {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            item {
                Text(
                    modifier = Modifier.padding(vertical = 16.dp),
                    text = stringResource(id = info.anodsplace.carwidget.content.R.string.bluetooth_device_category_title)
                )
            }
            items(list.size) { index ->
                val device = list[index]
                PreferenceCheckbox(
                    checked = device.selected,
                    item = PreferenceItem.Text(
                        title = if (device.name.isEmpty()) device.address else device.name,
                        summary = device.btClassName
                    ),
                    onCheckedChange = { onChecked(device, it) }
                )
            }
        }
    }
}

@Composable
fun BluetoothDeviceEmpty(btState: Int, modifier: Modifier = Modifier) {
    val turnOnBluetooth = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        AppLog.d("Bluetooth turned on: ${result.resultCode}")
    }
    Column(modifier = modifier) {
        Text(text = stringResource(id = info.anodsplace.carwidget.content.R.string.bluetooth_device_category_title))
        Text(
            text = stringResource(id = info.anodsplace.carwidget.content.R.string.no_paired_devices_found_summary),
            modifier = Modifier.padding(top = 16.dp)
        )
        if (btState != BluetoothAdapter.STATE_ON) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = stringResource(id = info.anodsplace.carwidget.content.R.string.turn_on_bluetooth_summary),
                )
                Switch(
                    modifier = Modifier.padding(start = 16.dp),
                    checked = false,
                    onCheckedChange = {
                        turnOnBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    },
                    enabled = btState == BluetoothAdapter.STATE_OFF
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun BluetoothDeviceEmptyScreen() {
    CarWidgetTheme {
        BluetoothDevicesScreen(
            screenState = BluetoothDevicesViewState(
                listState = BluetoothDevicesListState.SwitchedOff,
                btAdapterState = BluetoothAdapter.STATE_OFF,
            ),
            onEvent = { }
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun BluetoothDevicesListScreen() {
    CarWidgetTheme {
        BluetoothDevicesScreen(
            screenState = BluetoothDevicesViewState(
                listState = BluetoothDevicesListState.Devices(listOf(
                    BluetoothDevice("banana", name = "Headphones", "headphones", selected = true)
                )),
                btAdapterState = BluetoothAdapter.STATE_ON,
            ),
            onEvent = { }
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun BluetoothDevicesListOffScreen() {
    CarWidgetTheme {
        BluetoothDevicesScreen(
            screenState = BluetoothDevicesViewState(
                listState = BluetoothDevicesListState.Devices(listOf()),
                btAdapterState = BluetoothAdapter.STATE_OFF,
            ),
            onEvent = { }
        )
    }
}