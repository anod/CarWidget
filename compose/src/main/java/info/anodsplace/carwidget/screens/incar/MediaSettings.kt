package info.anodsplace.carwidget.screens.incar

import android.app.UiModeManager
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.VolumeDecreaseIcon
import info.anodsplace.carwidget.VolumeIncreaseIcon
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.compose.PreferenceSlider

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
        startIcon = { VolumeDecreaseIcon() },
        endIcon = { VolumeIncreaseIcon() }
    )
}

@Composable
fun MediaSettings(
    screenState: InCarViewState,
    modifier: Modifier = Modifier,
    onEvent: (InCarViewEvent) -> Unit = { },
) {
    Column(
        modifier = modifier
    ) {
        VolumeSlider(
            initialValue = screenState.inCar.mediaVolumeLevel,
            onValueChanged = { onEvent(InCarViewEvent.ApplyChange("volume-level", it)) },
            item = PreferenceItem.Text(
                key = "volume-level",
                titleRes = info.anodsplace.carwidget.content.R.string.pref_media_volume_level,
                summaryRes = info.anodsplace.carwidget.content.R.string.pref_volume_level_summary
            ),
        )
        VolumeSlider(
            initialValue = screenState.inCar.callVolumeLevel,
            onValueChanged = { onEvent(InCarViewEvent.ApplyChange("call-volume-level", it)) },
            item = PreferenceItem.Text(
                key = "call-volume-level",
                titleRes = info.anodsplace.carwidget.content.R.string.pref_phone_volume_level,
                summaryRes = info.anodsplace.carwidget.content.R.string.pref_volume_level_summary
            ),
        )
    }
}

@Preview("Media screen Dark")
@Composable
fun MediaScreenDark() {
    CarWidgetTheme(uiMode = UiModeManager.MODE_NIGHT_YES) {
        Surface {
            MediaSettings(
                screenState = InCarViewState(
                    inCar = InCarInterface.NoOp(
                        mediaVolumeLevel = 100,
                        callVolumeLevel = 100
                    )
                )
            )
        }
    }
}