package com.anod.car.home.prefs;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.anod.car.home.R;
import com.anod.car.home.prefs.ActivityPicker.PickAdapter.Item;
import com.anod.car.home.utils.IntentUtils;
import com.anod.car.home.utils.Version;

public class CarWidgetShortcutsPicker extends ActivityPicker {

	private static final int SHORCUTS_LENGTH = 5;
	public static final int IDX_TUNEIN = 4;
	
	
	private static final int[] ICONS = {
		R.drawable.ic_launcher,
		R.drawable.ic_media_play_pause,
		R.drawable.ic_media_next,
		R.drawable.ic_media_prev,
		0
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Version v = new Version(this);
		setTitle(v.isFree() ? R.string.car_widget_shortcuts : R.string.car_widget_pro_shortcuts);

	}
	@Override
	protected List<Item> getItems() {
		List<PickAdapter.Item> items = new ArrayList<PickAdapter.Item>();
		Resources r = getResources();
		String[] titles = r.getStringArray(R.array.carwidget_shortcuts);
		for(int i=0; i< SHORCUTS_LENGTH; i++) {
			PickAdapter.Item item = null;
			if (i == IDX_TUNEIN){
				item = addTuneInShortcut(titles[i]);
			} else {
				Intent intent = IntentUtils.createPickShortcutLocalIntent(i, titles[i], ICONS[i], this);
				item = new PickAdapter.Item(this, titles[i], r.getDrawable(ICONS[i]), intent);
			}
			if (item!=null) {
				items.add(item);
			}
		}
		return items;
	}

	private Item addTuneInShortcut(String title) {
		final PackageManager pm = getPackageManager();

		boolean tuneInPro = true;
		Drawable icon = IntentUtils.getApplicationIcon(pm, new ComponentName(IntentUtils.TUNEIN_PRO_PKG, IntentUtils.TUNEIN_PRO_CLS));
		if (icon == null) {
			tuneInPro = false;
			icon = IntentUtils.getApplicationIcon(pm, new ComponentName(IntentUtils.TUNEIN_FREE_PKG, IntentUtils.TUNEIN_FREE_CLS));
		}
		if (icon == null) {
			return null;
		}

		Intent launchIntent = IntentUtils.createPickShortcutAppIntent(title, icon, IntentUtils.createTuneInIntent(tuneInPro), this);
		return new PickAdapter.Item(this, title, icon, launchIntent);
	}




}
