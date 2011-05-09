package com.anod.car.home.incar;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

import com.anod.car.home.Provider;
import com.anod.car.home.R;

public class ModeService extends Service{
	public static boolean sInCarMode = false;
	private static final int NOTIFICATION_ID = 1;
	public static String EXTRA_MODE = "extra_mode";
	public static final int MODE_SWITCH_OFF = 1;
	public static final int MODE_SWITCH_ON = 0;

	@Override
	public void onCreate() {
		String notifTitle=getResources().getString(R.string.incar_mode_enabled);
		String notifText=getResources().getString(R.string.click_to_disable);
		long when = System.currentTimeMillis();
		Notification notification = new Notification(R.drawable.notification_icon, notifTitle, when);
		
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
		Handler.switchOff(this);
		sInCarMode = false;
		requestWidgetsUpdate();
		super.onDestroy();
	}

	private void requestWidgetsUpdate() {
        Provider appWidgetProvider = Provider.getInstance();
        appWidgetProvider.performUpdate(this, null);
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
		if (intent.getIntExtra(EXTRA_MODE, MODE_SWITCH_ON) == MODE_SWITCH_OFF) {
			stopSelf();
			return;
		}
		sInCarMode = true;
		Handler.switchOn(this);
		requestWidgetsUpdate();		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
