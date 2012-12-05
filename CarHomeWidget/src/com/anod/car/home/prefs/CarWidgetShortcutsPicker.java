package com.anod.car.home.prefs;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcelable;
import android.view.KeyEvent;

import com.anod.car.home.R;
import com.anod.car.home.ShortcutActivity;
import com.anod.car.home.incar.SwitchInCarActivity;
import com.anod.car.home.prefs.ActivityPicker.PickAdapter.Item;
import com.anod.car.home.utils.UtilitiesBitmap;

public class CarWidgetShortcutsPicker extends ActivityPicker {
	private static final String TUNEIN_FREE_CLS = "tunein.player.Activity";
	private static final String TUNEIN_FREE_PKG = "tunein.player";
	
	private static final String TUNEIN_PRO_CLS = "tunein.player.pro.Activity";
	private static final String TUNEIN_PRO_PKG = "radiotime.player";
	
	private static final int SHORCUTS_LENGTH = 5;
	private static final int IDX_TUNEIN = 4;
	
	
	private static final int[] ICONS = {
		R.drawable.ic_launcher,
		R.drawable.ic_media_play_pause,
		R.drawable.ic_media_next,
		R.drawable.ic_media_prev,
		0
	};
	

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
				Intent intent = createLocalIntent(i,titles[i], ICONS[i]);
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
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(new ComponentName(TUNEIN_PRO_PKG,TUNEIN_PRO_CLS));

		ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);
		if (resolveInfo == null) {
			tuneInPro = false;
			intent.setComponent(new ComponentName(TUNEIN_FREE_PKG,TUNEIN_FREE_CLS));
			resolveInfo = pm.resolveActivity(intent, 0);
		}
		if (resolveInfo == null) {
			return null;
		}
		
		Drawable icon = resolveInfo.activityInfo.loadIcon(pm);

		Intent launchIntent = createAppIntent(title, icon, createTuneInIntent(tuneInPro));
		return new PickAdapter.Item(this, title, icon, launchIntent);
	}

	private Intent createAppIntent(String title, Drawable icon, Intent shortcutIntent) {
        Bitmap bitmap = UtilitiesBitmap.createIconBitmap(icon, this);
        Intent intent = commonLaunchIntent(title, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap);
		return intent;
	}

	private Intent createLocalIntent(int i, String title, int icnResId) {
		Intent shortcutIntent = createShortcutIntent(i);
        Intent intent = commonLaunchIntent(title, shortcutIntent);
        Parcelable iconResource = Intent.ShortcutIconResource.fromContext(this,  icnResId);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
		return intent;
	}
	
	private Intent commonLaunchIntent(String title, Intent shortcutIntent) {
		Intent intent = new Intent();
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
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
			default:
		}
		return null;
	}

	private Intent createTuneInIntent(boolean tuneInPro) {
		Intent localIntent = new Intent(Intent.ACTION_RUN);
		localIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		Uri data;
		if (tuneInPro) {
			localIntent.setClassName(TUNEIN_PRO_PKG, "tunein.player.pro.Proxy");
			data = Uri.parse(TUNEIN_PRO_PKG + "://carmode");
		} else {
			localIntent.setClassName(TUNEIN_FREE_PKG, "tunein.player.Proxy");
			data = Uri.parse(TUNEIN_FREE_PKG + "://carmode");
		}
		localIntent.setData(data);
		return localIntent;		
	}
	
	private Intent creatMediaButtonIntent(int keyCode) {
		Intent shortcutIntent = new Intent(this,ShortcutActivity.class);
		shortcutIntent.setAction(ShortcutActivity.ACTION_MEDIA_BUTTON);
		shortcutIntent.putExtra(ShortcutActivity.EXTRA_MEDIA_BUTTON, keyCode);
		
		return shortcutIntent;
	}

}
