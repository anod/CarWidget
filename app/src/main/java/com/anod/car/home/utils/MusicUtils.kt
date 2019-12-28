package com.anod.car.home.utils

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, key))
            audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, key))
        } else {
            handleMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, key), context)
            handleMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, key), context)
        }
    }

    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    private fun handleMediaKeyEvent(keyEvent: KeyEvent, context: Context) {
        var hasDispatchSucceeded = false
        try {
            // Get binder from ServiceManager.checkService(String)
            val iBinder = Class.forName("android.os.ServiceManager")
                    .getDeclaredMethod("checkService", String::class.java)
                    .invoke(null, Context.AUDIO_SERVICE) as IBinder

            // get audioService from IAudioService.Stub.asInterface(IBinder)
            val audioService = Class.forName("android.media.IAudioService\$Stub")
                    .getDeclaredMethod("asInterface", IBinder::class.java).invoke(null, iBinder)

            // Dispatch keyEvent using IAudioService.dispatchMediaKeyEvent(KeyEvent)
            Class.forName("android.media.IAudioService")
                    .getDeclaredMethod("dispatchMediaKeyEvent", KeyEvent::class.java)
                    .invoke(audioService, keyEvent)
            hasDispatchSucceeded = true
        } catch (e: Exception) {
            AppLog.e(e)
        }

        // If dispatchMediaKeyEvent failed then try using broadcast
        if (!hasDispatchSucceeded) {
            val intent = Intent(Intent.ACTION_MEDIA_BUTTON)
            intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent)
            context.sendOrderedBroadcast(intent, null)
        }
    }

    fun sendKeyEventComponent(key: Int, context: Context, component: ComponentName,
                              start: Boolean) {
        val downIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        downIntent.putExtra(Intent.EXTRA_KEY_EVENT,
                KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                        KeyEvent.ACTION_DOWN, key, 0))
        downIntent.component = component

        val upIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        upIntent.putExtra(Intent.EXTRA_KEY_EVENT,
                KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                        KeyEvent.ACTION_UP, key, 0))
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
