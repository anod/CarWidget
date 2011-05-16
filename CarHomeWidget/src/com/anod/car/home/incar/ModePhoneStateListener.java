package com.anod.car.home.incar;

import android.content.Context;
import android.media.AudioManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class ModePhoneStateListener extends PhoneStateListener {
	private Context mContext;
	public ModePhoneStateListener(Context context)
	{
		mContext = context;
	}
	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
		super.onCallStateChanged(state, incomingNumber);
	    switch (state)
	    {
		    case TelephonyManager.CALL_STATE_IDLE:
		    case TelephonyManager.CALL_STATE_OFFHOOK:
		    case TelephonyManager.CALL_STATE_RINGING:
		    	AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
		    	am.setSpeakerphoneOn(true);
		    break;
	    }
	}

	

}
