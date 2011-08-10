package com.anod.car.home.prefs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import com.anod.car.home.Launcher;
import com.anod.car.home.R;
import com.anod.car.home.incar.Bluetooth;
import com.anod.car.home.incar.BluetoothClassHelper;

public class ConfigurationInCar extends PreferenceActivity {
	private static final String SCREEN_BT_DEVICE = "bt-device-screen";
	private static final String CATEGORY_BT_DEVICE = "bt-device-category";
	private static final String PREF_BT_SWITCH = "bt-switch";
    
	private static final int DIALOG_WAIT=1;
	private static final int DIALOG_DONATE=2;
	private static final int DIALOG_INIT=3;
	
    private static final String DETAIL_MARKET_URL="market://details?id=%s";
    
	private static final String PRO_PACKAGE_NAME="com.anod.car.home.pro";
	
    private static final IntentFilter INTENT_FILTER = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    private PreferenceCategory mBluetoothDevicesCategory;
    private BroadcastReceiver mBluetoothReceiver;
	
	private boolean mFreeVersion;
	private Context mContext;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	addPreferencesFromResource(R.xml.preference_incar);
    	setResult(RESULT_OK);
    	
        mFreeVersion = Launcher.isFreeVersion(this.getPackageName());
        mContext = (Context)this;

        if (mFreeVersion) {
       		initInCarFreeDialog();
       	} else {
       		initInCar();
       	}
       	
    }
	
    @Override
    protected void onPause()
    {
    	super.onPause();
    	if (mBluetoothReceiver != null) {
    		unregisterReceiver(mBluetoothReceiver);
    	}
    }

    @Override
    protected void onResume()
    {
    	super.onResume();
    	if (mBluetoothReceiver != null) {
	    	registerReceiver(mBluetoothReceiver, INTENT_FILTER);
    	}
    }
	
    @Override
    public Dialog onCreateDialog(int id) {
    	switch(id) {
	    	case DIALOG_INIT :
	    		ProgressDialog initDialog = new ProgressDialog(this);
	    		initDialog.setCancelable(true);
	    		initDialog.setMessage(getResources().getString(R.string.load_paired_device));
	    		initDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface arg0) {
						finish();
					}
				});
	    		return initDialog;    	
	    	case DIALOG_WAIT :
	    		ProgressDialog waitDialog = new ProgressDialog(this);
	    		waitDialog.setCancelable(true);
	    		String message = getResources().getString(R.string.please_wait);
	    		waitDialog.setMessage(message);
	    		return waitDialog;
			case DIALOG_DONATE :
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.dialog_donate_title);
				builder.setMessage(R.string.dialog_donate_message);
				builder.setCancelable(true);
				builder.setPositiveButton(R.string.dialog_donate_btn_yes, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String url = DETAIL_MARKET_URL;
						Uri uri = Uri.parse(String.format(url,PRO_PACKAGE_NAME));
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(uri);
						startActivity(intent);
						dialog.dismiss();
					}
				});
				builder.setNegativeButton(R.string.dialog_donate_btn_no, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				return builder.create();
		}
    	return null;
    }
    
    private void initInCarFreeDialog() {
		String[] prefNames = {
			PreferencesStorage.INCAR_MODE_ENABLED,
			PreferencesStorage.POWER_BT_DISABLE,
			PreferencesStorage.POWER_BT_ENABLE,
			PreferencesStorage.HEADSET_REQUIRED,
			PreferencesStorage.POWER_REQUIRED,
			PreferencesStorage.SCREEN_TIMEOUT,
			PreferencesStorage.BRIGHTNESS,
			PreferencesStorage.BLUETOOTH,
			PreferencesStorage.ADJUST_VOLUME_LEVEL,
			PreferencesStorage.VOLUME_LEVEL,
			PreferencesStorage.ADJUST_WIFI,
			PreferencesStorage.AUTO_SPEAKER
		};
		final PreferenceScreen prefScr = (PreferenceScreen)findPreference(SCREEN_BT_DEVICE);
		prefScr.setEnabled(false);
		for(String prefName : prefNames) {
	    	final Preference pref = (Preference)findPreference(prefName);
	    	pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					if (preference instanceof CheckBoxPreference) {
						((CheckBoxPreference) preference).setChecked(false);
					} else if (preference instanceof ListPreference) {
						((ListPreference) preference).getDialog().hide();
					}
					showDialog(DIALOG_DONATE);
					return true;
				}
	    	});
		}
    }
    
    private void initInCar() {
    	initBluetooth();
    }
    
    private void initBluetooth() {
    	CheckBoxPreference btSwitch = (CheckBoxPreference)findPreference(PREF_BT_SWITCH);
    	btSwitch.setChecked(Bluetooth.getState() == BluetoothAdapter.STATE_ON);
    	btSwitch.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Boolean switchOn = (Boolean)newValue;
				showDialog(DIALOG_WAIT);
				mBluetoothReceiver = new BluetoothStateReceiver();
				registerReceiver(mBluetoothReceiver, INTENT_FILTER);
				if (Bluetooth.getState() == BluetoothAdapter.STATE_ON) {
					Bluetooth.switchOff();
					return (switchOn == false);
				} else {
					Bluetooth.switchOn();
					return (switchOn == true);
				}
			}
		});
    	mBluetoothDevicesCategory = (PreferenceCategory)findPreference(CATEGORY_BT_DEVICE);
    	PreferenceScreen bluetoothDevicesScreen = (PreferenceScreen)findPreference(SCREEN_BT_DEVICE);
     	bluetoothDevicesScreen.setOnPreferenceClickListener( new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				new InitBluetoothDevicesTask().execute(0);
				return true;
			}
    		
    	});

    }
    
    private class InitBluetoothDevicesTask extends AsyncTask<Integer, Integer, Boolean> { 
    	private ArrayList<CheckBoxPreference> mPairedList;
    	private BluetoothAdapter mBtAdapter;
    	
    	protected void onPreExecute() {
    		showDialog(DIALOG_INIT);
    		mBluetoothDevicesCategory.removeAll();
            // Get the local Bluetooth adapter
        	mBtAdapter = BluetoothAdapter.getDefaultAdapter();

    	}

    	protected void onPostExecute(Boolean result) {
    		if (result == true) {
    			for (int i = 0; i<mPairedList.size(); i++) { 
    				mBluetoothDevicesCategory.addPreference(mPairedList.get(i));
    			}
    		} else {
    			Preference emptyPref = new Preference(mContext); 
    			Resources r = mContext.getResources();
    			emptyPref.setTitle(r.getString(R.string.no_paired_devices_found_title));
    			emptyPref.setSummary(r.getString(R.string.no_paired_devices_found_summary));    			
    			mBluetoothDevicesCategory.addPreference(emptyPref);    			    			
    		}
    		mPairedList = null;
    		try {
    			dismissDialog(DIALOG_INIT);
    		}  catch (IllegalArgumentException e) {}
        }
        public void onProgressUpdate(Integer... values) {

        }
    	@Override
    	protected Boolean doInBackground(Integer... arg0) {
    		return loadPairedDevices();
    	}
    	
        private Boolean loadPairedDevices()  {

            // Get a set of currently paired devices
            Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

            // If there are paired devices, add each one to the ArrayAdapter
            if (pairedDevices.size() > 0) {
            	HashMap<String,String> devices=PreferencesStorage.getBtDevices(mContext);
            	mPairedList = new ArrayList<CheckBoxPreference>(pairedDevices.size());
                for (BluetoothDevice device : pairedDevices) {
                	boolean checked = (devices == null) ? false : devices.containsKey(device.getAddress());
                	CheckBoxPreference pref = createPref(device, checked);
                	mPairedList.add(pref);
                }
                return true;
            }
            return false;
        }
        
        private CheckBoxPreference createPref(BluetoothDevice device, boolean checked) {
        	CheckBoxPreference pref = new CheckBoxPreference(mContext);
        	pref.setPersistent(false);
        	pref.setChecked(checked);
        	pref.setDefaultValue(checked);
        	pref.setKey(device.getAddress());
	        pref.setTitle(device.getName());
	        BluetoothClass btClass = device.getBluetoothClass();
	        int res = BluetoothClassHelper.getBtClassString(btClass);
	        if (res > 0) {
	        	String title = mContext.getResources().getString(res);
	        	pref.setSummary(title);
	        }
	        pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {	
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					HashMap<String,String> devices=PreferencesStorage.getBtDevices(mContext);
					String address = preference.getKey();
					Boolean checked = (Boolean)newValue;
					
					if (checked) {
						if (devices == null) {
							devices = new HashMap<String,String>();
						}
						devices.put(address, address);
						PreferencesStorage.saveBtDevices(mContext, devices);
						((CheckBoxPreference)preference).setChecked(true);
					} else {
						if (devices == null) {
							return true;
						}
						devices.remove(address);
						PreferencesStorage.saveBtDevices(mContext, devices);
						((CheckBoxPreference)preference).setChecked(false);
					}
					return true;
				}
			});
	        return pref;
 	
        }
    }
    
    class BluetoothStateReceiver extends BroadcastReceiver
    {
    	public void onReceive(Context paramContext, Intent paramIntent)
    	{
    		int state = paramIntent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
    		if (state == BluetoothAdapter.STATE_ON) {
        		try{
        			dismissDialog(DIALOG_WAIT);
        		}  catch (IllegalArgumentException e) {}
	    		unregisterReceiver(mBluetoothReceiver);
		    	mBluetoothReceiver = null;
    			new InitBluetoothDevicesTask().execute(0);
    		} else if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.ERROR) {
        		try{
        			dismissDialog(DIALOG_WAIT);
        		}  catch (IllegalArgumentException e) {}
	    		unregisterReceiver(mBluetoothReceiver);
		    	mBluetoothReceiver = null;
    		}
    	}
    }    
}