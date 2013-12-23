package com.anod.car.home.speech;

/**
 * @author alex
 * @date 12/12/13
 */

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;

import com.anod.car.home.utils.Utils;

import java.util.List;

import root.gast.speech.SpeechRecognizingAndSpeakingActivity;

/**
 * Starts a speech recognition dialog and then sends the results to
 //* {@link SpeechRecognitionResultsActivity}
 *
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class SpeechRecognitionLauncher extends SpeechRecognizingAndSpeakingActivity
{
	private static final String TAG = "SpeechRecognitionLauncher";

	private static final String ON_DONE_PROMPT_TTS_PARAM = "ON_DONE_PROMPT";
	public static final String VOICE_RESUME = "VOICE_RESUME";

	private boolean mResume;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Check whether we're recreating a previously destroyed instance
		if (savedInstanceState != null) {
			// Restore value of members from saved state
			boolean resume = savedInstanceState.getBoolean(VOICE_RESUME);
			if (resume) {
				startService(SpeechActivationService.makeStartIntent(this));
				finish();
				return;
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mResume) {
			startService(SpeechActivationService.makeStartIntent(this));
			finish();
			return;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save the user's current game state
		savedInstanceState.putBoolean(VOICE_RESUME, mResume);
		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onSuccessfulInit(TextToSpeech tts)
	{
		super.onSuccessfulInit(tts);
		prompt();
	}

	private void prompt()
	{
		//AppLog.d(TAG, "Speak prompt");

		/*
		getTts().speak("Say a command.",
				TextToSpeech.QUEUE_FLUSH,
				TextToSpeechUtils.makeParamsWith(ON_DONE_PROMPT_TTS_PARAM));
		*/
		mResume = true;
		startVoice();
	}

	public void startVoice() {

		try {
			ComponentName activityName = getGlobalSearchActivity();
			Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if (activityName != null) {
				intent.setPackage(activityName.getPackageName());
			}
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			Utils.startActivitySafely(intent, this);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private ComponentName getGlobalSearchActivity() {
		if (Utils.IS_JELLYBEAN_OR_GREATER) {
			final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			return searchManager.getGlobalSearchActivity();
		}
		return null;
	}

	/**
	 * super class handles registering the UtteranceProgressListener
	 * and calling this
	 */
	@Override
	public void onDone(String utteranceId)
	{
		if (utteranceId.equals(ON_DONE_PROMPT_TTS_PARAM))
		{
			Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
			recognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say a command.");
			recognize(recognizerIntent);
		}
	}

	@Override
	protected void	onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE)
		{
			if (resultCode == RESULT_OK)
			{
				//Intent showResults = new Intent(data);
				//showResults.setClass(this, SpeechRecognitionResultsActivity.class);
				//startActivity(showResults);
			}
			finish();
		}

	}

	@Override
	protected void receiveWhatWasHeard(List<String> heard,
									   float[] confidenceScores)
	{
		// satisfy abstract class, this class handles the results directly
		// instead of using this method
	}
}
