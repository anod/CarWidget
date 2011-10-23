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
import com.anod.car.home.prefs.PreferencesStorage;

public class ModePhoneStateListener extends PhoneStateListener {
	private static final int ANSWER_DALAY_MS = 5000;
	private Context mContext;
	private AudioManager mAudioManager;
	private Timer mAnswerTimer;
	private boolean mUseAutoSpeaker;
	private String mAutoAnswerMode; 
	public ModePhoneStateListener(Context context)
	{
		mContext = context;
		mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
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
		
	    switch (state)
	    {
		    case TelephonyManager.CALL_STATE_IDLE:
		    	mAudioManager.setSpeakerphoneOn(false);
		    	cancelAnswerTimer();
		    break;
		    //case TelephonyManager.CALL_STATE_OFFHOOK:
		    case TelephonyManager.CALL_STATE_RINGING:
		    	if (mAutoAnswerMode.equals(PreferencesStorage.AUTOANSWER_IMMEDIATLY)) {
		    		answerCall();
		    	} else if (mAutoAnswerMode.equals(PreferencesStorage.AUTOANSWER_DELAY_5)) {
					answerCallDelayed();
				} else {
					cancelAnswerTimer();
				}

		    	if (mUseAutoSpeaker) {
		    		mAudioManager.setSpeakerphoneOn(true);
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
        try {
        	answerPhoneAidl(mContext);
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.d("CarHomeWidget","Error trying to answer using telephony service.  Falling back to headset.");
            answerPhoneHeadsethook(mContext);
        }
	}
	
    private void answerPhoneHeadsethook(Context context) {
	    // Simulate a press of the headset button to pick up the call
	    Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);             
	    buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
	    context.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");
	
	    // froyo and beyond trigger on buttonUp instead of buttonDown
	    Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);               
	    buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
	    context.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
	}
	
	@SuppressWarnings("unchecked")
	private void answerPhoneAidl(Context context) throws Exception {
        // Set up communication with the telephony service (thanks to Tedd's Droid Tools!)
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressWarnings("rawtypes")
		Class c = Class.forName(tm.getClass().getName());
        Method m = c.getDeclaredMethod("getITelephony");
        m.setAccessible(true);
        ITelephony telephonyService;
        telephonyService = (ITelephony)m.invoke(tm);

        // Silence the ringer and answer the call!
        telephonyService.silenceRinger();
        telephonyService.answerRingingCall();
	}	

}
