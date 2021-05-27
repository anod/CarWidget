package info.anodsplace.carwidget.screens

import android.view.View
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.compose.BackgroundSurface
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.carwidget.compose.WarningColor
import info.anodsplace.carwidget.content.Version
import info.anodsplace.carwidget.incar.InCarStatus

@Composable
fun WidgetsEmptyScreen() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(WarningColor)
            .padding(16.dp)
            .clip(MaterialTheme.shapes.medium)
    ) {
        Text(text = stringResource(id = R.string.no_active_widget))
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Icon(
                imageVector = Icons.Filled.Widgets,
                contentDescription = null,
                modifier = Modifier
                    .padding(4.dp)
                    .size(36.dp)
            )
        }
    }
}

@Composable
fun WidgetsScreen(widgetList: List<WidgetItem>) {
    Column (
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ){
        if (widgetList.isEmpty()) {
            WidgetsEmptyScreen()
            Spacer(modifier = Modifier.height(16.dp))
            InCarHeader(widgetList.size)
        } else {
            InCarHeader(widgetList.size)
            for (item in widgetList) {
                when (item) {
                    is WidgetItem.Shortcut -> {

                    }
                    is WidgetItem.Large -> {

                    }
                    is WidgetItem.Hint -> {

                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(id = R.string.widgets_hint))
        }
    }
}

@Composable
fun InCarHeader(widgetsCount: Int) {
    val version = Version(LocalContext.current)
    val status = InCarStatus(widgetsCount, version, LocalContext.current)
    val active = stringResource(status.resId)

    Column(modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colors.surface)
        .padding(16.dp)
        .clip(MaterialTheme.shapes.medium)
    ) {
        Text(
            text = stringResource(id = R.string.pref_incar_mode_title) + " - " + active,
            color = MaterialTheme.colors.onSurface
        )
        when {
            version.isFreeAndTrialExpired -> {
                Text(
                    text = stringResource(R.string.dialog_donate_title_expired) + " " + stringResource(R.string.notif_consider),
                    color = MaterialTheme.colors.onSurface
                )
            }
            version.isFree -> {
                val resources = LocalContext.current.resources
                val activationsLeft =
                    resources
                    .getQuantityString(R.plurals.notif_activations_left,
                        version.trialTimesLeft, version.trialTimesLeft)
                Text(
                    text = stringResource(R.string.dialog_donate_title_trial) + " " + activationsLeft,
                    color = MaterialTheme.colors.onSurface
                )
            }
            else -> { }
        }
    }
    

}

@Preview("Widgets Screen Light")
@Composable
fun PreviewWidgetsScreenLight() {
    CarWidgetTheme(darkTheme = false) {
        BackgroundSurface {
            WidgetsScreen(emptyList())
        }
    }
}
