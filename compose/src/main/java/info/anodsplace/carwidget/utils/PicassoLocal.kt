package info.anodsplace.carwidget.utils

import androidx.compose.runtime.compositionLocalOf
import com.squareup.picasso.Picasso

val LocalPicasso = compositionLocalOf<Picasso> {
    error("No Picasso provided")
}