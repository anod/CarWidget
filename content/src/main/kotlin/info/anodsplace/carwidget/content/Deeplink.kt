package info.anodsplace.carwidget.content

import android.net.Uri
import androidx.core.net.toUri

sealed interface Deeplink {
    fun toUri(): Uri

    interface AppWidgetIdAware {
        val appWidgetId: Int
    }

    data class SwitchMode(val enable: Boolean) : Deeplink {
        companion object {
            const val uriPattern = "carwidget://mode/switch/{enable}"
        }
        override fun toUri(): Uri = build("mode", listOf("switch", if (enable) 1 else 0)).toUri()
    }

    data class EditShortcut(override val appWidgetId: Int, val shortcutId: Long, val position: Int) : Deeplink, AppWidgetIdAware {
        companion object {
            const val uriPattern = "carwidget://widgets/{app_widget_id}/edit/{shortcut_id}/{pos_id}"
        }
        override fun toUri(): Uri = build("widgets", listOf(appWidgetId, "edit", shortcutId, position)).toUri()
    }

    data class EditWidgetButton(override val appWidgetId: Int, val buttonId: Int) : Deeplink, AppWidgetIdAware {
        companion object {
            const val uriPattern = "carwidget://widgets/{app_widget_id}/button/{btn_id}/edit"
        }
        override fun toUri(): Uri = build("widgets", listOf(appWidgetId, "button", buttonId, "edit")).toUri()
    }

    object PlayMediaButton : Deeplink {
        const val uriPattern = "carwidget://music/play"
        override fun toUri(): Uri = build("music", listOf("play")).toUri()
    }

    data class OpenWidgetShortcut(override val appWidgetId: Int, val position: Int) : Deeplink, AppWidgetIdAware {
        companion object {
            const val uriPattern = "carwidget://widgets/{app_widget_id}/open/{pos_id}"
        }
        override fun toUri(): Uri = build("widgets", listOf(appWidgetId, "open", position)).toUri()
    }

    data class OpenNotificationShortcut(val position: Int) : Deeplink {
        companion object {
            const val uriPattern = "carwidget://notification/open/{pos_id}"
        }
        override fun toUri(): Uri = build("notification", listOf("open", position)).toUri()
    }

    companion object {
        private const val SCHEME = "carwidget"
        private fun build(authority: String, path: List<Any>): String = "$SCHEME://$authority/${path.joinToString("/")}"

        fun match(uri: Uri): Deeplink? {
            if (uri.scheme != SCHEME) {
                return null
            }
            if (uri.authority == "widgets") {
                if (uri.pathSegments.size < 4) {
                    return null
                }
                val appWidgetId = uri.pathSegments[0].toInt()
                // {app_widget_id}/edit/{shortcut_id}/{pos_id}
                if (uri.pathSegments[1] == "edit") {
                    val shortcutId = uri.pathSegments[2].toLong()
                    val position = uri.pathSegments[3].toInt()
                    return EditShortcut(appWidgetId, shortcutId, position)
                }
                // {app_widget_id}/button/{btn_id}/edit
                if (uri.pathSegments[1] == "button") {
                    val buttonId = uri.pathSegments[2].toInt()
                    return EditWidgetButton(appWidgetId, buttonId)
                }
            }
            if (uri.authority == "music") {
                if (uri.pathSegments[0] == "play") {
                    return PlayMediaButton
                }
            }
            return null
        }
    }
}