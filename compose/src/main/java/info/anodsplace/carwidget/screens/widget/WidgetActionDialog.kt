package info.anodsplace.carwidget.screens.widget

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.screens.WidgetDialogEvent
import info.anodsplace.carwidget.screens.WidgetDialogType
import info.anodsplace.compose.ColorDialogContent

@Composable
fun WidgetActionDialogContent(current: WidgetDialogType, onEvent: (event: WidgetDialogEvent) -> Unit, dismiss: () -> Unit, widgetSettings: WidgetInterface.NoOp) {
    when (current) {
        WidgetDialogType.ChooseBackgroundColor -> ColorDialogContent(
            color = Color(widgetSettings.backgroundColor),
            //title = stringResource(id = R.string.color_picker_default_title),
            onColorChange = {
                onEvent(WidgetDialogEvent.UpdateBackgroundColor(it))
                dismiss()
            }
        )
        WidgetDialogType.ChooseIconsScale -> IconScaleDialogContent(widgetSettings, dismiss = dismiss, onEvent = onEvent)
        WidgetDialogType.ChooseIconsTheme -> { }
        WidgetDialogType.ChooseTileColor -> ColorDialogContent(
            color = Color(widgetSettings.tileColor),
            //title = stringResource(id = R.string.color_picker_default_title),
            onColorChange = {
                onEvent(WidgetDialogEvent.UpdateTileColor(it))
                dismiss()
            }
        )
        else -> {}
    }
}

@Composable
fun IconScaleDialogContent(prefs: WidgetInterface.NoOp, dismiss: () -> Unit = { }, onEvent: (event: WidgetDialogEvent) -> Unit = { }) {
    Surface {
        Column(
            modifier = Modifier
                .padding(all = 16.dp),
        ) {
            Text(
                text = stringResource(id = R.string.pref_scale_icon),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                textAlign = TextAlign.Center
            )
            val current = prefs.iconsScale.toInt()
            IconScale(current) { scale ->
                onEvent(WidgetDialogEvent.UpdateIconScale(scale.toString()))
                dismiss()
            }
        }
    }
}

@Composable
fun IconScale(current: Int, onClick: (Int) -> Unit) {
    FlowRow(
        mainAxisAlignment = MainAxisAlignment.Start
    ) {
        repeat((0..20).count()) { scale ->
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .clickable { onClick(scale) }
                    .size(size = 64.dp)
                    .border(
                        width = if (current == scale) 2.dp else 0.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                val scaleText = "x%.1f".format(WidgetInterface.convertIconsScale(scale = scale))
                Text(text = scaleText, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Preview
@Composable
fun IconScalePreview() {
    CarWidgetTheme {
        IconScaleDialogContent(WidgetInterface.NoOp())
    }
}