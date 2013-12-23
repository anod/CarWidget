package com.anod.car.home;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.anod.car.home.app.CarWidgetActivity;
import com.anod.car.home.incar.Handler;
import com.anod.car.home.prefs.preferences.InCar;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.utils.AppLog;

import org.apache.commons.codec.binary.StringUtils;

import java.util.HashMap;

public class DebugActivity extends CarWidgetActivity implements AppLog.LogListener {

	private ListView mListView;
	private LogAdapter mAdapter;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);


		updateStatus();
		mListView = (ListView)findViewById(R.id.log);
		mAdapter = new LogAdapter(this);
		mListView.setAdapter(mAdapter);
    }

	private void updateStatus() {

		InCar incar = PreferencesStorage.loadInCar(this);

		boolean isInCarEnabled = PreferencesStorage.isInCarModeEnabled(this);
		setStatusText(R.id.incar, (isInCarEnabled) ? "InCar: Enabled" : "InCar: Disabled", Color.WHITE);

		boolean powerEvent = Handler.getEventState(Handler.FLAG_POWER);
		boolean powerPref = incar.isPowerRequired();
		setStatusText(R.id.power,  String.format("Power: %b", powerEvent), (powerPref) ? Color.GREEN : Color.RED);

		boolean headsetEvent = Handler.getEventState(Handler.FLAG_HEADSET);
		boolean headsetPref = incar.isHeadsetRequired();
		setStatusText(R.id.headset, String.format("Headset: %b", headsetEvent), (headsetPref) ? Color.GREEN : Color.RED);

		boolean btEvent = Handler.getEventState(Handler.FLAG_BLUETOOTH);
		boolean btPref = incar.isBluetoothRequired();

		String devices = "";
		if (incar.getBtDevices() != null) {
			TextUtils.join(",", incar.getBtDevices().values());
		}

		setStatusText(R.id.bluetooth, String.format("Bluetooth: %b [%s]", btEvent,devices), (btPref) ? Color.GREEN : Color.RED);

		boolean activityEvent = Handler.getEventState(Handler.FLAG_HEADSET);
		boolean activityPref = incar.isActivityRequired();
		setStatusText(R.id.activity,  String.format("Activity: %b",activityEvent), (activityPref) ? Color.GREEN : Color.RED);
	}

	private void setStatusText(int resId, String text, int color) {
		TextView textView = (TextView) findViewById(resId);

		textView.setText(text);
		textView.setTextColor(color);
	}

	@Override
	protected void onResume() {
		registerLogListener();
		super.onResume();
	}

	@Override
	protected void onPause() {
		unregisterLogListener();
		super.onPause();
	}


	private void registerLogListener() {
		AppLog.setListener(this);
	}

	private void unregisterLogListener() {
		AppLog.setListener(null);
	}

	@Override
	public void onMessage(final AppLog.Entry entry) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mAdapter.add(entry);
				mAdapter.notifyDataSetChanged();
			}
		});
	}


	public static class LogAdapter extends ArrayAdapter<AppLog.Entry> {

		public LogAdapter(Context context) {
			super(context, R.layout.logrow);
		}


		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
				convertView = inflater.inflate(R.layout.logrow, parent, false);
			}

			AppLog.Entry entry = getItem(position);
			TextView text = (TextView) convertView.findViewById(android.R.id.text1);
			text.setText(entry.msg);

			return convertView;
		}
	}
}
