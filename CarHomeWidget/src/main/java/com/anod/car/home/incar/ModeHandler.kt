package com.anod.car.home.incar

import android.app.UiModeManager
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.wifi.WifiManager

import com.anod.car.home.prefs.model.InCarInterface
import com.anod.car.home.prefs.preferences.InCar
import info.anodsplace.framework.AppLog
import com.anod.car.home.utils.PowerUtil
import com.anod.car.home.utils.Utils

class ModeHandler(private val mContext: Context, private val mScreenOrientation: ScreenOrientation) {

    fun enable(prefs: InCarInterface) {
        if (prefs.isDisableScreenTimeout) {
            if (prefs.isDisableScreenTimeoutCharging) {
                if (PowerUtil.isConnected(mContext)) {
                    ModeService.acquireWakeLock(mContext)
                }
            } else {
                ModeService.acquireWakeLock(mContext)
            }
        }
        if (prefs.isAdjustVolumeLevel) {
            adjustVolume(prefs, mContext)
        }
        if (prefs.isEnableBluetooth) {
            enableBluetooth()
        }
        if (prefs.disableWifi != InCar.WIFI_NOACTION) {
            disableWifi(mContext)
        }
        if (prefs.isActivateCarMode) {
            activateCarMode(mContext)
        }

        if (SamsungDrivingMode.hasMode() && prefs.isSamsungDrivingMode) {
            SamsungDrivingMode.enable(mContext)
        }

        if (prefs.isHotspotOn) {
            if (prefs.disableWifi == InCar.WIFI_NOACTION) {
                disableWifi(mContext)
            }
            switchHotspot(mContext, true)
        }

        //		Intent intent = new Intent()
        //				.setComponent(new ComponentName("com.RSen.OpenMic.Pheonix", "com.RSen.OpenMic.Pheonix.StartListeningActivity"))
        //				.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //		Utils.startActivitySafely(intent, context);

        if (prefs.screenOrientation != ScreenOrientation.DISABLED) {
            mScreenOrientation.set(prefs.screenOrientation)
        }

        val autorunApp = prefs.autorunApp
        if (autorunApp != null) {
            runApp(autorunApp, mContext)
        }
        val brightSetting = prefs.brightness
        if (brightSetting != InCar.BRIGHTNESS_DISABLED) {
            adjustBrightness(brightSetting, mContext)
        }
    }

    fun disable(prefs: InCarInterface) {
        if (prefs.isDisableScreenTimeout) {
            ModeService.releaseWakeLock(mContext)
        }
        if (prefs.isAdjustVolumeLevel) {
            restoreVolume(mContext)
        }
        if (prefs.isEnableBluetooth) {
            restoreBluetooth()
        }
        if (prefs.isActivateCarMode) {
            deactivateCarMode(mContext)
        }
        if (prefs.isHotspotOn) {
            switchHotspot(mContext, false)
        }
        if (prefs.disableWifi == InCar.WIFI_TURNOFF) {
            restoreWiFi(mContext)
        }
        if (SamsungDrivingMode.hasMode() && prefs.isSamsungDrivingMode) {
            SamsungDrivingMode.disable(mContext)
        }

        mScreenOrientation.set(ScreenOrientation.DISABLED)

        val brightSetting = prefs.brightness
        if (brightSetting != InCar.BRIGHTNESS_DISABLED) {
            restoreBrightness(brightSetting, mContext)
        }
    }

    companion object {

        private const val VOLUME_NOT_SET = -1
        private const val BRIGHTNESS_MAX = 255
        private const val BRIGHTNESS_NIGHT = 30
        private const val BRIGHTNESS_DAY = BRIGHTNESS_MAX

        private var sCurrentBtState: Int = 0
        private var sCurrentWiFiState: Int = 0
        private var sCurrentMediaVolume = VOLUME_NOT_SET
        private var sCurrentCallVolume = VOLUME_NOT_SET
        private var sCurrentBrightness: Int = 0
        private var sCurrentAutoBrightness: Boolean = false

        private fun runApp(autorunApp: ComponentName, context: Context) {
            val intent = Intent(Intent.ACTION_MAIN)
                    .setComponent(autorunApp)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            Utils.startActivitySafely(intent, context)
        }

        private fun activateCarMode(context: Context) {
            val ui = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
            ui.enableCarMode(0)
        }

        private fun deactivateCarMode(context: Context) {
            val ui = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
            ui.disableCarMode(0)
        }

        private fun disableWifi(context: Context) {
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            sCurrentWiFiState = wm.wifiState
            if (sCurrentWiFiState != WifiManager.WIFI_STATE_DISABLED) {
                wm.isWifiEnabled = false
            }
        }

        private fun restoreWiFi(context: Context) {
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (sCurrentWiFiState == WifiManager.WIFI_STATE_ENABLED) {
                wm.isWifiEnabled = true
            }
        }

        private fun enableBluetooth() {
            sCurrentBtState = Bluetooth.getState()
            if (sCurrentBtState != BluetoothAdapter.STATE_ON) {
                Bluetooth.switchOn()
            }
        }

        private fun restoreBluetooth() {
            if (sCurrentBtState != BluetoothAdapter.STATE_ON) {
                Bluetooth.switchOff()
            }
        }

        private fun adjustBrightness(brightSetting: String, context: Context) {
            val cr = context.contentResolver

            sCurrentBrightness = android.provider.Settings.System.getInt(cr,
                    android.provider.Settings.System.SCREEN_BRIGHTNESS, BRIGHTNESS_MAX
            )
            sCurrentAutoBrightness = android.provider.Settings.System.getInt(cr,
                    android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
                    android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            ) == android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC

            var newBrightLevel = -1
            var newBrightMode = -1
            if (InCar.BRIGHTNESS_AUTO == brightSetting) {
                if (!sCurrentAutoBrightness) {
                    newBrightLevel = sCurrentBrightness
                    newBrightMode = android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                }
            } else if (InCar.BRIGHTNESS_DAY == brightSetting) {
                newBrightLevel = BRIGHTNESS_DAY
                newBrightMode = android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            } else if (InCar.BRIGHTNESS_NIGHT == brightSetting) {
                newBrightLevel = BRIGHTNESS_NIGHT
                newBrightMode = android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            }

            if (newBrightLevel == -1) {
                AppLog.d("Wrong brightness setting Mode : " + brightSetting + " Level : "
                        + newBrightLevel)
                return
            }

            android.provider.Settings.System
                    .putInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, newBrightMode)
            android.provider.Settings.System
                    .putInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS, newBrightLevel)

            sendBrightnessIntent(newBrightLevel, context)
        }

        private fun restoreBrightness(brightSetting: String, context: Context): Boolean {
            if (sCurrentAutoBrightness && InCar.BRIGHTNESS_AUTO == brightSetting) {
                return false
            }
            var newBrightMode = android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            if (sCurrentAutoBrightness) {
                newBrightMode = android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            }
            val cr = context.contentResolver
            android.provider.Settings.System
                    .putInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, newBrightMode)
            android.provider.Settings.System
                    .putInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS, sCurrentBrightness)

            sendBrightnessIntent(sCurrentBrightness, context)
            return true
        }

        fun adjustVolume(prefs: InCarInterface, context: Context) {
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

        /**
         * Turn on or off Hotspot.
         *
         */
        private fun switchHotspot(context: Context, isTurnToOn: Boolean) {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val apControl = WifiApControl.getApControl(wifiManager)
            if (apControl != null) {
                AppLog.d("is WiFi AP enabled:" + apControl.isWifiApEnabled())
                apControl.setWifiApEnabled(apControl.wifiApConfiguration!!, isTurnToOn)
            }
        }
    }
}
