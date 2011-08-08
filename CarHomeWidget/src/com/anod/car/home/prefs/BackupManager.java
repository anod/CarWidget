package com.anod.car.home.prefs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.anod.car.home.prefs.preferences.InCar;
import com.anod.car.home.prefs.preferences.Main;
import com.jsonobjectserialization.JSONInputStream;
import com.jsonobjectserialization.JSONOutputStream;
import com.jsonobjectserialization.JSONStreamException;

public class BackupManager {
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
    
	public void doBackupMain(String filename, int appWidgetId) {
        if (!checkMediaWritable()) {
        	return;
        }

        File saveDir = getMainBackupDir();
        if (!saveDir.exists()) {
        	saveDir.mkdirs();
        } 
        
        File dataFile = new File(saveDir, filename);
        
        Main prefs = PreferencesStorage.loadMain(mContext, appWidgetId);
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
            Toast.makeText(mContext, "BackupManager failed to write the file", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			return;
		}
		saveDir.setLastModified(System.currentTimeMillis());
        Toast.makeText(mContext, "Backup is done.", Toast.LENGTH_SHORT).show();
	}
	
	public void doBackupInCar() {
        if (!checkMediaWritable()) {
        	return;
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
            Toast.makeText(mContext, "BackupManager failed to write the file", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			return;
		}
        Toast.makeText(mContext, "Backup is done.", Toast.LENGTH_SHORT).show();
	}
	
	public void doRestoreInCar() {
		if (!checkMediaReadable()) {
			return;
		}

        File dataFile = new File(mContext.getExternalFilesDir(null), FILE_INCAR_JSON);
        if (!dataFile.exists()) {
            Toast.makeText(mContext, "Backup file is not exists", Toast.LENGTH_SHORT).show();
        	return;  
        }
        if (!dataFile.canRead()) {
            Toast.makeText(mContext, "Can't read the backup", Toast.LENGTH_SHORT).show();
        	return;       	
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
            Toast.makeText(mContext, "BackupManager failed to read the file", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			return;
		} catch (JSONStreamException e) 
        {
            Toast.makeText(mContext, "Failed to deserialize backup", Toast.LENGTH_SHORT).show();
            Log.e("JSONObjectSerialization", "Failed to deserialize the object");
            return;
        }
		PreferencesStorage.saveInCar(mContext, prefs);
        Toast.makeText(mContext, "Restore is done.", Toast.LENGTH_SHORT).show();
	}

	public void doRestoreMain(String filename, int appWidgetId) {
		if (!checkMediaReadable()) {
			return;
		}
		
        File saveDir = getMainBackupDir();
        File dataFile = new File(saveDir, filename);
        if (!dataFile.exists()) {
            Toast.makeText(mContext, "Backup file is not exists", Toast.LENGTH_SHORT).show();
        	return;  
        }
        if (!dataFile.canRead()) {
            Toast.makeText(mContext, "Can't read the backup", Toast.LENGTH_SHORT).show();
        	return;       	
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
            Toast.makeText(mContext, "BackupManager failed to read the file", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			return;
		} catch (JSONStreamException e) 
        {
            Toast.makeText(mContext, "Failed to deserialize backup", Toast.LENGTH_SHORT).show();
            Log.e("JSONObjectSerialization", "Failed to deserialize the object");
            return;
        }
		PreferencesStorage.saveMain(mContext, prefs, appWidgetId);
        Toast.makeText(mContext, "Restore is done.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(mContext, "External storage is not avialable", Toast.LENGTH_SHORT).show();
        	return false;
        	
        }
        return true;
	}
	
	private boolean checkMediaReadable() {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            Toast.makeText(mContext, "External storage is not avialable", Toast.LENGTH_SHORT).show();
        	return false;
        }
        return true;
	}
}
