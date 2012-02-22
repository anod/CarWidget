package com.anod.car.home.incar;

import com.anod.car.home.prefs.PreferencesStorage;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class SwitchInCarActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (PreferencesStorage.isInCarModeEnabled(this)) {
			Uri data;
			Intent service = new Intent(this, ModeService.class);
			if (ModeService.sInCarMode == true) {
				service.putExtra(ModeService.EXTRA_MODE, ModeService.MODE_SWITCH_OFF);
				service.putExtra(ModeService.EXTRA_FORCE_STATE, true);		
		    	data = Uri.parse("com.anod.car.home.pro://mode/0/2");
			} else {
				service.putExtra(ModeService.EXTRA_MODE, ModeService.MODE_SWITCH_ON);
				service.putExtra(ModeService.EXTRA_FORCE_STATE, true);	
		    	data = Uri.parse("com.anod.car.home.pro://mode/1/2");
			}
			service.setData(data);
			startService(service);
		}
		finish();
	}

	
}
