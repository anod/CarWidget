package com.anod.car.home.incar;

import com.anod.car.home.prefs.preferences.InCar;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.utils.AppLog;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class BroadcastService extends Service {

    private static boolean sRegistered;

    public static void startService(Context context) {
        final Intent updateIntent = new Intent(context.getApplicationContext(), BroadcastService.class);
        context.startService(updateIntent);
    }

    public static void stopService(Context context) {
        final Intent receiverIntent = new Intent(context.getApplicationContext(), BroadcastService.class);
        context.stopService(receiverIntent);
    }

    @Override
    public void onDestroy() {
        unregister(this);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        register(this);
        return super.onStartCommand(intent, flags, startId);
    }

    private void register(Context context) {
        AppLog.d("BroadcastService::register");
        if (!sRegistered) {

            InCar prefs = PreferencesStorage.loadInCar(context);

            if (prefs.isActivityRequired()) {
                ActivityRecognitionClientService.startService(context);
            }

            ModeDetector.onRegister(context);

            if (!prefs.isHeadsetRequired()) {
                AppLog.d("Broadcast service is not required");
                stopSelf();
                return;
            }

            sRegistered = true;

            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_HEADSET_PLUG);
            ModeBroadcastReceiver receiver = ModeBroadcastReceiver.getInstance();
            context.registerReceiver(receiver, filter);
        }
    }

    private void unregister(Context context) {
        AppLog.d("BroadcastService::unregister");
        if (sRegistered) {
            ModeBroadcastReceiver receiver = ModeBroadcastReceiver.getInstance();
            context.unregisterReceiver(receiver);
        }
        sRegistered = false;
        ActivityRecognitionClientService.stopService(context);
    }


}
