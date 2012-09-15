package com.anod.car.home.prefs.backup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.backup.BackupManager;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;

import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.model.ShortcutModel;
import com.anod.car.home.prefs.PreferencesStorage;
import com.anod.car.home.prefs.preferences.InCar;
import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.prefs.preferences.ShortcutsMain;

public class PreferencesBackupManager {
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
	
	/**
     * We serialize access to our persistent data through a global static
     * object.  This ensures that in the unlikely event of the our backup/restore
     * agent running to perform a backup while our UI is updating the file, the
     * agent will not accidentally read partially-written data.
     *
     * <p>Curious but true: a zero-length array is slightly lighter-weight than
     * merely allocating an Object, and can still be synchronized on.
     */
    static final Object[] sDataLock = new Object[0];

    private Context mContext;
    
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
        
        ShortcutModel smodel = new ShortcutModel(mContext, appWidgetId);
        smodel.init();
        Main main = PreferencesStorage.loadMain(mContext, appWidgetId);
        ShortcutsMain prefs = new ShortcutsMain(convertToHashMap(smodel.getShortcuts()), main);
        
        try {
            synchronized (PreferencesBackupManager.sDataLock) {
            	FileOutputStream fos = new FileOutputStream(dataFile);
            	ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(prefs);
                oos.close();
                Log.d("CarHomeWidget",oos.toString());
            }
		} catch (IOException e) {
			e.printStackTrace();
			return ERROR_FILE_WRITE;
		}
		saveDir.setLastModified(System.currentTimeMillis());
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
	
	public int doBackupInCar() {
        if (!checkMediaWritable()) {
        	return ERROR_STORAGE_NOT_AVAILABLE;
        }
        File saveDir = getBackupDir();
        if (!saveDir.exists()) {
        	saveDir.mkdirs();
        } 
        File dataFile = new File(saveDir, FILE_INCAR_JSON);
  
        InCar prefs = PreferencesStorage.loadInCar(mContext);
        try {
            synchronized (PreferencesBackupManager.sDataLock) {
            	FileOutputStream fos = new FileOutputStream(dataFile);
            	ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(prefs);
                oos.close();
            }
		} catch (IOException e) {
			e.printStackTrace();
			return ERROR_FILE_WRITE;
		}
		BackupManager.dataChanged(BACKUP_PACKAGE);
		return RESULT_DONE;
	}
	
	public int doRestoreInCar() {
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
        
        InCar prefs = null;
        try {
            synchronized (PreferencesBackupManager.sDataLock) {
            	FileInputStream fis = new FileInputStream(dataFile);
            	ObjectInputStream is = new ObjectInputStream(fis);
                prefs = (InCar) is.readObject();
                is.close();
            }
		} catch (IOException e) {
			e.printStackTrace();
			return ERROR_FILE_READ;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return ERROR_DESERIALIZE;
		}
		//version 1.42
		if (prefs.getAutoAnswer() == null || prefs.getAutoAnswer().equals("")) {
			prefs.setAutoAnswer(PreferencesStorage.AUTOANSWER_DISABLED);
		}
		PreferencesStorage.saveInCar(mContext, prefs);
		return RESULT_DONE;
	}

	public int doRestoreMain(String filename, int appWidgetId) {
		if (!checkMediaReadable()) {
			return ERROR_STORAGE_NOT_AVAILABLE;
		}
		
        File saveDir = getMainBackupDir();
        File dataFile = new File(saveDir, filename+FILE_EXT_DAT);
        if (!dataFile.exists()) {
        	return ERROR_FILE_NOT_EXIST;  
        }
        if (!dataFile.canRead()) {
        	return ERROR_FILE_READ;       	
        }
        
        ShortcutsMain prefs = null;
        try {
            synchronized (PreferencesBackupManager.sDataLock) {
            	FileInputStream fis = new FileInputStream(dataFile);
            	ObjectInputStream is = new ObjectInputStream(fis);
                prefs = (ShortcutsMain) is.readObject();
                is.close();
            }
		} catch (IOException e) {
			e.printStackTrace();
			return ERROR_FILE_READ;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
            return ERROR_DESERIALIZE;
        }
		PreferencesStorage.saveMain(mContext, prefs.getMain(), appWidgetId);
		
        ShortcutModel smodel = new ShortcutModel(mContext, appWidgetId);
        smodel.init();
		
        HashMap<Integer, ShortcutInfo> shortcuts = prefs.getShortcuts();
		
        for (int cellId=0;cellId<shortcuts.size();cellId++) {
        	smodel.dropShortcut(cellId, appWidgetId);
        	final ShortcutInfo info = shortcuts.get(cellId);
        	if (info != null) {
        		info.id = ShortcutInfo.NO_ID;
        		smodel.saveShortcut(cellId, info);
        	}
        }
        
		return RESULT_DONE;
	}
	
	public File getBackupDir() {
		File externalPath = Environment.getExternalStorageDirectory();
		return new File(externalPath.getAbsolutePath() + DIR_BACKUP);
	}
	
	private File getMainBackupDir() {
        StringBuilder sb = new StringBuilder(getBackupDir().getPath());
        sb.append(File.separator);
        sb.append(BACKUP_MAIN_DIRNAME);
        return new File(sb.toString());
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
