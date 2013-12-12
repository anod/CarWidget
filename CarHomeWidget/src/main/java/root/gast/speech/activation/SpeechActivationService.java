/*
 * Copyright 2011 Greg Milette and Adam Stroud
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
package root.gast.speech.activation;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.anod.car.home.R;
import com.anod.car.home.utils.AppLog;

/**
 * Persistently run a speech activator in the background.
 * Use {@link Intent}s to start and stop it
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 * 
 */
public class SpeechActivationService extends Service implements
        SpeechActivationListener
{
    private static final String TAG = "SpeechActivationService";
    public static final String NOTIFICATION_ICON_RESOURCE_INTENT_KEY =
        "NOTIFICATION_ICON_RESOURCE_INTENT_KEY";
    public static final String ACTIVATION_TYPE_INTENT_KEY =
            "ACTIVATION_TYPE_INTENT_KEY";
    public static final String ACTIVATION_RESULT_INTENT_KEY =
            "ACTIVATION_RESULT_INTENT_KEY";
    public static final String ACTIVATION_RESULT_BROADCAST_NAME =
            "root.gast.playground.speech.ACTIVATION";

    /**
     * send this when external code wants the Service to stop
     */
    public static final String ACTIVATION_STOP_INTENT_KEY =
            "ACTIVATION_STOP_INTENT_KEY";

    public static final int NOTIFICATION_ID = 10298;

    private boolean isStarted;

    private SpeechActivator activator;

    @Override
    public void onCreate()
    {
        super.onCreate();
        isStarted = false;
    }

    public static Intent makeStartServiceIntent(Context context)
    {
        Intent i = new Intent(context, SpeechActivationService.class);
   //     i.putExtra(ACTIVATION_TYPE_INTENT_KEY, activationType);
        return i;
    }

    public static Intent makeServiceStopIntent(Context context)
    {
        Intent i = new Intent(context, SpeechActivationService.class);
        i.putExtra(ACTIVATION_STOP_INTENT_KEY, true);
        return i;
    }

    /**
     * stop or start an activator based on the activator type and if an
     * activator is currently running
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent != null)
        {
            if (intent.hasExtra(ACTIVATION_STOP_INTENT_KEY))
            {
				AppLog.d("stop service intent");
                activated(false);
            }
            else
            {
                if (isStarted)
                {
                    // the activator is currently started
                    // if the intent is requesting a new activator
                    // stop the current activator and start
                    // the new one
					AppLog.d("already started this type");
                }
                else
                {
                    // activator not started, start it
                    startDetecting(intent);
                }
            }
        }

        // restart in case the Service gets canceled
        return START_REDELIVER_INTENT;
    }

    private void startDetecting(Intent intent)
    {
        if (activator == null)
        {
			AppLog.d("null activator");
        }

		activator = new WordActivator(this, this, "hello");
        AppLog.d("started: " + activator.getClass().getSimpleName());
        isStarted = true;
        activator.detectActivation();
        startForeground(NOTIFICATION_ID, getNotification(intent));
    }


    @Override
    public void activated(boolean success)
    {
        // make sure the activator is stopped before doing anything else
        stopActivator();

        // broadcast result
        Intent intent = new Intent(ACTIVATION_RESULT_BROADCAST_NAME);
        intent.putExtra(ACTIVATION_RESULT_INTENT_KEY, success);
        sendBroadcast(intent);

        // always stop after receive an activation
        stopSelf();
    }

    @Override
    public void onDestroy()
    {
		AppLog.d("On destroy");
        super.onDestroy();
        stopActivator();
        stopForeground(true);
    }

    private void stopActivator()
    {
        if (activator != null)
        {
            AppLog.d("stopped: " + activator.getClass().getSimpleName());
            activator.stop();
            isStarted = false;
        }
    }

    @SuppressLint("NewApi")
    private Notification getNotification(Intent intent)
    {
        // determine label based on the class
        String name = "Hello";
        String message =
                getString(R.string.speech_activation_notification_listening)
                        + " " + name;
        String title = getString(R.string.speech_activation_notification_title);

        PendingIntent pi =
                PendingIntent.getService(this, 0, makeServiceStopIntent(this),
                        0);

        int icon = intent.getIntExtra(NOTIFICATION_ICON_RESOURCE_INTENT_KEY, R.drawable.ic_action_headphones);

        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            Notification.Builder builder = new Notification.Builder(this);
            builder.setSmallIcon(icon)
                    .setWhen(System.currentTimeMillis()).setTicker(message)
                    .setContentTitle(title).setContentText(message)
                    .setContentIntent(pi);
            notification = builder.getNotification();
        }
        else
        {
            notification =
                    new Notification(icon, message,
                            System.currentTimeMillis());
            notification.setLatestEventInfo(this, title, message, pi);
        }

        return notification;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}