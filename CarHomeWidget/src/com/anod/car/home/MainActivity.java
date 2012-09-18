package com.anod.car.home;

import com.anod.car.home.utils.TitleBarUtils;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class MainActivity extends Activity {

	private TitleBarUtils mTitleBarUtils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		mTitleBarUtils = new TitleBarUtils(this);
		mTitleBarUtils.setCustomTitleBar();
		mTitleBarUtils.setupActionBar();
	}

}
