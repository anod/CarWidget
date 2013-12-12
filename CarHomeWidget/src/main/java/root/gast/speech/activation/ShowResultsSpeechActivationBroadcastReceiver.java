package root.gast.speech.activation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;

import com.anod.car.home.utils.AppLog;

import root.gast.speech.SpeechRecognitionLauncher;
import root.gast.speech.activation.SpeechActivationService;

/**
 * @author alex
 * @date 12/12/13
 */
public class ShowResultsSpeechActivationBroadcastReceiver extends BroadcastReceiver
{
	private static final int RECOGNIZER_REQ_CODE = 1234;
	@Override
	public void onReceive(Context context, Intent intent)
	{

		if (intent.getAction().equals(SpeechActivationService.ACTIVATION_RESULT_BROADCAST_NAME))
		{
			if (intent.getBooleanExtra(SpeechActivationService.ACTIVATION_RESULT_INTENT_KEY, false))
			{
				AppLog.d("KeywordBroadcastReceiver taking action");
				// launch something that prompts the user...
				Intent i = new Intent(context, SpeechRecognitionLauncher.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(i);
	


			}
		}
	}
}