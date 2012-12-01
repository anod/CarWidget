package com.anod.car.home.model;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.utils.IntentUtils;

public class LauncherShortcutsModel extends AbstractShortcutsModel {
	private final Context mContext;
	private final int mAppWidgetId;
	
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
		ComponentName[] list = {
			new ComponentName("com.google.android.apps.maps", "com.google.android.maps.driveabout.app.DestinationActivity"),
			new ComponentName("com.android.contacts", "com.android.contacts.DialtactsActivity"),
			new ComponentName("com.android.htccontacts", "com.android.htccontacts.DialerTabActivity"), //HTC Phone
			new ComponentName("com.android.music", "com.android.music.MusicBrowserActivity"),
			new ComponentName("com.htc.music", "com.htc.music.HtcMusic"),
			new ComponentName("com.android.contacts", "com.android.contacts.DialtactsContactsEntryActivity"),
			new ComponentName("com.android.htccontacts", "com.android.htccontacts.BrowseLayerCarouselActivity"),
			new ComponentName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity"),
		};
		
		int cellId = 0;
		Intent data = new Intent();
		for (int i = 0; i < list.length; i++) {
			ShortcutInfo info = null;
			data.setComponent(list[i]);
			if (!IntentUtils.isIntentAvailable(mContext, data)) {
				continue;
			}
			Log.d("CarHomeWidget", "Init shortcut - " + info + " Widget - " + appWidgetId);
			info = ShortcutInfoUtils.infoFromApplicationIntent(mContext, data);
			saveShortcut(cellId, info);
			cellId++;
			if (cellId == 6) {
				break;
			}
		}

	}
}
