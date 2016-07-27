package com.anod.car.home.incar;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

public class ChangeBrightnessActivity extends Activity {
    final static String EXTRA_BRIGHT_LEVEL = "bright_level";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        float bt = getIntent().getFloatExtra(EXTRA_BRIGHT_LEVEL, 1.0f);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = bt;
        getWindow().setAttributes(lp);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ChangeBrightnessActivity.this.finish();
            }
        }, 500);
    }
}
