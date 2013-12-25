package com.anod.car.home.incar;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.anod.car.home.Provider;
import com.anod.car.home.R;
import com.anod.car.home.appwidget.ShortcutPendingIntent;
import com.anod.car.home.model.NotificationShortcutsModel;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.prefs.preferences.InCar;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.utils.Utils;
import com.anod.car.home.utils.Version;

public class ModeService extends Service {
	private static final String PREFIX_NOTIF = "notif";
	private ModePhoneStateListener mPhoneListener;
	private static final int[] NOTIF_BTN_IDS = { R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3 };
	public static boolean sInCarMode;
	private static final int DEBUG_ID = 3;
	private static final int EXPIRED_ID = 2;
	private static final int NOTIFICATION_ID = 1;
	public static final String EXTRA_MODE = "extra_mode";
	public static final String EXTRA_FORCE_STATE = "extra_force_state";
	public static final int MODE_SWITCH_OFF = 1;
	public static final int MODE_SWITCH_ON = 0;

	private boolean mForceState;

	private void showExpiredNotification() {
		Notification notification = new Notification();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.icon = R.drawable.ic_stat_incar;
		String notifTitle = getResources().getString(R.string.notif_expired);
		String notifText = getResources().getString(R.string.notif_consider);
		notification.tickerText = notifTitle;
		notification.setLatestEventInfo(this, notifTitle, notifText, PendingIntent.getActivity(this, 0, new Intent(), 0));

		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager notificationManager = (NotificationManager) getSystemService(ns);
		notificationManager.notify(EXPIRED_ID, notification);
		notificationManager.cancel(EXPIRED_ID);
	}

	private Notification createNotification(Version version) {
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
			if (version.isFree()) {
				contentView.setTextViewText(R.id.text, getString(R.string.click_to_disable_trial, version.getTrialTimesLeft()));
			}
			notification.contentIntent = contentIntent;
			notification.contentView = contentView;
			setNotificationPriority(notification);
		} else {
			String notifTitle = getString(R.string.incar_mode_enabled);
			String notifText;
			if (version.isFree()) {
				notifText = getString(R.string.click_to_disable_trial, version.getTrialTimesLeft());
				notification.tickerText = getResources().getQuantityString(R.plurals.notif_activations_left, version.getTrialTimesLeft(), version.getTrialTimesLeft());
			} else {
				notifText = getString(R.string.click_to_disable);
			}
			notification.setLatestEventInfo(this, notifTitle, notifText, contentIntent);
		}
		return notification;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void setNotificationPriority(Notification notification) {
		if (Utils.IS_JELLYBEAN_OR_GREATER) {
			notification.priority = Notification.PRIORITY_MAX;
		}
	}

	private RemoteViews createShortcuts() {
		RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification);
		NotificationShortcutsModel model = new NotificationShortcutsModel(this);
		model.init();
		boolean viewGone = true;
		ShortcutPendingIntent spi = new ShortcutPendingIntent(this);
		for (int i = 0; i < model.getCount(); i++) {
			ShortcutInfo info = model.getShortcut(i);
			int resId = NOTIF_BTN_IDS[i];
			if (info == null) {
				contentView.setViewVisibility(resId, (viewGone) ? View.GONE : View.INVISIBLE);
			} else {
				viewGone = false;
				contentView.setImageViewBitmap(resId, info.getIcon());
				PendingIntent pendingIntent = spi.createShortcut(info.intent, PREFIX_NOTIF, i);
				contentView.setOnClickPendingIntent(resId, pendingIntent);
			}
		}
		return contentView;
	}

	@Override
	public void onDestroy() {
		stopForeground(true);
		// if (sInCarMode) {
		InCar prefs = PreferencesStorage.loadInCar(this);
		if (mForceState) {
			ModeDetector.forceState(prefs, false);
		}
		ModeDetector.switchOff(prefs, this);
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
	public int onStartCommand(Intent intent, int flags, int startId) {
		// If service killed
		if (intent == null) {
			mForceState = PreferencesStorage.restoreForceState(this);
			Log.d("CarHomeWidget", "Intent is null... sInCarMode = " + sInCarMode + ", mForceState = " + mForceState);
		} else {
			if (intent.getIntExtra(EXTRA_MODE, MODE_SWITCH_ON) == MODE_SWITCH_OFF) {
				stopSelf();
				return START_STICKY;
			}
			mForceState = intent.getBooleanExtra(EXTRA_FORCE_STATE, false);
		}

		Version version = new Version(this);
		if (version.isFreeAndTrialExpired()) {
			showExpiredNotification();
			stopSelf();
			return START_STICKY;
		}

		InCar prefs = PreferencesStorage.loadInCar(this);
		sInCarMode = true;
		if (mForceState) {
			ModeDetector.forceState(prefs, true);
		}
		ModeDetector.switchOn(prefs, this);
		handlePhoneListener(prefs);
		requestWidgetsUpdate();

		if (version.isFree()) {
			version.increaseTrialCounter();
		}

		Notification notification = createNotification(version);
		startForeground(NOTIFICATION_ID, notification);

		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	private void handlePhoneListener(InCar prefs) {
		if (prefs.isAutoSpeaker() || !prefs.getAutoAnswer().equals(InCar.AUTOANSWER_DISABLED)) {
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
		TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		mPhoneListener = new ModePhoneStateListener(this);
		tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
	}

	private void detachPhoneListener() {
		Log.d("CarHomeWidget", "Remove phone listener");
		TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
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
