package com.anod.car.home.incar;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

public class ChangeBrightnessActivity extends Activity {

    public final static String EXTRA_BRIGHT_LEVEL = "bright_level";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        float bt = getIntent().getFloatExtra(EXTRA_BRIGHT_LEVEL, 1.0f);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = bt;
        getWindow().setAttributes(lp);

        final Activity activity = this;
        Thread t = new Thread() {
            public void run() {
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                }
                activity.finish();
            }
        };
        t.start();
    }

}
