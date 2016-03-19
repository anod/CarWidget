package com.anod.car.home.prefs.preferences;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.anod.car.home.incar.ScreenOrientation;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.utils.AppLog;
import com.anod.car.home.utils.Utils;

import java.util.ArrayList;
import java.util.Locale;

/**
 * @author algavris
 * @date 19/03/2016.
 */
@SuppressLint("CommitPrefEdits")
public class InCarStorage {

    public static final int NOTIFICATION_COMPONENT_NUMBER = 3;
    private static final String MODE_FORCE_STATE = "mode-force-state";
    private static final String NOTIF_COMPONENT = "notif-component-%d";
    public static final String INCAR_MODE_ENABLED = "incar-mode-enabled";
    public static final String POWER_BT_ENABLE = "power-bt-enable";
    public static final String POWER_BT_DISABLE = "power-bt-disable";
    public static final String HEADSET_REQUIRED = "headset-required";
    public static final String POWER_REQUIRED = "power-required";
    public static final String BLUETOOTH_DEVICE_ADDRESSES = "bt-device-addresses";
    public static final String SCREEN_TIMEOUT = "screen-timeout";
    public static final String SCREEN_TIMEOUT_CHARGING = "screen-timeout-charging";
    public static final String BRIGHTNESS = "brightness";
    public static final String BLUETOOTH = "bluetooth";
    public static final String ADJUST_VOLUME_LEVEL = "adjust-volume-level";
    public static final String MEDIA_VOLUME_LEVEL = "volume-level";
    public static final String AUTO_SPEAKER = "auto_speaker";
    public static final String AUTO_ANSWER = "auto_answer";
    public static final String ADJUST_WIFI = "wi-fi";
    public static final String ACTIVATE_CAR_MODE = "activate-car-mode";
    public static final String AUTORUN_APP = "autorun-app";
    public static final String CALL_VOLUME_LEVEL = "call-volume-level";
    public static final String ACTIVITY_RECOGNITION = "activity-recognition";
    public static final String SAMSUNG_DRIVING_MODE = "sam_driving_mode";
    public static final String SCREEN_ORIENTATION = "screen-orientation";
    public static final String CAR_DOCK_REQUIRED = "car-dock";
    public static final String HOTSPOT = "hotspot";

    static final String PREF_NAME = "incar";

    static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static InCar loadInCar(Context context) {
        if (InCarMigrate.required(context)) {
            return InCarMigrate.migrate(context);
        }

        return loadInCar(getSharedPreferences(context));
    }

    public static boolean isInCarModeEnabled(Context context) {
        return isInCarModeEnabled(getSharedPreferences(context));
    }

    public static void saveInCar(Context context, InCar prefs) {
        saveInCar(getSharedPreferences(context), prefs);
    }

    public static void saveBtDevices(Context context, ArrayMap<String, String> devices) {
        saveBtDevices(getSharedPreferences(context), devices);
    }

    public static boolean isAdjustVolumeLevel(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getBoolean(ADJUST_VOLUME_LEVEL, false);
    }

    public static void saveScreenTimeout(boolean disabled, boolean disableCharging,
                                         Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(SCREEN_TIMEOUT, disabled);
        editor.putBoolean(SCREEN_TIMEOUT_CHARGING, disableCharging);
        editor.commit();
    }

    public static ArrayMap<String, String> getBtDevices(Context context) {
        return getBtDevices(getSharedPreferences(context));
    }

    public static boolean isActivityRecognitionEnabled(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getBoolean(ACTIVITY_RECOGNITION, false);
    }

    public static String getNotifComponentName(int position) {
        return String.format(Locale.US, NOTIF_COMPONENT, position);
    }

    public static boolean restoreForceState(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getBoolean(MODE_FORCE_STATE, false);
    }

    public static void saveForceState(Context context, boolean forceState) {
        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(MODE_FORCE_STATE, forceState);
        editor.commit();
    }

    public static void setAdjustVolumeLevel(Context context, boolean isChecked) {
        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(ADJUST_VOLUME_LEVEL, isChecked);
        editor.commit();
    }

    public static void saveAutorunApp(ComponentName component, Context context) {
        SharedPreferences p = getSharedPreferences(context);
        SharedPreferences.Editor editor =
                p.edit();

        if (component == null) {
            editor.remove(AUTORUN_APP);
        } else {
            String autorunAppString = Utils.componentToString(component);
            editor.putString(AUTORUN_APP, autorunAppString);
        }
        editor.commit();
    }

    public static void dropNotifShortcut(int position, Context context) {
        String key = getNotifComponentName(position);
        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(key);
        editor.commit();
    }

    public static ArrayList<Long> getNotifComponents(Context context) {
        return getNotifComponents(getSharedPreferences(context));
    }
    public static void saveNotifShortcut(Context context, long shortcutId, int position) {
        saveNotifShortcut(context, getSharedPreferences(context), shortcutId, position);
    }

    static InCar loadInCar(SharedPreferences prefs) {
        boolean incarEnabled = isInCarModeEnabled(prefs);

        InCar p = new InCar();

        p.setInCarEnabled(incarEnabled);
        p.setDisableBluetoothOnPower(prefs.getBoolean(POWER_BT_DISABLE, false));
        p.setEnableBluetoothOnPower(prefs.getBoolean(POWER_BT_ENABLE, false));

        p.setPowerRequired(prefs.getBoolean(POWER_REQUIRED, false));
        p.setBtDevices(getBtDevices(prefs));
        p.setHeadsetRequired(prefs.getBoolean(HEADSET_REQUIRED, false));

        p.setAutoSpeaker(prefs.getBoolean(AUTO_SPEAKER, false));
        p.setEnableBluetooth(prefs.getBoolean(BLUETOOTH, false));
        boolean disableScreenTimeout = prefs.getBoolean(SCREEN_TIMEOUT, true);
        p.setDisableScreenTimeout(disableScreenTimeout);
        if (disableScreenTimeout) {
            p.setDisableScreenTimeoutCharging(prefs.getBoolean(SCREEN_TIMEOUT_CHARGING, false));
        }

        p.setBrightness(prefs.getString(BRIGHTNESS, InCar.BRIGHTNESS_DISABLED));
        p.setAdjustVolumeLevel(prefs.getBoolean(ADJUST_VOLUME_LEVEL, true));
        p.setMediaVolumeLevel(prefs.getInt(MEDIA_VOLUME_LEVEL, InCar.DEFAULT_VOLUME_LEVEL));
        p.setCallVolumeLevel(prefs.getInt(CALL_VOLUME_LEVEL, InCar.DEFAULT_VOLUME_LEVEL));
        p.setDisableWifi(prefs.getString(ADJUST_WIFI, InCar.WIFI_NOACTION));
        p.setActivateCarMode(prefs.getBoolean(ACTIVATE_CAR_MODE, false));
        p.setAutoAnswer(prefs.getString(AUTO_ANSWER, InCar.AUTOANSWER_DISABLED));

        p.setActivityRequired(prefs.getBoolean(ACTIVITY_RECOGNITION, false));
        p.setCarDockRequired(prefs.getBoolean(CAR_DOCK_REQUIRED, false));
        String autorunAppString = prefs.getString(AUTORUN_APP, null);

        ComponentName autorunApp = null;
        if (autorunAppString != null) {
            autorunApp = Utils.stringToComponent(autorunAppString);
        }
        p.setAutorunApp(autorunApp);

        p.setSamsungDrivingMode(prefs.getBoolean(SAMSUNG_DRIVING_MODE, false));

        String orientation = prefs
                .getString(SCREEN_ORIENTATION, String.valueOf(ScreenOrientation.DISABLED));
        p.setScreenOrientation(Integer.parseInt(orientation));

        p.setHotspotOn(prefs.getBoolean(HOTSPOT, false));

        return p;
    }

    static void saveInCar(SharedPreferences p, InCar prefs) {
        final SharedPreferences.Editor editor = p.edit();

        editor.putBoolean(INCAR_MODE_ENABLED, prefs.isInCarEnabled());
        editor.putBoolean(POWER_BT_DISABLE, prefs.isDisableBluetoothOnPower());
        editor.putBoolean(POWER_BT_ENABLE, prefs.isEnableBluetoothOnPower());

        editor.putBoolean(POWER_REQUIRED, prefs.isPowerRequired());
        editor.putBoolean(HEADSET_REQUIRED, prefs.isHeadsetRequired());

        editor.putBoolean(AUTO_SPEAKER, prefs.isAutoSpeaker());
        editor.putBoolean(BLUETOOTH, prefs.isEnableBluetooth());
        boolean disableScreenTimeout = prefs.isDisableScreenTimeout();
        editor.putBoolean(SCREEN_TIMEOUT, disableScreenTimeout);
        if (disableScreenTimeout) {
            editor.putBoolean(SCREEN_TIMEOUT_CHARGING, prefs.isDisableScreenTimeoutCharging());
        } else {
            editor.putBoolean(SCREEN_TIMEOUT_CHARGING, false);
        }
        editor.putString(BRIGHTNESS, prefs.getBrightness());
        editor.putBoolean(ADJUST_VOLUME_LEVEL, prefs.isAdjustVolumeLevel());
        editor.putInt(MEDIA_VOLUME_LEVEL, prefs.getMediaVolumeLevel());
        editor.putInt(CALL_VOLUME_LEVEL, prefs.getCallVolumeLevel());
        editor.putString(ADJUST_WIFI, prefs.getDisableWifi());
        editor.putBoolean(ACTIVATE_CAR_MODE, prefs.isActivateCarMode());
        editor.putString(AUTO_ANSWER, prefs.getAutoAnswer());

        editor.putBoolean(ACTIVITY_RECOGNITION, prefs.isActivityRequired());
        ComponentName autorunApp = prefs.getAutorunApp();
        if (autorunApp == null) {
            editor.remove(AUTORUN_APP);
        } else {
            String autorunAppString = Utils.componentToString(autorunApp);
            editor.putString(AUTORUN_APP, autorunAppString);
        }

        editor.putBoolean(SAMSUNG_DRIVING_MODE, prefs.isSamsungDrivingMode());

        editor.putString(SCREEN_ORIENTATION, String.valueOf(prefs.getScreenOrientation()));

        editor.putBoolean(HOTSPOT, prefs.isHotspotOn());
        editor.commit();
        saveBtDevices(p, prefs.getBtDevices());
    }

    static boolean isInCarModeEnabled(SharedPreferences prefs) {
        return prefs.getBoolean(INCAR_MODE_ENABLED, false);
    }

    private static ArrayMap<String, String> getBtDevices(SharedPreferences prefs) {
        String addrStr = prefs.getString(BLUETOOTH_DEVICE_ADDRESSES, null);
        if (addrStr == null) {
            return null;
        }
        String[] addrs = addrStr.split(",");
        ArrayMap<String, String> devices = new ArrayMap<String, String>(addrs.length);
        for (int i = 0; i < addrs.length; i++) {
            String addr = addrs[i];
            devices.put(addr, addr);
        }
        return devices;
    }

    static void saveBtDevices(SharedPreferences prefs, ArrayMap<String, String> devices) {
        SharedPreferences.Editor editor = prefs.edit();
        if (devices == null || devices.isEmpty()) {
            editor.remove(BLUETOOTH_DEVICE_ADDRESSES);
        } else {
            String addrStr = TextUtils.join(",", devices.values());
            AppLog.d(addrStr);
            editor.putString(BLUETOOTH_DEVICE_ADDRESSES, addrStr);
        }
        editor.commit();
    }

    static void saveNotifShortcut(Context context, SharedPreferences prefs, long shortcutId, int position) {
        String key = getNotifComponentName(position);
        PreferencesStorage.saveShortcutId(context, prefs, shortcutId, key);
    }

    static ArrayList<Long> getNotifComponents(SharedPreferences prefs) {
        ArrayList<Long> ids = new ArrayList<Long>(NOTIFICATION_COMPONENT_NUMBER);
        for (int i = 0; i < NOTIFICATION_COMPONENT_NUMBER; i++) {
            String key = getNotifComponentName(i);
            long id = prefs.getLong(key, ShortcutInfo.NO_ID);
            ids.add(i, id);
        }
        return ids;
    }

}
