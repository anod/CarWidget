package com.anod.car.home.incar;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.anod.car.home.Provider;
import com.anod.car.home.R;
import com.anod.car.home.prefs.PreferencesStorage;
import com.anod.car.home.prefs.preferences.InCar;

public class ModeService extends Service{
	private ModePhoneStateListener mPhoneListener;
	private boolean mForceState = false;
	public static boolean sInCarMode = false;
	private static final int NOTIFICATION_ID = 1;
	public static String EXTRA_MODE = "extra_mode";
	public static String EXTRA_FORCE_STATE = "extra_force_state";
	public static final int MODE_SWITCH_OFF = 1;
	public static final int MODE_SWITCH_ON = 0;

	@Override
	public void onCreate() {
		String notifTitle=getResources().getString(R.string.incar_mode_enabled);
		String notifText=getResources().getString(R.string.click_to_disable);
		long when = System.currentTimeMillis();
		Notification notification = new Notification(R.drawable.ic_stat_incar, notifTitle, when);
		
		Intent notificationIntent = new Intent(this, ModeService.class);
		notificationIntent.putExtra(EXTRA_MODE, MODE_SWITCH_OFF);
    	Uri data = Uri.parse("com.anod.car.home.pro://mode/0/");
    	notificationIntent.setData(data);
		
		PendingIntent contentIntent = PendingIntent.getService(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(this, notifTitle, notifText, contentIntent);
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		startForeground(NOTIFICATION_ID, notification);
		
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		stopForeground(true);
		InCar prefs = PreferencesStorage.loadInCar(this);
		if (mForceState) {
			Handler.forceState(prefs, false);
		}
		Handler.switchOff(prefs,this);
        if (mPhoneListener != null) {
        	detachPhoneListener();
        }
		sInCarMode = false;
		requestWidgetsUpdate();
		super.onDestroy();
	}

	private void requestWidgetsUpdate() {
        Provider appWidgetProvider = Provider.getInstance();
        appWidgetProvider.performUpdate(this, null);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		// Tracking a bug
		if (intent == null) {
			Log.d("CarHomeWidget", "Intent is null...");
		}
		mForceState = intent.getBooleanExtra(EXTRA_FORCE_STATE, false);
		if (intent.getIntExtra(EXTRA_MODE, MODE_SWITCH_ON) == MODE_SWITCH_OFF) {
			stopSelf();
			return START_STICKY;
		}
		InCar prefs = PreferencesStorage.loadInCar(this);
		sInCarMode = true;
		if (mForceState) {
			Handler.forceState(prefs, true);
		}
		Handler.switchOn(prefs,this);
		handlePhoneListener(prefs);
		requestWidgetsUpdate();
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	private void handlePhoneListener(InCar prefs) {
    	if ( prefs.isAutoSpeaker() || 
    		!prefs.getAutoAnswer().equals(PreferencesStorage.AUTOANSWER_DISABLED)
    	) {
    		if (mPhoneListener == null) {
    			attachPhoneListener();
    		}
    		mPhoneListener.setActions(prefs.isAutoSpeaker(), prefs.getAutoAnswer());
    	} else {
            if (mPhoneListener != null) {
            	detachPhoneListener();
            }
    	}		
	}
	
	private void attachPhoneListener() {
		Log.d("CarHomeWidget", "Set phone listener");
		TelephonyManager tm = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
		mPhoneListener = new ModePhoneStateListener(this);
		tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
	}
	
	private void detachPhoneListener() {
    	Log.d("CarHomeWidget", "Remove phone listener");
    	TelephonyManager tm = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
    	tm.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
    	mPhoneListener.cancelActions();
    	mPhoneListener = null;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
