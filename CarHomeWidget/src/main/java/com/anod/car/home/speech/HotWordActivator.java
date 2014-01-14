package com.anod.car.home.speech;

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

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.view.accessibility.AccessibilityManagerCompat;
import android.view.accessibility.AccessibilityManager;

import com.anod.car.home.utils.AppLog;

import java.util.List;

import root.gast.speech.SpeechRecognitionUtil;
import root.gast.speech.activation.SpeechActivationListener;
import root.gast.speech.activation.SpeechActivator;

/**
 * Uses direct speech recognition to activate when the user speaks
 * one of the target words
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class HotWordActivator implements SpeechActivator, RecognitionListener
{
	private static final String TAG = "HotWordActivator";

	private Context context;
	private SpeechRecognizer recognizer;
	private DoubleMetaphoneWordMatcher matcher;

	private SpeechActivationListener resultListener;

	private final AudioManager mAudioManager;
	private final AccessibilityManager mAccessibilityService;

	private StatusChangeListener mStatusChangeListener;

	private boolean mActive;

	public interface StatusChangeListener {
		void onActivatorActive();
		void onActivatorStop();
	}

	public HotWordActivator(Context context, SpeechActivationListener resultListener, String... targetWords)
	{
		this.context = context;
		this.matcher = new DoubleMetaphoneWordMatcher(targetWords);
		this.resultListener = resultListener;

		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		mAccessibilityService= (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);

	}

	public void setStatusChangeListener(StatusChangeListener listener) {
		mStatusChangeListener = listener;
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

	public boolean isActive() {

		return mActive;
	}


	private boolean isSpokenFeedbackEnabled()
	{
		if (mAccessibilityService.isEnabled()) {

			return !AccessibilityManagerCompat.getEnabledAccessibilityServiceList(
					mAccessibilityService, AccessibilityServiceInfo.FEEDBACK_SPOKEN
			).isEmpty();

		}
		return false;
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
		SpeechRecognitionUtil.recognizeSpeechDirectly(context, recognizerIntent, this, getSpeechRecognizer());
		if (mStatusChangeListener != null) {
			mStatusChangeListener.onActivatorActive();
		}
	}

	public void stop()
	{
		mActive = false;
		if (getSpeechRecognizer() != null)
		{
			getSpeechRecognizer().stopListening();
			getSpeechRecognizer().cancel();
			getSpeechRecognizer().destroy();
			recognizer = null;
		}
		if (mStatusChangeListener != null) {
			mStatusChangeListener.onActivatorStop();
		}
	}

	@Override
	public void onResults(Bundle results)
	{
		AppLog.d(TAG + " full results");
		receiveResults(results);
	}

	@Override
	public void onPartialResults(Bundle partialResults)
	{
		AppLog.d(TAG + " partial results");
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
			List<String> heard =
					results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
			float[] scores =
					results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
			receiveWhatWasHeard(heard, scores);
		}
		else
		{
			AppLog.d(TAG + " no results");
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

			AppLog.d(TAG + " possible = '" + possible);
			if (matcher.isIn(possible.toLowerCase()))
			{
				heardTargetWord = true;
				break;
			}
		}

		if (heardTargetWord)
		{
			AppLog.d(TAG + " Hotword detected");
			stop();
			resultListener.activated(true);
		}
		else
		{
			AppLog.d(TAG + " keep going");
			// keep going
			recognizeSpeechDirectly();
		}
	}

	@Override
	public void onError(int errorCode)
	{
		if ((errorCode == SpeechRecognizer.ERROR_NO_MATCH)
				|| (errorCode == SpeechRecognizer.ERROR_SPEECH_TIMEOUT))
		{
			AppLog.d(TAG + " didn't recognize anything");

			mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
			// keep going
			recognizeSpeechDirectly();

			mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
		}
		else
		{
			AppLog.d(TAG + " FAILED " + SpeechRecognitionUtil.diagnoseErrorCode(errorCode));
		}
	}

	/**
	 * lazy initialize the speech recognizer
	 */
	private SpeechRecognizer getSpeechRecognizer()
	{
		if (recognizer == null)
		{
			recognizer = SpeechRecognizer.createSpeechRecognizer(context);
		}
		return recognizer;
	}

	// other unused methods from RecognitionListener...

	@Override
	public void onReadyForSpeech(Bundle params)
	{
		AppLog.d(TAG + " ready for speech " + params);
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
}