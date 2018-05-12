package com.anod.car.home.prefs.preferences;

import android.annotation.SuppressLint;
import android.content.Context;

import com.anod.car.home.model.AbstractShortcutsContainerModel;
import com.anod.car.home.model.NotificationShortcutsModel;
import com.anod.car.home.model.Shortcut;
import com.anod.car.home.model.ShortcutIcon;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.model.WidgetShortcutsModel;
import com.anod.car.home.backup.PreferencesBackupManager;
import com.anod.car.home.prefs.model.InCarSettings;
import com.anod.car.home.prefs.model.InCarStorage;
import com.anod.car.home.prefs.model.PrefsMigrate;
import com.anod.car.home.prefs.model.WidgetSettings;
import com.anod.car.home.prefs.model.WidgetStorage;

import info.anodsplace.framework.AppLog;

import java.io.BufferedInputStream;
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
        return new InCarBackup(new HashMap<>(), (InCar) readObject);
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
            AppLog.Companion.e(e);
            return PreferencesBackupManager.ERROR_FILE_READ;
        } catch (ClassNotFoundException | ClassCastException e) {
            AppLog.Companion.e(e);
            return PreferencesBackupManager.ERROR_DESERIALIZE;
        }

        Main main = prefs.getMain();
        WidgetSettings widget = WidgetStorage.INSTANCE.load(mContext, appWidgetId);
        PrefsMigrate.migrateMain(widget, main);
        widget.apply();

        HashMap<Integer, ShortcutInfo> shortcuts = prefs.getShortcuts();
        // small check
        if (shortcuts.size() % 2 == 0) {
            WidgetStorage.INSTANCE.saveLaunchComponentNumber(shortcuts.size(), mContext, appWidgetId);
        }
        WidgetShortcutsModel model = WidgetShortcutsModel.Companion.init(mContext, appWidgetId);
        restoreShortcuts(model, shortcuts);

        return PreferencesBackupManager.RESULT_DONE;
    }

    private void restoreShortcuts(AbstractShortcutsContainerModel model, HashMap<Integer, ShortcutInfo> shortcuts) {
        for (int pos = 0; pos < model.getCount(); pos++) {
            model.dropShortcut(pos);
            final ShortcutInfo info = shortcuts.get(pos);
            if (info != null) {
                Shortcut newInfo = new Shortcut(ShortcutInfo.NO_ID, info.itemType, info.title, info.isCustomIcon(), info.intent);
                ShortcutIcon newIcon = new ShortcutIcon(ShortcutInfo.NO_ID, info.isCustomIcon(), info.isUsingFallbackIcon(), info.getIconResource(), info.getIcon());

                model.saveShortcut(pos, newInfo, newIcon);
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
            AppLog.Companion.e(e);
            return PreferencesBackupManager.ERROR_FILE_READ;
        } catch (ClassNotFoundException | ClassCastException e) {
            AppLog.Companion.e(e);
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

    static void migrateIncar(InCarSettings dest, InCar source) {
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
