package com.anod.car.home

import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.IntentSanitizer
import androidx.fragment.app.FragmentActivity
import com.anod.car.home.appwidget.ShortcutPendingIntent
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.Deeplink
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.content.shortcuts.ShortcutExtra
import info.anodsplace.carwidget.content.shortcuts.ShortcutExtra.EXTRA_MEDIA_BUTTON
import info.anodsplace.framework.content.startActivitySafely
import info.anodsplace.framework.media.MediaKeyEvent
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
        val appIntent = extractSanitizedIntent(intent)
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

    /**
     * Extracts and sanitizes the nested Intent to prevent Intent Redirection attacks.
     * Uses IntentSanitizer to validate the Intent is safe to launch.
     */
    private fun extractSanitizedIntent(intent: Intent): Intent? {
        // Get the nested intent using the appropriate API
        val rawIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_INTENT, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_INTENT)
        }

        if (rawIntent == null) {
            return null
        }

        // Security: Validate that the caller is from our own app
        val callingPackage = callingActivity?.packageName
        if (callingPackage != null && callingPackage != packageName) {
            AppLog.e("ShortcutActivity: Intent from untrusted package: $callingPackage")
            return null
        }

        // Use IntentSanitizer to sanitize the nested intent
        return try {
            @Suppress("DEPRECATION")
            IntentSanitizer.Builder()
                // Standard Android actions
                .allowAction(Intent.ACTION_MAIN)
                .allowAction(Intent.ACTION_VIEW)
                .allowAction(Intent.ACTION_DIAL)
                .allowAction(Intent.ACTION_CALL)
                .allowAction(ShortcutPendingIntent.INTENT_ACTION_CALL_PRIVILEGED)
                .allowAction(Intent.ACTION_SENDTO)
                .allowAction(Intent.ACTION_SEND)
                .allowAction(Intent.ACTION_SEND_MULTIPLE)
                .allowAction(Intent.ACTION_PICK)
                .allowAction(Intent.ACTION_EDIT)
                .allowAction(Intent.ACTION_INSERT)
                .allowAction(Intent.ACTION_DELETE)
                .allowAction(Intent.ACTION_SEARCH)
                .allowAction(Intent.ACTION_WEB_SEARCH)
                .allowAction(Intent.ACTION_ASSIST)
                .allowAction(Intent.ACTION_VOICE_COMMAND)
                .allowAction(Intent.ACTION_APPLICATION_PREFERENCES)
                .allowAction(Intent.ACTION_SHOW_APP_INFO)
                .allowAction(Intent.ACTION_GET_CONTENT)
                .allowAction(Intent.ACTION_QUICK_VIEW)
                // Contact shortcuts
                .allowAction("android.provider.action.QUICK_CONTACT")
                // Music & media actions
                .allowAction("com.google.android.googlequicksearchbox.MUSIC_SEARCH")
                .allowAction("android.media.action.MEDIA_PLAY_FROM_SEARCH")
                .allowAction(Intent.ACTION_MEDIA_BUTTON)
                // Widget/launcher actions
                .allowAction(ShortcutExtra.ACTION_MEDIA_BUTTON)
                .allowAction(ShortcutExtra.ACTION_FOLDER)
                // Common categories
                .allowCategory(Intent.CATEGORY_DEFAULT)
                .allowCategory(Intent.CATEGORY_LAUNCHER)
                .allowCategory(Intent.CATEGORY_BROWSABLE)
                .allowCategory(Intent.CATEGORY_APP_MAPS)
                .allowCategory(Intent.CATEGORY_APP_EMAIL)
                .allowCategory(Intent.CATEGORY_APP_BROWSER)
                .allowCategory(Intent.CATEGORY_APP_CALENDAR)
                .allowCategory(Intent.CATEGORY_APP_CONTACTS)
                .allowCategory(Intent.CATEGORY_APP_MESSAGING)
                .allowCategory(Intent.CATEGORY_APP_MUSIC)
                // Allow any component (since we're launching shortcuts to various apps)
                .allowAnyComponent()
                // Allow common data schemes
                .allowData { true }
                // Allow contact MIME types
                .allowType("vnd.android.cursor.item/contact")
                .allowType("vnd.android.cursor.item/phone_v2")
                .allowType("vnd.android.cursor.item/person")
                // Allow extras but sanitize them
                .allowExtra(Intent.EXTRA_SHORTCUT_NAME, String::class.java)
                .allowExtra(Intent.EXTRA_SHORTCUT_INTENT, Intent::class.java)
                .allowExtra("android.intent.extra.shortcut.ID", String::class.java)
                // Important: Don't allow dangerous flags that grant URI permissions
                .allowFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .allowFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                .allowFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .allowFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .allowFlags(Intent.FLAG_FROM_BACKGROUND)
                .allowFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET) // Used by contact shortcuts
                .build()
                .sanitizeByFiltering(rawIntent)
        } catch (e: SecurityException) {
            AppLog.e("ShortcutActivity: Failed to sanitize intent", e)
            null
        }
    }

    private fun handleKeyCode(keyCode: Int) {
        val mediaKeyEvent = MediaKeyEvent(this)
        if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
            val audio = getSystemService(AUDIO_SERVICE) as AudioManager
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
                try {
                    callPhonePermission.launch(AppPermissions.Request.Input.Permissions(arrayOf(AppPermission.CallPhone.value)))
                } catch (e: Exception) {
                    AppLog.e(e)
                    Toast.makeText(this, "Cannot request cell phone permissions ${e.message}", Toast.LENGTH_SHORT).show()
                }
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