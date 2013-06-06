package com.anod.car.home.model;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.anod.car.home.R;
import com.anod.car.home.prefs.CarWidgetShortcutsPicker;
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
			//
			new ComponentName("com.android.music", "com.android.music.MusicBrowserActivity"),
			new ComponentName("com.htc.music", "com.htc.music.HtcMusic"),
			new ComponentName("com.sec.android.app.music", "com.sec.android.app.music.MusicActionTabActivity"),
			//new ComponentName("com.android.contacts", "com.android.contacts.DialtactsContactsEntryActivity"),
			//new ComponentName("com.android.htccontacts", "com.android.htccontacts.BrowseLayerCarouselActivity"),
			new ComponentName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity"),
		};

		int cellId = 0;
		Intent data = new Intent();
		ShortcutInfo info = null;
		for (int i = 0; i < list.length; i++) {
			data.setComponent(list[i]);
			if (!IntentUtils.isIntentAvailable(mContext, data)) {
				continue;
			}
			Log.d("CarHomeWidget", "Init shortcut - " + info + " Widget - " + appWidgetId);
			info = ShortcutInfoUtils.infoFromApplicationIntent(mContext, data);
			saveShortcut(cellId, info);
			cellId++;
			if (cellId == 5) {
				break;
			}
		}
		//Add TuneIn car mode
		if (cellId < 5) {
			Drawable icon = null;
			final PackageManager pm = mContext.getPackageManager();
			icon = IntentUtils.getApplicationIcon(pm,new ComponentName("radiotime.player","tunein.player.pro.Activity"));
			if (icon != null) {
				info = getTuneInShortcutInfo(icon, true);
				saveShortcut(cellId, info);
				cellId++;
			} else {
				icon = IntentUtils.getApplicationIcon(pm,new ComponentName("tunein.player","tunein.player.Activity"));
				if (icon != null) {
					info = getTuneInShortcutInfo(icon, false);
					saveShortcut(cellId, info);
					cellId++;
				}
			}
		}

	}

	private ShortcutInfo getTuneInShortcutInfo(Drawable icon, boolean tuneInPro) {
		String[] titles;
		ShortcutInfo info;
		titles = mContext.getResources().getStringArray(R.array.carwidget_shortcuts);
		Intent pickIntent = IntentUtils.createPickShortcutAppIntent(
			titles[CarWidgetShortcutsPicker.IDX_TUNEIN], icon, IntentUtils.createTuneInIntent(tuneInPro), mContext
		);
		info = ShortcutInfoUtils.infoFromShortcutIntent(mContext, pickIntent);
		return info;
	}
}
