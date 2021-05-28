package info.anodsplace.carwidget.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.squareup.picasso.Picasso
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.compose.BackgroundSurface
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.carwidget.compose.WarningColor
import info.anodsplace.carwidget.content.Version
import info.anodsplace.carwidget.content.db.iconUri
import info.anodsplace.carwidget.incar.InCarStatus
import info.anodsplace.carwidget.utils.LocalPicasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun cardStyle(): Modifier = Modifier
    .fillMaxWidth()
    .background(MaterialTheme.colors.surface)
    .padding(16.dp)
    .clip(MaterialTheme.shapes.medium)

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

sealed class RemoteImageState {
    object Loading : RemoteImageState()
    class Loaded(val image: ImageBitmap) : RemoteImageState()
    object LoadError : RemoteImageState()
}

@Composable
fun loadNetworkImage(
    url: Uri,
    picasso: Picasso = LocalPicasso.current
): State<RemoteImageState> {
    return produceState(initialValue = RemoteImageState.Loading, url, picasso) {
        value = try {
            val result = withContext(Dispatchers.IO) { picasso.load(url).get() }
            RemoteImageState.Loaded(result.asImageBitmap())
        } catch (e: Exception) {
            RemoteImageState.LoadError
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
                val imageResult by loadNetworkImage(shortcut.iconUri(context, item.adaptiveIconStyle))
                when (imageResult) {
                    is RemoteImageState.Loading -> {
                        CircularProgressIndicator()
                    }
                    is RemoteImageState.Loaded -> {
                        Image(bitmap = (imageResult as RemoteImageState.Loaded).image, contentDescription = null)
                    }
                    is RemoteImageState.LoadError -> {
                        Image(imageVector = Icons.Filled.Cancel, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun LargeWidgetItem(item: WidgetItem.Large, onClick: () -> Unit) {
    Column(modifier = cardStyle().clickable { onClick() }) {
        LargeWidgetRow(item = item, indexes = listOf(1, 3, 5, 7))
        LargeWidgetRow(item = item, indexes = listOf(0, 2, 4, 5))
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
            var hasLargeItem = false
            for (item in widgetList) {
                Spacer(modifier = Modifier.height(16.dp))
                when (item) {
                    is WidgetItem.Shortcut -> {
                        Box(modifier = cardStyle()) {
                            Icon(imageVector = Icons.Filled.Widgets, contentDescription = null)
                        }
                    }
                    is WidgetItem.Large -> {
                        hasLargeItem = true
                        LargeWidgetItem(item, onClick = { })
                    }
                }
            }
            if (hasLargeItem) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = stringResource(id = R.string.widgets_hint))
            }
        }
    }
}

@Composable
fun InCarHeader(widgetsCount: Int) {
    val version = Version(LocalContext.current)
    val status = InCarStatus(widgetsCount, version, LocalContext.current)
    val active = stringResource(status.resId)

    Column(modifier = cardStyle()) {
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

@Preview("Widgets Screen Empty dark")
@Composable
fun PreviewWidgetsScreenEmptyDark() {
    CarWidgetTheme(darkTheme = true) {
        BackgroundSurface {
            WidgetsScreen(emptyList())
        }
    }
}


@Preview("Widgets Screen Light")
@Composable
fun PreviewWidgetsScreenLight() {
    CarWidgetTheme(darkTheme = false) {
        BackgroundSurface {
            WidgetsScreen(listOf(
                WidgetItem.Shortcut()
            ))
        }
    }
}
