package com.anod.car.home.prefs;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.anod.car.home.R;
import com.anod.car.home.incar.Bluetooth;
import com.anod.car.home.incar.BluetoothClassHelper;
import com.anod.car.home.prefs.preferences.PreferencesStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author alex
 * @date 6/6/14
 */
public class BluetoothDeviceActivity extends Activity implements AdapterView.OnItemClickListener {
	private BroadcastReceiver mBluetoothReceiver;
	private static final IntentFilter INTENT_FILTER = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
	@InjectView(android.R.id.list) ListView mDevicesList;
	private DeviceAdapter mListAdapter;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth_device_activity);

        ButterKnife.inject(this);
		mDevicesList.setOnItemClickListener(this);
		mListAdapter = new DeviceAdapter(this);

		mDevicesList.setEmptyView(ButterKnife.findById(this,android.R.id.empty));
		mDevicesList.setAdapter(mListAdapter);

		mContext = this;

		Button okButton = (Button)findViewById(android.R.id.button1);
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		initSwitch();

		new InitBluetoothDevicesTask().execute(0);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mBluetoothReceiver != null) {
			unregisterReceiver(mBluetoothReceiver);
		}
	}


	@Override
	public void onResume() {
		super.onResume();
		if (mBluetoothReceiver != null) {
			registerReceiver(mBluetoothReceiver, INTENT_FILTER);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		Device device = mListAdapter.getItem(position);

		CheckBox checkbox = (CheckBox) view.findViewById(android.R.id.checkbox);

		HashMap<String, String> devices = PreferencesStorage.getBtDevices(mContext);
		Boolean wasChecked = checkbox.isChecked();

		if (wasChecked) {
			if (devices == null) {
				return;
			}
			devices.remove(device.address);
			PreferencesStorage.saveBtDevices(mContext, devices);
			checkbox.setChecked(false);
		} else {
			if (devices == null) {
				devices = new HashMap<String, String>();
			}
			devices.put(device.address, device.address);
			PreferencesStorage.saveBtDevices(mContext, devices);
			checkbox.setChecked(true);
		}
	}


	static class Device {
		public String address;
		public String name;
		public String btClassName;
		public boolean selected;

		@Override
		public String toString() {
			return name;
		}
	}

	private void initSwitch() {
		final Switch btSwitch = (Switch) findViewById(android.R.id.toggle);
		btSwitch.setChecked(Bluetooth.getState() == BluetoothAdapter.STATE_ON);
		btSwitch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mBluetoothReceiver = new BluetoothStateReceiver();
				registerReceiver(mBluetoothReceiver, INTENT_FILTER);
				if (Bluetooth.getState() == BluetoothAdapter.STATE_ON) {
					Bluetooth.switchOff();
					btSwitch.setChecked(false);
				} else {
					Bluetooth.switchOn();
					btSwitch.setChecked(true);
				}
			}
		});

	}

	private class BluetoothStateReceiver extends BroadcastReceiver {
		public void onReceive(Context paramContext, Intent paramIntent) {
			int state = paramIntent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
			if (state == BluetoothAdapter.STATE_ON) {
				new InitBluetoothDevicesTask().execute(0);
			} else if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.ERROR) {
			}
		}

	}

	private class InitBluetoothDevicesTask extends AsyncTask<Integer, Integer, Boolean> {
		private ArrayList<Device> mPairedList;
		private BluetoothAdapter mBtAdapter;

		protected void onPreExecute() {
			mListAdapter.clear();
			// Get the local Bluetooth adapter
			mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		}

		protected void onPostExecute(Boolean result) {
			mListAdapter.clear();
			if (result) {
				mListAdapter.addAll(mPairedList);
			} else {
				// TODO
				//	emptyPref.setTitle(r.getString(R.string.no_paired_devices_found_title));
				//		emptyPref.setSummary(r.getString(R.string.no_paired_devices_found_summary));
			}
			mPairedList = null;
		}

		public void onProgressUpdate(Integer... values) {
			//Nothing
		}

		@Override
		protected Boolean doInBackground(Integer... arg0) {

			// Get a set of currently paired devices
			Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

			// If there are paired devices, add each one to the ArrayAdapter
			if (!pairedDevices.isEmpty()) {
				HashMap<String, String> devices = PreferencesStorage.getBtDevices(mContext);
				mPairedList = new ArrayList<Device>(pairedDevices.size());
				for (BluetoothDevice device : pairedDevices) {
					Device d = new Device();
					d.selected = (devices == null) ? false : devices.containsKey(device.getAddress());
					d.address = device.getAddress();
					d.name = device.getName();
					BluetoothClass btClass = device.getBluetoothClass();
					int res = 0;
					if (btClass != null) {
						res = BluetoothClassHelper.getBtClassString(btClass);
					}
					if (res > 0) {
						d.btClassName = mContext.getResources().getString(res);
					}

					mPairedList.add(d);
				}
				return true;
			}
			return false;
		}
	}


	private static class DeviceAdapter extends ArrayAdapter<Device> {

		public DeviceAdapter(Context context) {
			super(context, R.layout.bluetooth_device_item, android.R.id.title);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			Device d = getItem(position);
			CheckBox checkbox = (CheckBox) view.findViewById(android.R.id.checkbox);
			checkbox.setChecked(d.selected);

			TextView summary = (TextView) view.findViewById(android.R.id.summary);
			if (d.btClassName != null) {
				summary.setText(d.btClassName);
			} else {
				summary.setText("");
			}
			return view;
		}
	}
}
