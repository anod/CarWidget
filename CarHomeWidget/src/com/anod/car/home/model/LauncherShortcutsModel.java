package com.anod.car.home.model;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.anod.car.home.prefs.PreferencesStorage;
import com.anod.car.home.utils.Utils;

public class LauncherShortcutsModel extends AbstractShortcutsModel {
	private Context mContext;
	private int mAppWidgetId;
	
	public LauncherShortcutsModel(Context context, int appWidgetId) {
		super(context);
		mContext = context;
		mAppWidgetId = appWidgetId;
	}

	@Override
	protected ArrayList<Long> loadShortcutIds() {
		return PreferencesStorage.getLauncherComponents(mContext, mAppWidgetId);
	}

	@Override
	protected void saveShortcutId(int position, long shortcutId) {
		PreferencesStorage.saveShortcut(mContext, shortcutId, position, mAppWidgetId);
	}

	@Override
	protected void dropShortcutId(int position) {
		PreferencesStorage.dropShortcutPreference(position, mAppWidgetId, mContext);
	}

	@Override
	public int getCount() {
		return PreferencesStorage.LAUNCH_COMPONENT_NUMBER;
	}

	@Override
	public void createDefaultShortcuts() {
		initShortcuts(mAppWidgetId);
	}

	public void initShortcuts(int appWidgetId) {
		ComponentName s1 = new ComponentName("com.google.android.apps.maps", "com.google.android.maps.driveabout.app.DestinationActivity");
		ComponentName s2 = new ComponentName("com.android.contacts", "com.android.contacts.DialtactsActivity");
		ComponentName s3 = new ComponentName("com.android.music", "com.android.music.MusicBrowserActivity");
		ComponentName s4 = new ComponentName("com.android.contacts", "com.android.contacts.DialtactsContactsEntryActivity");

		ArrayList<ComponentName> list = new ArrayList<ComponentName>(4);
		list.add(s1);
		list.add(s2);
		list.add(s3);
		list.add(s4);
		int cellId = 0;
		for (int i = 0; i < list.size(); i++) {
			ShortcutInfo info = null;
			Intent data = new Intent();
			data.setComponent(list.get(i));
			if (!Utils.isIntentAvailable(mContext, data))
				continue;
			Log.d("CarHomeWidget", "Init shortcut - " + info + " Widget - " + appWidgetId);
			info = ShortcutInfoUtils.infoFromApplicationIntent(mContext, data);
			saveShortcut(cellId, info);
			cellId++;
		}

	}
}
