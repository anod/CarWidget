package com.anod.car.home.incar;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.anod.car.home.CarWidgetApplication;
import com.anod.car.home.ObjectGraph;
import com.anod.car.home.Provider;
import com.anod.car.home.prefs.preferences.InCar;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.utils.AppLog;
import com.anod.car.home.utils.Version;

public class ModeService extends Service {
    private static final String TAG = "CarHomeWidget.WakeLock";

    public static final String EXTRA_MODE = "extra_mode";
    public static final String EXTRA_FORCE_STATE = "extra_force_state";

    private static final int NOTIFICATION_ID = 1;
    public static final int MODE_SWITCH_OFF = 1;
    public static final int MODE_SWITCH_ON = 0;

    public static boolean sInCarMode;

    private ModePhoneStateListener mPhoneListener;

    Handler mHandler;

	private boolean mForceState;

    private static volatile PowerManager.WakeLock sLockStatic=null;

    synchronized private static PowerManager.WakeLock getLock(Context context) {
        if (sLockStatic == null) {
            PowerManager mgr=(PowerManager)context.getSystemService(Context.POWER_SERVICE);

            sLockStatic=mgr.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
            //sLockStatic.setReferenceCounted(false);
        }

        return(sLockStatic);
    }

    public static boolean isWakeLockHeld(Context context) {
        PowerManager.WakeLock lock = ModeService.getLock(context.getApplicationContext());
        return lock.isHeld();
    }

    public static void acquireWakeLock(Context context) {
        PowerManager.WakeLock lock = ModeService.getLock(context.getApplicationContext());
        if (!lock.isHeld()) {
            AppLog.d("WakeLock is not held");
            lock.acquire();
        }
        AppLog.d("WakeLock acquired");
    }

    public static void releaseWakeLock(Context context) {
        PowerManager.WakeLock lock=ModeService.getLock(context.getApplicationContext());

        if (lock.isHeld()) {
            AppLog.d("WakeLock is held");
            lock.release();
        }
        AppLog.d("WakeLock released");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mHandler = CarWidgetApplication.provide(this).getHandler();

    }

    @Override
	public void onDestroy() {
		stopForeground(true);

		InCar prefs = PreferencesStorage.loadInCar(this);
		if (mForceState) {
			ModeDetector.forceState(prefs, false);
		}
		ModeDetector.switchOff(prefs, mHandler);
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

        boolean redelivered = (flags & START_FLAG_REDELIVERY) == START_FLAG_REDELIVERY;
        AppLog.d("ModeService onStartCommand, sInCarMode = " + sInCarMode + ", redelivered = " + redelivered);


		// If service killed
		if (intent == null) {
			mForceState = PreferencesStorage.restoreForceState(this);
			AppLog.d("Intent is null... sInCarMode = " + sInCarMode + ", mForceState = " + mForceState);
		} else {
			if (intent.getIntExtra(EXTRA_MODE, MODE_SWITCH_ON) == MODE_SWITCH_OFF) {
				stopSelf();
				return START_NOT_STICKY;
			}
			mForceState = intent.getBooleanExtra(EXTRA_FORCE_STATE, false);
		}

		Version version = new Version(this);
		if (version.isFreeAndTrialExpired()) {
			ModeNotification.showExpiredNotification(this);
			stopSelf();
			return START_NOT_STICKY;
		}

		InCar prefs = PreferencesStorage.loadInCar(this);
		sInCarMode = true;
		if (mForceState) {
			ModeDetector.forceState(prefs, true);
		}

        ModeDetector.switchOn(prefs, mHandler);
		initPhoneListener(prefs);
		requestWidgetsUpdate();

		if (version.isFree()) {
			version.increaseTrialCounter();
		}

		Notification notification = ModeNotification.createNotification(version, this);
		startForeground(NOTIFICATION_ID, notification);

		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_REDELIVER_INTENT;
	}

	private void initPhoneListener(InCar prefs) {
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
		AppLog.d("Attach phone listener");
        ObjectGraph og = CarWidgetApplication.provide(this);
		mPhoneListener = og.getModePhoneStateListener();
        TelephonyManager tm = og.getTelephonyManager();
        tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
	}

	private void detachPhoneListener() {
		AppLog.d("Detach phone listener");
		TelephonyManager tm = CarWidgetApplication.provide(this).getTelephonyManager();
		tm.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
		mPhoneListener.cancelActions();
		mPhoneListener = null;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
