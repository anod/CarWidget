package com.anod.car.home.incar

import android.app.UiModeManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.anod.car.home.utils.Power
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.incar.ScreenOnAlert
import info.anodsplace.carwidget.incar.ScreenOrientation
import info.anodsplace.framework.content.startActivitySafely
import org.koin.core.Koin

class ModeHandler(
    private val context: Context,
    private val screenOrientation: ScreenOrientation,
    private val koin: Koin
) {
    private val alertWindow: ScreenOnAlert by lazy { koin.get() }

    fun enable(prefs: info.anodsplace.carwidget.content.preferences.InCarInterface) {
        if (prefs.isDisableScreenTimeout) {
            if (prefs.isDisableScreenTimeoutCharging) {
                if (Power.isConnected(context)) {
                    ModeService.acquireWakeLock(context)
                    if (prefs.screenOnAlert.enabled) {
                        alertWindow.show()
                    }
                }
            } else {
                ModeService.acquireWakeLock(context)
                if (prefs.screenOnAlert.enabled) {
                    alertWindow.show()
                }
            }
        }
        if (prefs.isAdjustVolumeLevel) {
            adjustVolume(prefs, context)
        }
        if (prefs.isActivateCarMode) {
            activateCarMode(context)
        }

        if (prefs.screenOrientation != ScreenOrientation.DISABLED) {
            screenOrientation.set(prefs.screenOrientation)
        }

        val autorunApp = prefs.autorunApp
        if (autorunApp != null) {
            runApp(autorunApp, context)
        }
        val brightSetting = prefs.brightness
        if (brightSetting != info.anodsplace.carwidget.content.preferences.InCarInterface.BRIGHTNESS_DISABLED) {
            adjustBrightness(brightSetting, context)
        }
    }

    fun disable(prefs: info.anodsplace.carwidget.content.preferences.InCarInterface) {
        alertWindow.hide()
        if (prefs.isDisableScreenTimeout) {
            ModeService.releaseWakeLock(context)
        }
        if (prefs.isAdjustVolumeLevel) {
            restoreVolume(context)
        }
        if (prefs.isActivateCarMode) {
            deactivateCarMode(context)
        }

        screenOrientation.set(ScreenOrientation.DISABLED)
        val brightSetting = prefs.brightness
        if (brightSetting != info.anodsplace.carwidget.content.preferences.InCarInterface.BRIGHTNESS_DISABLED) {
            restoreBrightness(brightSetting, context)
        }
    }

    companion object {

        private const val VOLUME_NOT_SET = -1
        private const val BRIGHTNESS_MAX = 255
        private const val BRIGHTNESS_NIGHT = 30
        private const val BRIGHTNESS_DAY = BRIGHTNESS_MAX

        private var sCurrentMediaVolume = VOLUME_NOT_SET
        private var sCurrentCallVolume = VOLUME_NOT_SET
        private var sCurrentBrightness: Int = 0
        private var sCurrentAutoBrightness: Boolean = false

        private fun runApp(autorunApp: ComponentName, context: Context) {
            val intent = Intent(Intent.ACTION_MAIN)
                    .setComponent(autorunApp)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivitySafely(intent)
        }

        private fun activateCarMode(context: Context) {
            val ui = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
            ui.enableCarMode(0)
        }

        private fun deactivateCarMode(context: Context) {
            val ui = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
            ui.disableCarMode(0)
        }

        private fun adjustBrightness(brightSetting: String, context: Context) {
            val cr = context.contentResolver

            sCurrentBrightness = Settings.System.getInt(cr,
                    Settings.System.SCREEN_BRIGHTNESS, BRIGHTNESS_MAX
            )
            sCurrentAutoBrightness = Settings.System.getInt(cr,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            ) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC

            var newBrightLevel = -1
            var newBrightMode = -1
            if (info.anodsplace.carwidget.content.preferences.InCarInterface.BRIGHTNESS_AUTO == brightSetting) {
                if (!sCurrentAutoBrightness) {
                    newBrightLevel = sCurrentBrightness
                    newBrightMode = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                }
            } else if (info.anodsplace.carwidget.content.preferences.InCarInterface.BRIGHTNESS_DAY == brightSetting) {
                newBrightLevel = BRIGHTNESS_DAY
                newBrightMode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            } else if (info.anodsplace.carwidget.content.preferences.InCarInterface.BRIGHTNESS_NIGHT == brightSetting) {
                newBrightLevel = BRIGHTNESS_NIGHT
                newBrightMode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            }

            if (newBrightLevel == -1) {
                AppLog.e("Wrong brightness setting Mode : " + brightSetting + " Level : "
                        + newBrightLevel)
                return
            }

            writeBrightness(context, newBrightMode, newBrightLevel)
            sendBrightnessIntent(newBrightLevel, context)
        }

        private fun restoreBrightness(brightSetting: String, context: Context): Boolean {
            if (sCurrentAutoBrightness && info.anodsplace.carwidget.content.preferences.InCarInterface.BRIGHTNESS_AUTO == brightSetting) {
                return false
            }
            var newBrightMode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            if (sCurrentAutoBrightness) {
                newBrightMode = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            }

            writeBrightness(context, newBrightMode, sCurrentBrightness)
            sendBrightnessIntent(sCurrentBrightness, context)
            return true
        }

        private fun writeBrightness(context: Context, newBrightMode: Int, newBrightLevel: Int) {
            val cr = context.contentResolver
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(context)) {
                    Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS_MODE, newBrightMode)
                    Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS, newBrightLevel)
                } else {
                    Toast.makeText(context, info.anodsplace.carwidget.content.R.string.allow_permissions_brightness, Toast.LENGTH_LONG).show()
                }
            } else {
                Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS_MODE, newBrightMode)
                Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS, newBrightLevel)
            }
        }

        fun adjustVolume(prefs: info.anodsplace.carwidget.content.preferences.InCarInterface, context: Context) {
            val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            val adjVolume = prefs.mediaVolumeLevel
            val maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val volume = maxVolume * adjVolume / 100

            if (sCurrentMediaVolume == VOLUME_NOT_SET) {
                sCurrentMediaVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
            }

            audio.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)

            val adjCallVolume = prefs.callVolumeLevel
            val maxCallVolume = audio.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
            val callVolume = maxCallVolume * adjCallVolume / 100

            if (sCurrentCallVolume == VOLUME_NOT_SET) {
                sCurrentCallVolume = audio.getStreamVolume(AudioManager.STREAM_VOICE_CALL)
            }

            audio.setStreamVolume(AudioManager.STREAM_VOICE_CALL, callVolume, 0)
        }


        private fun restoreVolume(context: Context) {
            val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            if (sCurrentMediaVolume != VOLUME_NOT_SET) {
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, sCurrentMediaVolume, 0)
                sCurrentMediaVolume = VOLUME_NOT_SET
            }

            if (sCurrentCallVolume != VOLUME_NOT_SET) {
                audio.setStreamVolume(AudioManager.STREAM_VOICE_CALL, sCurrentCallVolume, 0)
                sCurrentCallVolume = VOLUME_NOT_SET
            }

        }

        private fun sendBrightnessIntent(newBrightLevel: Int, context: Context) {
            val intent = Intent(context, ChangeBrightnessActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            val bt = newBrightLevel.toFloat() / BRIGHTNESS_MAX
            intent.putExtra(ChangeBrightnessActivity.EXTRA_BRIGHT_LEVEL, bt)
            context.startActivity(intent)
        }
    }
}