package com.anod.car.home.incar;

import com.anod.car.home.prefs.preferences.InCar;
import info.anodsplace.android.log.AppLog;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;


public class ModePhoneStateListener extends PhoneStateListener {

    private static final int ANSWER_DALAY_MS = 5000;

    private boolean mAnswered = false;

    private Context mContext;

    private AudioManager mAudioManager;

    private Timer mAnswerTimer;

    private boolean mUseAutoSpeaker;

    private String mAutoAnswerMode;

    private int mState = -1;

    public ModePhoneStateListener(Context context, AudioManager audioManager) {
        mContext = context;
        mAudioManager = audioManager;
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
                AppLog.d("Call state idle");
                mAudioManager.setSpeakerphoneOn(false);
                cancelAnswerTimer();
                mAnswered = false;
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                AppLog.d("Call state off hook");
                if (mUseAutoSpeaker && !mAudioManager.isSpeakerphoneOn()) {
                    AppLog.d("Enable speakerphone while off hook");
                    mAudioManager.setSpeakerphoneOn(true);
                }
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                AppLog.d("Call state ringing");
                if (mAutoAnswerMode.equals(InCar.AUTOANSWER_IMMEDIATLY)) {
                    AppLog.d("Check if already answered");
                    if (!mAnswered) {
                        AppLog.d("Answer immediately");
                        answerCall();
                        mAnswered = true;
                    }
                } else if (mAutoAnswerMode.equals(InCar.AUTOANSWER_DELAY_5)) {
                    AppLog.d("Check if already answered");
                    if (!mAnswered) {
                        AppLog.d("Answer delayed");
                        answerCallDelayed();
                        mAnswered = true;
                    }
                } else {
                    AppLog.d("Cancel answer timer");
                    cancelAnswerTimer();
                }
                break;
        }
        AppLog.d("Call state <unknown>: " + state);
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
        answerPhoneHeadsethook(mContext);
        mAudioManager.setMicrophoneMute(false);

        if (mUseAutoSpeaker && !mAudioManager.isSpeakerphoneOn()) {
            AppLog.d("Enable speakerphone while ringing");
            mAudioManager.setSpeakerphoneOn(true);
        }
    }

    private void answerPhoneHeadsethook(Context context) {

        Intent intent = new Intent(context, AcceptCallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.putExtra(AcceptCallActivity.EXTRA_ENABLE_SPEAKER, mUseAutoSpeaker);
        context.startActivity(intent);
            /*

		boolean headsetImmitation = false;
		if (!mAudioManager.isWiredHeadsetOn()) {
			sendHeadsetPlug(1, context);
			headsetImmitation = true;
		}
		// Simulate a press of the headset button to pick up the call
		Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
		buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
		context.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");

		// Froyo and beyond trigger on buttonUp instead of buttonDown
		Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
		buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
		context.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");

		if (headsetImmitation) {
			sendHeadsetPlug(0, context);
		}
		*/
    }

    private void sendHeadsetPlug(int state, Context context) {
        Intent headSetUnPluggedintent = new Intent(Intent.ACTION_HEADSET_PLUG);
        headSetUnPluggedintent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        headSetUnPluggedintent.putExtra("state", state);
        headSetUnPluggedintent.putExtra("name", "Headset");
        // TODO: Should we require a permission?
        try {
            context.sendOrderedBroadcast(headSetUnPluggedintent,
                    "android.permission.CALL_PRIVILEGED");
        } catch (Exception e) {
            Log.e("CarHomeWidget", e.getMessage());
        }
    }


}
