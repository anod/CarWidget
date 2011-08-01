package com.anod.car.home.prefs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.anod.car.home.prefs.Preferences.InCar;

public class Backup {
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
    
    public Backup(Context context) {
    	mContext = context;
    }
    
    
	public void doBackupInCar(String filename) throws FileNotFoundException {
		
		
        File dataFile = new File("path/here", filename);
        RandomAccessFile file = new RandomAccessFile(dataFile, "rw");
         
        InCar prefs = PreferencesStorage.loadInCar(mContext);
        try {
            synchronized (Backup.sDataLock) {
            	writeDataToFileLocked(file, prefs);
            }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	
	/**
     * Handy helper routine to write the UI data to a file.
     */
    void writeDataToFileLocked(RandomAccessFile file, InCar prefs)
        throws IOException {
            file.setLength(0L);
    		file.writeBoolean(prefs.isAutoSpeaker());
    		HashMap<String, String> devices = prefs.getBtDevices();
    		String addrStr = (devices.size() == 0) ? "" : TextUtils.join(",", devices.values());
    		file.writeUTF(addrStr);
    		file.writeBoolean(prefs.isPowerRequired());
    		file.writeBoolean(prefs.isHeadsetRequired());
    		file.writeBoolean(prefs.isBluetoothRequired());
    		file.writeBoolean(prefs.isDisableBluetoothOnPower());
    		file.writeBoolean(prefs.isEnableBluetoothOnPower());
    		file.writeBoolean(prefs.isDisableScreenTimeout());
    		file.writeBoolean(prefs.isAdjustVolumeLevel());
    		file.writeInt(prefs.getMediaVolumeLevel());
    		file.writeBoolean(prefs.isEnableBluetooth());
    		file.writeUTF(prefs.getBrightness());
    		file.writeUTF(prefs.getDisableWifi());
    		file.writeBoolean(prefs.activateCarMode());
    }

}
