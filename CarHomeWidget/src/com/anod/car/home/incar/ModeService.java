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
import android.view.View;
import android.widget.RemoteViews;

import com.anod.car.home.Provider;
import com.anod.car.home.R;
import com.anod.car.home.ShortcutPendingIntent;
import com.anod.car.home.model.NotificationShortcutsModel;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.prefs.PreferencesStorage;
import com.anod.car.home.prefs.preferences.InCar;
import com.anod.car.home.utils.Utils;

public class ModeService extends Service{
	private static final String PREFIX_NOTIF = "notif";
	private ModePhoneStateListener mPhoneListener;
	private int[] sBtnIds = {
		R.id.btn0,
		R.id.btn1,
		R.id.btn2,
		R.id.btn3
	};
	private boolean mForceState = false;
	public static boolean sInCarMode = false;
	private static final int NOTIFICATION_ID = 1;
	public static String EXTRA_MODE = "extra_mode";
	public static String EXTRA_FORCE_STATE = "extra_force_state";
	public static final int MODE_SWITCH_OFF = 1;
	public static final int MODE_SWITCH_ON = 0;

	@Override
	public void onCreate() {
		
		super.onCreate();
	}

	private Notification createNotification() {
		Intent notificationIntent = new Intent(this, ModeService.class);
		notificationIntent.putExtra(EXTRA_MODE, MODE_SWITCH_OFF);
    	Uri data = Uri.parse("com.anod.car.home.pro://mode/0/");
    	notificationIntent.setData(data);
		
		PendingIntent contentIntent = PendingIntent.getService(this, 0, notificationIntent, 0);

		Notification notification = new Notification();
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.icon = R.drawable.ic_stat_incar;
		if (Utils.IS_HONEYCOMB_OR_GREATER) {
			RemoteViews contentView = createShortcuts();
			notification.contentIntent = contentIntent;
			notification.contentView = contentView;
		} else {
			String notifTitle=getResources().getString(R.string.incar_mode_enabled);
			String notifText=getResources().getString(R.string.click_to_disable);
			notification.setLatestEventInfo(this, notifTitle, notifText, contentIntent);
		}
		return notification;
	}

	private RemoteViews createShortcuts() {
		RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification);
		NotificationShortcutsModel model = new NotificationShortcutsModel(this);
		model.init();
		boolean viewGone = true;
		for (int i = 0; i < model.getCount(); i++) {
			ShortcutInfo info = model.getShortcut(i);
			int resId = sBtnIds[i];
			if (info == null) {
				contentView.setViewVisibility(resId, (viewGone) ? View.GONE : View.INVISIBLE);
			} else {
				viewGone = false;
				contentView.setImageViewBitmap(resId, info.getIcon());
				PendingIntent pendingIntent = ShortcutPendingIntent.getShortcutPendingInent(info.intent, PREFIX_NOTIF, this, i);
				contentView.setOnClickPendingIntent(resId, pendingIntent);
			}
		}
		return contentView;
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
		
		Notification notification = createNotification();
		startForeground(NOTIFICATION_ID, notification);

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
