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
	private int mAppWidgetId;
	
	public ShortcutModel(Context context, int appWidgetId) {
        mModel = new LauncherModel();
        mContext = context;
        mAppWidgetId = appWidgetId;
	}
	
	public void createDefaultShortcuts() {
		mModel.initShortcuts(mContext,mAppWidgetId);	
	}
		
	public void init() {
		ArrayList<Long> currentShortcutIds = PreferencesStorage.getLauncherComponents(mContext, mAppWidgetId);
        for (int cellId=0; cellId<PreferencesStorage.LAUNCH_COMPONENT_NUMBER;cellId++) {
        	long shortcutId = currentShortcutIds.get(cellId);
        	ShortcutInfo info = null;
        	if (shortcutId != ShortcutInfo.NO_ID) {
        		info = mModel.loadShortcut(mContext,shortcutId);
        	}
        	mShortcuts.put(cellId, info);
        	
        }
	}
	
	public void reloadShortcut(int cellId, long shortcutId) {
		if (shortcutId != ShortcutInfo.NO_ID) {
			final ShortcutInfo info = mModel.loadShortcut(mContext, shortcutId);
			mShortcuts.put(cellId, info);
		} else {
			mShortcuts.put(cellId, null);
		}
	}
	
	public HashMap<Integer, ShortcutInfo> getShortcuts() {
		return mShortcuts;
	}
	
	public ShortcutInfo getShortcut(int cellId) {
		return mShortcuts.get(cellId);
	}
	
	public ShortcutInfo putShortcut(int cellId, Intent data, boolean isApplicationShortcut) {
    	final ShortcutInfo info = mModel.addShortcut(mContext, data, cellId, mAppWidgetId, isApplicationShortcut);		
		mShortcuts.put(cellId, info);
		PreferencesStorage.saveShortcut(mContext,info.id,cellId,mAppWidgetId);		
		return mShortcuts.get(cellId);
	}
	
	public void dropShortcut(int cellId, int appWidgetId) {
		ShortcutInfo info = mShortcuts.get(cellId);
		LauncherModel.deleteItemFromDatabase(mContext, info.id);
		PreferencesStorage.dropShortcutPreference(cellId,appWidgetId,mContext);
		mShortcuts.put(cellId, null);
	}
}
