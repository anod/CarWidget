package com.anod.car.home.utils;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.view.KeyEvent;

/**
 * @author alex
 * @date 1/25/14
 */
public class MusicUtils {

/*
	public static void broadCastMediaCode(Integer keyCode, Context ctx) {
		Intent mediaButtonDownIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		KeyEvent downKe = new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, keyCode, 0);
		mediaButtonDownIntent.putExtra(Intent.EXTRA_KEY_EVENT, downKe);

		Intent mediaButtonUpIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		KeyEvent upKe = new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_UP, keyCode, 0);
		mediaButtonUpIntent.putExtra(Intent.EXTRA_KEY_EVENT, upKe);

		ctx.sendOrderedBroadcast(mediaButtonDownIntent, null, null, null, Activity.RESULT_OK, null, null);
		ctx.sendOrderedBroadcast(mediaButtonUpIntent, null, null, null, Activity.RESULT_OK, null, null);
	}
*/
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
					.getDeclaredMethod("checkService", String.class).invoke(null, Context.AUDIO_SERVICE);

			// get audioService from IAudioService.Stub.asInterface(IBinder)
			Object audioService = Class.forName("android.media.IAudioService$Stub")
					.getDeclaredMethod("asInterface", IBinder.class).invoke(null, iBinder);

			// Dispatch keyEvent using IAudioService.dispatchMediaKeyEvent(KeyEvent)
			Class.forName("android.media.IAudioService").getDeclaredMethod("dispatchMediaKeyEvent", KeyEvent.class)
					.invoke(audioService, keyEvent);
			hasDispatchSucceeded = true;
		} catch (Exception e) {
			AppLog.ex(e);
		}

		// If dispatchMediaKeyEvent failed then try using broadcast
		if (!hasDispatchSucceeded) {
			Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
			intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
			context.sendOrderedBroadcast(intent, null);
		}
	}



}
