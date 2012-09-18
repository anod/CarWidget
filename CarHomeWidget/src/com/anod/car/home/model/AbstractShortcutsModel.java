package com.anod.car.home.model;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;

public abstract class AbstractShortcutsModel implements ShortcutsModel {
	private SparseArray<ShortcutInfo> mShortcuts;
	private Context mContext;
	private LauncherModel mModel;

	abstract public int getCount();
	abstract protected void saveShortcutId(int position, long shortcutId);
	abstract protected void dropShortcutId(int position);
	abstract protected ArrayList<Long> loadShortcutIds();

	public AbstractShortcutsModel(Context context) {
		mShortcuts = new SparseArray<ShortcutInfo>(getCount());
		mModel = new LauncherModel();
		mContext = context;
	}
	
	@Override
	public void init() {
		ArrayList<Long> currentShortcutIds = loadShortcutIds();
		for (int cellId = 0; cellId < getCount(); cellId++) {
			long shortcutId = currentShortcutIds.get(cellId);
			ShortcutInfo info = null;
			if (shortcutId != ShortcutInfo.NO_ID) {
				info = mModel.loadShortcut(mContext, shortcutId);;
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
	
	@Override
	public SparseArray<ShortcutInfo> getShortcuts() {
		return mShortcuts;
	}

	@Override
	public ShortcutInfo getShortcut(int position) {
		return mShortcuts.get(position);
	}

	@Override
	public void reloadShortcut(int position, long shortcutId) {
		if (shortcutId != ShortcutInfo.NO_ID) {
			final ShortcutInfo info = mModel.loadShortcut(mContext, shortcutId);;
			mShortcuts.put(position, info);
		} else {
			mShortcuts.put(position, null);
		}
	}

	@Override
	public ShortcutInfo saveShortcutIntent(int position, Intent data, boolean isApplicationShortcut) {
		final ShortcutInfo info = ShortcutInfoUtils.createShortcut(mContext, data, position, isApplicationShortcut);
		saveShortcut(position, info);
		return mShortcuts.get(position);
	}

	@Override
	public void saveShortcut(int position, ShortcutInfo info) {
		mShortcuts.put(position, info);
		if (info != null) {
			mModel.addItemToDatabase(mContext, info, position);
		}
		saveShortcutId(position, info.id);
	}

	@Override
	public void dropShortcut(int position) {
		ShortcutInfo info = mShortcuts.get(position);
		if (info != null) {
			mModel.deleteItemFromDatabase(mContext, info.id);
			mShortcuts.put(position, null);
			dropShortcutId(position);
		}
	}

}
