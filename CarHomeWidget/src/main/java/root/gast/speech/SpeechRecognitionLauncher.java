package root.gast.speech;

/**
 * @author alex
 * @date 12/12/13
 */

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;

import com.anod.car.home.utils.AppLog;

import java.util.List;

import root.gast.speech.tts.TextToSpeechUtils;

/**
 * Starts a speech recognition dialog and then sends the results to
 * {@link SpeechRecognitionResultsActivity}
 *
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class SpeechRecognitionLauncher extends SpeechRecognizingAndSpeakingActivity
{
	private static final String TAG = "SpeechRecognitionLauncher";

	private static final String ON_DONE_PROMPT_TTS_PARAM = "ON_DONE_PROMPT";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
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
		Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
		startActivity(intent);
		finish();
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
		}

		finish();
	}

	@Override
	protected void receiveWhatWasHeard(List<String> heard,
									   float[] confidenceScores)
	{
		// satisfy abstract class, this class handles the results directly
		// instead of using this method
	}
}
