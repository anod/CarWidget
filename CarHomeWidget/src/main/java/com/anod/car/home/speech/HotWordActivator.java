/*
 * Copyright 2012 Greg Milette and Adam Stroud
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.anod.car.home.speech;

import java.util.List;

import root.gast.speech.SpeechRecognitionUtil;
import root.gast.speech.activation.SpeechActivationListener;
import root.gast.speech.activation.SpeechActivator;
import root.gast.speech.text.WordList;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.accessibility.AccessibilityManager;

import com.anod.car.home.utils.AppLog;

/**
 * Uses direct speech recognition to activate when the user speaks
 * one of the target words
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class HotWordActivator implements SpeechActivator, RecognitionListener
{
    private static final String TAG = "HotWordActivator";
	private final AudioManager mAudioManager;
	private final AccessibilityManager mAccessibilityService;
	private Context mContext;
    private SpeechRecognizer recognizer;
    private DoubleMetaphoneWordMatcher mMatcher;

    private SpeechActivationListener mResultListener;

	private boolean mActive;
	private boolean mAbort;

	public HotWordActivator(Context context, SpeechActivationListener resultListener, String... hotword)
    {
        mContext = context;

        mMatcher = new DoubleMetaphoneWordMatcher(hotword);
        mResultListener = resultListener;
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		mAccessibilityService= (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);

	}

	public boolean isMusicActive() {
		synchronized (mAudioManager) {
			return mAudioManager.isMusicActive();
		}
	}

	public boolean canStartHotword()
	{
		synchronized (mAudioManager) {
			if (isSpokenFeedbackEnabled())
				return false;
			if (mAudioManager.isMusicActive())
				return false;
			if (mAudioManager.isSpeakerphoneOn())
				return false;
			if (mAudioManager.getMode() != AudioManager.MODE_NORMAL)
				return false;
			if (mActive) {
				return false;
			}
			return true;
		}
	}

    @Override
    public void detectActivation()
    {
        recognizeSpeechDirectly();
    }

    private void recognizeSpeechDirectly()
    {
        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        // accept partial results if they come
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.anod.car.home");
		mActive = true;
		mAbort = false;
		SpeechRecognitionUtil.recognizeSpeechDirectly(mContext, recognizerIntent, this, getSpeechRecognizer());
    }

    public void stop()
    {
		mActive = false;
        if (getSpeechRecognizer() != null)
        {
            getSpeechRecognizer().stopListening();
            getSpeechRecognizer().cancel();
            getSpeechRecognizer().destroy();
        }
    }

    @Override
    public void onResults(Bundle results)
    {
		AppLog.d(TAG + ": full results");
        receiveResults(results);
    }

    @Override
    public void onPartialResults(Bundle partialResults)
    {
		AppLog.d(TAG + ": partial results");
        receiveResults(partialResults);
    }

    /**
     * common method to process any results bundle from {@link SpeechRecognizer}
     */
    private void receiveResults(Bundle results)
    {
        if ((results != null)
                && results.containsKey(SpeechRecognizer.RESULTS_RECOGNITION))
        {
            List<String> heard = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            float[] scores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
            receiveWhatWasHeard(heard, scores);
        }
        else
        {
			AppLog.d(TAG + ": no results");
        }
    }

    private void receiveWhatWasHeard(List<String> heard, float[] scores)
    {
        boolean heardTargetWord = false;
        // find the target word
		for(int i = 0; i<heard.size(); i++) {
       		String possible = heard.get(i);

			if ("".equals(possible)) {
				continue;
			}

			AppLog.d(TAG + " possible = '"+possible);
            if (mMatcher.isIn(possible.toLowerCase()))
            {
				AppLog.d(TAG + " Hotword detected");
                heardTargetWord = true;
                break;
            }
        }

        if (heardTargetWord)
        {
            stop();
			mAbort = true;
            mResultListener.activated(true);
        }
        else
        {
            // keep going
            recognizeSpeechDirectly();
        }
    }

    @Override
    public void onError(int errorCode)
    {
        if ((errorCode == SpeechRecognizer.ERROR_NO_MATCH) || (errorCode == SpeechRecognizer.ERROR_SPEECH_TIMEOUT))
        {
			AppLog.d(TAG + ": didn't recognize anything");
			mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
            // keep going
            recognizeSpeechDirectly();
        }
        else
        {
			AppLog.e(TAG + ":: onError - " + SpeechRecognitionUtil.diagnoseErrorCode(errorCode));
        }
    }

	public boolean isActive() {
		return mActive;
	}

    /**
     * lazy initialize the speech recognizer
     */
    private SpeechRecognizer getSpeechRecognizer()
    {
        if (recognizer == null)
        {
            recognizer = SpeechRecognizer.createSpeechRecognizer(mContext);
        }
        return recognizer;
    }

    // other unused methods from RecognitionListener...

    @Override
    public void onReadyForSpeech(Bundle params)
    {
		AppLog.d(TAG + ": ready for speech " + params);
    }

    @Override
    public void onEndOfSpeech()
    {
    }

    /**
     * @see android.speech.RecognitionListener#onBeginningOfSpeech()
     */
    @Override
    public void onBeginningOfSpeech()
    {
    }

    @Override
    public void onBufferReceived(byte[] buffer)
    {
    }

    @Override
    public void onRmsChanged(float rmsdB)
    {
    }

    @Override
    public void onEvent(int eventType, Bundle params)
    {
    }


	private boolean isSpokenFeedbackEnabled()
	{
		if (mAccessibilityService.isEnabled())
			return !mAccessibilityService.getEnabledAccessibilityServiceList(1).isEmpty();
		return false;
	}

	public boolean aborting() {
		return mAbort;
	}

}
