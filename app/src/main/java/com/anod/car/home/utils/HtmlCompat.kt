package com.anod.car.home.utils

import android.os.Build
import android.text.Html
import android.text.Spanned

/**
 * @author algavris
 * @date 13/08/2016.
 */
object HtmlCompat {
    fun fromHtml(text: String): Spanned {
        return if (Build.VERSION.SDK_INT >= 24) {
            Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
        } else Html.fromHtml(text)
    }
}
