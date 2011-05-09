package com.anod.car.home;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

public class Preferences {
	public static final int LAUNCH_COMPONENT_NUMBER=6;
	public static final String SKIN_GLOSSY = "glossy";
	public static final String SKIN_CARHOME = "carhome";
	public static final String SKIN_WINDOWS7 = "windows7";
	
	public static final String BRIGHTNESS_DEFAULT = "default";
	public static final String BRIGHTNESS_AUTO = "auto";
	public static final String BRIGHTNESS_DAY = "day";
	public static final String BRIGHTNESS_NIGHT = "night";
	
	public static final int COLOR_UNDEFINED = -1;
	public static final int FONT_SIZE_UNDEFINED = -1;
	public static final boolean DEFAULT_ICONS_MONO = true;
	public static final int DEFAULT_VOLUME_LEVEL = 100;
	
    private static final String LAUNCH_COMPONENT = "launch-component-%d";
    public static final String SKIN = "skin-%d";
    public static final String BG_COLOR = "bg-color-%d";
    public static final String BUTTON_COLOR = "button-color-%d";
    public static final String ICONS_MONO = "icons-mono-%d";
    public static final String ICONS_COLOR = "icons-color-%d";
    public static final String FONT_SIZE = "font-size-%d";
    public static final String FONT_COLOR = "font-color-%d";
    public static final String FIRST_TIME = "first-time-%d";
    
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
    
    public static ArrayList<Long> getLauncherComponents(Context context, int appWidgetId) {      
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		ArrayList<Long> ids = new ArrayList<Long>(LAUNCH_COMPONENT_NUMBER);
		for (int i=0; i<LAUNCH_COMPONENT_NUMBER; i++) {
			String key = Preferences.getLaunchComponentName(i, appWidgetId);
	        long id = prefs.getLong(key, ShortcutInfo.NO_ID);
	        ids.add(i, id);
		}
		return ids;
    }

    public static String getSkin(Context context, int appWidgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String prefName = getName(SKIN, appWidgetId);
        return prefs.getString(prefName, SKIN_GLOSSY);    	
    }
    
    public static int getTileColor(Context context, int appWidgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String prefName = getName(BUTTON_COLOR, appWidgetId);
        return prefs.getInt(prefName, COLOR_UNDEFINED);      	    	
    }

	public static boolean isIconsMono(Context context, int appWidgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String prefName = getName(ICONS_MONO, appWidgetId);
        return prefs.getBoolean(prefName, DEFAULT_ICONS_MONO);
	}

	public static boolean getBool(String prefName, boolean defValue, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(prefName, defValue);
	}

	public static String getBrightness(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(BRIGHTNESS, BRIGHTNESS_DEFAULT);
	}
	
    public static int getVolumeLevel(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(VOLUME_LEVEL, DEFAULT_VOLUME_LEVEL);
	}
	
    public static int getIconsColor(Context context, int appWidgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String prefName = getName(ICONS_COLOR, appWidgetId);
        return prefs.getInt(prefName, COLOR_UNDEFINED);     	    	
    }
    
    public static int getBackgroundColor(Context context, int appWidgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String prefName = getName(BG_COLOR, appWidgetId);
        return prefs.getInt(prefName, COLOR_UNDEFINED);    	    	
    }

    public static int getFontColor(Context context, int appWidgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String prefName = getName(FONT_COLOR, appWidgetId);
        return prefs.getInt(prefName, COLOR_UNDEFINED);   
    }
    
    public static int getFontSize(Context context, int appWidgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String prefName = getName(FONT_SIZE, appWidgetId);
        return prefs.getInt(prefName, FONT_SIZE_UNDEFINED);
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
		String key = Preferences.getLaunchComponentName(cellId, appWidgetId);
		long curShortcutId = preferences.getLong(key, ShortcutInfo.NO_ID);
		if (curShortcutId != ShortcutInfo.NO_ID) {
			LauncherModel.deleteItemFromDatabase(context, curShortcutId);
		}
		Editor editor = preferences.edit();
		editor.putLong(key, shortcutId);
		editor.commit();
    }
    
    public static boolean isInCarModeEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(INCAR_MODE_ENABLED, false);
    }
    
    public static boolean enableBluetoothOnPower(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(POWER_BT_ENABLE, false);
    }
    
    public static boolean disableBluetoothOnPower(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(POWER_BT_DISABLE, false);
    }
    
    public static boolean isPlugRequired(String prefName, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(prefName, false);
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
    		edit.remove(getName(FONT_COLOR, appWidgetId));
    		edit.remove(getName(FONT_SIZE, appWidgetId));
    		edit.remove(getName(FIRST_TIME, appWidgetId));
    		
        	for(int i = 0; i<LAUNCH_COMPONENT_NUMBER; i++) {
        		String key = Preferences.getLaunchComponentName(i, appWidgetId);
        		long curShortcutId = prefs.getLong(key, ShortcutInfo.NO_ID);
        		if (curShortcutId!=ShortcutInfo.NO_ID) {
        			LauncherModel.deleteItemFromDatabase(context, curShortcutId);
        		}
        		edit.remove(key);
        	}
         }
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
        edit.commit();
    }
    
	public static void saveColor(Context context, String prefName, int color) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	    Editor edit = prefs.edit();
	    edit.putInt(prefName, color);
        edit.commit();

	}
}
