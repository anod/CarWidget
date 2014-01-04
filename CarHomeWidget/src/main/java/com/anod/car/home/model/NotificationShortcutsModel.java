package com.anod.car.home.model;

import android.content.Context;

import com.anod.car.home.prefs.preferences.PreferencesStorage;

import java.util.ArrayList;

public class NotificationShortcutsModel extends AbstractShortcutsModel {
	private final Context mContext;
	public NotificationShortcutsModel(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public void createDefaultShortcuts() {
		// Nothing
	}

	@Override
	public int getCount() {
		return PreferencesStorage.NOTIFICATION_COMPONENT_NUMBER;
	}

	@Override
	public void updateCount(Integer count) {
		// :( Exception
	}

	@Override
	protected void saveShortcutId(int position, long shortcutId) {
		PreferencesStorage.saveNotifShortcut(mContext, shortcutId, position);
		
	}

	@Override
	protected void dropShortcutId(int position) {
		PreferencesStorage.dropNotifShortcut(position, mContext);
	}

	@Override
	protected ArrayList<Long> loadShortcutIds() {
		return PreferencesStorage.getNotifComponents(mContext);
	}



}
