package com.anod.car.home.prefs;

import com.anod.car.home.R;
import com.anod.car.home.app.CarWidgetActivity;
import com.anod.car.home.incar.Bluetooth;
import com.anod.car.home.incar.BluetoothClassHelper;
import com.anod.car.home.incar.BroadcastService;
import com.anod.car.home.prefs.preferences.InCar;
import com.anod.car.home.prefs.preferences.PreferencesStorage;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author alex
 * @date 6/6/14
 */
public class BluetoothDeviceActivity extends CarWidgetActivity
        implements AdapterView.OnItemClickListener {

    private BroadcastReceiver mBluetoothReceiver;

    private static final IntentFilter INTENT_FILTER = new IntentFilter(
            BluetoothAdapter.ACTION_STATE_CHANGED);

    @InjectView(android.R.id.list)
    ListView mDevicesList;

    private DeviceAdapter mListAdapter;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_device);

        ButterKnife.inject(this);
        mDevicesList.setOnItemClickListener(this);
        mListAdapter = new DeviceAdapter(this);

        mDevicesList.setDivider(new ColorDrawable(Color.TRANSPARENT));
        mDevicesList.setDividerHeight(getResources().getDimensionPixelOffset(R.dimen.preference_item_margin));

        mDevicesList.setEmptyView(ButterKnife.findById(this, android.R.id.empty));
        mDevicesList.setAdapter(mListAdapter);

        mContext = this;

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

        Boolean wasChecked = checkbox.isChecked();

        if (wasChecked) {
            onDeviceStateChange(device, false);
            checkbox.setChecked(false);
        } else {
            onDeviceStateChange(device, true);
            checkbox.setChecked(true);
        }
    }

    private void onDeviceStateChange(Device device, boolean newState) {
        ArrayMap<String, String> devices = PreferencesStorage.getBtDevices(mContext);
        if (newState) {
            if (devices == null) {
                devices = new ArrayMap<String, String>();
            }
            devices.put(device.address, device.address);
        } else {
            if (devices == null) {
                return;
            }
            devices.remove(device.address);
        }
        PreferencesStorage.saveBtDevices(mContext, devices);

        if (newState || isBroadcastServiceRequired()) {
            BroadcastService.startService(mContext);
        } else {
            BroadcastService.stopService(mContext);
        }
    }

    private boolean isBroadcastServiceRequired() {
        InCar incar = PreferencesStorage.loadInCar(mContext);
        return BroadcastService.isServiceRequired(incar);
    }

    static class Device {

        public String address;

        public String name;

        public String btClassName;

        public boolean selected;

        Device(String address, String name, String btClassName, boolean selected) {
            this.address = address;
            this.name = name;
            this.btClassName = btClassName;
            this.selected = selected;
        }

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
            int state = paramIntent
                    .getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
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
            ArrayMap<String, String> devices = PreferencesStorage.getBtDevices(mContext);
            mPairedList = new ArrayList<Device>();

            Resources r = mContext.getResources();

            // If there are paired devices, add each one to the ArrayAdapter
            if (!pairedDevices.isEmpty()) {
                for (BluetoothDevice device : pairedDevices) {
                    String addr = device.getAddress();
                    boolean selected = (devices == null) ? false : devices.containsKey(addr);
                    if (selected) {
                        devices.remove(addr);
                    }
                    BluetoothClass btClass = device.getBluetoothClass();
                    int res = 0;
                    if (btClass != null) {
                        res = BluetoothClassHelper.getBtClassString(btClass);
                    }
                    String btClassName = null;
                    if (res > 0) {
                        btClassName = r.getString(res);
                    }
                    Device d = new Device(device.getAddress(), device.getName(), btClassName,
                            selected);

                    mPairedList.add(d);
                }

            }
            if (devices != null && !devices.isEmpty()) {
                for (String addr : devices.keySet()) {
                    Device d = new Device(addr, addr, r.getString(R.string.unavailable_bt_device),
                            true);
                    mPairedList.add(d);
                }
            }
            return !mPairedList.isEmpty();
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
