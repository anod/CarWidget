package info.anodsplace.carwidget.screens.main

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.compose.*
import info.anodsplace.carwidget.content.InCarStatus
import info.anodsplace.carwidget.content.Version
import info.anodsplace.carwidget.content.db.iconUri
import info.anodsplace.carwidget.utils.SystemIconSize

@Composable
fun Modifier.cardStyle(): Modifier = then(
    fillMaxWidth()
        .clip(shape = RoundedCornerShape(16.dp))
        .background(MaterialTheme.colors.surface)
        .padding(16.dp)
)

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
fun LargeWidgetRow(item: WidgetItem.Large, indexes: List<Int>, iconModifier: Modifier = Modifier) {
    val context = LocalContext.current
    Row {
        for (idx in indexes) {
            val shortcut = item.shortcuts.get(idx)
            if (shortcut != null) {
                PicassoIcon(shortcut.iconUri(context, item.adaptiveIconStyle), modifier = iconModifier)
            } else {
                Box(modifier = iconModifier.border(1.dp, MaterialTheme.colors.onSurface, shape = RoundedCornerShape(8.dp))) {

                }
            }
        }
    }
}

@Composable
fun LargeWidgetItem(item: WidgetItem.Large, onClick: () -> Unit, iconModifier: Modifier = Modifier) {
    Column(modifier = Modifier
        .clickable { onClick() }
        .cardStyle()) {
        LargeWidgetRow(item = item, indexes = listOf(1, 3, 5, 7), iconModifier = iconModifier)
        LargeWidgetRow(item = item, indexes = listOf(0, 2, 4, 5), iconModifier = iconModifier)
    }
}

@Composable
fun EventStates(eventsState: List<InCarStatus.EventState>) {
    val titles = listOf(
            R.string.pref_power_connected_title,
            R.string.pref_headset_connected_title,
            R.string.pref_blutooth_device_title,
            R.string.activity_recognition,
            R.string.car_dock
    )
    Column(modifier = Modifier.padding(top = 16.dp)) {
        for (event in eventsState) {
            val title = stringResource(titles[event.id])
            val enabled = if (event.enabled) stringResource(R.string.enabled) else stringResource(R.string.disabled)
            val active = if (event.active) stringResource(R.string.active) else stringResource(R.string.not_active)
            Text(
                text = String.format("%s: %s - %s", title, enabled, active),
                style = MaterialTheme.typography.overline,
                color = MaterialTheme.colors.onSurface
            )
        }
    }
}

@Composable
fun InCarHeader(screen: WidgetListScreenState) {
    val version = Version(LocalContext.current)
    val active = stringResource(screen.statusResId)

    Column(modifier = Modifier.cardStyle()) {
        Text(
            text = stringResource(id = R.string.pref_incar_mode_title) + " - " + active,
            color = MaterialTheme.colors.onSurface
        )
        if (screen.isServiceRequired) {
            Text(
                text = if (screen.isServiceRunning) "Detector service is running" else "Detector service is NOT running",
                color = MaterialTheme.colors.onSurface
            )
        }

        EventStates(screen.eventsState)

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
                        .getQuantityString(
                            R.plurals.notif_activations_left,
                            version.trialTimesLeft, version.trialTimesLeft
                        )
                Text(
                    text = stringResource(R.string.dialog_donate_title_trial) + " " + activationsLeft,
                    color = MaterialTheme.colors.onSurface
                )
            }
            else -> {
            }
        }
    }
}

@Composable
fun WidgetsListScreen(screen: WidgetListScreenState, onClick: (appWidgetId: Int) -> Unit) {
    if (screen.items.isEmpty()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            WidgetsEmptyScreen()
            Spacer(modifier = Modifier.height(16.dp))
            InCarHeader(screen)
        }
    } else {
        val iconModifier = Modifier
            .size(SystemIconSize)
            .padding(4.dp)

        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            var hasLargeItem = false

            item {
                InCarHeader(screen)
            }

            if (screen.isServiceRequired && !screen.isServiceRunning) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier
                        .cardStyle()) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            modifier = Modifier.size(SystemIconSize),
                            tint = WarningColor,
                            contentDescription = null)
                        Text(
                            modifier = Modifier.padding(start = 16.dp),
                            text = "InCar detector service is not running, please disable battery optimization",
                            color = WarningColor
                        )
                    }
                }
            }

            items(screen.items.size) { idx ->
                val item = screen.items[idx]
                Spacer(modifier = Modifier.height(16.dp))
                when (item) {
                    is WidgetItem.Shortcut -> {
                        Box(modifier = Modifier.cardStyle()) {
                            PicassoIcon(
                                uri = Uri.Builder()
                                    .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                                    .encodedAuthority(LocalContext.current.packageName)
                                    .appendEncodedPath("mipmap")
                                    .appendEncodedPath("ic_launcher")
                                    .build(),
                                modifier = iconModifier)
                        }
                    }
                    is WidgetItem.Large -> {
                        hasLargeItem = true
                        LargeWidgetItem(item, onClick = { onClick(item.appWidgetId) }, iconModifier = iconModifier)
                    }
                }
            }

            if (hasLargeItem) {
                item {
                    Text(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = stringResource(id = R.string.widgets_hint)
                    )
                }
            }
        }
    }
}

@Preview("Widgets Screen Empty dark")
@Composable
fun PreviewWidgetsScreenEmptyDark() {
    CarWidgetTheme(darkTheme = true) {
        BackgroundSurface {
            WidgetsListScreen(
                WidgetListScreenState(
                items = emptyList(),
                isServiceRunning = true,
                isServiceRequired = true,
                eventsState = emptyList(),
                statusResId = R.string.enabled
            ), onClick = { })
        }
    }
}


@Preview("Widgets Screen Light")
@Composable
fun PreviewWidgetsScreenLight() {
    CarWidgetTheme(darkTheme = false) {
        BackgroundSurface {
            WidgetsListScreen(
                WidgetListScreenState(
                    items = listOf( WidgetItem.Shortcut() ),
                    isServiceRunning = false,
                    isServiceRequired = true,
                    eventsState = emptyList(),
                    statusResId = R.string.enabled
                ),
                onClick = { }
            )
        }
    }
}
