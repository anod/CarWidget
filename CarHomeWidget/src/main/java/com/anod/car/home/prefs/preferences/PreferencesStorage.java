package com.anod.car.home.prefs.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.anod.car.home.R;
import com.anod.car.home.model.LauncherModel;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.prefs.preferences.WidgetSharedPreferences.WidgetEditor;
import com.anod.car.home.utils.BitmapTransform.RotateDirection;
import com.anod.car.home.utils.Utils;

public class PreferencesStorage {
	private static final String MODE_FORCE_STATE = "mode-force-state";

	public static final int NOTIFICATION_COMPONENT_NUMBER = 3;
	public static final int LAUNCH_COMPONENT_NUMBER = 6;

	private static final String NOTIF_COMPONENT = "notif-component-%d";
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
	public static final String KEEP_ORDER = "keep-order-%d";

	private static final String ICONS_THEME = "icons-theme-%d";

	public static final String ICONS_ROTATE = "icons-rotate-%d";
	public static final String TITLES_HIDE = "titles-hide-%d";
	
	private static final String[] sAppWidgetPrefs = {
		SKIN,
		BG_COLOR,
		BUTTON_COLOR,
		ICONS_MONO,
		ICONS_COLOR,
		ICONS_SCALE,
		FONT_SIZE,
		FONT_COLOR,
		FIRST_TIME,
		TRANSPARENT_BTN_SETTINGS,
		TRANSPARENT_BTN_INCAR,
		KEEP_ORDER,
		ICONS_THEME,
		ICONS_ROTATE,
		TITLES_HIDE,
	};

	public static final String INCAR_MODE_ENABLED = "incar-mode-enabled";

	public static final String POWER_BT_ENABLE = "power-bt-enable";
	public static final String POWER_BT_DISABLE = "power-bt-disable";

	public static final String HEADSET_REQUIRED = "headset-required";
	public static final String POWER_REQUIRED = "power-required";

	public static final String BLUETOOTH_DEVICE_ADDRESSES = "bt-device-addresses";

	public static final String SCREEN_TIMEOUT = "screen-timeout";
	public static final String BRIGHTNESS = "brightness";
	public static final String BLUETOOTH = "bluetooth";
	public static final String ADJUST_VOLUME_LEVEL = "adjust-volume-level";
	public static final String MEDIA_VOLUME_LEVEL = "volume-level";
	public static final String AUTO_SPEAKER = "auto_speaker";
	public static final String AUTO_ANSWER = "auto_answer";
	public static final String ADJUST_WIFI = "wi-fi";
	public static final String ACTIVATE_CAR_MODE = "activate-car-mode";
	public static final String AUTORUN_APP = "autorun-app";

	public static final String CALL_VOLUME_LEVEL = "call-volume-level";

	
	private static final String DELIMETER_PACKAGES = "\n";
	public static final String DEF_VALUE = "2";
	public static final String ACTIVITY_RECOGNITION = "activity-recognition";

	public static Main loadMain(Context context, int appWidgetId) {
		final WidgetSharedPreferences prefs = new WidgetSharedPreferences(context);
		prefs.setAppWidgetId(appWidgetId);
		Resources res = context.getResources();

		Main p = new Main();
		String skinName = prefs.getString(SKIN, Main.SKIN_HOLO);
		p.setSkin(skinName);

		int defTileColor = res.getColor(R.color.w7_tale_default_background);
		int tileColor = prefs.getInt(BUTTON_COLOR, defTileColor);
		p.setTileColor(tileColor);

		p.setIconsScaleString(prefs.getString(ICONS_SCALE, DEF_VALUE));
		p.setIconsMono(prefs.getBoolean(ICONS_MONO, false));
		p.setBackgroundColor(prefs.getInt(BG_COLOR, res.getColor(R.color.default_background)));
		p.setIconsColor(prefs.getColor(ICONS_COLOR));
		p.setFontColor(prefs.getInt(FONT_COLOR, res.getColor(R.color.default_font_color)));
		p.setFontSize(prefs.getInt(FONT_SIZE, Main.FONT_SIZE_UNDEFINED));
		p.setSettingsTransparent(prefs.getBoolean(TRANSPARENT_BTN_SETTINGS, false));
		p.setIncarTransparent(prefs.getBoolean(TRANSPARENT_BTN_INCAR, false));
		p.setIconsTheme(prefs.getString(ICONS_THEME, null));
		
		p.setIconsRotate(RotateDirection.valueOf(prefs.getString(ICONS_ROTATE, RotateDirection.NONE.name())));
		p.setTitlesHide(prefs.getBoolean(TITLES_HIDE, false));

		return p;
	}

	public static void saveMain(Context context, Main prefs, int appWidgetId) {
		final WidgetSharedPreferences p = new WidgetSharedPreferences(context);
		p.setAppWidgetId(appWidgetId);
		
		WidgetEditor editor = p.edit();

		editor.putString(SKIN, prefs.getSkin());

		Integer defTileColor = prefs.getTileColor();
		if (defTileColor != null) {
			editor.putInt(BUTTON_COLOR, defTileColor);
		}
		editor.putString(ICONS_SCALE, prefs.getIconsScale());
		editor.putBoolean(ICONS_MONO, prefs.isIconsMono());
		editor.putInt(BG_COLOR, prefs.getBackgroundColor());
		Integer iconsColor = prefs.getIconsColor();
		if (iconsColor != null) {
			editor.putInt(ICONS_COLOR, iconsColor);
		}

		editor.putInt(FONT_COLOR, prefs.getFontColor());
		editor.putInt(FONT_SIZE, prefs.getFontSize());

		editor.putBoolean(TRANSPARENT_BTN_SETTINGS, prefs.isSettingsTransparent());
		editor.putBoolean(TRANSPARENT_BTN_INCAR, prefs.isSettingsTransparent());

		editor.putStringOrRemove(ICONS_THEME, prefs.getIconsTheme());

        editor.putString(ICONS_ROTATE, prefs.getIconsRotate().name());
        editor.putBoolean(TITLES_HIDE, prefs.isTitlesHide());

		editor.commit();

	}

	public static InCar loadInCar(Context context) {
		boolean incarEnabled = isInCarModeEnabled(context);

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		InCar p = new InCar();

		p.setInCarEnabled(incarEnabled);
		p.setDisableBluetoothOnPower(prefs.getBoolean(POWER_BT_DISABLE, false));
		p.setEnableBluetoothOnPower(prefs.getBoolean(POWER_BT_ENABLE, false));

		p.setPowerRequired(prefs.getBoolean(PreferencesStorage.POWER_REQUIRED, false));
		p.setBtDevices(getBtDevices(context));
		p.setHeadsetRequired(prefs.getBoolean(PreferencesStorage.HEADSET_REQUIRED, false));

		p.setAutoSpeaker(prefs.getBoolean(AUTO_SPEAKER, false));
		p.setEnableBluetooth(prefs.getBoolean(BLUETOOTH, false));
		p.setDisableScreenTimeout(prefs.getBoolean(SCREEN_TIMEOUT, true));
		p.setBrightness(prefs.getString(BRIGHTNESS, InCar.BRIGHTNESS_DISABLED));
		p.setAdjustVolumeLevel(prefs.getBoolean(ADJUST_VOLUME_LEVEL, true));
		p.setMediaVolumeLevel(prefs.getInt(MEDIA_VOLUME_LEVEL, InCar.DEFAULT_VOLUME_LEVEL));
		p.setCallVolumeLevel(prefs.getInt(CALL_VOLUME_LEVEL, InCar.DEFAULT_VOLUME_LEVEL));
		p.setDisableWifi(prefs.getString(ADJUST_WIFI, InCar.WIFI_NOACTION));
		p.setActivateCarMode(prefs.getBoolean(ACTIVATE_CAR_MODE, false));
		p.setAutoAnswer(prefs.getString(AUTO_ANSWER, InCar.AUTOANSWER_DISABLED));

		p.setActivityRequired(prefs.getBoolean(ACTIVITY_RECOGNITION, false));// TODO
		String autorunAppString = prefs.getString(AUTORUN_APP, null);
		Log.d("CarWidget", "Autroun app:" + autorunAppString);
		ComponentName autorunApp = null;
		if (autorunAppString != null) {
			autorunApp = Utils.stringToComponent(autorunAppString);
		}
		p.setAutorunApp(autorunApp);

		return p;
	}

	public static void saveInCar(Context context, InCar prefs) {
		final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
		final Editor editor = p.edit();

		editor.putBoolean(INCAR_MODE_ENABLED, prefs.isInCarEnabled());
		editor.putBoolean(POWER_BT_DISABLE, prefs.isDisableBluetoothOnPower());
		editor.putBoolean(POWER_BT_ENABLE, prefs.isEnableBluetoothOnPower());

		editor.putBoolean(POWER_REQUIRED, prefs.isPowerRequired());
		editor.putBoolean(HEADSET_REQUIRED, prefs.isHeadsetRequired());

		editor.putBoolean(AUTO_SPEAKER, prefs.isAutoSpeaker());
		editor.putBoolean(BLUETOOTH, prefs.isEnableBluetooth());
		editor.putBoolean(SCREEN_TIMEOUT, prefs.isDisableScreenTimeout());
		editor.putString(BRIGHTNESS, prefs.getBrightness());
		editor.putBoolean(ADJUST_VOLUME_LEVEL, prefs.isAdjustVolumeLevel());
		editor.putInt(MEDIA_VOLUME_LEVEL, prefs.getMediaVolumeLevel());
		editor.putInt(CALL_VOLUME_LEVEL, prefs.getCallVolumeLevel());
		editor.putString(ADJUST_WIFI, prefs.getDisableWifi());
		editor.putBoolean(ACTIVATE_CAR_MODE, prefs.isActivateCarMode());
		editor.putString(AUTO_ANSWER, prefs.getAutoAnswer());

		editor.putBoolean(ACTIVITY_RECOGNITION, prefs.isActivityRequired());
		ComponentName autorunApp = prefs.getAutorunApp();
		if (autorunApp == null) {
			editor.remove(AUTORUN_APP);
		} else {
			String autorunAppString = Utils.componentToString(autorunApp);
			editor.putString(AUTORUN_APP, autorunAppString);
		}
		editor.commit();

		saveBtDevices(context, prefs.getBtDevices());
	}

	public static void saveAutorunApp(ComponentName component, Context context) {
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = p.edit();

		if (component == null) {
			editor.remove(AUTORUN_APP);
		} else {
			String autorunAppString = Utils.componentToString(component);
			editor.putString(AUTORUN_APP, autorunAppString);
		}
		editor.commit();
	}

	public static HashMap<String, String> getBtDevices(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String addrStr = prefs.getString(BLUETOOTH_DEVICE_ADDRESSES, null);
		if (addrStr == null) {
			return null;
		}
		String[] addrs = addrStr.split(",");
		HashMap<String, String> devices = new HashMap<String, String>(addrs.length);
		for (int i = 0; i < addrs.length; i++) {
			String addr = addrs[i];
			devices.put(addr, addr);
		}
		return devices;
	}

	public static void saveBtDevices(Context context, HashMap<String, String> devices) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		if (devices == null || devices.isEmpty()) {
			editor.remove(BLUETOOTH_DEVICE_ADDRESSES);
		} else {
			String addrStr = TextUtils.join(",", devices.values());
			Log.d("CarHomeWidget", addrStr);
			editor.putString(BLUETOOTH_DEVICE_ADDRESSES, addrStr);
		}
		editor.commit();
	}

	public static String getLaunchComponentKey(int id) {
		return String.format(LAUNCH_COMPONENT, id) + "-%d";
	}

	public static String getLaunchComponentName(int id, int aAppWidgetId) {
		return String.format(getLaunchComponentKey(id), aAppWidgetId);
	}

	public static ArrayList<Long> getNotifComponents(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		ArrayList<Long> ids = new ArrayList<Long>(NOTIFICATION_COMPONENT_NUMBER);
		for (int i = 0; i < NOTIFICATION_COMPONENT_NUMBER; i++) {
			String key = getNotifComponentName(i);
			long id = prefs.getLong(key, ShortcutInfo.NO_ID);
			ids.add(i, id);
		}
		return ids;
	}

	public static ArrayList<Long> getLauncherComponents(Context context, int appWidgetId) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		ArrayList<Long> ids = new ArrayList<Long>(LAUNCH_COMPONENT_NUMBER);
		for (int i = 0; i < LAUNCH_COMPONENT_NUMBER; i++) {
			String key = PreferencesStorage.getLaunchComponentName(i, appWidgetId);
			long id = prefs.getLong(key, ShortcutInfo.NO_ID);
			ids.add(i, id);
		}
		return ids;
	}

	public static boolean isFirstTime(Context context, int appWidgetId) {
		WidgetSharedPreferences prefs = new WidgetSharedPreferences(context);
		prefs.setAppWidgetId(appWidgetId);
		return prefs.getBoolean(FIRST_TIME, true);
	}

	public static void setFirstTime(boolean value, Context context, int appWidgetId) {
		WidgetSharedPreferences prefs = new WidgetSharedPreferences(context);
		prefs.setAppWidgetId(appWidgetId);
		WidgetEditor editor = prefs.edit();
		editor.putBoolean(FIRST_TIME, value);
		editor.commit();
	}

	public static void saveNotifShortcut(Context context, long shortcutId, int position) {
		String key = getNotifComponentName(position);
		saveShortcutId(context, shortcutId, key);
	}

	public static void saveShortcut(Context context, long shortcutId, int cellId, int appWidgetId) {
		String key = PreferencesStorage.getLaunchComponentName(cellId, appWidgetId);
		saveShortcutId(context, shortcutId, key);
	}

	private static void saveShortcutId(Context context, long shortcutId, String key) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		long curShortcutId = preferences.getLong(key, ShortcutInfo.NO_ID);
		if (curShortcutId != ShortcutInfo.NO_ID) {
			LauncherModel model = new LauncherModel(context);
			model.deleteItemFromDatabase(curShortcutId);
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
				if (i != last) {
					sb.append(DELIMETER_PACKAGES);
				}
			}

			editor.putString(ACTIVATE_CAR_MODE, sb.toString());
		} else {
			editor.remove(ACTIVATE_CAR_MODE);
		}

		editor.commit();
	}

	public static boolean isAdjustVolumeLevel(Context context) {
		return getBoolean(context, ADJUST_VOLUME_LEVEL, false);
	}
	
	public static boolean isInCarModeEnabled(Context context) {
		return getBoolean(context, INCAR_MODE_ENABLED, false);
	}

	public static void dropWidgetSettings(Context context, int[] appWidgetIds) {
		LauncherModel model = new LauncherModel(context);
		WidgetSharedPreferences prefs = new WidgetSharedPreferences(context);
		for (int appWidgetId : appWidgetIds) {
			prefs.setAppWidgetId(appWidgetId);
			WidgetEditor edit = prefs.edit();
			for(int i = 0 ; i < sAppWidgetPrefs.length; i++) {
				edit.remove(sAppWidgetPrefs[i]);
			}

			for (int i = 0; i < LAUNCH_COMPONENT_NUMBER; i++) {
				String key = PreferencesStorage.getLaunchComponentKey(i);
				long curShortcutId = prefs.getLong(key, ShortcutInfo.NO_ID);
				if (curShortcutId != ShortcutInfo.NO_ID) {
					model.deleteItemFromDatabase(curShortcutId);
				}
				edit.remove(key);
			}
			edit.commit();
		}
	}

	public static void dropNotifShortcut(int position, Context context) {
		String key = getNotifComponentName(position);
		remove(context, key);
	}
	
	public static void dropShortcutPreference(int cellId, int appWidgetId, Context context) {
		String key = PreferencesStorage.getLaunchComponentName(cellId, appWidgetId);
		remove(context, key);
	}


	public static String getNotifComponentName(int position) {
		return String.format(Locale.US, NOTIF_COMPONENT, position);
	}

	public static boolean restoreForceState(Context context) {
		return getBoolean(context, MODE_FORCE_STATE, false);
	}
	
	public static void saveForceState(Context context, boolean forceState) {
		putBoolean(context, MODE_FORCE_STATE, forceState);
	}

	public static void setAdjustVolumeLevel(Context context, boolean isChecked) {
		putBoolean(context, ADJUST_VOLUME_LEVEL, isChecked);
	}
	
	private static boolean getBoolean(Context context, String key, boolean defaultValue) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(key, defaultValue);
	}
	
	private static void putBoolean(Context context, String key, boolean value) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = prefs.edit();
		edit.putBoolean(key, value);
		edit.commit();
	}
	
	private static void remove(Context context, String key) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = prefs.edit();
		edit.remove(key);
		edit.commit();
	}


	public static boolean isActivityRecognitionEnabled(Context context) {
		return getBoolean(context, ACTIVITY_RECOGNITION, false);
	}
}