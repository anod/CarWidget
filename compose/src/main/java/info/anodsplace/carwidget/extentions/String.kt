package info.anodsplace.carwidget.extentions

import android.content.ComponentName

fun String.toComponentName(): ComponentName {
    val compParts = this.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    return ComponentName(compParts[0], compParts[1])
}