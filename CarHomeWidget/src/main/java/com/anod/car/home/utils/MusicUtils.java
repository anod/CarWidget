package com.anod.car.home.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.KeyEvent;

import info.anodsplace.android.log.AppLog;

/**
 * @author alex
 * @date 1/25/14
 */
public class MusicUtils {

    public static void sendKeyEvent(int key, Context context) {
        AppLog.d("Sending event key " + key);
        handleMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, key), context);
        handleMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, key), context);
    }

    private static void handleMediaKeyEvent(KeyEvent keyEvent, Context context) {
        boolean hasDispatchSucceeded = false;
        try {
            // Get binder from ServiceManager.checkService(String)
            IBinder iBinder = (IBinder) Class.forName("android.os.ServiceManager")
                    .getDeclaredMethod("checkService", String.class)
                    .invoke(null, Context.AUDIO_SERVICE);

            // get audioService from IAudioService.Stub.asInterface(IBinder)
            Object audioService = Class.forName("android.media.IAudioService$Stub")
                    .getDeclaredMethod("asInterface", IBinder.class).invoke(null, iBinder);

            // Dispatch keyEvent using IAudioService.dispatchMediaKeyEvent(KeyEvent)
            Class.forName("android.media.IAudioService")
                    .getDeclaredMethod("dispatchMediaKeyEvent", KeyEvent.class)
                    .invoke(audioService, keyEvent);
            hasDispatchSucceeded = true;
        } catch (Exception e) {
            AppLog.e(e);
        }

        // If dispatchMediaKeyEvent failed then try using broadcast
        if (!hasDispatchSucceeded) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
            context.sendOrderedBroadcast(intent, null);
        }
    }

    public static void sendKeyEventComponent(int key, Context context, ComponentName component,
            boolean start) {
        Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        downIntent.putExtra(Intent.EXTRA_KEY_EVENT,
                new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                        KeyEvent.ACTION_DOWN, key, 0));
        downIntent.setComponent(component);

        Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        upIntent.putExtra(Intent.EXTRA_KEY_EVENT,
                new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                        KeyEvent.ACTION_UP, key, 0));
        upIntent.setComponent(component);

        if (start) {
            Intent startIntent = context.getPackageManager()
                    .getLaunchIntentForPackage(component.getPackageName());
            if (startIntent != null) {
                context.startActivity(startIntent);
            }
        }

        context.sendOrderedBroadcast(downIntent, null, null, null, -1, null, null);
        context.sendOrderedBroadcast(upIntent, null, null, null, -1, null, null);
    }


}
