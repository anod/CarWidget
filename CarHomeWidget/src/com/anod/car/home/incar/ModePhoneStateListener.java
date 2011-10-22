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

public class ModePhoneStateListener extends PhoneStateListener {
	private static final int ANSWER_DALAY_MS = 3000;
	private Context mContext;
	private AudioManager mAudioManager;
	private Timer mAnswerTimer; 
	public ModePhoneStateListener(Context context)
	{
		mContext = context;
		mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
	}
	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
		super.onCallStateChanged(state, incomingNumber);
		
	    switch (state)
	    {
		    case TelephonyManager.CALL_STATE_IDLE:
		    	mAudioManager.setSpeakerphoneOn(false);
		    	if (mAnswerTimer != null) {
		    		mAnswerTimer.cancel();
		    		mAnswerTimer = null;
		    	}
		    break;
		    //case TelephonyManager.CALL_STATE_OFFHOOK:
		    case TelephonyManager.CALL_STATE_RINGING:
		    	mAudioManager.setSpeakerphoneOn(true);
		    	if (mAnswerTimer == null) {
		    		mAnswerTimer = new Timer();
		    		mAnswerTimer.schedule(new TimerTask() {
		    			@Override
		    			public void run() {
		                    // Answer the phone
		                    try {
		                    	answerPhoneAidl(mContext);
		                    }
		                    catch (Exception e) {
		                        e.printStackTrace();
		                        Log.d("CarHomeWidget","Error trying to answer using telephony service.  Falling back to headset.");
		                        answerPhoneHeadsethook(mContext);
		                    }
		    			}
		    		}, ANSWER_DALAY_MS);
		    	}
		    break;
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
