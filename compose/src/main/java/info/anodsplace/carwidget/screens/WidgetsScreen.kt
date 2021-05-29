package info.anodsplace.carwidget.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.compose.*
import info.anodsplace.carwidget.content.Version
import info.anodsplace.carwidget.content.db.iconUri
import info.anodsplace.carwidget.incar.InCarStatus
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
fun LargeWidgetRow(item: WidgetItem.Large, indexes: List<Int>) {
    val context = LocalContext.current
    Row {
        for (idx in indexes) {
            val shortcut = item.shortcuts.get(idx)
            if (shortcut != null) {
                val iconModifier = Modifier
                    .size(SystemIconSize)
                    .padding(4.dp)
                PicassoIcon(shortcut.iconUri(context, item.adaptiveIconStyle), modifier = iconModifier)
            }
        }
    }
}

@Composable
fun LargeWidgetItem(item: WidgetItem.Large, onClick: () -> Unit) {
    Column(modifier = Modifier
        .clickable { onClick() }
        .cardStyle()) {
        LargeWidgetRow(item = item, indexes = listOf(1, 3, 5, 7))
        LargeWidgetRow(item = item, indexes = listOf(0, 2, 4, 5))
    }
}

@Composable
fun InCarHeader(widgetsCount: Int) {
    val version = Version(LocalContext.current)
    val status = InCarStatus(widgetsCount, version, LocalContext.current)
    val active = stringResource(status.resId)

    Column(modifier = Modifier.cardStyle()) {
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
fun WidgetsScreen(widgetList: List<WidgetItem>, onClick: (appWidgetId: Int) -> Unit) {
    if (widgetList.isEmpty()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            WidgetsEmptyScreen()
            Spacer(modifier = Modifier.height(16.dp))
            InCarHeader(widgetList.size)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            var hasLargeItem = false

            item {
                InCarHeader(widgetList.size)
            }

            items(widgetList.size) { idx ->
                val item = widgetList[idx]
                Spacer(modifier = Modifier.height(16.dp))
                when (item) {
                    is WidgetItem.Shortcut -> {
                        Box(modifier = Modifier.cardStyle()) {
                            Icon(
                                modifier = Modifier.size(SystemIconSize),
                                imageVector = Icons.Filled.Widgets,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                    is WidgetItem.Large -> {
                        hasLargeItem = true
                        LargeWidgetItem(item, onClick = { onClick(item.appWidgetId) } )
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
            WidgetsScreen(emptyList(), onClick = { })
        }
    }
}


@Preview("Widgets Screen Light")
@Composable
fun PreviewWidgetsScreenLight() {
    CarWidgetTheme(darkTheme = false) {
        BackgroundSurface {
            WidgetsScreen(
                listOf(
                    WidgetItem.Shortcut()
                ),
                onClick = { }
            )
        }
    }
}
