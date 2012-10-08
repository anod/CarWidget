package com.anod.car.home.prefs.preferences;

import java.util.ArrayList;
import java.util.HashMap;

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
import com.anod.car.home.utils.Utils;

public class PreferencesStorage {

	private static final String MODE_FORCE_STATE = "mode-force-state";

	public static final int NOTIFICATION_COMPONENT_NUMBER = 3;

	public static final int LAUNCH_COMPONENT_NUMBER = 6;
	public static final String SKIN_GLOSSY = "glossy";
	public static final String SKIN_CARHOME = "carhome";
	public static final String SKIN_WINDOWS7 = "windows7";
	public static final String SKIN_HOLO = "holo";
	public static final String SKIN_BBB = "blackbearblanc";

	public static final String BRIGHTNESS_DEFAULT = "default";
	public static final String BRIGHTNESS_AUTO = "auto";
	public static final String BRIGHTNESS_DAY = "day";
	public static final String BRIGHTNESS_NIGHT = "night";

	public static final String AUTOANSWER_DISABLED = "disabled";
	public static final String AUTOANSWER_IMMEDIATLY = "immediately";
	public static final String AUTOANSWER_DELAY_5 = "delay-5";

	public static final String WIFI_NOACTION = "no_action";
	public static final String WIFI_TURNOFF = "turn_off_wifi";
	public static final String WIFI_DISABLE = "disable_wifi";

	public static final int FONT_SIZE_UNDEFINED = -1;
	public static final boolean DEFAULT_ICONS_MONO = true;
	public static final int DEFAULT_VOLUME_LEVEL = 100;

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
	public static final String VOLUME_LEVEL = "volume-level";
	public static final String AUTO_SPEAKER = "auto_speaker";
	public static final String AUTO_ANSWER = "auto_answer";
	public static final String ADJUST_WIFI = "wi-fi";
	public static final String ACTIVATE_CAR_MODE = "activate-car-mode";
	public static final String AUTORUN_APP = "autorun-app";

	private static final String DELIMETER_PACKAGES = "\n";

	public static Main loadMain(Context context, int appWidgetId) {
		WidgetSharedPreferences prefs = new WidgetSharedPreferences(appWidgetId, context);
		Resources res = context.getResources();

		Main p = new Main();
		String skinName = prefs.getString(SKIN, SKIN_GLOSSY);
		p.setSkin(skinName);

		int defTileColor = res.getColor(R.color.w7_tale_default_background);
		int tileColor = prefs.getInt(BUTTON_COLOR, defTileColor);
		p.setTileColor(tileColor);

		p.setIconsScaleString(prefs.getString(ICONS_SCALE, "0"));
		p.setIconsMono(prefs.getBoolean(ICONS_MONO, DEFAULT_ICONS_MONO));
		p.setBackgroundColor(prefs.getInt(BG_COLOR, res.getColor(R.color.default_background)));
		p.setIconsColor(prefs.getColor(ICONS_COLOR));
		p.setFontColor(prefs.getInt(FONT_COLOR, res.getColor(R.color.default_font_color)));
		p.setFontSize(prefs.getInt(FONT_SIZE, FONT_SIZE_UNDEFINED));
		p.setSettingsTransparent(prefs.getBoolean(TRANSPARENT_BTN_SETTINGS, false));
		p.setIncarTransparent(prefs.getBoolean(TRANSPARENT_BTN_INCAR, false));
		p.setIconsTheme(prefs.getString(ICONS_THEME, null));
		return p;
	}

	public static void saveMain(Context context, Main prefs, int appWidgetId) {
		WidgetSharedPreferences p = new WidgetSharedPreferences(appWidgetId, context);
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
		editor.commit();

	}

	public static InCar loadInCar(Context context) {
		boolean incarEnabled = isInCarModeEnabled(context);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		InCar p = new InCar();

		p.setInCarEnabled(incarEnabled);
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
		p.setAutoAnswer(prefs.getString(AUTO_ANSWER, PreferencesStorage.AUTOANSWER_DISABLED));

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
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = p.edit();

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
		editor.putInt(VOLUME_LEVEL, prefs.getMediaVolumeLevel());
		editor.putString(ADJUST_WIFI, prefs.getDisableWifi());
		editor.putBoolean(ACTIVATE_CAR_MODE, prefs.activateCarMode());
		editor.putString(AUTO_ANSWER, prefs.getAutoAnswer());

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
		if (devices == null || devices.size() == 0) {
			editor.remove(BLUETOOTH_DEVICE_ADDRESSES);
		} else {
			String addrStr = TextUtils.join(",", devices.values());
			Log.d("CarHomeWidget", addrStr);
			editor.putString(BLUETOOTH_DEVICE_ADDRESSES, addrStr);
		}
		editor.commit();
	}

	/**
	 * @deprecated
	 * @param aPref
	 * @param aAppWidgetId
	 * @return
	 */
	public static String getName(String aPref, int aAppWidgetId) {
		return String.format(aPref, aAppWidgetId);
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
		WidgetSharedPreferences prefs = new WidgetSharedPreferences(appWidgetId, context);
		return prefs.getBoolean(FIRST_TIME, true);
	}

	public static void setFirstTime(boolean value, Context context, int appWidgetId) {
		WidgetSharedPreferences prefs = new WidgetSharedPreferences(appWidgetId, context);
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
			LauncherModel model = new LauncherModel();
			model.deleteItemFromDatabase(context, curShortcutId);
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

	public static boolean isInCarModeEnabled(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(INCAR_MODE_ENABLED, false);
	}

	public static void DropWidgetSettings(Context context, int[] appWidgetIds) {
		LauncherModel model = new LauncherModel();
		for (int appWidgetId : appWidgetIds) {
			WidgetSharedPreferences prefs = new WidgetSharedPreferences(appWidgetId, context);
			WidgetEditor edit = prefs.edit();
			edit.remove(SKIN);
			edit.remove(BG_COLOR);
			edit.remove(BUTTON_COLOR);
			edit.remove(ICONS_MONO);
			edit.remove(ICONS_COLOR);
			edit.remove(ICONS_SCALE);
			edit.remove(FONT_COLOR);
			edit.remove(FONT_SIZE);
			edit.remove(FIRST_TIME);
			edit.remove(TRANSPARENT_BTN_SETTINGS);
			edit.remove(TRANSPARENT_BTN_INCAR);
			edit.remove(KEEP_ORDER);

			for (int i = 0; i < LAUNCH_COMPONENT_NUMBER; i++) {
				String key = PreferencesStorage.getLaunchComponentKey(i);
				long curShortcutId = prefs.getLong(key, ShortcutInfo.NO_ID);
				if (curShortcutId != ShortcutInfo.NO_ID) {
					model.deleteItemFromDatabase(context, curShortcutId);
				}
				edit.remove(key);
			}
			edit.commit();
		}
	}

	public static void dropNotifShortcut(int position, Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = prefs.edit();
		String key = getNotifComponentName(position);
		edit.remove(key);
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

		if (Utils.IS_HONEYCOMB_OR_GREATER) {
			LauncherModel model = new LauncherModel();
			for (int i = 0; i < NOTIFICATION_COMPONENT_NUMBER; i++) {
				String key = getNotifComponentName(i);
				long curShortcutId = prefs.getLong(key, ShortcutInfo.NO_ID);
				if (curShortcutId != ShortcutInfo.NO_ID) {
					model.deleteItemFromDatabase(context, curShortcutId);
				}
				edit.remove(key);
			}
		}
		edit.commit();
	}

	public static void saveColor(Context context, String prefName, int color) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = prefs.edit();
		edit.putInt(prefName, color);
		edit.commit();

	}

	public static String getNotifComponentName(int position) {
		return String.format(NOTIF_COMPONENT, position);
	}

	public static boolean restoreForceState(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(MODE_FORCE_STATE, false);
	}
	
	public static void saveForceState(Context context, boolean forceState) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = prefs.edit();
		edit.putBoolean(MODE_FORCE_STATE, forceState);
		edit.commit();
	}
}
