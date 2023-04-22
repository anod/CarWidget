package info.anodsplace.carwidget.content.shortcuts

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import info.anodsplace.carwidget.content.extentions.isAvailable

object DefaultShortcuts {
    suspend fun load(context: Context, maxItems: Int = 5): List<ShortcutInfoFactory.Result> {
        val list = arrayOf(
            ComponentName("com.google.android.dialer", "com.google.android.dialer.extensions.GoogleDialtactsActivity"),
            ComponentName("com.android.contacts", "com.android.contacts.DialtactsActivity"),
            ComponentName("com.android.htccontacts", "com.android.htccontacts.DialerTabActivity"), //HTC CallPhone
            ComponentName("com.samsung.android.dialer", "com.samsung.android.dialer.DialtactsActivity"), //Samsung CallPhone
            //
            ComponentName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity"),
            //
            ComponentName("com.google.android.music", "com.android.music.activitymanagement.TopLevelActivity"),
            ComponentName("com.htc.music", "com.htc.music.HtcMusic"),
            ComponentName("com.sec.android.app.music", "com.sec.android.app.music.MusicActionTabActivity"),
            ComponentName("com.spotify.music", "com.spotify.music.MainActivity"),
            ComponentName("tunein.player", "tunein.ui.actvities.TuneInHomeActivity"),
            ComponentName("com.google.android.apps.youtube.music", "com.google.android.apps.youtube.music.activities.MusicActivity"),

            ComponentName("com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox.VoiceSearchActivity"),
            ComponentName("com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox.ImplicitSearchLongPressEndpointInternal"),
        )

        var position = 0
        val data = Intent()
        val result = mutableListOf<ShortcutInfoFactory.Result>()
        for (i in list.indices) {
            data.component = list[i]
            if (!data.isAvailable(context)) {
                continue
            }
            val shortcut = ShortcutInfoFactory.infoFromApplicationIntent(context, position, data)
            if (shortcut.info != null) {
                result.add(shortcut)
                position++
            }
            if (position == maxItems) {
                break
            }
        }
        return result
    }
}