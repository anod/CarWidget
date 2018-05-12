package com.anod.car.home.prefs.model;

import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.ComponentInfo;
import android.support.v4.util.SimpleArrayMap;

import info.anodsplace.framework.AppLog;
import com.anod.car.home.utils.Utils;

/**
 * @author algavris
 * @date 09/04/2016.
 */
public class ChangeableSharedPreferences {

    private SimpleArrayMap<String,Object> mChanges;
    protected SharedPreferences mPrefs;

    public ChangeableSharedPreferences(SharedPreferences mPrefs) {
        this.mPrefs = mPrefs;
    }

    public SharedPreferences getPrefs()
    {
        return mPrefs;
    }

    protected void putChange(String key, Object value) {
        if (mChanges == null)
        {
            mChanges = new SimpleArrayMap<>();
        }
        mChanges.put(key, value);
    }

    public void apply()
    {
        if (mChanges == null || mChanges.isEmpty())
        {
            return;
        }
        SharedPreferences.Editor edit = mPrefs.edit();
        for (int i = 0; i < mChanges.size(); i++)
        {
            String key = mChanges.keyAt(i);
            Object value = mChanges.get(key);
            if (value == null) {
                edit.remove(key);
            } else if (value instanceof Boolean) {
                edit.putBoolean(key, (Boolean) value);
            } else if (value instanceof String) {
                edit.putString(key, (String) value);
            } else if (value instanceof Integer) {
                edit.putInt(key, (Integer) value);
            } else if (value instanceof Long) {
                 edit.putLong(key, (Long) value);
            } else if (value instanceof ComponentName) {
                edit.putString(key, ((ComponentName) value).flattenToString());
            } else {
                AppLog.Companion.e("Unknown value "+value.toString()+" for key "+key);
            }
        }
        edit.putBoolean("migrated", true);
        edit.apply();
        mChanges = null;
    }
}
