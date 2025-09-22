package info.anodsplace.carwidget.appwidget

import android.view.KeyEvent
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import info.anodsplace.carwidget.chooser.ChooserDialog
import info.anodsplace.carwidget.chooser.MediaListLoader
import info.anodsplace.framework.media.MediaKeyEvent

@Composable
fun PlayMediaButton(onDismissRequest: () -> Unit, imageLoader: ImageLoader) {
    val context = LocalContext.current
    val loader = remember { MediaListLoader(context) }
    ChooserDialog(
        modifier = Modifier.padding(16.dp),
        headers = listOf(),
        loader = loader,
        onClick = { entry ->
            if (entry.componentName != null) {
                MediaKeyEvent(context).sendToComponent(
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                    entry.componentName,
                    false
                )
            }
            onDismissRequest()
        },
        imageLoader = imageLoader,
        onDismissRequest = onDismissRequest
    )
}