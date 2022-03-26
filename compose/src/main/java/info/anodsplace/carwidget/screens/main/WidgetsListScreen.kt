package info.anodsplace.carwidget.screens.main

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.WarningColor
import info.anodsplace.carwidget.content.InCarStatus
import info.anodsplace.carwidget.content.Version
import info.anodsplace.carwidget.content.db.iconUri
import info.anodsplace.carwidget.content.graphics.imageLoader
import info.anodsplace.carwidget.content.iconUri
import info.anodsplace.carwidget.utils.SystemIconSize
import info.anodsplace.compose.BackgroundSurface

@Composable
fun Modifier.cardStyle(): Modifier = then(
    fillMaxWidth()
        .clip(shape = RoundedCornerShape(16.dp))
        .background(MaterialTheme.colors.secondary)
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
            val shortcut = item.shortcuts.getOrNull(idx)
            if (shortcut != null) {
                AsyncImage(
                    model = shortcut.iconUri(context, item.adaptiveIconStyle),
                    contentDescription = shortcut.title.toString(),
                    imageLoader = context.imageLoader,
                    modifier = iconModifier.border(1.dp, MaterialTheme.colors.onSurface, shape = RoundedCornerShape(8.dp))
                )
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
        LargeWidgetRow(item = item, indexes = listOf(0, 2, 4, 6), iconModifier = iconModifier)
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
                color = MaterialTheme.colors.onSecondary
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
            color = MaterialTheme.colors.onSecondary
        )
        if (screen.isServiceRequired) {
            Text(
                text = if (screen.isServiceRunning) "Detector service is running" else "Detector service is NOT running",
                color = MaterialTheme.colors.onSecondary
            )
        }

        EventStates(screen.eventsState)

        when {
            version.isFreeAndTrialExpired -> {
                Text(
                    text = stringResource(R.string.dialog_donate_title_expired) + " " + stringResource(R.string.notif_consider),
                    color = MaterialTheme.colors.onSecondary
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
                    color = MaterialTheme.colors.onSecondary
                )
            }
            else -> {
            }
        }
    }
}

@Composable
fun WidgetsListScreen(screen: WidgetListScreenState, onClick: (appWidgetId: Int) -> Unit, modifier: Modifier = Modifier) {
    if (screen.items.isEmpty()) {
        Column(
            modifier = modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            WidgetsEmptyScreen()
            Spacer(modifier = Modifier.height(16.dp))
            InCarHeader(screen)
        }
    } else {
        val imageLoader = LocalContext.current.imageLoader
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
                            text = "InCar detector service is not running, disable battery optimization",
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
                            AsyncImage(
                                model = LocalContext.current.iconUri("mipmap", "ic_launcher"),
                                contentDescription = "",
                                imageLoader = imageLoader,
                                modifier = iconModifier.border(1.dp, MaterialTheme.colors.onSurface, shape = RoundedCornerShape(8.dp))
                            )
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

@Preview("Widgets Screen Light")
@Composable
fun PreviewWidgetsScreenLight() {
    CarWidgetTheme() {
        BackgroundSurface {
            WidgetsListScreen(
                WidgetListScreenState(
                    items = listOf( WidgetItem.Shortcut(appWidgetId = 0) ),
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