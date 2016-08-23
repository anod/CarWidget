package com.anod.car.home.backup;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.SparseArray;

import com.anod.car.home.model.AbstractShortcutsContainerModel;
import com.anod.car.home.model.NotificationShortcutsModel;
import com.anod.car.home.model.Shortcut;
import com.anod.car.home.model.ShortcutsContainerModel;
import com.anod.car.home.model.WidgetShortcutsModel;
import com.anod.car.home.prefs.model.InCarSettings;
import com.anod.car.home.prefs.model.InCarStorage;
import com.anod.car.home.prefs.model.WidgetSettings;
import com.anod.car.home.prefs.model.WidgetStorage;
import com.anod.car.home.prefs.preferences.ObjectRestoreManager;
import info.anodsplace.android.log.AppLog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class PreferencesBackupManager {

    public static final int TYPE_MAIN = 1;

    public static final int TYPE_INCAR = 2;

    private static final String BACKUP_PACKAGE = "com.anod.car.home.pro";

    static final String DIR_BACKUP = "/data/com.anod.car.home/backup";

    public static final String FILE_EXT_JSON = ".json";

    public static final int RESULT_DONE = 0;

    public static final int ERROR_STORAGE_NOT_AVAILABLE = 1;

    public static final int ERROR_FILE_NOT_EXIST = 2;

    public static final int ERROR_FILE_READ = 3;

    public static final int ERROR_FILE_WRITE = 4;

    public static final int ERROR_DESERIALIZE = 5;

    public static final int ERROR_UNEXPECTED = 6;

    private static final String BACKUP_MAIN_DIRNAME = "backup_main";

    public static final String FILE_INCAR_JSON = "backup_incar.json";

    static final Object[] sLock = new Object[0];

    private final Context mContext;

    public PreferencesBackupManager(Context context) {
        mContext = context;
    }

    public File[] getMainBackups() {
        File saveDir = getMainBackupDir();
        if (!saveDir.isDirectory()) {
            return null;
        }
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(FILE_EXT_JSON) || filename.endsWith(ObjectRestoreManager.FILE_EXT_DAT);
            }
        };
        return saveDir.listFiles(filter);
    }

    public long getIncarTime() {
        File dataFile = new File(getBackupDir(), FILE_INCAR_JSON);
        if (!dataFile.exists()) {
            dataFile = new File(getBackupDir(), ObjectRestoreManager.FILE_INCAR_DAT);
            if (!dataFile.exists()) {
                return 0;
            }
        }
        return dataFile.lastModified();
    }

    public int doBackupWidget(String filename, int appWidgetId) {
        if (!checkMediaWritable()) {
            return ERROR_STORAGE_NOT_AVAILABLE;
        }

        File saveDir = getMainBackupDir();
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        File dataFile = new File(saveDir, filename + FILE_EXT_JSON);

        ShortcutsContainerModel model = WidgetShortcutsModel.init(mContext, appWidgetId);
        WidgetSettings widget = WidgetStorage.load(mContext, appWidgetId);
        try {
            synchronized (sLock)
            {
                FileOutputStream fos = new FileOutputStream(dataFile);
                JsonWriter writer = new JsonWriter(new OutputStreamWriter(fos));

                writer.beginObject();

                JsonWriter settingsWriter = writer.name("settings");
                widget.writeJson(settingsWriter);

                JsonWriter arrayWriter = writer.name("shortcuts").beginArray();
                ShortcutsJsonWriter shortcutsJsonWriter = new ShortcutsJsonWriter();
                shortcutsJsonWriter.writeList(arrayWriter, model.getShortcuts(), model);
                arrayWriter.endArray();

                writer.endObject();
                writer.close();
            }
        } catch (IOException e) {
            AppLog.d(e.getMessage());
            return ERROR_FILE_WRITE;
        }
        saveDir.setLastModified(System.currentTimeMillis());
        return RESULT_DONE;
    }

    public File getBackupIncarFile() {
        File saveDir = getBackupDir();
        return new File(saveDir, FILE_INCAR_JSON);
    }

    public int doBackupInCar() {
        if (!checkMediaWritable()) {
            return ERROR_STORAGE_NOT_AVAILABLE;
        }
        File saveDir = getBackupDir();
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        File dataFile = getBackupIncarFile();

        NotificationShortcutsModel model = NotificationShortcutsModel.init(mContext);

        InCarSettings prefs = InCarStorage.load(mContext);

        try {
            synchronized (sLock) {
                FileOutputStream fos = new FileOutputStream(dataFile);
                JsonWriter writer = new JsonWriter(new OutputStreamWriter(fos));
                writer.beginObject();

                JsonWriter settingsWriter = writer.name("settings");
                prefs.writeJson(settingsWriter);

                JsonWriter arrayWriter = writer.name("shortcuts").beginArray();
                ShortcutsJsonWriter shortcutsJsonWriter = new ShortcutsJsonWriter();
                shortcutsJsonWriter.writeList(arrayWriter, model.getShortcuts(), model);
                arrayWriter.endArray();

                writer.endObject();
                writer.close();
            }
        } catch (IOException e) {
            AppLog.e(e);
            return ERROR_FILE_WRITE;
        }
        BackupManager.dataChanged(BACKUP_PACKAGE);
        return RESULT_DONE;
    }

    public File getBackupWidgetFile(String filename) {
        File saveDir = getMainBackupDir();
        File dataFile = new File(saveDir, filename);
        return dataFile;
    }

    public int doRestoreWidgetLocal(String filepath, int appWidgetId) {
        if (!checkMediaReadable()) {
            return ERROR_STORAGE_NOT_AVAILABLE;
        }

        File dataFile = new File(filepath);
        if (!dataFile.exists()) {
            return ERROR_FILE_NOT_EXIST;
        }
        if (!dataFile.canRead()) {
            return ERROR_FILE_READ;
        }

        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(dataFile);
        } catch (FileNotFoundException e) {
            AppLog.d(e.getMessage());
            return ERROR_FILE_READ;
        }

        int pos = dataFile.getName().lastIndexOf('.');
        if (pos > 0) {
            String extension = dataFile.getName().substring(pos);
            if (extension.equals(ObjectRestoreManager.FILE_EXT_DAT))
            {
                ObjectRestoreManager objectRestore = new ObjectRestoreManager(mContext);
                return objectRestore.doRestoreMain(inputStream, appWidgetId);
            }
        }

        return doRestoreWidget(inputStream, appWidgetId);
    }

    public Integer doRestoreWidgetUri(Uri uri, int appWidgetId) {
        InputStream inputStream = null;
        try {
            inputStream = mContext.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            AppLog.d(e.getMessage());
            return ERROR_FILE_READ;
        }
        return doRestoreWidget(inputStream, appWidgetId);
    }

    public int doRestoreWidget(final InputStream inputStream, int appWidgetId) {
        SharedPreferences sharedPrefs = WidgetStorage.getSharedPreferences(mContext, appWidgetId);
        sharedPrefs.edit().clear().apply();
        WidgetSettings widget = new WidgetSettings(sharedPrefs, mContext.getResources());

        ShortcutsJsonReader shortcutsJsonReader = new ShortcutsJsonReader(mContext);
        SparseArray<ShortcutsJsonReader.ShortcutWithIconAndPosition> shortcuts = new SparseArray<>();

        try {
            synchronized (sLock) {
                JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(inputStream)));
                reader.beginObject();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    if (name.equals("settings")) {
                        widget.readJson(reader);
                    } else if (name.equals("shortcuts")) {
                        shortcuts = shortcutsJsonReader.readList(reader);
                    } else {
                        reader.skipValue();
                    }
                }
                reader.endObject();
                reader.close();
            }
        } catch (IOException e) {
            AppLog.e(e);
            return ERROR_FILE_READ;
        } catch (ClassCastException e) {
            AppLog.e(e);
            return ERROR_DESERIALIZE;
        }
        widget.apply();
        // small check
        if (shortcuts.size() % 2 == 0) {
            WidgetStorage.saveLaunchComponentNumber(shortcuts.size(), mContext, appWidgetId);
        }
        WidgetShortcutsModel model = WidgetShortcutsModel.init(mContext, appWidgetId);

        restoreShortcuts(model, shortcuts);

        return RESULT_DONE;
    }

    private void restoreShortcuts(AbstractShortcutsContainerModel model, SparseArray<ShortcutsJsonReader.ShortcutWithIconAndPosition> shortcuts) {
        for (int pos = 0; pos < model.getCount(); pos++) {
            model.dropShortcut(pos);
            final ShortcutsJsonReader.ShortcutWithIconAndPosition shortcut = shortcuts.get(pos);
            if (shortcut.icon != null && shortcut.info != null) {
                Shortcut info = new Shortcut(Shortcut.NO_ID, shortcut.info);
                model.saveShortcut(pos, info, shortcut.icon);
            }
        }
    }

    public int doRestoreInCarLocal(String filepath) {
        if (!checkMediaReadable()) {
            return ERROR_STORAGE_NOT_AVAILABLE;
        }

        File dataFile = new File(filepath);
        if (!dataFile.exists()) {
            return ERROR_FILE_NOT_EXIST;
        }
        if (!dataFile.canRead()) {
            return ERROR_FILE_READ;
        }

        FileInputStream fis;
        try {
            fis = new FileInputStream(dataFile);
        } catch (FileNotFoundException e) {
            AppLog.d(e.getMessage());
            return ERROR_FILE_READ;
        }

        int pos = dataFile.getName().lastIndexOf('.');
        if (pos > 0) {
            String extension = dataFile.getName().substring(pos);
            if (extension.equals(ObjectRestoreManager.FILE_EXT_DAT)) {
                ObjectRestoreManager objectRestore = new ObjectRestoreManager(mContext);
                return objectRestore.doRestoreInCar(fis);
            }
        }

        return doRestoreInCar(fis);
    }

    public int doRestoreInCarUri(Uri uri) {
        InputStream inputStream;
        try {
            inputStream = mContext.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            AppLog.d(e.getMessage());
            return ERROR_FILE_READ;
        }
        return doRestoreInCar(inputStream);
    }

    public int doRestoreInCar(final InputStream inputStream) {
        SharedPreferences sharedPrefs = InCarStorage.getSharedPreferences(mContext);
        sharedPrefs.edit().clear().apply();
        InCarSettings incar = new InCarSettings(sharedPrefs);

        ShortcutsJsonReader shortcutsJsonReader = new ShortcutsJsonReader(mContext);
        SparseArray<ShortcutsJsonReader.ShortcutWithIconAndPosition> shortcuts = new SparseArray<>();

        try {
            synchronized (sLock) {
                JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(inputStream)));
                reader.beginObject();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    if (name.equals("settings")) {
                        incar.readJson(reader);
                    } else if (name.equals("shortcuts")) {
                        shortcuts = shortcutsJsonReader.readList(reader);
                    } else {
                        reader.skipValue();
                    }
                }
                reader.endObject();
                reader.close();
            }
        } catch (IOException e) {
            AppLog.e(e);
            return ERROR_FILE_READ;
        } catch (ClassCastException e) {
            AppLog.e(e);
            return ERROR_DESERIALIZE;
        }

        NotificationShortcutsModel model = NotificationShortcutsModel.init(mContext);

        incar.apply();
        restoreShortcuts(model, shortcuts);

        return RESULT_DONE;
    }


    public File getBackupDir() {
        File externalPath = Environment.getExternalStorageDirectory();
        return new File(externalPath.getAbsolutePath() + DIR_BACKUP);
    }

    private File getMainBackupDir() {
        return new File(getBackupDir().getPath() + File.separator + BACKUP_MAIN_DIRNAME);
    }

    private boolean checkMediaWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY
                .equals(state);
    }

    private boolean checkMediaReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }


}
