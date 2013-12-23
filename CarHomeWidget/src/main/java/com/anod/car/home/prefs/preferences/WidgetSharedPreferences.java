package com.anod.car.home.prefs.preferences;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.preference.PreferenceManager;

import java.util.Set;

public class WidgetSharedPreferences /* implements SharedPreferences */{

	private final SharedPreferences mPrefs;
	private int mAppWidgetId;
	private WidgetEditor mWidgetEdit;

	public WidgetSharedPreferences(Context context) {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public void setAppWidgetId(int appWidgetId) {
		mAppWidgetId = appWidgetId;
	}

	public static String getName(String prefName, int aAppWidgetId) {
		return String.format(prefName, aAppWidgetId);
	}

	/**
	 * 
	 * @param prefName
	 * @param listId
	 * @param aAppWidgetId
	 * @return
	 */
	public static String getListName(String prefName, int listId, int aAppWidgetId) {
		return String.format(prefName, aAppWidgetId, listId);
	}

	@SuppressLint("CommitPrefEdits")
	public WidgetEditor edit() {
		if (mWidgetEdit == null) {
			Editor edit = mPrefs.edit();
			mWidgetEdit = new WidgetEditor(mAppWidgetId, edit);
		}
		return mWidgetEdit;
	}

	public boolean getBoolean(String key, boolean defValue) {
		String keyId = getName(key, mAppWidgetId);
		return mPrefs.getBoolean(keyId, defValue);
	}

	/**
	 * 
	 * @param key
	 * @param listId
	 * @param defValue
	 * @return
	 */
	public boolean getBoolean(String key, int listId, boolean defValue) {
		String keyId = getListName(key, listId, mAppWidgetId);
		return mPrefs.getBoolean(keyId, defValue);
	}

	public float getFloat(String key, float defValue) {
		String keyId = getName(key, mAppWidgetId);
		return mPrefs.getFloat(keyId, defValue);
	}

	public int getInt(String key, int defValue) {
		String keyId = getName(key, mAppWidgetId);
		return mPrefs.getInt(keyId, defValue);
	}

	public long getLong(String key, long defValue) {
		String keyId = getName(key, mAppWidgetId);
		return mPrefs.getLong(keyId, defValue);
	}

	public String getString(String key, String defValue) {
		String keyId = getName(key, mAppWidgetId);
		return mPrefs.getString(keyId, defValue);
	}

	public Integer getColor(String key) {
		String prefName = getName(key, mAppWidgetId);
		if (!mPrefs.contains(prefName)) {
			return null;
		}
		return mPrefs.getInt(prefName, Color.WHITE);
	}

	public ComponentName getComponentName(String key) {
		String compString = getString(key, null);
		if (compString == null) {
			return null;
		}
		String[] compParts = compString.split("/");
		return new ComponentName(compParts[0], compParts[1]);
	}

	public final class WidgetEditor implements Editor {
		private final Editor mEdit;
		private int mEditAppWidgetId;

		public WidgetEditor(int appWidgetId, Editor edit) {
			mEdit = edit;
			mEditAppWidgetId = appWidgetId;
		}

		public void setAppWidgetId(int appWidgetId) {
			mEditAppWidgetId = appWidgetId;
		}

		@Override
		public void apply() {
			mEdit.commit();
		}

		@Override
		public Editor clear() {
			mEdit.clear();
			return this;
		}

		@Override
		public boolean commit() {
			return mEdit.commit();
		}

		@Override
		public Editor putBoolean(String key, boolean value) {
			String keyId = getName(key, mEditAppWidgetId);
			mEdit.putBoolean(keyId, value);
			return this;
		}

		public Editor putBoolean(String key, int listId, boolean value) {
			String keyId = getListName(key, listId, mEditAppWidgetId);
			mEdit.putBoolean(keyId, value);
			return this;
		}

		@Override
		public Editor putFloat(String key, float value) {
			String keyId = getName(key, mEditAppWidgetId);
			mEdit.putFloat(keyId, value);
			return this;
		}

		@Override
		public Editor putInt(String key, int value) {
			String keyId = getName(key, mEditAppWidgetId);
			mEdit.putInt(keyId, value);
			return this;
		}

		/**
		 * 
		 * @param key
		 * @param value
		 * @return
		 */
		public WidgetEditor putIntOrRemove(String key, int value) {
			String keyId = getName(key, mEditAppWidgetId);
			if (value > 0) {
				mEdit.putInt(keyId, value);
			} else {
				mEdit.remove(keyId);
			}
			return this;
		}

		@Override
		public Editor putLong(String key, long value) {
			String keyId = getName(key, mEditAppWidgetId);
			mEdit.putLong(keyId, value);
			return this;
		}

		@Override
		public Editor putString(String key, String value) {
			String keyId = getName(key, mEditAppWidgetId);
			mEdit.putString(keyId, value);
			return this;
		}

		/**
		 * 
		 * @param key
		 * @param value
		 * @return
		 */
		public WidgetEditor putStringOrRemove(String key, String value) {
			String keyId = getName(key, mEditAppWidgetId);
			if (value != null) {
				mEdit.putString(keyId, value);
			} else {
				mEdit.remove(keyId);
			}
			return this;
		}

		public WidgetEditor putComponentOrRemove(String key, ComponentName component) {
			String keyId = getName(key, mEditAppWidgetId);
			if (component != null) {
				String value = component.getPackageName() + "/" + component.getClassName();
				mEdit.putString(keyId, value);
			} else {
				mEdit.remove(keyId);
			}
			return this;
		}

		@Override
		public Editor putStringSet(String key, Set<String> value) {
			//String keyId = getName(key, mEditAppWidgetId);
			//mEdit.putStringSet(keyId, value);
			throw new IllegalAccessError("Not implemented");
			//return this;
		}

		@Override
		public Editor remove(String key) {
			String keyId = getName(key, mEditAppWidgetId);
			mEdit.remove(keyId);
			return this;
		}

		public Editor remove(String key, int listId) {
			String keyId = getListName(key, listId, mEditAppWidgetId);
			mEdit.remove(keyId);
			return this;
		}
	}

}
