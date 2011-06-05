package com.anod.car.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class CarHomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("CarHomeWidget", " --- CarHomeActivity::onCreate ---");
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(startMain);
		finish();
	}


	@Override
	protected void onDestroy() {
//		Log.d("CarHomeWidget", " --- CarHomeActivity::onDestroy ---");
		super.onDestroy();
	}


	@Override
	protected void onPause() {
//		Log.d("CarHomeWidget", " --- CarHomeActivity::onPause ---");
		super.onPause();
	}


	@Override
	protected void onStart() {
//		Log.d("CarHomeWidget", " --- CarHomeActivity::onStart ---");
		super.onStart();
	}


	@Override
	protected void onStop() {
//		Log.d("CarHomeWidget", " --- CarHomeActivity::onStop ---");
		super.onStop();
	}


	@Override
	protected void onResume() {
//		Log.d("CarHomeWidget", " --- CarHomeActivity::onResume ---");
		super.onResume();
	}

}
