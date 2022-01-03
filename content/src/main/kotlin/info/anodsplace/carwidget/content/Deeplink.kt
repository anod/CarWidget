package info.anodsplace.carwidget.content

import android.net.Uri
import androidx.core.net.toUri

sealed interface Deeplink {
    fun toUri(): Uri

    data class SwitchMode(val enable: Boolean) : Deeplink {
        override fun toUri(): Uri = build("mode", listOf("switch", if (enable) 1 else 0)).toUri()
    }

    data class EditShortcut(val appWidgetId: Int, val shortcutId: Long, val position: Int) : Deeplink {
        override fun toUri(): Uri = build("widgets", listOf(appWidgetId,"edit",shortcutId,position)).toUri()
    }

    data class OpenWidgetShortcut(val appWidgetId: Int, val position: Int) : Deeplink {
        override fun toUri(): Uri = build("widgets", listOf(appWidgetId, "open", position)).toUri()
    }

    data class OpenNotificationShortcut(val position: Int) : Deeplink {
        override fun toUri(): Uri = build("notification", listOf("open", position)).toUri()
    }

    companion object {
        private const val SCHEME = "carwidget"
        private fun build(authority: String, path: List<Any>): String = "$SCHEME://$authority/${path.joinToString("/")}"
    }
}