package com.anod.car.home.prefs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.anod.car.home.prefs.preferences.InCar;
import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.prefs.preferences.ShortcutsMain;
import com.jsonobjectserialization.JSONInputStream;
import com.jsonobjectserialization.JSONOutputStream;
import com.jsonobjectserialization.JSONStreamException;

public class BackupManager {
	public static final int RESULT_DONE = 0;
	public static final int ERROR_STORAGE_NOT_AVAILABLE = 1;
	public static final int ERROR_FILE_NOT_EXIST = 2;
	public static final int ERROR_FILE_READ = 3;
	public static final int ERROR_FILE_WRITE = 4;
	public static final int ERROR_DESERIALIZE = 5;

    private static final String BACKUP_MAIN_DIRNAME = "backup_main";
	private static final String FILE_INCAR_JSON = "backup_incar.json";
	
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
    
    public BackupManager(Context context) {
    	mContext = context;
    }
    
    public long getMainTime() {
    	File saveDir = getMainBackupDir();
    	if (!saveDir.isDirectory()) {
    		return 0;
    	}
    	return saveDir.lastModified();
    }
    
    public long getIncarTime() {
    	File dataFile = new File(mContext.getExternalFilesDir(null), FILE_INCAR_JSON);
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
        
        File dataFile = new File(saveDir, filename);
        
        ShortcutModel smodel = new ShortcutModel(mContext, appWidgetId);
        smodel.init();
        Main main = PreferencesStorage.loadMain(mContext, appWidgetId);
        ShortcutsMain prefs = new ShortcutsMain(smodel.getShortcuts(), main);
        
        try {
            RandomAccessFile file = new RandomAccessFile(dataFile, "rw");
            synchronized (BackupManager.sDataLock) {
    	        // Serializing it to JSON string
    	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	        JSONOutputStream jos = new JSONOutputStream(baos);
    	        jos.writeObject(prefs);
    	        jos.close();
    	        
    	        // Get the string form the output stream and print it
    	        String jsonString = baos.toString();
            	Log.d("CarHomeWidget","jsonString: " + jsonString);
    	        file.writeUTF(jsonString);
            }
		} catch (IOException e) {
			e.printStackTrace();
			return ERROR_FILE_WRITE;
		}
		saveDir.setLastModified(System.currentTimeMillis());
		return RESULT_DONE;
	}
	
	public int doBackupInCar() {
        if (!checkMediaWritable()) {
        	return ERROR_STORAGE_NOT_AVAILABLE;
        }

        File dataFile = new File(mContext.getExternalFilesDir(null), FILE_INCAR_JSON);
  
        InCar prefs = PreferencesStorage.loadInCar(mContext);
        try {
            RandomAccessFile file = new RandomAccessFile(dataFile, "rw");
            synchronized (BackupManager.sDataLock) {
    	        // Serializing it to JSON string
    	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	        JSONOutputStream jos = new JSONOutputStream(baos);
    	        jos.writeObject(prefs);
    	        jos.close();
    	        
    	        // Get the string form the output stream and print it
    	        String jsonString = baos.toString();
            	Log.d("CarHomeWidget","jsonString: " + jsonString);
    	        file.writeUTF(jsonString);
            }
		} catch (IOException e) {
			e.printStackTrace();
			return ERROR_FILE_WRITE;
		}
		return RESULT_DONE;
	}
	
	public int doRestoreInCar() {
		if (!checkMediaReadable()) {
			return ERROR_STORAGE_NOT_AVAILABLE;
		}

        File dataFile = new File(mContext.getExternalFilesDir(null), FILE_INCAR_JSON);
        if (!dataFile.exists()) {
        	return ERROR_FILE_NOT_EXIST;  
        }
        if (!dataFile.canRead()) {
        	return ERROR_FILE_READ;       	
        }
        
        InCar prefs = null;
        try {
            RandomAccessFile file = new RandomAccessFile(dataFile, "r");
            synchronized (BackupManager.sDataLock) {
            	String jsonString = file.readUTF();
            	Log.d("CarHomeWidget","jsonString: " + jsonString);
            	JSONInputStream jis = new JSONInputStream(jsonString);
            	prefs = jis.readObject(InCar.class);
            }
		} catch (IOException e) {
			return ERROR_FILE_READ;
		} catch (JSONStreamException e) 
        {
            return ERROR_DESERIALIZE;
        }
		PreferencesStorage.saveInCar(mContext, prefs);
		return RESULT_DONE;
	}

	public int doRestoreMain(String filename, int appWidgetId) {
		if (!checkMediaReadable()) {
			return ERROR_STORAGE_NOT_AVAILABLE;
		}
		
        File saveDir = getMainBackupDir();
        File dataFile = new File(saveDir, filename);
        if (!dataFile.exists()) {
        	return ERROR_FILE_NOT_EXIST;  
        }
        if (!dataFile.canRead()) {
        	return ERROR_FILE_READ;       	
        }
        
        Main prefs = null;
        try {
            RandomAccessFile file = new RandomAccessFile(dataFile, "r");
            synchronized (BackupManager.sDataLock) {
            	String jsonString = file.readUTF();
            	Log.d("CarHomeWidget","jsonString: " + jsonString);
            	JSONInputStream jis = new JSONInputStream(jsonString);
            	prefs = jis.readObject(Main.class);
            }
		} catch (IOException e) {
			return ERROR_FILE_READ;
		} catch (JSONStreamException e) 
        {
            return ERROR_DESERIALIZE;
        }
		PreferencesStorage.saveMain(mContext, prefs, appWidgetId);
		return RESULT_DONE;
	}
	
	private File getMainBackupDir() {
        StringBuilder sb = new StringBuilder(mContext.getExternalFilesDir(null).getPath());
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
