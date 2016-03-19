package com.anod.car.home.prefs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;

import com.anod.car.home.R;
import com.anod.car.home.appwidget.WidgetHelper;
import com.anod.car.home.incar.ActivityRecognitionClientService;
import com.anod.car.home.incar.BroadcastService;
import com.anod.car.home.incar.SamsungDrivingMode;
import com.anod.car.home.prefs.preferences.InCar;
import com.anod.car.home.prefs.preferences.InCarStorage;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.utils.TrialDialogs;
import com.anod.car.home.utils.Utils;
import com.anod.car.home.utils.Version;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class ConfigurationInCar extends ConfigurationPreferenceFragment {

    private static final String MEDIA_SCREEN = "media-screen";

    private static final String MORE_SCREEN = "more-screen";

    private static final String SCREEN_BT_DEVICE = "bt-device-screen";

    private static final String PREF_NOTIF_SHORTCUTS = "notif-shortcuts";

    public static final int PS_DIALOG_REQUEST_CODE = 4;

    public static final String SCREEN_TIMEOUT_LIST = "screen-timeout-list";

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
        InCar incar = InCarStorage.loadInCar(mContext);

        int[] appWidgetIds = WidgetHelper.getAllWidgetIds(mContext);
        final int widgetsCount = appWidgetIds.length;

        Preference incarSwitch = findPreference(InCarStorage.INCAR_MODE_ENABLED);

        if (widgetsCount == 0) {
            incarSwitch.setEnabled(false);
            incarSwitch.setSummary(R.string.please_add_widget);
        } else {
            incarSwitch.setEnabled(true);
            incarSwitch.setSummary("");
        }

        incarSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((Boolean) newValue) {
                    BroadcastService.startService(mContext);
                } else {
                    BroadcastService.stopService(mContext);
                }
                return true;
            }
        });

        registerBroadcastServiceSwitchListener(InCarStorage.HEADSET_REQUIRED);
        registerBroadcastServiceSwitchListener(InCarStorage.POWER_REQUIRED);
        registerBroadcastServiceSwitchListener(InCarStorage.CAR_DOCK_REQUIRED);

        initActivityRecognition();
        initScreenTimeout(incar);

        setIntent(SCREEN_BT_DEVICE, BluetoothDeviceActivity.class, 0);
        showFragmentOnClick(MEDIA_SCREEN, ConfigurationInCarVolume.class);
        showFragmentOnClick(MORE_SCREEN, ConfigurationInCarMore.class);
        showFragmentOnClick(PREF_NOTIF_SHORTCUTS, ConfigurationNotifShortcuts.class);

    }

    private void registerBroadcastServiceSwitchListener(String key) {
        Preference pref = findPreference(key);
        pref.setOnPreferenceChangeListener(mBroadcastServiceSwitchListener);
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

        pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String value = (String) newValue;
                if ("disabled-charging".equals(value)) {
                    InCarStorage.saveScreenTimeout(true, true, mContext);
                    pref.setValue("disabled-charging");
                } else if ("disabled".equals(value)) {
                    InCarStorage.saveScreenTimeout(true, false, mContext);
                    pref.setValue("disabled");
                } else {
                    InCarStorage.saveScreenTimeout(false, false, mContext);
                    pref.setValue("enabled");
                }
                return false;
            }
        });
    }

    private void initActivityRecognition() {
        final Preference pref = findPreference(InCarStorage.ACTIVITY_RECOGNITION);
        final Handler handler = new Handler();

        new Thread(new Runnable() {
            public void run() {
                final int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mContext);
                final String summary = renderPlayServiceStatus(status);
                handler.post(new Runnable() {
                    public void run() {
                        updateActivityRecognition(status, summary, pref);
                    }
                });
            }
        }).start();

    }

    private void updateActivityRecognition(final int status, final String summary,
            final Preference pref) {
        pref.setSummary(summary);
        if (status != ConnectionResult.SUCCESS) {
            pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Dialog d = GoogleApiAvailability.getInstance()
                            .getErrorDialog(getActivity(), status, PS_DIALOG_REQUEST_CODE);
                    d.show();
                    return false;
                }
            });
        } else {
            pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Boolean val = (Boolean) newValue;
                    if (val) {
                        ActivityRecognitionClientService.startService(mContext);
                    } else {
                        ActivityRecognitionClientService.stopService(mContext);
                    }
                    return true;
                }
            });
        }

    }

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
        return GoogleApiAvailability.getInstance().getErrorString(errorCode);
    }

    private Preference.OnPreferenceChangeListener mBroadcastServiceSwitchListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if ((Boolean) newValue || isBroadcastServiceRequired()) {
                BroadcastService.startService(mContext);
            } else {
                BroadcastService.stopService(mContext);
            }
            return true;
        }
    };

    private boolean isBroadcastServiceRequired() {
        InCar incar = InCarStorage.loadInCar(mContext);
        return BroadcastService.isServiceRequired(incar);
    }
}