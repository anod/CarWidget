package info.anodsplace.carwidget.compose

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.squareup.picasso.Picasso
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.utils.LocalPicasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


sealed class PicassoImage {
    object Loading : PicassoImage()
    class Loaded(val image: ImageBitmap) : PicassoImage()
    object Error : PicassoImage()
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