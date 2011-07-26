package com.anod.car.home.prefs;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;

import com.anod.car.home.model.LauncherModel;
import com.anod.car.home.model.ShortcutInfo;

public class ShortcutModel {
	private HashMap<Integer, ShortcutInfo> mShortcuts = new HashMap<Integer, ShortcutInfo>(PreferencesStorage.LAUNCH_COMPONENT_NUMBER);
	private LauncherModel mModel;
	private Context mContext;
	
	public ShortcutModel(Context context) {
        mModel = new LauncherModel();
        mContext = context;
	}
	
	public void init(ArrayList<Long> currentShortcutIds) {
        for (int cellId=0; cellId<PreferencesStorage.LAUNCH_COMPONENT_NUMBER;cellId++) {
        	long shortcutId = currentShortcutIds.get(cellId);
        	ShortcutInfo info = null;
        	if (shortcutId != ShortcutInfo.NO_ID) {
        		info = mModel.loadShortcut(mContext,shortcutId);
        	}
        	mShortcuts.put(cellId, info);
        	
        }
	}
	
	public ShortcutInfo getShortcut(int cellId) {
		return mShortcuts.get(cellId);
	}
	
	public ShortcutInfo putShortcut(int cellId, int appWidgetId, Intent data, boolean isApplicationShortcut) {
    	final ShortcutInfo info = mModel.addShortcut(mContext, data, cellId, appWidgetId, isApplicationShortcut);		
		mShortcuts.put(cellId, info);
		PreferencesStorage.saveShortcut(mContext,info.id,cellId,appWidgetId);		
		return mShortcuts.get(cellId);
	}
	
	public void dropShortcut(int cellId, int appWidgetId) {
		ShortcutInfo info = mShortcuts.get(cellId);
		LauncherModel.deleteItemFromDatabase(mContext, info.id);
		PreferencesStorage.dropShortcutPreference(cellId,appWidgetId,mContext);
		mShortcuts.put(cellId, null);
	}
}
