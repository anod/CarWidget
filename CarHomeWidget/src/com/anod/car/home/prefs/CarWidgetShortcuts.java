package com.anod.car.home.prefs;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.os.Bundle;

import com.anod.car.home.R;

public class CarWidgetShortcuts {
	private static final int SHORCUTS_LENGTH = 5;

	private final Context mContext;
	
	private static final String[] TITLES = {
		"Siwtch InCar",
		"Play/Pause",
		"Next",
		"Previous",
		"TuneIn Radio Car Mode"
	};

	private static final int[] ICONS = {
		R.drawable.ic_launcher,
		R.drawable.ic_launcher,
		R.drawable.ic_launcher,
		R.drawable.ic_launcher,
		R.drawable.ic_launcher
	};
	
	
	public CarWidgetShortcuts(Context context) {
		mContext = context;
	}
	
	public Bundle prepareBundle(int cellPosision) {
		Bundle bundle = new Bundle();

		ArrayList<String> shortcutNames = new ArrayList<String>();
		ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList<ShortcutIconResource>();

		for(int i=0; i<SHORCUTS_LENGTH; i++) {
			shortcutNames.add(TITLES[i]);
			shortcutIcons.add(ShortcutIconResource.fromContext(mContext, ICONS[i]));
		}
		
		bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);
		bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);

		return bundle;
	}
	
}
