package com.anod.car.home.prefs;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Parcelable;
import android.view.KeyEvent;

import com.anod.car.home.R;
import com.anod.car.home.ShortcutActivity;
import com.anod.car.home.incar.SwitchInCarActivity;
import com.anod.car.home.prefs.ActivityPicker.PickAdapter.Item;

public class CarWidgetShortcutsPicker extends ActivityPicker {
	private static final int SHORCUTS_LENGTH = 5;
	
	private static final String[] TITLES = {
		"Switch InCar",
		"Play/Pause",
		"Next",
		"Previous",
		"TuneIn Radio Car Mode"
	};

	private static final int[] ICONS = {
		R.drawable.ic_launcher,
		R.drawable.ic_media_play_pause,
		R.drawable.ic_media_next,
		R.drawable.ic_media_prev,
		R.drawable.ic_tunein
	};
	

	@Override
	protected List<Item> getItems() {
		List<PickAdapter.Item> items = new ArrayList<PickAdapter.Item>();
		Resources r = getResources();
		for(int i=0; i< SHORCUTS_LENGTH; i++) {
			Intent intent = createIntent(i,TITLES[i], ICONS[i]);
			items.add(new PickAdapter.Item(this, TITLES[i], r.getDrawable(ICONS[i]), intent));
		}
		return items;
	}

	private Intent createIntent(int i, String title, int icnResId) {
		Intent shortcutIntent = createShortcutIntent(i);
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
        Parcelable iconResource = Intent.ShortcutIconResource.fromContext(this,  icnResId);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
        
		return intent;
	}


	private Intent createShortcutIntent(int i) {        

		switch(i) {
			case 0:
				return new Intent(this, SwitchInCarActivity.class);
			case 1:
				return creatMediaButtonIntent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
			case 2:
				return creatMediaButtonIntent(KeyEvent.KEYCODE_MEDIA_NEXT);
			case 3:
				return creatMediaButtonIntent(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
			case 4:
				Intent localIntent = new Intent(Intent.ACTION_RUN);
				localIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				localIntent.setClassName("radiotime.player", "tunein.player.pro.Proxy");
				Uri data = Uri.parse("radiotime.player://carmode");
				localIntent.setData(data);
				return localIntent;
			default:
		}
		return null;
	}

	private Intent creatMediaButtonIntent(int keyCode) {
		Intent shortcutIntent = new Intent(this,ShortcutActivity.class);
		shortcutIntent.setAction(ShortcutActivity.ACTION_MEDIA_BUTTON);
		shortcutIntent.putExtra(ShortcutActivity.EXTRA_MEDIA_BUTTON, keyCode);
		
		return shortcutIntent;
	}
	/*
	 *     public static boolean isMediaButton(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD || keyCode == KeyEvent.KEYCODE_MEDIA_NEXT
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS
                || keyCode == KeyEvent.KEYCODE_MEDIA_REWIND || keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KEYCODE_MEDIA_PLAY || keyCode == KEYCODE_MEDIA_PAUSE;
    }
	 */
}
