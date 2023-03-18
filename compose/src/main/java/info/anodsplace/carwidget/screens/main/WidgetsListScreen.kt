package info.anodsplace.carwidget.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.LocalWidgetSystemTheme
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.WarningColor
import info.anodsplace.carwidget.content.InCarStatus
import info.anodsplace.carwidget.content.Version
import info.anodsplace.carwidget.content.db.iconUri
import info.anodsplace.carwidget.content.iconUri
import info.anodsplace.carwidget.utils.SystemIconSize

private fun Modifier.cardStyle(): Modifier = composed { then(
    fillMaxWidth()
        .clip(shape = MaterialTheme.shapes.large)
        .background(MaterialTheme.colorScheme.secondary)
        .padding(16.dp)
) }

private fun Modifier.iconContainer(): Modifier = composed {
    val widgetSystemTheme = LocalWidgetSystemTheme.current
    val shape = RoundedCornerShape(widgetSystemTheme.radius.background)
    then(
        padding(2.dp)
        .clip(shape = shape)
        .background(widgetSystemTheme.colorScheme.colorDynamicWidgetBackground)
        .border(1.dp, widgetSystemTheme.colorScheme.colorDynamicWidgetPrimary, shape = shape)
        .size(48.dp)
    ) }


@Composable
fun WidgetsListScreen(screen: WidgetListScreenState, onClick: (appWidgetId: Int) -> Unit, imageLoader: ImageLoader, innerPadding: PaddingValues = PaddingValues(0.dp)) {
    when (val loadState = screen.loadState) {
        is WidgetListLoadState.Ready -> {
            WidgetsLisItems(
                screen = screen,
                onClick = onClick,
                modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp),
                imageLoader = imageLoader
            )
        }
        is WidgetListLoadState.Error -> Text(text = loadState.message, color = MaterialTheme.colorScheme.error)
        WidgetListLoadState.Loading -> CircularProgressIndicator()
    }
}

@Composable
private fun WidgetsLisItems(screen: WidgetListScreenState, onClick: (appWidgetId: Int) -> Unit, modifier: Modifier = Modifier, imageLoader: ImageLoader) {
    LazyColumn(
        modifier = modifier
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

        if (screen.items.isEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                NoWidgetsItem()
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
                            modifier = Modifier
                                .iconContainer()
                        )
                    }
                }
                is WidgetItem.Large -> {
                    hasLargeItem = true
                    LargeWidgetItem(
                        item = item,
                        onClick = { onClick(item.appWidgetId) },
                        imageLoader = imageLoader,
                    )
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

@Composable
private fun NoWidgetsItem() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(WarningColor)
            .padding(16.dp)
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.no_active_widget),
            color = MaterialTheme.colorScheme.onError
        )
        Icon(
            imageVector = Icons.Filled.Widgets,
            contentDescription = null,
            modifier = Modifier
                .padding(4.dp)
                .size(36.dp),
            tint = MaterialTheme.colorScheme.onError.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun LargeWidgetRow(item: WidgetItem.Large, indexes: List<Int>, imageLoader: ImageLoader) {
    val context = LocalContext.current
    val count = item.shortcuts.size
    Row {
        for (idx in indexes) {
            if (idx >= count) {
                break
            }
            val shortcut = item.shortcuts.getOrDefault(idx, null)
            Box(
                modifier = Modifier
                    .iconContainer()
            ) {
                if (shortcut != null) {
                    AsyncImage(
                        model = shortcut.iconUri(context, item.adaptiveIconStyle, item.skinName),
                        contentDescription = shortcut.title.toString(),
                        imageLoader = imageLoader,
                        modifier = Modifier.fillMaxSize(),
                        transform = {
                            if (it is AsyncImagePainter.State.Success) {
                                AppLog.d("AsyncImage ${it.result.dataSource}")
                            }
                            it
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LargeWidgetItem(item: WidgetItem.Large, onClick: () -> Unit, imageLoader: ImageLoader) {
    Column(modifier = Modifier
        .clickable { onClick() }
        .cardStyle()) {
        LargeWidgetRow(item = item, indexes = listOf(1, 3, 5, 7, 9), imageLoader = imageLoader)
        LargeWidgetRow(item = item, indexes = listOf(0, 2, 4, 6, 8), imageLoader = imageLoader)
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
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondary
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
            color = MaterialTheme.colorScheme.onSecondary
        )
        if (screen.isServiceRequired) {
            Text(
                text = if (screen.isServiceRunning) "Detector service is running" else "Detector service is NOT running",
                color = MaterialTheme.colorScheme.onSecondary
            )
        }

        EventStates(screen.eventsState)

        when {
            version.isFreeAndTrialExpired -> {
                Text(
                    text = stringResource(R.string.dialog_donate_title_expired) + " " + stringResource(R.string.notif_consider),
                    color = MaterialTheme.colorScheme.onSecondary
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
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
            else -> {
            }
        }
    }
}

@Preview("Widgets Screen")
@Composable
fun PreviewWidgetsScreen() {
    CarWidgetTheme {
        WidgetsListScreen(
            screen = WidgetListScreenState(
                loadState = WidgetListLoadState.Ready,
                items = listOf(
                    WidgetItem.Shortcut(appWidgetId = 0),
                    WidgetItem.Shortcut(appWidgetId = 0),
                    WidgetItem.Large(
                        appWidgetId = 0, shortcuts = mapOf(
                            0 to null,
                            1 to null,
                            2 to null,
                            3 to null
                        ),
                        adaptiveIconStyle = "",
                        skinName = ""
                    )
                ),
                isServiceRunning = false,
                isServiceRequired = true,
                eventsState = emptyList(),
                statusResId = R.string.enabled
            ),
            onClick = { },
            imageLoader = ImageLoader.Builder(LocalContext.current).build()
        )
    }
}

@Preview("Widgets Empty Screen")
@Composable
fun PreviewWidgetsEmptyScreen() {
    CarWidgetTheme {
        WidgetsListScreen(
            screen = WidgetListScreenState(
                loadState = WidgetListLoadState.Ready,
                items = listOf(),
                isServiceRunning = false,
                isServiceRequired = true,
                eventsState = emptyList(),
                statusResId = R.string.enabled
            ),
            onClick = { },
            imageLoader = ImageLoader.Builder(LocalContext.current).build()
        )
    }
}