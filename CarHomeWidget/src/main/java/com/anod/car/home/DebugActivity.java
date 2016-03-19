package com.anod.car.home;

import com.anod.car.home.incar.BroadcastService;
import com.anod.car.home.incar.ModeDetector;
import com.anod.car.home.incar.ModeService;
import com.anod.car.home.prefs.preferences.InCar;
import com.anod.car.home.prefs.preferences.InCarStorage;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.utils.AppLog;
import com.anod.car.home.utils.LogCatCollector;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Timer;

import butterknife.ButterKnife;
import butterknife.Bind;

public class DebugActivity extends Activity {

    @Bind(R.id.log)
    ListView mListView;

    @Bind({
            R.id.broadcast,
            R.id.incar,
            R.id.power,
            R.id.headset,
            R.id.bluetooth,
            R.id.activity,
            R.id.cardock,
            R.id.wakelock
    })
    TextView[] mTextViews;

    private LogAdapter mAdapter;

    private Timer mLogTimer;

    private Handler mHandle;

    private Runnable mRunnable;

    private InCar mInCarPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        ButterKnife.bind(this);
        mAdapter = new LogAdapter(this);
        mListView.setAdapter(mAdapter);

        mHandle = new Handler();
    }

    private void updateStatus() {

        boolean isBroadcastServiceRunning = isBroadcastServiceRunning();
        setStatusText(mTextViews[0],
                (isBroadcastServiceRunning) ? "Broadcast Service: On" : "Broadcast Service: Off",
                Color.WHITE);

        boolean isInCarEnabled = InCarStorage.isInCarModeEnabled(this);
        setStatusText(mTextViews[1], (isInCarEnabled) ? "InCar: Enabled" : "InCar: Disabled",
                Color.WHITE);

        boolean powerEvent = ModeDetector.getEventState(ModeDetector.FLAG_POWER);
        boolean powerPref = mInCarPrefs.isPowerRequired();
        setStatusText(mTextViews[2], String.format("Power: %b", powerEvent),
                getColor(powerPref, powerEvent));

        boolean headsetEvent = ModeDetector.getEventState(ModeDetector.FLAG_HEADSET);
        boolean headsetPref = mInCarPrefs.isHeadsetRequired();
        setStatusText(mTextViews[3], String.format("Headset: %b", headsetEvent),
                getColor(headsetPref, headsetEvent));

        boolean btEvent = ModeDetector.getEventState(ModeDetector.FLAG_BLUETOOTH);
        boolean btPref = mInCarPrefs.isBluetoothRequired();

        String devices = "";
        if (mInCarPrefs.getBtDevices() != null) {
            devices = TextUtils.join(",", mInCarPrefs.getBtDevices().values());
        }

        setStatusText(mTextViews[4], String.format("Bluetooth: %b [%s]", btEvent, devices),
                getColor(btPref, btEvent));

        boolean activityEvent = ModeDetector.getEventState(ModeDetector.FLAG_ACTIVITY);
        boolean activityPref = mInCarPrefs.isActivityRequired();
        setStatusText(mTextViews[5], String.format("Activity: %b", activityEvent),
                getColor(activityPref, activityEvent));

        boolean dockEvent = ModeDetector.getEventState(ModeDetector.FLAG_CAR_DOCK);
        boolean docPref = mInCarPrefs.isCarDockRequired();
        setStatusText(mTextViews[6], String.format("CarDock: %b", dockEvent),
                getColor(docPref, dockEvent));

        boolean wlHeld = ModeService.isWakeLockHeld(this);
        setStatusText(mTextViews[7], String.format("WakeLock Held: %b", wlHeld), Color.WHITE);

    }

    private int getColor(boolean pref, boolean event) {
        return (pref) ? (event) ? Color.GREEN : Color.RED : Color.GRAY;
    }

    private boolean isBroadcastServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (BroadcastService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void setStatusText(TextView textView, String text, int color) {
        textView.setText(text);
        textView.setTextColor(color);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mInCarPrefs = InCarStorage.loadInCar(this);
        registerLogListener();
        updateStatus();
        AppLog.d("Debug activity resumed");
    }

    @Override
    protected void onPause() {
        unregisterLogListener();
        super.onPause();
    }


    private void registerLogListener() {
        mRunnable = new Runnable() {
            @Override
            public void run() {
                final LinkedList<String> out = LogCatCollector.collectLogCat("main");

                mAdapter.clear();
                mAdapter.addAll(out);
                mAdapter.notifyDataSetChanged();

                updateStatus();

                mHandle.postDelayed(mRunnable, 1000L);
            }
        };

        mHandle.postDelayed(mRunnable, 1000L);

    }

    private void unregisterLogListener() {
        if (mHandle != null) {
            mHandle.removeCallbacks(mRunnable);
            mRunnable = null;
        }
    }


    public static class LogAdapter extends ArrayAdapter<String> {

        public LogAdapter(Context context) {
            super(context, R.layout.logrow);
        }

        @Override
        public String getItem(int position) {
            return super.getItem(super.getCount() - position - 1);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
                convertView = inflater.inflate(R.layout.logrow, parent, false);
            }

            String entry = getItem(position);
            TextView text = ButterKnife.findById(convertView, android.R.id.text1);
            text.setText(entry);

            return convertView;
        }
    }
}
