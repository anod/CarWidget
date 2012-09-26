package com.anod.car.home.prefs;

import android.content.Intent;
import android.os.Bundle;

import com.anod.car.home.R;
import com.anod.car.home.model.NotificationShortcutsModel;
import com.anod.car.home.model.ShortcutsModel;
import com.anod.car.home.prefs.PickShortcutUtils.PreferenceKey;

public class ConfigurationNotifShortcuts extends ConfigurationActivity implements PreferenceKey {

	private PickShortcutUtils mPickShortcutUtils;

	@Override
	protected boolean isAppWidgetIdRequired() {
		return false;
	}

	@Override
	protected int getXmlResource() {
		return R.xml.preference_notif_shortcuts;
	}

	@Override
	protected void onCreateImpl(Bundle savedInstanceState) {

		ShortcutsModel model = new NotificationShortcutsModel(this);
		model.init();
		mPickShortcutUtils = new PickShortcutUtils(this, model, this);
		mPickShortcutUtils.onRestoreInstanceState(savedInstanceState);

		for (int i = 0; i < PreferencesStorage.NOTIFICATION_COMPONENT_NUMBER; i++) {
			mPickShortcutUtils.initLauncherPreference(i);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mPickShortcutUtils.onSaveInstanceState(outState);
	}
	
	@Override
	public String getInitialKey(int position) {
		return PreferencesStorage.getNotifComponentName(position);
	}

	@Override
	public String getCompiledKey(int position) {
		return PreferencesStorage.getNotifComponentName(position);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mPickShortcutUtils.onActivityResult(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}
}
