package com.anod.car.home.prefs.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.prefs.preferences.WidgetMigrateStorage;
import com.anod.car.home.utils.AppLog;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

/**
 * @author algavris
 * @date 19/03/2016.
 */
public class PrefsMigrate {

    private static final Object sLock = new Object();
    private static boolean sMigrated;

    public static InCarSettings migrate(Context context) {
        synchronized (sLock) {
            SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            InCarSettings source = new InCarSettings(defaultPrefs);

            SharedPreferences destPrefs = InCarStorage.getSharedPreferences(context);
            InCarSettings dest = new InCarSettings(destPrefs);

            dest.setAutorunApp(source.getAutorunApp());
            dest.setAdjustVolumeLevel(source.isAdjustVolumeLevel());
            dest.setActivateCarMode(source.isActivateCarMode());
            dest.setActivityRequired(source.isActivityRequired());
            dest.setAutoAnswer(source.getAutoAnswer());
            dest.setAutoSpeaker(source.isAutoSpeaker());
            dest.setBrightness(source.getBrightness());
            dest.setBtDevices(source.getBtDevices());
            dest.setCallVolumeLevel(source.getCallVolumeLevel());
            dest.setCarDockRequired(source.isCarDockRequired());
            dest.setDisableScreenTimeoutCharging(source.isDisableScreenTimeoutCharging());
            dest.setDisableScreenTimeout(source.isDisableScreenTimeout());
            dest.setDisableBluetoothOnPower(source.isDisableBluetoothOnPower());
            dest.setDisableWifi(source.getDisableWifi());
            dest.setEnableBluetooth(source.isEnableBluetooth());
            dest.setEnableBluetoothOnPower(source.isEnableBluetoothOnPower());
            dest.setHeadsetRequired(source.isHeadsetRequired());
            dest.setHotspotOn(source.isHotspotOn());
            dest.setInCarEnabled(source.isInCarEnabled());
            dest.setMediaVolumeLevel(source.getMediaVolumeLevel());
            dest.setPowerRequired(source.isPowerRequired());
            dest.setSamsungDrivingMode(source.isSamsungDrivingMode());
            dest.setScreenOrientation(source.getScreenOrientation());

            ArrayList<Long> ids = InCarStorage.getNotifComponents(defaultPrefs);
            for (int position = 0; position < ids.size(); position++) {
                String key = InCarStorage.getNotifComponentName(position);
                dest.putChange(key, ids.get(position));
            }

            dest.apply();
            sMigrated = true;

            return dest;
        }
    }

    public static boolean required(Context context) {
        synchronized (sLock) {
            if (sMigrated) {
                return false;
            }

            String filePath = context.getFilesDir().getParent() +
                    String.format(Locale.US, WidgetStorage.SHARED_PREFS_PATH, InCarStorage.PREF_NAME);
            AppLog.d(filePath);
            File file = new File(filePath);
            return !file.exists();
        }
    }

    public static WidgetSettings migrate(Context context, int appWidgetId) {
        synchronized (sLock) {
            Main prefs = WidgetMigrateStorage.loadMain(context, appWidgetId);
            WidgetSettings widget = new WidgetSettings(WidgetStorage.getSharedPreferences(context, appWidgetId), context.getResources());

            widget.setFirstTime(WidgetMigrateStorage.isFirstTime(context, appWidgetId));
            widget.setSkin(prefs.getSkin());
            widget.setTitlesHide(prefs.isTitlesHide());

            widget.setFontColor(prefs.getFontColor());
            widget.setFontSize(prefs.getFontSize());

            widget.setBackgroundColor(prefs.getBackgroundColor());
            widget.setTileColor(prefs.getTileColor());

            widget.setIconsColor(prefs.getIconsColor());
            widget.setIconsMono(prefs.isIconsMono());
            widget.setIconsRotate(prefs.getIconsRotate());
            widget.setIconsScaleString(prefs.getIconsScale());
            widget.setIconsTheme(prefs.getIconsTheme());

            widget.setIncarTransparent(prefs.isIncarTransparent());
            widget.setSettingsTransparent(prefs.isSettingsTransparent());

            widget.setWidgetButton1(prefs.getWidgetButton1());
            widget.setWidgetButton2(prefs.getWidgetButton2());

            int count = WidgetMigrateStorage.getLaunchComponentNumber(context, appWidgetId);
            ArrayList<Long> ids = WidgetMigrateStorage.getLauncherComponents(context, appWidgetId, count);
            for (int position = 0; position < ids.size(); position++) {
                String key = WidgetStorage.getLaunchComponentKey(position);
                widget.putChange(key, ids.get(position));
            }

            widget.apply();

            return widget;
        }
    }

}
