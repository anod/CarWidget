package com.anod.car.home.incar;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

import com.android.internal.telephony.ITelephony;
import com.anod.car.home.prefs.preferences.InCar;
import com.anod.car.home.utils.Utils;

public class ModePhoneStateListener extends PhoneStateListener {
	private static final int ANSWER_DALAY_MS = 5000;
	private boolean mAnswered = false;
	private Context mContext;
	private AudioManager mAudioManager;
	private Timer mAnswerTimer;
	private boolean mUseAutoSpeaker;
	private String mAutoAnswerMode;
	private int mState = -1;

	public ModePhoneStateListener(Context context) {
		mContext = context;
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	}

	public void setActions(boolean useAutoSpeaker, String autoAnswerMode) {
		mUseAutoSpeaker = useAutoSpeaker;
		mAutoAnswerMode = autoAnswerMode;
	}

	public void cancelActions() {
		mAudioManager.setSpeakerphoneOn(false);
		cancelAnswerTimer();
	}

	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
		super.onCallStateChanged(state, incomingNumber);
		mState = state;
		switch (state) {
		case TelephonyManager.CALL_STATE_IDLE:
			Utils.logd("Call state idle");
			mAudioManager.setSpeakerphoneOn(false);
			cancelAnswerTimer();
			mAnswered = false;
			break;
		case TelephonyManager.CALL_STATE_OFFHOOK:
			Utils.logd("Call state offhook");
			if (mUseAutoSpeaker && !mAudioManager.isSpeakerphoneOn()) {
				Utils.logd("Enable speakerphone while offhook");
				mAudioManager.setSpeakerphoneOn(true);
			}
			break;
		case TelephonyManager.CALL_STATE_RINGING:
			Utils.logd("Call state ringing");
			if (mAutoAnswerMode.equals(InCar.AUTOANSWER_IMMEDIATLY)) {
				Utils.logd("Check if already answered");
				if (!mAnswered) {
					Utils.logd("Answer immediatly");
					answerCall();
					mAnswered = true;
				}
			} else if (mAutoAnswerMode.equals(InCar.AUTOANSWER_DELAY_5)) {
				Utils.logd("Check if already answered");
				if (!mAnswered) {
					Utils.logd("Answer delayed");
					answerCallDelayed();
					mAnswered = true;
				}
			} else {
				Utils.logd("Cancel answer timer");
				cancelAnswerTimer();
				// mAnswered=false;
			}
			break;
		}
	}

	private void cancelAnswerTimer() {
		if (mAnswerTimer != null) {
			mAnswerTimer.cancel();
			mAnswerTimer = null;
		}
	}

	private void answerCallDelayed() {
		if (mAnswerTimer == null) {
			mAnswerTimer = new Timer();
			mAnswerTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					answerCall();
				}
			}, ANSWER_DALAY_MS);
		}
	}

	private void answerCall() {
		if (mState != TelephonyManager.CALL_STATE_RINGING) {
			return;
		}
		try {
			answerPhoneAidl(mContext);
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("CarHomeWidget", "Error trying to answer using telephony service.  Falling back to headset.");
			answerPhoneHeadsethook(mContext);
		}
		mAudioManager.setMicrophoneMute(false);

		if (mUseAutoSpeaker && !mAudioManager.isSpeakerphoneOn()) {
			Utils.logd("Enable speakerphone while ringing");
			mAudioManager.setSpeakerphoneOn(true);
		}
	}
	private void answerPhoneHeadsethook(Context context) {

		boolean headsetImmitation = false;
		if (!mAudioManager.isWiredHeadsetOn()) {
			sendHeadsetPlug(1, context);
			headsetImmitation = true;
		}
		// Simulate a press of the headset button to pick up the call
		Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
		buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
		context.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");

		// froyo and beyond trigger on buttonUp instead of buttonDown
		Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
		buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
		context.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");

		if (headsetImmitation) {
			sendHeadsetPlug(0, context);
		}
	}

	private void sendHeadsetPlug(int state, Context context) {
		Intent headSetUnPluggedintent = new Intent(Intent.ACTION_HEADSET_PLUG);
		headSetUnPluggedintent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
		headSetUnPluggedintent.putExtra("state", state);
		headSetUnPluggedintent.putExtra("name", "Headset");
		// TODO: Should we require a permission?
		try {
			context.sendOrderedBroadcast(headSetUnPluggedintent, "android.permission.CALL_PRIVILEGED");
		} catch (Exception e) {
			Log.e("CarHomeWidget", e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private void answerPhoneAidl(Context context) throws Exception {
		// Set up communication with the telephony service (thanks to Tedd's
		// Droid Tools!)
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		@SuppressWarnings("rawtypes")
		Class c = Class.forName(tm.getClass().getName());
		Method m = c.getDeclaredMethod("getITelephony");
		m.setAccessible(true);
		ITelephony telephonyService;
		telephonyService = (ITelephony) m.invoke(tm);

		if (tm.getCallState() != TelephonyManager.CALL_STATE_RINGING) {
			return;
		}
		// Silence the ringer and answer the call!
		telephonyService.silenceRinger();
		telephonyService.answerRingingCall();

		// com.android.internal.telephony.Phone
	}

}
