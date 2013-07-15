package com.anod.car.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.anod.car.home.utils.Utils;

public class CarHomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.logd(" --- CarHomeActivity::onCreate ---");
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(startMain);
		finish();
	}

}
