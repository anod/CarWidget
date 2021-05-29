package info.anodsplace.carwidget.screens.incar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.compose.*
import info.anodsplace.carwidget.content.preferences.InCarInterface

@Composable
fun VolumeSlider(initialVolume: Int, onValueChanged: (Int) -> Unit) {
    var volumeLevel: Float by remember { mutableStateOf(initialVolume.toFloat()) }
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = volumeLevel.toInt().toString(),
        textAlign = TextAlign.Center
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier
                .size(24.dp)
                .weight(1f),
            imageVector = Icons.Filled.VolumeDown, contentDescription = null
        )
        Slider(
            modifier = Modifier.weight(6f),
            value = volumeLevel,
            valueRange = 0f..100f,
            onValueChangeFinished = {
                onValueChanged(volumeLevel.toInt())
            },
            onValueChange = { volumeLevel = it }
        )
        Icon(
            modifier = Modifier
                .size(24.dp)
                .weight(1f),
            imageVector = Icons.Filled.VolumeUp, contentDescription = null)
    }
}

@Composable
fun MediaScreen(inCar: InCarInterface, modifier: Modifier) {
    var isAdjustVolumeLevel by remember { mutableStateOf(inCar.isAdjustVolumeLevel) }

    Column(
        modifier = modifier.verticalScroll(state = rememberScrollState())
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
        Preference(item = PreferenceItem.Text(
            key = "volume-level",
            titleRes = R.string.pref_media_volume_level,
            summaryRes = R.string.pref_volume_level_summary
        ), onClick = { }) { }
        VolumeSlider(inCar.mediaVolumeLevel) {
            inCar.mediaVolumeLevel = it
        }
        Preference(item = PreferenceItem.Text(
            key = "call-volume-level",
            titleRes = R.string.pref_phone_volume_level,
            summaryRes = R.string.pref_volume_level_summary
        ), onClick = { }) { }
        VolumeSlider(inCar.callVolumeLevel) {
            inCar.callVolumeLevel = it
        }
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