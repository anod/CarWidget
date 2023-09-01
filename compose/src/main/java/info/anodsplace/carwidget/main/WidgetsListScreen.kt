package info.anodsplace.carwidget.main

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import info.anodsplace.carwidget.WarningColor
import info.anodsplace.carwidget.WarningIcon
import info.anodsplace.carwidget.WidgetsIcon
import info.anodsplace.carwidget.content.InCarStatus
import info.anodsplace.carwidget.content.R
import info.anodsplace.carwidget.content.db.iconUri
import info.anodsplace.carwidget.content.iconUri
import info.anodsplace.carwidget.utils.SystemIconSize
import info.anodsplace.compose.SystemIconShape
import info.anodsplace.framework.content.CommonActivityAction
import info.anodsplace.framework.content.forRequestIgnoreBatteryOptimization

private val IconSize = 52.dp

private fun Modifier.cardStyle(backgroundColor: Color? = null): Modifier = composed { then(
    fillMaxWidth()
        .clip(shape = MaterialTheme.shapes.large)
        .background(backgroundColor ?: MaterialTheme.colorScheme.secondary)
        .padding(16.dp)
) }

private fun Modifier.iconContainer(iconShape: Shape): Modifier = composed {
    val widgetSystemTheme = LocalWidgetSystemTheme.current
    then(
        padding(2.dp)
            .clip(shape = iconShape)
            .background(widgetSystemTheme.colorScheme.colorDynamicWidgetBackground)
            .border(
                1.dp,
                widgetSystemTheme.colorScheme.colorDynamicWidgetPrimary,
                shape = iconShape
            )
            .size(IconSize)
    ) }


@Composable
fun WidgetsListScreen(
    screen: WidgetListScreenState,
    onEvent: (MainViewEvent) -> Unit,
    imageLoader: ImageLoader,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    onActivityAction: (CommonActivityAction) -> Unit = { }
) {
    when (val loadState = screen.loadState) {
        is WidgetListLoadState.Ready -> {
            WidgetsLisItems(
                screen = screen,
                onEvent = onEvent,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                onActivityAction = onActivityAction,
                imageLoader = imageLoader
            )
        }
        is WidgetListLoadState.Error -> Text(text = loadState.message, color = MaterialTheme.colorScheme.error)
        WidgetListLoadState.Loading -> CircularProgressIndicator()
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun WidgetsLisItems(
    screen: WidgetListScreenState,
    onEvent: (MainViewEvent) -> Unit,
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
    onActivityAction: (CommonActivityAction) -> Unit
) {
    val iconSizePx = with(LocalDensity.current) { IconSize.roundToPx() }
    val iconShape = SystemIconShape(iconSizePx)
    val context = LocalContext.current

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
                    .clickable {
                        if (!screen.ignoringBatteryOptimization) {
                            onActivityAction(
                                CommonActivityAction.StartActivity(
                                    Intent().forRequestIgnoreBatteryOptimization(context.packageName)
                                )
                            )
                        }
                    }
                    .cardStyle(backgroundColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    WarningIcon(
                        modifier = Modifier.size(SystemIconSize),
                        tint = MaterialTheme.colorScheme.error)
                    Column(
                        modifier = Modifier.padding(start = 16.dp),
                    ) {
                        Text(
                            text = stringResource(info.anodsplace.carwidget.content.R.string.incar_service_is_not_running),
                            color = MaterialTheme.colorScheme.error
                        )
                        if (!screen.ignoringBatteryOptimization) {
                            Text(
                                text = stringResource(info.anodsplace.carwidget.content.R.string.disable_battery_optimization_warning),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        if (screen.items.isEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                NoWidgetsItem(onClick = { onEvent(MainViewEvent.ShowWizard) })
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
                                .iconContainer(iconShape)
                        )
                    }
                }
                is WidgetItem.Large -> {
                    hasLargeItem = true
                    LargeWidgetItem(
                        item = item,
                        onClick = {
                            throw IllegalStateException("test exception")
                            onEvent(MainViewEvent.OpenWidgetConfig(item.appWidgetId))
                        },
                        iconShape = iconShape,
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
                    text = stringResource(id = info.anodsplace.carwidget.content.R.string.widgets_hint)
                )
            }
        }
    }
}

@Composable
private fun NoWidgetsItem(onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(WarningColor)
            .padding(16.dp)
            .clickable(onClick = onClick)
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(id = info.anodsplace.carwidget.content.R.string.no_active_widget),
            color = MaterialTheme.colorScheme.onError
        )
        WidgetsIcon(
            modifier = Modifier
                .padding(4.dp)
                .size(36.dp),
            tint = MaterialTheme.colorScheme.onError.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun LargeWidgetRow(item: WidgetItem.Large, indexes: List<Int>, iconShape: Shape, imageLoader: ImageLoader) {
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
                    .iconContainer(iconShape)
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
fun LargeWidgetItem(item: WidgetItem.Large, onClick: () -> Unit, iconShape: Shape, imageLoader: ImageLoader) {
    Column(modifier = Modifier
        .clickable { onClick() }
        .cardStyle()) {
        LargeWidgetRow(item = item, indexes = listOf(1, 3, 5, 7, 9), iconShape = iconShape, imageLoader = imageLoader)
        LargeWidgetRow(item = item, indexes = listOf(0, 2, 4, 6, 8), iconShape = iconShape, imageLoader = imageLoader)
    }
}

@Composable
fun EventStates(eventsState: List<InCarStatus.EventState>) {
    val titles = listOf(
            info.anodsplace.carwidget.content.R.string.pref_power_connected_title,
            info.anodsplace.carwidget.content.R.string.pref_headset_connected_title,
            info.anodsplace.carwidget.content.R.string.pref_blutooth_device_title,
            info.anodsplace.carwidget.content.R.string.activity_recognition,
            info.anodsplace.carwidget.content.R.string.car_dock
    )
    Column(modifier = Modifier.padding(top = 16.dp)) {
        for (event in eventsState) {
            val title = stringResource(titles[event.id])
            val enabled = if (event.enabled) stringResource(info.anodsplace.carwidget.content.R.string.enabled) else stringResource(info.anodsplace.carwidget.content.R.string.disabled)
            val active = if (event.active) stringResource(info.anodsplace.carwidget.content.R.string.active) else stringResource(info.anodsplace.carwidget.content.R.string.not_active)
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
    val active = stringResource(screen.statusResId)

    Column(modifier = Modifier.cardStyle()) {
        Text(
            text = stringResource(id = info.anodsplace.carwidget.content.R.string.pref_incar_mode_title) + " - " + active,
            color = MaterialTheme.colorScheme.onSecondary
        )
        if (screen.isServiceRequired) {
            Text(
                text = if (screen.isServiceRunning) "Detector service is running" else "Detector service is NOT running",
                color = MaterialTheme.colorScheme.onSecondary
            )

            EventStates(screen.eventsState)
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
                isServiceRequired = true,
                isServiceRunning = false,
                eventsState = emptyList(),
                statusResId = R.string.enabled,
                ignoringBatteryOptimization = true
            ),
            onEvent = { },
            imageLoader = ImageLoader.Builder(LocalContext.current).build(),
            onActivityAction =  {  }
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
                isServiceRequired = true,
                isServiceRunning = false,
                eventsState = emptyList(),
                statusResId = R.string.enabled,
                ignoringBatteryOptimization = false
            ),
            onEvent = { },
            imageLoader = ImageLoader.Builder(LocalContext.current).build(),
            onActivityAction = {  }
        )
    }
}