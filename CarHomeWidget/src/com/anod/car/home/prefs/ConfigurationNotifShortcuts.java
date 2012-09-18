package com.anod.car.home.prefs;

import android.os.Bundle;

import com.anod.car.home.R;

public class ConfigurationNotifShortcuts extends ConfigurationActivity {

	@Override
	protected boolean isAppWidgetIdRequired() {
		return false;
	}

	@Override
	protected int getTitleResource() {
		return R.string.shortcuts;
	}

	@Override
	protected int getXmlResource() {
		return R.xml.preference_notif_shortcuts;
	}

	@Override
	protected void onCreateImpl(Bundle savedInstanceState) {

	}

}
