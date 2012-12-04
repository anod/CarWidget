package com.anod.car.home;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.anod.car.home.appwidget.ShortcutPendingIntent;

public class ShortcutActivity extends Activity {
	public static final String EXTRA_INTENT = "intent";
	public static final String EXTRA_MEDIA_BUTTON = "media_button";
	
	public static final String ACTION_MEDIA_BUTTON = "action_media_button";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent().getParcelableExtra(EXTRA_INTENT);
		if (intent instanceof Intent) {
			runFromIntent(intent);
			finish();
			return;
		}
		Integer keyCode = getIntent().getIntExtra(EXTRA_MEDIA_BUTTON, 0);
		if (keyCode > 0) {
			broadCastMediaCode(keyCode);
		}
		finish();
	}

	private void broadCastMediaCode(Integer keyCode) {
        Intent mediaButtonDownIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        KeyEvent downKe = new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, keyCode, 0);
        mediaButtonDownIntent.putExtra(Intent.EXTRA_KEY_EVENT, downKe);

        Intent mediaButtonUpIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        KeyEvent upKe = new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_UP, keyCode, 0);
        mediaButtonUpIntent.putExtra(Intent.EXTRA_KEY_EVENT, upKe);
        
        sendOrderedBroadcast(mediaButtonDownIntent, null, null, null, Activity.RESULT_OK, null, null);
        sendOrderedBroadcast(mediaButtonUpIntent, null, null, null, Activity.RESULT_OK, null, null);
	}

	private void runFromIntent(Intent intent) {
		//fix for Galaxy s3
		String action = intent.getAction();
		if (action != null && action.equals(ShortcutPendingIntent.INTENT_ACTION_CALL_PRIVILEGED)) {
			intent.setAction(Intent.ACTION_CALL);
		}
		if (intent.getSourceBounds() == null) {
			intent.setSourceBounds(getIntent().getSourceBounds());
		}
		
		startActivitySafetly(intent);
	}
	
    private void startActivitySafetly(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.activity_not_found), Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, getString(R.string.activity_not_found), Toast.LENGTH_SHORT).show();
            Log.e("CarHomeWidget", "Widget does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity.", e);
        }
    }	
}
