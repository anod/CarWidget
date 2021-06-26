package info.anodsplace.carwidget.screens.incar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.compose.*
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.compose.BackgroundSurface
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.compose.PreferenceSlider
import info.anodsplace.compose.PreferenceSwitch

@Composable
fun VolumeSlider(
    initialValue: Int,
    onValueChanged: (Int) -> Unit,
    item: PreferenceItem
) {
    PreferenceSlider(
        initialValue = initialValue,
        onValueChanged = onValueChanged,
        item = item,
        startIcon = { Icon(
            modifier = it,
            imageVector = Icons.Filled.VolumeDown,
            contentDescription = null
        ) },
        endIcon = { Icon(
            modifier = it,
            imageVector = Icons.Filled.VolumeUp,
            contentDescription = null
        ) }
    )
}

@Composable
fun MediaScreen(inCar: InCarInterface, modifier: Modifier) {
    var isAdjustVolumeLevel by remember { mutableStateOf(inCar.isAdjustVolumeLevel) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
    ) {
        PreferenceSwitch(checked = isAdjustVolumeLevel, item = PreferenceItem.Switch(
            checked = isAdjustVolumeLevel,
            key = "adjust-volume-level",
            titleRes = R.string.pref_change_media_volume,
            summaryRes = R.string.pref_change_media_volume_summary
        )) {
            inCar.isAdjustVolumeLevel = it
            isAdjustVolumeLevel = it
        }
        VolumeSlider(
            initialValue = inCar.mediaVolumeLevel,
            onValueChanged = { inCar.mediaVolumeLevel = it },
            item = PreferenceItem.Text(
                key = "volume-level",
                titleRes = R.string.pref_media_volume_level,
                summaryRes = R.string.pref_volume_level_summary
            ),
        )
        VolumeSlider(
            initialValue = inCar.callVolumeLevel,
            onValueChanged = { inCar.callVolumeLevel = it },
            item = PreferenceItem.Text(
                key = "call-volume-level",
                titleRes = R.string.pref_phone_volume_level,
                summaryRes = R.string.pref_volume_level_summary
            ),
        )
    }
}

@Preview("Media screen Dark")
@Composable
fun MediaScreenDark() {
    CarWidgetTheme(darkTheme = true) {
        BackgroundSurface {
            MediaScreen(
                inCar = InCarInterface.NoOp(),
                modifier = Modifier
            )
        }
    }
}