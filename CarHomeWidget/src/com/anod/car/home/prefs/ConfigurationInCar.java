package com.anod.car.home.prefs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import com.anod.car.home.R;
import com.anod.car.home.incar.Bluetooth;
import com.anod.car.home.incar.BluetoothClassHelper;
import com.anod.car.home.prefs.preferences.InCar;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.utils.IntentUtils;
import com.anod.car.home.utils.Utils;
import com.anod.car.home.utils.Version;

public class ConfigurationInCar extends ConfigurationActivity {
	private static final String SCREEN_BT_DEVICE = "bt-device-screen";
	private static final String CATEGORY_BT_DEVICE = "bt-device-category";
	private static final String PREF_BT_SWITCH = "bt-switch";
	private static final String PREF_NOTIF_SHORTCUTS = "notif-shortcuts";

	private static final int DIALOG_TRIAL = 2;
	private static final int DIALOG_INIT = 3;

	private static final String AUTORUN_APP_PREF = "autorun-app-choose";
	private static final String AUTORUN_APP_DISABLED = "disabled";
	private static final String AUTORUN_APP_CUSTOM = "custom";

	private static final IntentFilter INTENT_FILTER = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
	protected static final int REQUEST_PICK_APPLICATION = 0;
	private PreferenceCategory mBluetoothDevicesCategory;
	private BroadcastReceiver mBluetoothReceiver;

	private int mTrialsLeft = 0;
	private boolean mTrialMessageShown = false;

	@Override
	protected boolean isAppWidgetIdRequired() {
		return false;
	}

	@Override
	protected int getXmlResource() {
		return R.xml.preference_incar;
	}

	@Override
	protected void onCreateImpl(Bundle savedInstanceState) {
		setResult(RESULT_OK);

		Version version = new Version(this);
		mContext = (Context) this;

		initInCar();
		if (version.isFree()) {
			mTrialsLeft = version.getTrialTimesLeft();
			showTrialDialog();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mBluetoothReceiver != null) {
			unregisterReceiver(mBluetoothReceiver);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mBluetoothReceiver != null) {
			registerReceiver(mBluetoothReceiver, INTENT_FILTER);
		}
	}

	@Override
	public Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_INIT:
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
		case DIALOG_TRIAL:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setCancelable(true);
			
			if (Utils.isProInstalled(this)) {
				builder.setTitle(R.string.dialog_donate_title_install);
				builder.setMessage(R.string.dialog_donate_message_installed);
				builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
			} else {
				int negativeRes = 0;
				if (mTrialsLeft > 0) {
					builder.setTitle(R.string.dialog_donate_title_trial);
					builder.setMessage(getString(R.string.dialog_donate_message_trial, mTrialsLeft));
					negativeRes = R.string.dialog_donate_btn_trial;
				} else {
					builder.setTitle(R.string.dialog_donate_title_expired);
					builder.setMessage(R.string.dialog_donate_message_expired);
					negativeRes = R.string.dialog_donate_btn_no;
				}
				builder.setNeutralButton(R.string.dialog_donate_btn_yes, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = IntentUtils.createProVersionIntent();
						startActivity(intent);
						dialog.dismiss();
					}

				});
				builder.setNegativeButton(negativeRes, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
			}
			mTrialMessageShown = true;
			return builder.create();
		}
		return super.onCreateDialog(id);
	}

	@SuppressWarnings("deprecation")
	private void showTrialDialog() {
		if (!mTrialMessageShown || mTrialsLeft == 0) {
			showDialog(DIALOG_TRIAL);
		}
	}
	
	private void initInCar() {
		InCar incar = PreferencesStorage.loadInCar(this);
		initBluetooth();
		initAutorunApp(incar);
		setIntent(PREF_NOTIF_SHORTCUTS, ConfigurationNotifShortcuts.class, 0);
	}

	private void initAutorunApp(InCar incar) {
		final ListPreference pref = (ListPreference) findPreference(AUTORUN_APP_PREF);
		ComponentName autorunApp = incar.getAutorunApp();
		if (autorunApp == null) {
			updateAutorunAppPref(null);
		} else {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setComponent(autorunApp);
			updateAutorunAppPref(intent);
		}
		pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String selection = (String) newValue;
				if (selection.equals(AUTORUN_APP_DISABLED)) {
					saveAutorunApp(null);
				} else {
					Intent mainIntent = new Intent(ConfigurationInCar.this, AllAppsActivity.class);
					startActivityForResult(mainIntent, REQUEST_PICK_APPLICATION);
				}
				return false;
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_PICK_APPLICATION:
				saveAutorunApp(data);
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void saveAutorunApp(Intent data) {
		ComponentName component = null;
		if (data != null) {
			component = data.getComponent();
		}
		// update storage
		PreferencesStorage.saveAutorunApp(component, this);
		updateAutorunAppPref(data);
	}

	private void updateAutorunAppPref(Intent data) {
		final ListPreference pref = (ListPreference) findPreference(AUTORUN_APP_PREF);
		String title = null;
		String value = null;
		if (data == null) {
			title = getString(R.string.pref_autorun_app_disabled);
			value = AUTORUN_APP_DISABLED;
		} else {
			// get name
			PackageManager pm = getPackageManager();
			final ResolveInfo resolveInfo = pm.resolveActivity(data, 0);
			if (resolveInfo != null) {
				title = (String) resolveInfo.activityInfo.loadLabel(pm);
			} else {
				title = data.getComponent().getPackageName();
			}
			value = AUTORUN_APP_CUSTOM;
		}
		// update preference
		pref.setSummary(title);
		pref.setValue(value);

	}

	@SuppressLint("NewApi")
	private void initBluetooth() {
		Preference btSwitch = (Preference) findPreference(PREF_BT_SWITCH);
		if (Utils.IS_ICS_OR_GREATER) {
			((SwitchPreference) btSwitch).setChecked(Bluetooth.getState() == BluetoothAdapter.STATE_ON);
		} else {
			((CheckBoxPreference) btSwitch).setChecked(Bluetooth.getState() == BluetoothAdapter.STATE_ON);
		}
		btSwitch.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Boolean switchOn = (Boolean) newValue;
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
		mBluetoothDevicesCategory = (PreferenceCategory) findPreference(CATEGORY_BT_DEVICE);
		PreferenceScreen bluetoothDevicesScreen = (PreferenceScreen) findPreference(SCREEN_BT_DEVICE);
		bluetoothDevicesScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {
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
				for (int i = 0; i < mPairedList.size(); i++) {
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
			} catch (IllegalArgumentException e) {
			}
		}

		public void onProgressUpdate(Integer... values) {

		}

		@Override
		protected Boolean doInBackground(Integer... arg0) {
			return loadPairedDevices();
		}

		private Boolean loadPairedDevices() {

			// Get a set of currently paired devices
			Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

			// If there are paired devices, add each one to the ArrayAdapter
			if (pairedDevices.size() > 0) {
				HashMap<String, String> devices = PreferencesStorage.getBtDevices(mContext);
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
					HashMap<String, String> devices = PreferencesStorage.getBtDevices(mContext);
					String address = preference.getKey();
					Boolean checked = (Boolean) newValue;

					if (checked) {
						if (devices == null) {
							devices = new HashMap<String, String>();
						}
						devices.put(address, address);
						PreferencesStorage.saveBtDevices(mContext, devices);
						((CheckBoxPreference) preference).setChecked(true);
					} else {
						if (devices == null) {
							return true;
						}
						devices.remove(address);
						PreferencesStorage.saveBtDevices(mContext, devices);
						((CheckBoxPreference) preference).setChecked(false);
					}
					return true;
				}
			});
			return pref;

		}
	}

	class BluetoothStateReceiver extends BroadcastReceiver {
		public void onReceive(Context paramContext, Intent paramIntent) {
			int state = paramIntent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
			if (state == BluetoothAdapter.STATE_ON) {
				try {
					dismissDialog(DIALOG_WAIT);
				} catch (IllegalArgumentException e) {
				}
				unregisterReceiver(mBluetoothReceiver);
				mBluetoothReceiver = null;
				new InitBluetoothDevicesTask().execute(0);
			} else if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.ERROR) {
				try {
					dismissDialog(DIALOG_WAIT);
				} catch (IllegalArgumentException e) {
				}
				unregisterReceiver(mBluetoothReceiver);
				mBluetoothReceiver = null;
			}
		}
	}


}