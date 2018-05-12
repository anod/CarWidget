package com.anod.car.home

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast

import com.anod.car.home.app.MusicAppChoiceActivity
import com.anod.car.home.appwidget.ShortcutPendingIntent
import com.anod.car.home.prefs.model.AppSettings
import com.anod.car.home.utils.AppPermissions
import com.anod.car.home.utils.MusicUtils

import info.anodsplace.framework.AppLog

class ShortcutActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        execute(intent)
    }

    override fun onNewIntent(intent: Intent) {
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
                val musicCmp = AppSettings.create(this).musicApp
                if (musicCmp == null) {
                    startActivity(Intent(this, MusicAppChoiceActivity::class.java))
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
            if (!AppPermissions.isGranted(this, android.Manifest.permission.CALL_PHONE)) {
                AppPermissions.request(this, arrayOf(android.Manifest.permission.CALL_PHONE), requestPhone)
            }
        }

        if (intent.sourceBounds == null) {
            intent.sourceBounds = getIntent().sourceBounds
        }

        startActivitySafely(intent)
    }

    private fun startActivitySafely(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.activity_not_found), Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
            AppLog.e(e)
        }

    }

    companion object {
        const val EXTRA_INTENT = "intent"
        const val EXTRA_MEDIA_BUTTON = "media_button"
        const val ACTION_MEDIA_BUTTON = "action_media_button"

        const val requestPhone = 303
    }
}
