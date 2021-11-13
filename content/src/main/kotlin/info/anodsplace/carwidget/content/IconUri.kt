package info.anodsplace.carwidget.content

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.annotation.DrawableRes


fun Context.iconUri(@DrawableRes iconRes: Int): Uri {
    return Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .encodedAuthority(packageName)
        .appendEncodedPath(iconRes.toString())
        .build()
}

fun Context.iconUri(type: String, name: String): Uri {
    return Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .encodedAuthority(packageName)
        .appendEncodedPath(type)
        .appendEncodedPath(name)
        .build()
}