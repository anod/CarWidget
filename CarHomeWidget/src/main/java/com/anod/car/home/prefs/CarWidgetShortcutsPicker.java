package com.anod.car.home.prefs;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import com.anod.car.home.R;
import com.anod.car.home.prefs.ActivityPicker.PickAdapter.Item;
import com.anod.car.home.utils.IntentUtils;

import java.util.ArrayList;
import java.util.List;

public class CarWidgetShortcutsPicker extends ActivityPicker {

	private static final int ITEMS_NUM = 4;


	private static final int[] ICONS = {
		R.drawable.ic_launcher_carwidget,
		R.drawable.ic_media_play_pause,
		R.drawable.ic_media_next,
		R.drawable.ic_media_prev
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.car_widget_shortcuts);
	}

	@Override
	protected List<Item> getItems() {
		List<PickAdapter.Item> items = new ArrayList<PickAdapter.Item>();
		Resources r = getResources();
		String[] titles = r.getStringArray(R.array.carwidget_shortcuts);
		for(int i=0; i< ITEMS_NUM; i++) {
			Intent intent = IntentUtils.createPickShortcutLocalIntent(i, titles[i], ICONS[i], this);
            PickAdapter.Item item = new PickAdapter.Item(this, titles[i], r.getDrawable(ICONS[i]), intent);
			items.add(item);
		}
		return items;
	}

}
