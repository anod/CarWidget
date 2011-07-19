package com.anod.car.home.incar;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.WindowManager;

import com.anod.car.home.Utils;

public class ChangeBrightnessActivity extends Activity {
	public static String EXTRA_BRIGHT_LEVEL = "bright_level";
	public static String EXTRA_PACKAGE_NAMES = "package_names";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		float bt = getIntent().getFloatExtra(EXTRA_BRIGHT_LEVEL, 1.0f);
		
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = bt;
		getWindow().setAttributes(lp);
		
		ArrayList<String> packageNames = getIntent().getStringArrayListExtra(EXTRA_PACKAGE_NAMES);
		int size = packageNames.size();
		String[] params = new String[size];
		for(int i=0; i<size; i++) {
			ComponentName comp = Utils.stringToComponent(packageNames.get(i));
			params[i] = comp.getPackageName();
		}
		StopAppsTask task = new StopAppsTask();
		task.setActivityManager((ActivityManager)getSystemService(Activity.ACTIVITY_SERVICE));
		task.execute(params);
		
        final Activity activity = this;
        Thread t = new Thread(){
            public void run()
            {
                try {
                    sleep(500);
                } catch (InterruptedException e) {}
                activity.finish();
            }
        };
        t.start();
	}

}
