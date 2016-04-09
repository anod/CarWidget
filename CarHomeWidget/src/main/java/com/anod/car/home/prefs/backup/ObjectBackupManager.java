package com.anod.car.home.prefs.backup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.anod.car.home.model.AbstractShortcutsContainerModel;
import com.anod.car.home.model.NotificationShortcutsModel;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.model.ShortcutsContainerModel;
import com.anod.car.home.model.WidgetShortcutsModel;
import com.anod.car.home.prefs.model.WidgetStorage;
import com.anod.car.home.prefs.preferences.InCar;
import com.anod.car.home.prefs.preferences.InCarBackup;
import com.anod.car.home.prefs.preferences.ShortcutsMain;
import com.anod.car.home.utils.AppLog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;

public class ObjectBackupManager {

    public static final String FILE_EXT_DAT = ".dat";
    public static final String FILE_INCAR_DAT = "backup_incar.date";

    private final Context mContext;

    public ObjectBackupManager(Context context) {
        mContext = context;
    }

    public File getBackupIncarFile() {
        File saveDir = getBackupDir();
        return new File(saveDir, FILE_INCAR_DAT);
    }

    @SuppressLint("UseSparseArrays")
    private InCarBackup readInCarCompat(Object readObject) {
        if (readObject instanceof InCarBackup) {
            return (InCarBackup) readObject;
        }
        // InCar
        return new InCarBackup(new HashMap<Integer, ShortcutInfo>(), (InCar) readObject);
    }

    public int doRestoreMainLocal(String filepath, int appWidgetId) {
        if (!checkMediaReadable()) {
            return PreferencesBackupManager.ERROR_STORAGE_NOT_AVAILABLE;
        }

        File dataFile = new File(filepath);
        if (!dataFile.exists()) {
            return PreferencesBackupManager.ERROR_FILE_NOT_EXIST;
        }
        if (!dataFile.canRead()) {
            return PreferencesBackupManager.ERROR_FILE_READ;
        }

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(dataFile);
        } catch (FileNotFoundException e) {
            AppLog.d(e.getMessage());
            return PreferencesBackupManager.ERROR_FILE_READ;
        }

        return doRestoreMain(inputStream, appWidgetId);
    }

    public Integer doRestoreMainUri(Uri uri, int appWidgetId) {
        InputStream inputStream = null;
        try {
            inputStream = mContext.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            AppLog.d(e.getMessage());
            return PreferencesBackupManager.ERROR_FILE_READ;
        }
        return doRestoreMain(inputStream, appWidgetId);
    }

    public int doRestoreMain(final InputStream inputStream, int appWidgetId) {
        ShortcutsMain prefs = null;
        try {
            synchronized (PreferencesBackupManager.sLock) {
                ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(inputStream));
                prefs = (ShortcutsMain) is.readObject();
                is.close();
            }
        } catch (IOException e) {
            AppLog.ex(e);
            return PreferencesBackupManager.ERROR_FILE_READ;
        } catch (ClassNotFoundException e) {
            AppLog.ex(e);
            return PreferencesBackupManager.ERROR_DESERIALIZE;
        } catch (ClassCastException e) {
            AppLog.ex(e);
            return PreferencesBackupManager.ERROR_DESERIALIZE;
        }
      //  WidgetStorage.save(mContext, prefs.getMain(), appWidgetId);
        HashMap<Integer, ShortcutInfo> shortcuts = prefs.getShortcuts();
        // small check
        if (shortcuts.size() % 2 == 0) {
            WidgetStorage.saveLaunchComponentNumber(shortcuts.size(), mContext, appWidgetId);
        }
        ShortcutsContainerModel smodel = new WidgetShortcutsModel(mContext, appWidgetId);
        smodel.init();

        restoreShortcuts((AbstractShortcutsContainerModel) smodel, shortcuts);

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

    public int doRestoreInCarLocal() {
        if (!checkMediaReadable()) {
            return PreferencesBackupManager.ERROR_STORAGE_NOT_AVAILABLE;
        }

        File dataFile = new File(getBackupDir(), FILE_INCAR_DAT);
        if (!dataFile.exists()) {
            return PreferencesBackupManager.ERROR_FILE_NOT_EXIST;
        }
        if (!dataFile.canRead()) {
            return PreferencesBackupManager.ERROR_FILE_READ;
        }

        FileInputStream fis;
        try {
            fis = new FileInputStream(dataFile);
        } catch (FileNotFoundException e) {
            AppLog.d(e.getMessage());
            return PreferencesBackupManager.ERROR_FILE_READ;
        }
        return doRestoreInCar(fis);
    }

    public int doRestoreInCarUri(Uri uri) {
        InputStream inputStream;
        try {
            inputStream = mContext.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            AppLog.d(e.getMessage());
            return PreferencesBackupManager.ERROR_FILE_READ;
        }
        return doRestoreInCar(inputStream);
    }

    public int doRestoreInCar(final InputStream inputStream) {
        InCarBackup inCarBackup;
        try {
            synchronized (PreferencesBackupManager.sLock) {
                ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(inputStream));
                inCarBackup = readInCarCompat(is.readObject());
                is.close();
            }
        } catch (IOException e) {
            AppLog.ex(e);
            return PreferencesBackupManager.ERROR_FILE_READ;
        } catch (ClassNotFoundException e) {
            AppLog.ex(e);
            return PreferencesBackupManager.ERROR_DESERIALIZE;
        } catch (ClassCastException e) {
            AppLog.ex(e);
            return PreferencesBackupManager.ERROR_DESERIALIZE;
        }
        //version 1.42
        if (inCarBackup.getInCar().getAutoAnswer() == null || inCarBackup.getInCar().getAutoAnswer()
                .equals("")) {
            inCarBackup.getInCar().setAutoAnswer(InCar.AUTOANSWER_DISABLED);
        }

        NotificationShortcutsModel model = new NotificationShortcutsModel(mContext);
        model.init();

        HashMap<Integer, ShortcutInfo> shortcuts = inCarBackup.getNotificationShortcuts();
        restoreShortcuts(model, shortcuts);

        // TODO: Migrate
        //InCarStorage.saveInCar(mContext, inCarBackup.getInCar());
        return PreferencesBackupManager.RESULT_DONE;
    }


    public File getBackupDir() {
        File externalPath = Environment.getExternalStorageDirectory();
        return new File(externalPath.getAbsolutePath() + PreferencesBackupManager.DIR_BACKUP);
    }

    private boolean checkMediaReadable() {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY
                .equals(state)) {
            return false;
        }
        return true;
    }


}
