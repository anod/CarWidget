package com.anod.car.home.model;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;

import com.anod.car.home.prefs.PreferencesStorage;

public class ShortcutModel {
	private SparseArray<ShortcutInfo> mShortcuts = new SparseArray<ShortcutInfo>(PreferencesStorage.LAUNCH_COMPONENT_NUMBER);
	private LauncherModel mModel;
	private Context mContext;
	private int mAppWidgetId;

	public ShortcutModel(Context context, int appWidgetId) {
		mModel = new LauncherModel();
		mContext = context;
		mAppWidgetId = appWidgetId;
	}

	public void createDefaultShortcuts() {
		mModel.initShortcuts(mContext, mAppWidgetId);
	}

	public void init() {
		ArrayList<Long> currentShortcutIds = PreferencesStorage.getLauncherComponents(mContext, mAppWidgetId);
		for (int cellId = 0; cellId < PreferencesStorage.LAUNCH_COMPONENT_NUMBER; cellId++) {
			long shortcutId = currentShortcutIds.get(cellId);
			ShortcutInfo info = null;
			if (shortcutId != ShortcutInfo.NO_ID) {
				info = mModel.loadShortcut(mContext, shortcutId);
			}
			if (info!=null && info.intent != null && info.intent.getComponent() != null) {
				ComponentName cmp = info.intent.getComponent();
				if (cmp.getPackageName().equals("radiotime.player")) {
					Log.d("CarHome", cmp.toString());
					Intent localIntent = new Intent(Intent.ACTION_RUN);
					localIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					localIntent.setClassName("radiotime.player", "tunein.player.pro.Proxy");
					Uri data = Uri.parse("radiotime.player://carmode");
					localIntent.setData(data);
					info.intent = localIntent;
				}

				
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

	public SparseArray<ShortcutInfo> getShortcuts() {
		return mShortcuts;
	}

	public ShortcutInfo getShortcut(int cellId) {
		return mShortcuts.get(cellId);
	}

	public ShortcutInfo saveShortcutIntent(int cellId, Intent data, boolean isApplicationShortcut) {
		final ShortcutInfo info = mModel.createShortcut(mContext, data, cellId, mAppWidgetId, isApplicationShortcut);
		saveShortcut(cellId, info);
		return mShortcuts.get(cellId);
	}

	public void saveShortcut(int cellId, ShortcutInfo info) {
		mShortcuts.put(cellId, info);
		if (info != null) {
			mModel.addItemToDatabase(mContext, info, cellId, mAppWidgetId);
		}
		PreferencesStorage.saveShortcut(mContext, info.id, cellId, mAppWidgetId);
	}

	public void dropShortcut(int cellId) {
		ShortcutInfo info = mShortcuts.get(cellId);
		if (info != null) {
			LauncherModel.deleteItemFromDatabase(mContext, info.id);
			PreferencesStorage.dropShortcutPreference(cellId, mAppWidgetId, mContext);
			mShortcuts.put(cellId, null);
		}
	}
}
