package com.anod.car.home.prefs.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.anod.car.home.model.AbstractShortcutsContainerModel;
import com.anod.car.home.model.NotificationShortcutsModel;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.model.ShortcutsContainerModel;
import com.anod.car.home.model.WidgetShortcutsModel;
import com.anod.car.home.prefs.backup.PreferencesBackupManager;
import com.anod.car.home.prefs.model.InCarSettings;
import com.anod.car.home.prefs.model.InCarStorage;
import com.anod.car.home.prefs.model.PrefsMigrate;
import com.anod.car.home.prefs.model.WidgetSettings;
import com.anod.car.home.prefs.model.WidgetStorage;
import com.anod.car.home.prefs.preferences.InCar;
import com.anod.car.home.prefs.preferences.InCarBackup;
import com.anod.car.home.prefs.preferences.ShortcutsMain;
import info.anodsplace.android.log.AppLog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;

public class ObjectRestoreManager {

    public static final String FILE_EXT_DAT = ".dat";
    public static final String FILE_INCAR_DAT = "backup_incar.dat";

    private final Context mContext;

    static final Object[] sLock = new Object[0];

    public ObjectRestoreManager(Context context) {
        mContext = context;
    }

    @SuppressLint("UseSparseArrays")
    private InCarBackup readInCarCompat(Object readObject) {
        if (readObject instanceof InCarBackup) {
            return (InCarBackup) readObject;
        }
        // InCar
        return new InCarBackup(new HashMap<Integer, ShortcutInfo>(), (InCar) readObject);
    }

    public int doRestoreMain(final InputStream inputStream, int appWidgetId) {
        ShortcutsMain prefs;
        try {
            synchronized (sLock) {
                ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(inputStream));
                prefs = (ShortcutsMain) is.readObject();
                is.close();
            }
        } catch (IOException e) {
            AppLog.e(e);
            return PreferencesBackupManager.ERROR_FILE_READ;
        } catch (ClassNotFoundException e) {
            AppLog.e(e);
            return PreferencesBackupManager.ERROR_DESERIALIZE;
        } catch (ClassCastException e) {
            AppLog.e(e);
            return PreferencesBackupManager.ERROR_DESERIALIZE;
        }

        Main main = prefs.getMain();
        WidgetSettings widget = WidgetStorage.load(mContext, appWidgetId);
        PrefsMigrate.migrateMain(widget, main);
        widget.apply();

        HashMap<Integer, ShortcutInfo> shortcuts = prefs.getShortcuts();
        // small check
        if (shortcuts.size() % 2 == 0) {
            WidgetStorage.saveLaunchComponentNumber(shortcuts.size(), mContext, appWidgetId);
        }
        WidgetShortcutsModel model = WidgetShortcutsModel.init(mContext, appWidgetId);
        restoreShortcuts(model, shortcuts);

        return PreferencesBackupManager.RESULT_DONE;
    }

    private void restoreShortcuts(AbstractShortcutsContainerModel model,
            HashMap<Integer, ShortcutInfo> shortcuts) {
        for (int pos = 0; pos < model.getCount(); pos++) {
            model.dropShortcut(pos);
            final ShortcutInfo info = shortcuts.get(pos);
            if (info != null) {
                info.id = ShortcutInfo.NO_ID;
                model.saveShortcut(pos, info);
            }
        }
    }

    public int doRestoreInCar(final InputStream inputStream) {
        InCarBackup inCarBackup;
        try {
            synchronized (sLock) {
                ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(inputStream));
                inCarBackup = readInCarCompat(is.readObject());
                is.close();
            }
        } catch (IOException e) {
            AppLog.e(e);
            return PreferencesBackupManager.ERROR_FILE_READ;
        } catch (ClassNotFoundException e) {
            AppLog.e(e);
            return PreferencesBackupManager.ERROR_DESERIALIZE;
        } catch (ClassCastException e) {
            AppLog.e(e);
            return PreferencesBackupManager.ERROR_DESERIALIZE;
        }
        //version 1.42
        if (inCarBackup.getInCar().getAutoAnswer() == null || inCarBackup.getInCar().getAutoAnswer()
                .equals("")) {
            inCarBackup.getInCar().setAutoAnswer(InCar.AUTOANSWER_DISABLED);
        }

        InCarSettings dest = InCarStorage.load(mContext);
        migrateIncar(dest, inCarBackup.getInCar());
        dest.apply();

        NotificationShortcutsModel model = NotificationShortcutsModel.init(mContext);

        HashMap<Integer, ShortcutInfo> shortcuts = inCarBackup.getNotificationShortcuts();
        restoreShortcuts(model, shortcuts);

        return PreferencesBackupManager.RESULT_DONE;
    }

    public static void migrateIncar(InCarSettings dest, InCar source)
    {
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
    }
}
