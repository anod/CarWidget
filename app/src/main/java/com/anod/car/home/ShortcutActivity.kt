package com.anod.car.home

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.FragmentActivity
import com.anod.car.home.appwidget.ShortcutPendingIntent
import info.anodsplace.framework.media.MediaKeyEvent
import info.anodsplace.carwidget.OverlayComposeActivity
import info.anodsplace.carwidget.content.Deeplink
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.content.shortcuts.ShortcutExtra.EXTRA_MEDIA_BUTTON
import info.anodsplace.framework.content.startActivitySafely
import info.anodsplace.permissions.AppPermission
import info.anodsplace.permissions.AppPermissions
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class ShortcutActivity : FragmentActivity(), KoinComponent {

    private lateinit var callPhonePermission: ActivityResultLauncher<AppPermissions.Request.Input>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callPhonePermission = registerForActivityResult(AppPermissions.Request()) {

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

    private fun handleKeyCode(keyCode: Int) {
        val mediaKeyEvent = MediaKeyEvent(this)
        if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
            val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            // pause
            if (audio.isMusicActive) {
                mediaKeyEvent.send(keyCode)
            } else {
                val musicCmp = get<AppSettings>().musicApp
                if (musicCmp == null) {
                    startActivity(Intent(this, OverlayActivity::class.java).apply {
                        data = Deeplink.PlayMediaButton.toUri()
                    })
                } else {
                    mediaKeyEvent.sendToComponent(keyCode, musicCmp, false)
                }
            }
        } else {
            mediaKeyEvent.send(keyCode)
        }
    }

    private fun runFromIntent(intent: Intent) {
        //fix for Galaxy s3
        val action = intent.action
        if (action != null && action == ShortcutPendingIntent.INTENT_ACTION_CALL_PRIVILEGED) {
            intent.action = Intent.ACTION_CALL
        }
        if (Intent.ACTION_CALL == action) {
            if (!AppPermissions.isGranted(this, AppPermission.CallPhone)) {
                callPhonePermission.launch(AppPermissions.Request.Input.Permissions(arrayOf(AppPermission.CallPhone.value)))
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