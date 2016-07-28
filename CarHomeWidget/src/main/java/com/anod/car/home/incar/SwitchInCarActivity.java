package com.anod.car.home.incar;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.anod.car.home.R;
import com.anod.car.home.prefs.ConfigurationActivity;
import com.anod.car.home.prefs.ConfigurationInCar;
import com.anod.car.home.prefs.model.InCarStorage;

public class SwitchInCarActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (InCarStorage.load(this).isInCarEnabled()) {
            Uri data;
            Intent service = new Intent(this, ModeService.class);
            if (ModeService.sInCarMode) {
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
        } else {
            Toast.makeText(this, R.string.incar_mode_disabled, Toast.LENGTH_LONG).show();

            Intent intent = ConfigurationActivity.createFragmentIntent(this, ConfigurationInCar.class);
            startActivity(intent);
        }
        finish();
    }


}
