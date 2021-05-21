package com.anod.car.home.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.SystemClock
import android.view.KeyEvent
import info.anodsplace.framework.AppLog

/**
 * @author alex
 * @date 1/25/14
 */
object MusicUtils {

    fun sendKeyEvent(key: Int, context: Context) {
        AppLog.i("Sending event key $key")
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, key))
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, key))
    }

    fun sendKeyEventComponent(
        key: Int, context: Context, component: ComponentName,
        start: Boolean
    ) {
        val downIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        downIntent.putExtra(
            Intent.EXTRA_KEY_EVENT,
            KeyEvent(
                SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                KeyEvent.ACTION_DOWN, key, 0
            )
        )
        downIntent.component = component

        val upIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        upIntent.putExtra(
            Intent.EXTRA_KEY_EVENT,
            KeyEvent(
                SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                KeyEvent.ACTION_UP, key, 0
            )
        )
        upIntent.component = component

        if (start) {
            val startIntent = context.packageManager
                .getLaunchIntentForPackage(component.packageName)
            if (startIntent != null) {
                context.startActivity(startIntent)
            }
        }

        context.sendOrderedBroadcast(downIntent, null, null, null, -1, null, null)
        context.sendOrderedBroadcast(upIntent, null, null, null, -1, null, null)
    }
}
