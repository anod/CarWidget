package com.anod.car.home.model;

import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;

import java.util.ArrayList;

public abstract class AbstractShortcutsModel implements ShortcutsModel {
	private SparseArray<ShortcutInfo> mShortcuts;
	private final Context mContext;
	private final LauncherModel mModel;

	abstract public int getCount();
	abstract protected void saveShortcutId(int position, long shortcutId);
	abstract protected void dropShortcutId(int position);
	abstract protected ArrayList<Long> loadShortcutIds();

	public AbstractShortcutsModel(Context context) {
		mModel = new LauncherModel(context);
		mContext = context;
	}
	
	@Override
	public void init() {
		mShortcuts = new SparseArray<ShortcutInfo>(getCount());
		ArrayList<Long> currentShortcutIds = loadShortcutIds();
		for (int cellId = 0; cellId < getCount(); cellId++) {
			long shortcutId = currentShortcutIds.get(cellId);
			ShortcutInfo info = null;
			if (shortcutId != ShortcutInfo.NO_ID) {
				info = mModel.loadShortcut(shortcutId);
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
		if (shortcutId == ShortcutInfo.NO_ID) {
			mShortcuts.put(position, null);
		} else {
			final ShortcutInfo info = mModel.loadShortcut(shortcutId);
			mShortcuts.put(position, info);
		}
	}

	public void move(int from, int to) {
		if (from == to) {
			return;
		}

		ShortcutInfo fromInfo = mShortcuts.get(from);

		if (from > to) {
			for(int i = from; i>to;i--) {
				int j = i - 1;
				ShortcutInfo a = mShortcuts.get(i);
				ShortcutInfo b = mShortcuts.get(j);
				mShortcuts.put(i,b);
				mShortcuts.put(j,a);
			}
			mShortcuts.put(to,fromInfo);
		} else {
			for(int i = from; i<to-1;i++) {
				int j = i + 1;
				ShortcutInfo a = mShortcuts.get(i);
				ShortcutInfo b = mShortcuts.get(j);
				mShortcuts.put(i,b);
				mShortcuts.put(j,a);
			}
			mShortcuts.put(to-1,fromInfo);
		}


		int min = Math.min(from,to);
		int max = Math.max(from,to);
		//update mapping
		for (int cellId = min; cellId <= max; cellId++) {
			ShortcutInfo info = mShortcuts.get(cellId);
			if (info == null) {
				dropShortcutId(cellId);
			} else {
				saveShortcutId(cellId,info.id);
			}
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
			saveShortcutId(position, info.id);
		}
	}

	@Override
	public void dropShortcut(int position) {
		ShortcutInfo info = mShortcuts.get(position);
		if (info != null) {
			mModel.deleteItemFromDatabase(info.id);
			mShortcuts.put(position, null);
			dropShortcutId(position);
		}
	}

}
