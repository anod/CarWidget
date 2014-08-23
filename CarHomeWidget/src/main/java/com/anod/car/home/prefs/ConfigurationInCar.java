package com.anod.car.home.prefs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
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
import com.anod.car.home.appwidget.WidgetHelper;
import com.anod.car.home.incar.ActivityRecognitionClientService;
import com.anod.car.home.incar.Bluetooth;
import com.anod.car.home.incar.BluetoothClassHelper;
import com.anod.car.home.incar.BroadcastService;
import com.anod.car.home.incar.SamsungDrivingMode;
import com.anod.car.home.prefs.preferences.InCar;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.utils.Utils;
import com.anod.car.home.utils.Version;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class ConfigurationInCar extends ConfigurationPreferenceFragment {

    private static final String MEDIA_SCREEN = "media-screen";
	private static final String SCREEN_BT_DEVICE = "bt-device-screen";
	private static final String CATEGORY_BT_DEVICE = "bt-device-category";
	private static final String PREF_BT_SWITCH = "bt-switch";
	private static final String PREF_NOTIF_SHORTCUTS = "notif-shortcuts";

	private static final int DIALOG_TRIAL = 2;

	private static final String AUTORUN_APP_PREF = "autorun-app-choose";
	private static final String AUTORUN_APP_DISABLED = "disabled";
	private static final String AUTORUN_APP_CUSTOM = "custom";

	private static final IntentFilter INTENT_FILTER = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
	protected static final int REQUEST_PICK_APPLICATION = 0;
	public static final int PS_DIALOG_REQUEST_CODE = 4;
    public static final String SCREEN_TIMEOUT_LIST = "screen-timeout-list";
    private PreferenceCategory mBluetoothDevicesCategory;
	private BroadcastReceiver mBluetoothReceiver;

	private int mTrialsLeft;
	private boolean mTrialMessageShown;

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
		getActivity().setResult(Activity.RESULT_OK);

		final Version version = new Version(mContext);

		initInCar();
		if (version.isFree()) {
			mTrialsLeft = version.getTrialTimesLeft();
			showTrialDialog();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mBluetoothReceiver != null) {
			getActivity().unregisterReceiver(mBluetoothReceiver);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mBluetoothReceiver != null) {
			getActivity().registerReceiver(mBluetoothReceiver, INTENT_FILTER);
		}
	}

	@Override
	public Dialog onCreateDialog(int id) {
//		initDialog.setMessage(getResources().getString(R.string.load_paired_device));
		 if (id == DIALOG_TRIAL) {
			if (Utils.isProInstalled(mContext)) {
				return TrialDialogs.buildProInstalledDialog(mContext);
			} else {
				mTrialMessageShown = true;
				return TrialDialogs.buildTrialDialog(mTrialsLeft, mContext);
			}
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
		InCar incar = PreferencesStorage.loadInCar(mContext);

		int[] appWidgetIds = WidgetHelper.getAllWidgetIds(mContext);
		final int widgetsCount = appWidgetIds.length;

		Preference incarSwitch = findPreference(PreferencesStorage.INCAR_MODE_ENABLED);

		if (widgetsCount == 0) {
			incarSwitch.setEnabled(false);
			incarSwitch.setSummary(R.string.please_add_widget);
		} else {
			incarSwitch.setEnabled(true);
			incarSwitch.setSummary("");
		}

		incarSwitch.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
			if ((Boolean)newValue) {
				final Intent updateIntent = new Intent(mContext.getApplicationContext(), BroadcastService.class);
				mContext.startService(updateIntent);
			} else {
				final Intent updateIntent = new Intent(mContext.getApplicationContext(), BroadcastService.class);
				mContext.stopService(updateIntent);
			}
			return true;
			}
		});

		initBluetooth();
		initAutorunApp(incar);
		initActivityRecognition();
        initScreenTimeout(incar);


		showFragmentOnClick(MEDIA_SCREEN, ConfigurationInCarVolume.class);

		showFragmentOnClick(PREF_NOTIF_SHORTCUTS, ConfigurationNotifShortcuts.class);

		initSamsungHandsfree();
	}

    private void initScreenTimeout(InCar incar) {
        final ListPreference pref = (ListPreference) findPreference(SCREEN_TIMEOUT_LIST);

        if (incar.isDisableScreenTimeout()) {
            if (incar.isDisableScreenTimeoutCharging()) {
                pref.setValue("disabled-charging");
            } else {
                pref.setValue("disabled");
            }
        } else {
            pref.setValue("enabled");
        }


        pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String value = (String) newValue;
                if ("disabled-charging".equals(value)) {
                    PreferencesStorage.saveScreenTimeout(true, true, mContext);
                    pref.setValue("disabled-charging");
                } else if ("disabled".equals(value)) {
                    PreferencesStorage.saveScreenTimeout(true, false, mContext);
                    pref.setValue("disabled");
                } else {
                    PreferencesStorage.saveScreenTimeout(false, false, mContext);
                    pref.setValue("enabled");
                }
                return false;
            }
        });
    }

    private void initSamsungHandsfree() {
		if (!SamsungDrivingMode.hasMode()) {
			final Preference samDrivingPref = findPreference(PreferencesStorage.SAMSUNG_DRIVING_MODE);
			((PreferenceCategory)findPreference("incar-more-category"))
					.removePreference(samDrivingPref);
		}
	}

	private void initActivityRecognition() {
		final Preference pref = (Preference) findPreference(PreferencesStorage.ACTIVITY_RECOGNITION);
		final int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
		pref.setSummary(renderPlayServiceStatus(status));
		if (status != ConnectionResult.SUCCESS) {
			pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object o) {
					Dialog d = GooglePlayServicesUtil.getErrorDialog(status, getActivity(), PS_DIALOG_REQUEST_CODE);
					d.show();
					return false;
				}
			});
		} else {
			pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Boolean val = (Boolean)newValue;
					if (val) {
						mContext.startService(ActivityRecognitionClientService.makeStartIntent(mContext));
					} else {
						mContext.stopService(ActivityRecognitionClientService.makeStartIntent(mContext));
					}
					return true;
				}
			});
		}
	}

	/**
	 * @param errorCode
	 * @return
	 */
	private String renderPlayServiceStatus(int errorCode) {
		if (errorCode == ConnectionResult.SUCCESS) {
			return getString(R.string.gms_success);
		}
		if (errorCode == ConnectionResult.SERVICE_MISSING) {
			return getString(R.string.gms_service_missing);
		}
		if (errorCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
			return getString(R.string.gms_service_update_required);
		}
		if (errorCode == ConnectionResult.SERVICE_DISABLED) {
			return getString(R.string.gms_service_disabled);
		}
		if (errorCode == ConnectionResult.SERVICE_INVALID) {
			return getString(R.string.gms_service_invalid);
		}
		return GooglePlayServicesUtil.getErrorString(errorCode);
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
					Intent mainIntent = new Intent(mContext, AllAppsActivity.class);
					startActivityForResult(mainIntent, REQUEST_PICK_APPLICATION);
				}
				return false;
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_PICK_APPLICATION) {
			saveAutorunApp(data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void saveAutorunApp(Intent data) {
		ComponentName component = null;
		if (data != null) {
			component = data.getComponent();
		}
		// update storage
		PreferencesStorage.saveAutorunApp(component, mContext);
		updateAutorunAppPref(data);
	}

	private void updateAutorunAppPref(Intent data) {
		final ListPreference pref = (ListPreference) findPreference(AUTORUN_APP_PREF);
		String title = null;
		String value = null;
		if (data == null) {
			title = getString(R.string.disabled);
			value = AUTORUN_APP_DISABLED;
		} else {
			// get name
			PackageManager pm = mContext.getPackageManager();
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
		((SwitchPreference) btSwitch).setChecked(Bluetooth.getState() == BluetoothAdapter.STATE_ON);
		btSwitch.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Boolean switchOn = (Boolean) newValue;
				mBluetoothReceiver = new BluetoothStateReceiver();
				getActivity().registerReceiver(mBluetoothReceiver, INTENT_FILTER);
				if (Bluetooth.getState() == BluetoothAdapter.STATE_ON) {
					Bluetooth.switchOff();
					return (!switchOn);
				} else {
					Bluetooth.switchOn();
					return switchOn;
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
			mBluetoothDevicesCategory.removeAll();
			// Get the local Bluetooth adapter
			mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		}

		protected void onPostExecute(Boolean result) {
			mBluetoothDevicesCategory.removeAll();
			if (result) {
				for (int i = 0; i < mPairedList.size(); i++) {
					mBluetoothDevicesCategory.addPreference(mPairedList.get(i));
				}
			} else {
				Preference emptyPref = new Preference(mContext);
				Resources r = mContext.getResources();
				emptyPref.setTitle(r.getString(R.string.no_paired_devices_found_title));
				emptyPref.setSummary(r.getString(R.string.no_paired_devices_found_summary));
				emptyPref.setLayoutResource(R.layout.pref);
				mBluetoothDevicesCategory.addPreference(emptyPref);
			}
			mPairedList = null;
		}

		public void onProgressUpdate(Integer... values) {
			//Nothing
		}

		@Override
		protected Boolean doInBackground(Integer... arg0) {
			return loadPairedDevices();
		}

		private Boolean loadPairedDevices() {

			// Get a set of currently paired devices
			Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

			// If there are paired devices, add each one to the ArrayAdapter
			if (!pairedDevices.isEmpty()) {
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
			pref.setLayoutResource(R.layout.pref);
			BluetoothClass btClass = device.getBluetoothClass();
			int res = 0;
			if (btClass != null) {
				res = BluetoothClassHelper.getBtClassString(btClass);
			}
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
				new InitBluetoothDevicesTask().execute(0);
			} else if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.ERROR) {
			}
		}

	}

}