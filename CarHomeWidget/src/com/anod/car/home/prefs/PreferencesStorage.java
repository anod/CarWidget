package com.anod.car.home.prefs;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.anod.car.home.R;
import com.anod.car.home.model.LauncherModel;
import com.anod.car.home.model.ShortcutInfo;

public class PreferencesStorage {
	public static final int LAUNCH_COMPONENT_NUMBER=6;
	public static final String SKIN_GLOSSY = "glossy";
	public static final String SKIN_CARHOME = "carhome";
	public static final String SKIN_WINDOWS7 = "windows7";
	
	public static final String BRIGHTNESS_DEFAULT = "default";
	public static final String BRIGHTNESS_AUTO = "auto";
	public static final String BRIGHTNESS_DAY = "day";
	public static final String BRIGHTNESS_NIGHT = "night";
	
	public static final String WIFI_NOACTION = "no_action";
	public static final String WIFI_TURNOFF = "turn_off_wifi";
	public static final String WIFI_DISABLE = "disable_wifi";

	public static final int FONT_SIZE_UNDEFINED = -1;
	public static final boolean DEFAULT_ICONS_MONO = true;
	public static final int DEFAULT_VOLUME_LEVEL = 100;
	
    private static final String LAUNCH_COMPONENT = "launch-component-%d";
    public static final String SKIN = "skin-%d";
    public static final String BG_COLOR = "bg-color-%d";
    public static final String BUTTON_COLOR = "button-color-%d";
    public static final String ICONS_MONO = "icons-mono-%d";
    public static final String ICONS_COLOR = "icons-color-%d";
    public static final String ICONS_SCALE = "icons-scale-%d";
    public static final String FONT_SIZE = "font-size-%d";
    public static final String FONT_COLOR = "font-color-%d";
    public static final String FIRST_TIME = "first-time-%d";
    public static final String TRANSPARENT_BTN_SETTINGS = "transparent-btn-settings-%d";
    public static final String TRANSPARENT_BTN_INCAR = "transparent-btn-incar-%d";
    
    
    public static final String INCAR_MODE_ENABLED= "incar-mode-enabled";
    
    public static final String POWER_BT_ENABLE = "power-bt-enable";    
    public static final String POWER_BT_DISABLE = "power-bt-disable";

    public static final String HEADSET_REQUIRED = "headset-required";
    public static final String POWER_REQUIRED = "power-required";

    public static final String BLUETOOTH_DEVICE_ADDRESSES = "bt-device-addresses";

    public static final String SCREEN_TIMEOUT = "screen-timeout";
    public static final String BRIGHTNESS = "brightness";
    public static final String BLUETOOTH = "bluetooth";
    public static final String ADJUST_VOLUME_LEVEL = "adjust-volume-level";
    public static final String VOLUME_LEVEL = "volume-level";
    public static final String AUTO_SPEAKER = "auto_speaker";
    public static final String AUTO_ANSWER = "auto_answer";
    public static final String ADJUST_WIFI = "wi-fi";
    public static final String ACTIVATE_CAR_MODE = "activate-car-mode";
    
    private static final String DELIMETER_PACKAGES = "\n";
    
    public static Preferences.Main loadMain(Context context, int appWidgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Resources res = context.getResources();

        Preferences.Main p = (new Preferences()).getMain();
        String skinName = prefs.getString(getName(SKIN, appWidgetId), SKIN_GLOSSY);
    	p.setSkin(skinName);
    	p.setLauncherComponents(getLauncherComponents(prefs, appWidgetId));
    	
		Integer tileColor = null;
		if (skinName.equals(PreferencesStorage.SKIN_WINDOWS7)) {
			int defTileColor = res.getColor(R.color.w7_tale_default_background);
			tileColor = prefs.getInt(getName(BUTTON_COLOR, appWidgetId), defTileColor);
		}
		p.setTileColor(tileColor);
		
    	p.setIconsScaleString(prefs.getString(getName(ICONS_SCALE, appWidgetId), "0"));
    	p.setIconsMono(prefs.getBoolean(getName(ICONS_MONO, appWidgetId), DEFAULT_ICONS_MONO));
    	p.setBackgroundColor(prefs.getInt(getName(BG_COLOR, appWidgetId), res.getColor(R.color.default_background)));
    	p.setIconsColor(getIconsColor(prefs, appWidgetId));
    	p.setFontColor(prefs.getInt(getName(FONT_COLOR, appWidgetId), res.getColor(R.color.default_font_color)));
    	p.setFontSize(prefs.getInt(getName(FONT_SIZE, appWidgetId), FONT_SIZE_UNDEFINED));
    	p.setSettingsTransparent(prefs.getBoolean(getName(TRANSPARENT_BTN_SETTINGS, appWidgetId), false));
    	p.setIncarTransparent(prefs.getBoolean(getName(TRANSPARENT_BTN_INCAR, appWidgetId), false));
    	
    	return p;
    }
    
    public static Preferences.InCar loadInCar(Context context) {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    	Preferences.InCar p = (new Preferences()).getIncar();

    	p.setDisableBluetoothOnPower(prefs.getBoolean(POWER_BT_DISABLE, false));
    	p.setEnableBluetoothOnPower(prefs.getBoolean(POWER_BT_ENABLE, false));
    	
    	p.setPowerRequired(prefs.getBoolean(PreferencesStorage.POWER_REQUIRED, false));
    	p.setBtDevices(getBtDevices(context));
    	p.setHeadsetRequired(prefs.getBoolean(PreferencesStorage.HEADSET_REQUIRED, false));
    	
    	p.setAutoSpeaker(prefs.getBoolean(AUTO_SPEAKER, false));
    	p.setEnableBluetooth(prefs.getBoolean(BLUETOOTH, false));
    	p.setDisableScreenTimeout(prefs.getBoolean(SCREEN_TIMEOUT, false));
    	p.setBrightness(prefs.getString(BRIGHTNESS, BRIGHTNESS_DEFAULT));
    	p.setAdjustVolumeLevel(prefs.getBoolean(ADJUST_VOLUME_LEVEL, false));   	
    	p.setMediaVolumeLevel(prefs.getInt(VOLUME_LEVEL, DEFAULT_VOLUME_LEVEL));
    	p.setDisableWifi(prefs.getString(ADJUST_WIFI, WIFI_NOACTION));
    	p.setActivateCarMode(prefs.getBoolean(ACTIVATE_CAR_MODE, false));
    	return p;
    }

    public static HashMap<String,String> getBtDevices(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String addrStr = prefs.getString(BLUETOOTH_DEVICE_ADDRESSES, null);
        if (addrStr == null) {
        	return null;
        }
        String[] addrs = addrStr.split(",");
        HashMap<String,String> devices = new HashMap<String,String>(addrs.length);
        for(int i=0; i<addrs.length; i++) {
        	String addr = addrs[i];
        	devices.put(addr,addr);
        }
        return devices;
    }
    
    public static void saveBtDevices(Context context, HashMap<String,String> devices) {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    	Editor editor = prefs.edit();
    	if (devices == null || devices.size() == 0) {
    		editor.remove(BLUETOOTH_DEVICE_ADDRESSES);
    	} else {
    		String addrStr = TextUtils.join(",", devices.values());
    		Log.d("CarHomeWidget", addrStr);
    		editor.putString(BLUETOOTH_DEVICE_ADDRESSES, addrStr);
    	}
		editor.commit();
    }
    
    public static String getName(String aPref, int aAppWidgetId) {
    	return String.format(aPref, aAppWidgetId);
    }
    
    public static String getLaunchComponentKey(int id) {
    	return String.format(LAUNCH_COMPONENT, id) + "-%d";
    }
    
    public static String getLaunchComponentName(int id, int aAppWidgetId) {
    	return String.format(getLaunchComponentKey(id), aAppWidgetId);
    }
    
    private static ArrayList<Long> getLauncherComponents(SharedPreferences prefs, int appWidgetId) {      
		ArrayList<Long> ids = new ArrayList<Long>(LAUNCH_COMPONENT_NUMBER);
		for (int i=0; i<LAUNCH_COMPONENT_NUMBER; i++) {
			String key = PreferencesStorage.getLaunchComponentName(i, appWidgetId);
	        long id = prefs.getLong(key, ShortcutInfo.NO_ID);
	        ids.add(i, id);
		}
		return ids;
    }
	
    private static Integer getIconsColor(SharedPreferences prefs, int appWidgetId) {
        String prefName = getName(ICONS_COLOR, appWidgetId);
        if (!prefs.contains(prefName)) {
        	return null;
        }
        return prefs.getInt(prefName, Color.WHITE);     	    	
    }

    public static boolean isFirstTime(Context context, int appWidgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(getName(FIRST_TIME, appWidgetId), true);
    }
    
    public static void setFirstTime(boolean value,Context context, int appWidgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		editor.putBoolean(getName(FIRST_TIME, appWidgetId), value);
		editor.commit();
    }
    
    public static void saveShortcut(Context context,long shortcutId, int cellId, int appWidgetId) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String key = PreferencesStorage.getLaunchComponentName(cellId, appWidgetId);
		long curShortcutId = preferences.getLong(key, ShortcutInfo.NO_ID);
		if (curShortcutId != ShortcutInfo.NO_ID) {
			LauncherModel.deleteItemFromDatabase(context, curShortcutId);
		}
		Editor editor = preferences.edit();
		editor.putLong(key, shortcutId);
		editor.commit();
    }
    
    public static void saveStopAppPackages(Context context, ArrayList<String> packageNames) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();

		if (packageNames != null) {
			StringBuilder sb = new StringBuilder();
			int last = packageNames.size() - 1;
			for (int i = 0; i <= last; i++) {
			    sb.append(packageNames.get(i));
			    if (i!=last) {
			    	sb.append(DELIMETER_PACKAGES);
			    }
			}
			
			editor.putString(ACTIVATE_CAR_MODE, sb.toString());
		} else {
			editor.remove(ACTIVATE_CAR_MODE);
		}
		
		editor.commit();
    }
    
    public static boolean isInCarModeEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(INCAR_MODE_ENABLED, false);
    }
    
    public static void DropWidgetSettings(Context context, int[] appWidgetIds) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = prefs.edit();
         for(int appWidgetId : appWidgetIds) {
        	edit.remove(getName(SKIN, appWidgetId));
    		edit.remove(getName(BG_COLOR, appWidgetId));
    		edit.remove(getName(BUTTON_COLOR, appWidgetId));
    		edit.remove(getName(ICONS_MONO, appWidgetId));
    		edit.remove(getName(ICONS_COLOR, appWidgetId));
    		edit.remove(getName(ICONS_SCALE, appWidgetId));
    		edit.remove(getName(FONT_COLOR, appWidgetId));
    		edit.remove(getName(FONT_SIZE, appWidgetId));
    		edit.remove(getName(FIRST_TIME, appWidgetId));
    		edit.remove(getName(TRANSPARENT_BTN_SETTINGS, appWidgetId));
    		edit.remove(getName(TRANSPARENT_BTN_INCAR, appWidgetId));
    		
        	for(int i = 0; i<LAUNCH_COMPONENT_NUMBER; i++) {
        		String key = PreferencesStorage.getLaunchComponentName(i, appWidgetId);
        		long curShortcutId = prefs.getLong(key, ShortcutInfo.NO_ID);
        		if (curShortcutId!=ShortcutInfo.NO_ID) {
        			LauncherModel.deleteItemFromDatabase(context, curShortcutId);
        		}
        		edit.remove(key);
        	}
         }
         edit.commit();
    }
    
    public static void dropShortcutPreference(int cellId, int appWidgetId, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = prefs.edit();
        String key = PreferencesStorage.getLaunchComponentName(cellId, appWidgetId);
		edit.remove(key);
		edit.commit();
    }
    
    public static void DropSettings(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = prefs.edit();
       	edit.remove(POWER_BT_ENABLE);
   		edit.remove(POWER_BT_DISABLE);
   		edit.remove(INCAR_MODE_ENABLED);
   		edit.remove(BLUETOOTH_DEVICE_ADDRESSES);
       	edit.remove(HEADSET_REQUIRED);
   		edit.remove(POWER_REQUIRED);
   		edit.remove(SCREEN_TIMEOUT);
   		edit.remove(BRIGHTNESS);
   		edit.remove(BLUETOOTH);
   		edit.remove(ADJUST_VOLUME_LEVEL);
   		edit.remove(VOLUME_LEVEL);
   		edit.remove(AUTO_SPEAKER);
   		edit.remove(AUTO_ANSWER);
   		edit.remove(ADJUST_WIFI);
   		edit.remove(ACTIVATE_CAR_MODE);
        edit.commit();
    }
    
	public static void saveColor(Context context, String prefName, int color) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	    Editor edit = prefs.edit();
	    edit.putInt(prefName, color);
        edit.commit();

	}
}