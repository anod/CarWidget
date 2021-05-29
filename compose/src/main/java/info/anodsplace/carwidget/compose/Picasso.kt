package info.anodsplace.carwidget.compose

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.squareup.picasso.Picasso
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.db.iconUri
import info.anodsplace.carwidget.utils.LocalPicasso
import info.anodsplace.carwidget.utils.SystemIconSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


sealed class PicassoImage {
    object Loading : PicassoImage()
    class Loaded(val image: ImageBitmap) : PicassoImage()
    object Error : PicassoImage()
}

@Composable
fun PicassoIcon(uri: Uri, modifier: Modifier = Modifier) {
    val imageResult by loadPicassoImage(uri)
    when (imageResult) {
        is PicassoImage.Loading -> {
            Box(modifier = modifier) {

            }
        }
        is PicassoImage.Loaded -> {
            Icon(
                modifier = modifier,
                bitmap = (imageResult as PicassoImage.Loaded).image,
                contentDescription = null,
                tint = Color.Unspecified
            )
        }
        is PicassoImage.Error -> {
            Icon(
                modifier = modifier,
                imageVector = Icons.Filled.Cancel,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
fun loadPicassoImage(
    url: Uri,
    picasso: Picasso = LocalPicasso.current
): State<PicassoImage> {
    return produceState(initialValue = PicassoImage.Loading, url, picasso) {
        value = try {
            val result = withContext(Dispatchers.IO) { picasso.load(url).get() }
            PicassoImage.Loaded(result.asImageBitmap())
        } catch (e: Exception) {
            AppLog.e(e)
            PicassoImage.Error
        }
    }
}