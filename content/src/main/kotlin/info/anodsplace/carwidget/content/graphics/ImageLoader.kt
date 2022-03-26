package info.anodsplace.carwidget.content.graphics

import android.content.Context
import coil.ImageLoader
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

val Context.imageLoader: ImageLoader
    get() = (applicationContext as KoinComponent).get()