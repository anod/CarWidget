package com.anod.car.home.prefs.model;

import android.content.SharedPreferences;
import android.support.v4.util.SimpleArrayMap;

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
            }
        }
        edit.apply();
        mChanges = null;
    }
}
