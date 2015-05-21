package com.anod.car.home.incar;

import com.anod.car.home.prefs.preferences.InCar;
import com.anod.car.home.utils.AppLog;
import com.anod.car.home.utils.PowerUtil;
import com.anod.car.home.utils.Utils;

import android.app.UiModeManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiManager;

public class Handler {

    private static final int VOLUME_NOT_SET = -1;

    private static final int BRIGHTNESS_MAX = 255;

    private static final int BRIGHTNESS_NIGHT = 30;

    private static final int BRIGHTNESS_DAY = BRIGHTNESS_MAX;

    private static int sCurrentBtState;

    private static int sCurrentWiFiState;

    private static int sCurrentMediaVolume = VOLUME_NOT_SET;

    private static int sCurrentCallVolume = VOLUME_NOT_SET;

    private static int sCurrentBrightness;

    private static boolean sCurrentAutoBrightness;

    private final Context mContext;

    private final ScreenOrientation mScreenOrientation;

    public Handler(Context context, ScreenOrientation orientation) {
        mContext = context;
        mScreenOrientation = orientation;
    }

    public void enable(InCar prefs) {
        if (prefs.isDisableScreenTimeout()) {
            if (prefs.isDisableScreenTimeoutCharging()) {
                if (PowerUtil.isConnected(mContext)) {
                    ModeService.acquireWakeLock(mContext);
                }
            } else {
                ModeService.acquireWakeLock(mContext);
            }
        }
        if (prefs.isAdjustVolumeLevel()) {
            adjustVolume(prefs, mContext);
        }
        if (prefs.isEnableBluetooth()) {
            enableBluetooth();
        }
        if (!prefs.getDisableWifi().equals(InCar.WIFI_NOACTION)) {
            disableWifi(mContext);
        }
        if (prefs.isActivateCarMode()) {
            activateCarMode(mContext);
        }

        if (SamsungDrivingMode.hasMode() && prefs.isSamsungDrivingMode()) {
            SamsungDrivingMode.enable(mContext);
        }

//		Intent intent = new Intent()
//				.setComponent(new ComponentName("com.RSen.OpenMic.Pheonix", "com.RSen.OpenMic.Pheonix.StartListeningActivity"))
//				.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		Utils.startActivitySafely(intent, context);

        if (prefs.getScreenOrientation() != ScreenOrientation.DISABLED) {
            mScreenOrientation.set(prefs.getScreenOrientation());
        }

        ComponentName autorunApp = prefs.getAutorunApp();
        if (autorunApp != null) {
            runApp(autorunApp, mContext);
        }
        String brightSetting = prefs.getBrightness();
        if (!brightSetting.equals(InCar.BRIGHTNESS_DISABLED)) {
            adjustBrightness(brightSetting, mContext);
        }
    }

    private static void runApp(ComponentName autorunApp, Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN)
                .setComponent(autorunApp)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Utils.startActivitySafely(intent, context);
    }

    public void disable(InCar prefs) {
        if (prefs.isDisableScreenTimeout()) {
            ModeService.releaseWakeLock(mContext);
        }
        if (prefs.isAdjustVolumeLevel()) {
            restoreVolume(mContext);
        }
        if (prefs.isEnableBluetooth()) {
            restoreBluetooth();
        }
        if (prefs.getDisableWifi().equals(InCar.WIFI_TURNOFF)) {
            restoreWiFi(mContext);
        }
        if (prefs.isActivateCarMode()) {
            deactivateCarMode(mContext);
        }

        if (SamsungDrivingMode.hasMode() && prefs.isSamsungDrivingMode()) {
            SamsungDrivingMode.disable(mContext);
        }

        mScreenOrientation.set(ScreenOrientation.DISABLED);

        String brightSetting = prefs.getBrightness();
        if (!brightSetting.equals(InCar.BRIGHTNESS_DISABLED)) {
            restoreBrightness(brightSetting, mContext);
        }
    }

    private static void activateCarMode(Context context) {
        UiModeManager ui = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        ui.enableCarMode(0);
    }

    private static void deactivateCarMode(Context context) {
        UiModeManager ui = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        ui.disableCarMode(0);
    }

    private static void disableWifi(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        sCurrentWiFiState = wm.getWifiState();
        if (sCurrentWiFiState != WifiManager.WIFI_STATE_DISABLED) {
            wm.setWifiEnabled(false);
        }
    }

    private static void restoreWiFi(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (sCurrentWiFiState == WifiManager.WIFI_STATE_ENABLED) {
            wm.setWifiEnabled(true);
        }
    }

    private static void enableBluetooth() {
        sCurrentBtState = Bluetooth.getState();
        if (sCurrentBtState != BluetoothAdapter.STATE_ON) {
            Bluetooth.switchOn();
        }
    }

    private static void restoreBluetooth() {
        if (sCurrentBtState != BluetoothAdapter.STATE_ON) {
            Bluetooth.switchOff();
        }
    }

    private static void adjustBrightness(String brightSetting, Context context) {
        ContentResolver cr = context.getContentResolver();

        sCurrentBrightness = android.provider.Settings.System.getInt(cr,
                android.provider.Settings.System.SCREEN_BRIGHTNESS, BRIGHTNESS_MAX
        );
        sCurrentAutoBrightness = (android.provider.Settings.System.getInt(cr,
                android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
                android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        ) == android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);

        int newBrightLevel = -1;
        int newBrightMode = -1;
        if (InCar.BRIGHTNESS_AUTO.equals(brightSetting)) {
            if (!sCurrentAutoBrightness) {
                newBrightLevel = sCurrentBrightness;
                newBrightMode = android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
            }
        } else if (InCar.BRIGHTNESS_DAY.equals(brightSetting)) {
            newBrightLevel = BRIGHTNESS_DAY;
            newBrightMode = android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
        } else if (InCar.BRIGHTNESS_NIGHT.equals(brightSetting)) {
            newBrightLevel = BRIGHTNESS_NIGHT;
            newBrightMode = android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
        }

        if (newBrightLevel == -1) {
            AppLog.d("Wrong brightness setting Mode : " + brightSetting + " Level : "
                    + newBrightLevel);
            return;
        }

        android.provider.Settings.System
                .putInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, newBrightMode);
        android.provider.Settings.System
                .putInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS, newBrightLevel);

        sendBrightnessIntent(newBrightLevel, context);
    }

    private static boolean restoreBrightness(String brightSetting, Context context) {
        if (sCurrentAutoBrightness && InCar.BRIGHTNESS_AUTO.equals(brightSetting)) {
            return false;
        }
        int newBrightMode = android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
        if (sCurrentAutoBrightness) {
            newBrightMode = android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        }
        ContentResolver cr = context.getContentResolver();
        android.provider.Settings.System
                .putInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, newBrightMode);
        android.provider.Settings.System
                .putInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS, sCurrentBrightness);

        sendBrightnessIntent(sCurrentBrightness, context);
        return true;
    }


    protected static void adjustVolume(InCar prefs, Context context) {
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        int adjVolume = prefs.getMediaVolumeLevel();
        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int volume = (int) ((maxVolume * adjVolume) / 100);

        if (sCurrentMediaVolume == VOLUME_NOT_SET) {
            sCurrentMediaVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        }

        audio.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);

        int adjCallVolume = prefs.getCallVolumeLevel();
        int maxCallVolume = audio.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        int callVolume = (int) ((maxCallVolume * adjCallVolume) / 100);

        if (sCurrentCallVolume == VOLUME_NOT_SET) {
            sCurrentCallVolume = audio.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        }

        audio.setStreamVolume(AudioManager.STREAM_VOICE_CALL, callVolume, 0);
    }


    private static void restoreVolume(Context context) {
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (sCurrentMediaVolume != VOLUME_NOT_SET) {
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, sCurrentMediaVolume, 0);
            sCurrentMediaVolume = VOLUME_NOT_SET;
        }

        if (sCurrentCallVolume != VOLUME_NOT_SET) {
            audio.setStreamVolume(AudioManager.STREAM_VOICE_CALL, sCurrentCallVolume, 0);
            sCurrentCallVolume = VOLUME_NOT_SET;
        }

    }

    private static void sendBrightnessIntent(int newBrightLevel, Context context) {
        Intent intent = new Intent(context, ChangeBrightnessActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        float bt = (float) newBrightLevel / BRIGHTNESS_MAX;
        intent.putExtra(ChangeBrightnessActivity.EXTRA_BRIGHT_LEVEL, bt);
        context.startActivity(intent);
    }

}
