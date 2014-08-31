package com.anod.car.home;

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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.anod.car.home.incar.BroadcastService;
import com.anod.car.home.incar.ModeDetector;
import com.anod.car.home.prefs.preferences.InCar;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.utils.AppLog;
import com.anod.car.home.utils.LogCatCollector;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DebugActivity extends Activity{

	@InjectView(R.id.log) ListView mListView;
	private LogAdapter mAdapter;
    private Timer mLogTimer;
    private Handler mHandle;
    private Runnable mRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        ButterKnife.inject(this);
		mAdapter = new LogAdapter(this);
		mListView.setAdapter(mAdapter);

		ImageButton refresh = new ImageButton(this);
		refresh.setImageResource(R.drawable.ic_action_refresh);
		refresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				updateStatus();
			}
		});
		getActionBar().setCustomView(refresh);
		getActionBar().setDisplayShowCustomEnabled(true);

        mHandle = new Handler();

    }

	private void updateStatus() {

		InCar incar = PreferencesStorage.loadInCar(this);

		boolean isBroadcastServiceRunning = isBroadcastServiceRunning();
		setStatusText(R.id.broadcast, (isBroadcastServiceRunning) ? "Broadcast Service: On" : "Broadcast Service: Off", Color.WHITE);

		boolean isInCarEnabled = PreferencesStorage.isInCarModeEnabled(this);
		setStatusText(R.id.incar, (isInCarEnabled) ? "InCar: Enabled" : "InCar: Disabled", Color.WHITE);

		boolean powerEvent = ModeDetector.getEventState(ModeDetector.FLAG_POWER);
		boolean powerPref = incar.isPowerRequired();
		setStatusText(R.id.power,  String.format("Power: %b", powerEvent), getColor(powerPref, powerEvent));

		boolean headsetEvent = ModeDetector.getEventState(ModeDetector.FLAG_HEADSET);
		boolean headsetPref = incar.isHeadsetRequired();
		setStatusText(R.id.headset, String.format("Headset: %b", headsetEvent), getColor(headsetPref, headsetEvent));

		boolean btEvent = ModeDetector.getEventState(ModeDetector.FLAG_BLUETOOTH);
		boolean btPref = incar.isBluetoothRequired();

		String devices = "";
		if (incar.getBtDevices() != null) {
			devices = TextUtils.join(",", incar.getBtDevices().values());
		}

		setStatusText(R.id.bluetooth, String.format("Bluetooth: %b [%s]", btEvent,devices), getColor(btPref, btEvent));

		boolean activityEvent = ModeDetector.getEventState(ModeDetector.FLAG_HEADSET);
		boolean activityPref = incar.isActivityRequired();
		setStatusText(R.id.activity,  String.format("Activity: %b",activityEvent), getColor(activityPref, activityEvent));

        boolean dockEvent = ModeDetector.getEventState(ModeDetector.FLAG_CAR_DOCK);
        boolean docPref = incar.isCarDockRequired();
        setStatusText(R.id.cardock, String.format("CarDock: %b", dockEvent), getColor(docPref, dockEvent));
	}
    private int getColor(boolean pref, boolean event) {
        return (pref) ? (event) ? Color.GREEN : Color.RED : Color.GRAY;
    }

	private boolean isBroadcastServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (BroadcastService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private void setStatusText(int resId, String text, int color) {
		TextView textView = ButterKnife.findById(this,resId);

		textView.setText(text);
		textView.setTextColor(color);
	}

	@Override
	protected void onResume() {
		super.onResume();

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

                mHandle.postDelayed(mRunnable, 1000L);
            }
        };

        mHandle.postDelayed(mRunnable, 1000L);

    }

	private void unregisterLogListener() {
        if (mHandle !=null) {
            mHandle.removeCallbacks(mRunnable);
            mRunnable=null;
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
				LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
				convertView = inflater.inflate(R.layout.logrow, parent, false);
			}

			String entry = getItem(position);
			TextView text = ButterKnife.findById(convertView, android.R.id.text1);
			text.setText(entry);

			return convertView;
		}
	}
}
