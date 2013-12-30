package com.anod.car.home.prefs.backup;

import android.annotation.SuppressLint;
import android.app.backup.BackupManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseArray;

import com.anod.car.home.model.AbstractShortcutsModel;
import com.anod.car.home.model.LauncherShortcutsModel;
import com.anod.car.home.model.NotificationShortcutsModel;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.model.ShortcutsModel;
import com.anod.car.home.prefs.preferences.InCar;
import com.anod.car.home.prefs.preferences.InCarBackup;
import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.prefs.preferences.ShortcutsMain;
import com.anod.car.home.utils.AppLog;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class PreferencesBackupManager {

	public static final int TYPE_MAIN = 1;
	public static final int TYPE_INCAR = 2;

	private static final String BACKUP_PACKAGE = "com.anod.car.home.pro";
	private static final String DIR_BACKUP = "/data/com.anod.car.home/backup";
	public static final String FILE_EXT_DAT = ".dat";
	public static final int RESULT_DONE = 0;
	public static final int ERROR_STORAGE_NOT_AVAILABLE = 1;
	public static final int ERROR_FILE_NOT_EXIST = 2;
	public static final int ERROR_FILE_READ = 3;
	public static final int ERROR_FILE_WRITE = 4;
	public static final int ERROR_DESERIALIZE = 5;

    private static final String BACKUP_MAIN_DIRNAME = "backup_main";
	public static final String FILE_INCAR_JSON = "backup_incar.dat";
	
	public static final int DATE_FORMAT = DateUtils.FORMAT_SHOW_DATE
			| DateUtils.FORMAT_SHOW_WEEKDAY
			| DateUtils.FORMAT_SHOW_TIME
			| DateUtils.FORMAT_SHOW_YEAR
			| DateUtils.FORMAT_ABBREV_ALL
		;
	/**
     * We serialize access to our persistent data through a global static
     * object.  This ensures that in the unlikely event of the our backup/restore
     * agent running to perform a backup while our UI is updating the file, the
     * agent will not accidentally read partially-written data.
     *
     * <p>Curious but true: a zero-length array is slightly lighter-weight than
     * merely allocating an Object, and can still be synchronized on.
     */
    static final Object[] DATA_LOCK = new Object[0];

    private final Context mContext;
    
    public PreferencesBackupManager(Context context) {
    	mContext = context;
    }
    
    public long getMainTime() {
    	File saveDir = getMainBackupDir();
    	if (!saveDir.isDirectory()) {
    		return 0;
    	}
    	String[] files = saveDir.list();
    	if (files.length == 0) {
    		return 0;
    	}
    	return saveDir.lastModified();
    }
    
    public File[] getMainBackups() {
    	File saveDir = getMainBackupDir();
    	if (!saveDir.isDirectory()) {
    		return null;
    	}
    	FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(FILE_EXT_DAT);
			}
		};
    	return saveDir.listFiles(filter);
    }
    
    public long getIncarTime() {
    	File dataFile = new File(getBackupDir(), FILE_INCAR_JSON);
    	if (!dataFile.exists()) {
    		return 0;
    	}
    	return dataFile.lastModified();
    }
    
	public int doBackupMain(String filename, int appWidgetId) {
        if (!checkMediaWritable()) {
        	return ERROR_STORAGE_NOT_AVAILABLE;
        }

        File saveDir = getMainBackupDir();
        if (!saveDir.exists()) {
        	saveDir.mkdirs();
        } 
        
        File dataFile = new File(saveDir, filename+FILE_EXT_DAT);
        
        ShortcutsModel smodel = new LauncherShortcutsModel(mContext, appWidgetId);
        smodel.init();
        Main main = PreferencesStorage.loadMain(mContext, appWidgetId);
        ShortcutsMain prefs = new ShortcutsMain(convertToHashMap(smodel.getShortcuts()), main);
        
        try {
            synchronized (PreferencesBackupManager.DATA_LOCK) {
            	FileOutputStream fos = new FileOutputStream(dataFile);
            	ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(prefs);
                oos.close();
                Log.d("CarHomeWidget",oos.toString());
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
  
		NotificationShortcutsModel model = new NotificationShortcutsModel(mContext);
		model.init();
		
        InCar prefs = PreferencesStorage.loadInCar(mContext);
        InCarBackup inCarBackup = new InCarBackup(convertToHashMap(model.getShortcuts()),prefs);
        try {
            synchronized (PreferencesBackupManager.DATA_LOCK) {
            	FileOutputStream fos = new FileOutputStream(dataFile);
            	ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(inCarBackup);
                oos.close();
            }
		} catch (IOException e) {
			AppLog.d(e.getMessage());
			return ERROR_FILE_WRITE;
		}
		BackupManager.dataChanged(BACKUP_PACKAGE);
		return RESULT_DONE;
	}
	
	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, ShortcutInfo> convertToHashMap(SparseArray<ShortcutInfo> sparseArray) {
		HashMap<Integer, ShortcutInfo> map = new HashMap<Integer, ShortcutInfo>(sparseArray.size());
		int key = 0;
		for(int i = 0; i < sparseArray.size(); i++) {
		   key = sparseArray.keyAt(i);
		   ShortcutInfo value = sparseArray.valueAt(i);
		   map.put(key, value);
		}
		return map;
	}
	
	
	@SuppressLint("UseSparseArrays")
	private InCarBackup readInCarCompat(Object readObject) {
		if (readObject instanceof InCarBackup) {
			return (InCarBackup)readObject;
		}
		// InCar
		return new InCarBackup(new HashMap<Integer, ShortcutInfo>(), (InCar)readObject);
	}

	public File getBackupMainFile(String filename) {
		File saveDir = getMainBackupDir();
		File dataFile = new File(saveDir, filename+FILE_EXT_DAT);
		return dataFile;
	}

	public int doRestoreMainLocal(String filepath, int appWidgetId) {
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

		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(dataFile);
		} catch (FileNotFoundException e) {
			AppLog.d(e.getMessage());
			return ERROR_FILE_READ;
		}

		return doRestoreMain(inputStream, appWidgetId);
	}

	public Integer doRestoreMainUri(Uri uri, int appWidgetId) {
		InputStream inputStream = null;
		try {
			inputStream = mContext.getContentResolver().openInputStream(uri);
		} catch (FileNotFoundException e) {
			AppLog.d(e.getMessage());
			return ERROR_FILE_READ;
		}
		return doRestoreMain(inputStream, appWidgetId);
	}

	public int doRestoreMain(final InputStream inputStream, int appWidgetId) {
		ShortcutsMain prefs = null;
		try {
			synchronized (PreferencesBackupManager.DATA_LOCK) {
				ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(inputStream));
				prefs = (ShortcutsMain) is.readObject();
				is.close();
			}
		} catch (IOException e) {
			AppLog.ex(e);
			return ERROR_FILE_READ;
		} catch (ClassNotFoundException e) {
			AppLog.ex(e);
			return ERROR_DESERIALIZE;
		} catch (ClassCastException e) {
			AppLog.ex(e);
			return ERROR_DESERIALIZE;
		}
		PreferencesStorage.saveMain(mContext, prefs.getMain(), appWidgetId);

		ShortcutsModel smodel = new LauncherShortcutsModel(mContext, appWidgetId);
		smodel.init();

		HashMap<Integer, ShortcutInfo> shortcuts = prefs.getShortcuts();
		restoreShortcuts((AbstractShortcutsModel) smodel, shortcuts);

		return RESULT_DONE;
	}

	private void restoreShortcuts(AbstractShortcutsModel model, HashMap<Integer, ShortcutInfo> shortcuts) {
		for (int pos=0;pos<model.getCount();pos++) {
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
			return ERROR_STORAGE_NOT_AVAILABLE;
		}

        File dataFile = new File(getBackupDir(), FILE_INCAR_JSON);
        if (!dataFile.exists()) {
        	return ERROR_FILE_NOT_EXIST;  
        }
        if (!dataFile.canRead()) {
        	return ERROR_FILE_READ;       	
        }

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(dataFile);
		} catch (FileNotFoundException e) {
			AppLog.d(e.getMessage());
			return ERROR_FILE_READ;
		}
		return doRestoreInCar(fis);
	}
	public int doRestoreInCarUri(Uri uri) {
		InputStream inputStream = null;
		try {
			inputStream = mContext.getContentResolver().openInputStream(uri);
		} catch (FileNotFoundException e) {
			AppLog.d(e.getMessage());
			return ERROR_FILE_READ;
		}
		return doRestoreInCar(inputStream);
	}

	public int doRestoreInCar(final InputStream inputStream) {
		InCarBackup inCarBackup = null;
		try {
			synchronized (PreferencesBackupManager.DATA_LOCK) {
				ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(inputStream));
				inCarBackup = readInCarCompat(is.readObject());
				is.close();
			}
		} catch (IOException e) {
			AppLog.ex(e);
			return ERROR_FILE_READ;
		} catch (ClassNotFoundException e) {
			AppLog.ex(e);
			return ERROR_DESERIALIZE;
		} catch (ClassCastException e) {
			AppLog.ex(e);
			return ERROR_DESERIALIZE;
		}
		//version 1.42
		if (inCarBackup.getInCar().getAutoAnswer() == null || inCarBackup.getInCar().getAutoAnswer().equals("")) {
			inCarBackup.getInCar().setAutoAnswer(InCar.AUTOANSWER_DISABLED);
		}

		NotificationShortcutsModel model = new NotificationShortcutsModel(mContext);
		model.init();

		HashMap<Integer, ShortcutInfo> shortcuts = inCarBackup.getNotificationShortcuts();
		restoreShortcuts((AbstractShortcutsModel) model, shortcuts);

		PreferencesStorage.saveInCar(mContext, inCarBackup.getInCar());
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
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
        	return false;        	
        }
        return true;
	}
	
	private boolean checkMediaReadable() {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
        	return false;
        }
        return true;
	}


}
