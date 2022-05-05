package com.anod.car.home

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.FragmentActivity
import com.anod.car.home.appwidget.ShortcutPendingIntent
import com.anod.car.home.utils.MusicUtils
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.content.shortcuts.ShortcutExtra.EXTRA_MEDIA_BUTTON
import info.anodsplace.framework.content.startActivitySafely
import info.anodsplace.framework.permissions.AppPermissions
import info.anodsplace.framework.permissions.CallPhone
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class ShortcutActivity : FragmentActivity(), KoinComponent {

    private lateinit var callPhonePermission: ActivityResultLauncher<Void>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callPhonePermission = AppPermissions.register(this, CallPhone) {

        }
        execute(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        execute(intent)
    }

    private fun execute(intent: Intent) {
        val appIntent = intent.getParcelableExtra<Intent>(EXTRA_INTENT)
        if (appIntent != null) {
            runFromIntent(appIntent)
            finish()
            return
        }
        val keyCode = intent.getIntExtra(EXTRA_MEDIA_BUTTON, 0)
        if (keyCode > 0) {
            handleKeyCode(keyCode)
        }
        finish()
    }

    private fun handleKeyCode(keyCode: Int?) {
        if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
            val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            // pause
            if (audio.isMusicActive) {
                MusicUtils.sendKeyEvent(keyCode, this)
            } else {
                val musicCmp = get<AppSettings>().musicApp
                if (musicCmp == null) {
                    // TODO:
                    // startActivity(Intent(this, MusicAppChoiceActivity::class.java))
                } else {
                    MusicUtils.sendKeyEventComponent(keyCode, this, musicCmp, false)
                }
            }
        } else {
            MusicUtils.sendKeyEvent(keyCode!!, this)
        }
    }

    private fun runFromIntent(intent: Intent) {
        //fix for Galaxy s3
        val action = intent.action
        if (action != null && action == ShortcutPendingIntent.INTENT_ACTION_CALL_PRIVILEGED) {
            intent.action = Intent.ACTION_CALL
        }
        if (Intent.ACTION_CALL == action) {
            if (!AppPermissions.isGranted(this, CallPhone)) {
                callPhonePermission.launch(null)
            }
        }

        if (intent.sourceBounds == null) {
            intent.sourceBounds = getIntent().sourceBounds
        }

        startActivitySafely(intent)
    }

    companion object {
        const val EXTRA_INTENT = "intent"
    }
}