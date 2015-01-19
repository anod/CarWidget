package com.anod.car.home.prefs;

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
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.annotation.StringRes;

import com.anod.car.home.R;
import com.anod.car.home.appwidget.WidgetHelper;
import com.anod.car.home.drawer.NavigationList;
import com.anod.car.home.incar.ActivityRecognitionClientService;
import com.anod.car.home.incar.Bluetooth;
import com.anod.car.home.incar.BluetoothClassHelper;
import com.anod.car.home.incar.BroadcastService;
import com.anod.car.home.incar.SamsungDrivingMode;
import com.anod.car.home.prefs.preferences.InCar;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.utils.TrialDialogs;
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
	private static final String PREF_NOTIF_SHORTCUTS = "notif-shortcuts";

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
        getActivity().setTitle(R.string.incar_mode);

		final Version version = new Version(mContext);

		initInCar();
		if (version.isFree()) {
			mTrialsLeft = version.getTrialTimesLeft();
			showTrialDialog();
		}
	}

    @Override
    protected int getNavigationItem() {
        return NavigationList.ID_CAR_SETTINGS;
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

	public Dialog createTrialDialog() {
        if (Utils.isProInstalled(mContext)) {
            return TrialDialogs.buildProInstalledDialog(mContext);
        } else {
            mTrialMessageShown = true;
            return TrialDialogs.buildTrialDialog(mTrialsLeft, mContext);
        }
	}

	private void showTrialDialog() {
		if (!mTrialMessageShown || mTrialsLeft == 0) {
            createTrialDialog().show();
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


		initAutorunApp(incar);
		initActivityRecognition();
        initScreenTimeout(incar);


        setIntent(SCREEN_BT_DEVICE, BluetoothDeviceActivity.class, 0);
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
        final Handler handler = new Handler();

        new Thread(new Runnable() {
            public void run() {
                final int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
                final String summary = renderPlayServiceStatus(status);
                handler.post(new Runnable() {
                    public void run() {
                        updateActivityRecognition(status, summary, pref);
                    }
                });
            }
        }).start();

	}

    private void updateActivityRecognition(final int status, final String summary, final Preference pref) {
        pref.setSummary(summary);
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

}