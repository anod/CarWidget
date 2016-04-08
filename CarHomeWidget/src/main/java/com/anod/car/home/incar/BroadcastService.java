package com.anod.car.home.incar;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.anod.car.home.app.StoppableService;
import com.anod.car.home.prefs.preferences.InCarInterface;
import com.anod.car.home.prefs.preferences.InCarStorage;
import com.anod.car.home.utils.AppLog;

public class BroadcastService extends StoppableService {

    private ModeBroadcastReceiver mReceiver;

    public static void startService(Context context) {
        final Intent updateIntent = new Intent(context.getApplicationContext(), BroadcastService.class);
        context.startService(updateIntent);
    }

    public static void stopService(Context context) {
        final Intent receiverIntent = new Intent(context.getApplicationContext(), BroadcastService.class);
        fillStopIntent(receiverIntent);
        context.startService(receiverIntent);
    }

    @Override

    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onAfterStart(Intent intent) {
        register(this);
    }

    @Override
    protected void onBeforeStop(Intent intent) {
        unregister(this);
    }

    private void register(Context context) {
        AppLog.d("BroadcastService::register");
        if (mReceiver == null) {

            ModeDetector.onRegister(context);
            InCarInterface prefs = InCarStorage.load(context);
            if (prefs.isActivityRequired()) {
                AppLog.d("ActivityRecognitionClientService started");
                ActivityRecognitionClientService.startService(context);
            }

            if (!isServiceRequired(prefs)) {
                AppLog.d("Broadcast service is not required");
                stopSelf();
                return;
            }

            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_HEADSET_PLUG);
            mReceiver = ModeBroadcastReceiver.create();
            context.registerReceiver(mReceiver, filter);
        }
    }

    public static boolean isServiceRequired(InCarInterface prefs) {
        ModeDetector.updatePrefState(prefs);
        boolean[] states = ModeDetector.getPrefState();

        for(int i=0;i<states.length;i++) {
            if (i == ModeDetector.FLAG_ACTIVITY) {
                continue;
            }
            if (states[i]) {
                return true;
            }
        }
        return false;
    }

    private void unregister(Context context) {
        AppLog.d("BroadcastService::unregister");
        if (mReceiver != null) {
            context.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        InCarInterface prefs = InCarStorage.load(context);

        if (!prefs.isActivityRequired()) {
            ActivityRecognitionClientService.stopService(context);
        }
    }


}
