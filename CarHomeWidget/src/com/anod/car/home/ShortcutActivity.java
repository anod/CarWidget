package com.anod.car.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ShortcutActivity extends Activity {
	public static final String EXTRA_INTENT = "intent";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent().getParcelableExtra(EXTRA_INTENT);
		if (intent != null) {
			Log.d("CarHomeWidget"," Strating acticity: " + intent.toString());
			if (intent.getSourceBounds() == null) {
				intent.setSourceBounds(getIntent().getSourceBounds());
			}
			startActivity(intent);
		}
		finish();
	}
}
