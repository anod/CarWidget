package com.anod.car.home;

import com.anod.car.home.utils.AppLog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class CarHomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppLog.d(" --- CarHomeActivity::onCreate ---");
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
        finish();
    }

}
