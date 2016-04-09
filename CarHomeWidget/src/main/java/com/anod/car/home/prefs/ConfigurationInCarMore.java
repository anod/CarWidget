package com.anod.car.home.prefs;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.anod.car.home.R;
import com.anod.car.home.incar.SamsungDrivingMode;
import com.anod.car.home.prefs.model.InCarSettings;
import com.anod.car.home.prefs.model.InCarStorage;

public class ConfigurationInCarMore extends ConfigurationPreferenceFragment
        implements OnCheckedChangeListener {

    private static final String AUTORUN_APP_PREF = "autorun-app-choose";

    private static final String AUTORUN_APP_DISABLED = "disabled";

    private static final String AUTORUN_APP_CUSTOM = "custom";

    protected static final int REQUEST_PICK_APPLICATION = 0;
    private InCarSettings mPrefs;

    @Override
    protected boolean isAppWidgetIdRequired() {
        return false;
    }

    @Override
    protected int getXmlResource() {
        return R.xml.preference_incar_more;
    }

    @Override
    protected void onCreateImpl(Bundle savedInstanceState) {
        mPrefs = InCarStorage.load(getActivity());
        initAutorunApp();
        initSamsungHandsfree();
    }

    @Override
    protected String getSharedPreferencesName() {
        return InCarStorage.PREF_NAME;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mPrefs.setAdjustVolumeLevel(isChecked);
        mPrefs.apply();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_PICK_APPLICATION) {
            saveAutorunApp(data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void initSamsungHandsfree() {
        if (!SamsungDrivingMode.hasMode()) {
            final Preference samDrivingPref = findPreference(
                    InCarSettings.SAMSUNG_DRIVING_MODE);
            ((PreferenceCategory) findPreference("incar-more-category"))
                    .removePreference(samDrivingPref);
        }
    }

    private void saveAutorunApp(Intent data) {
        ComponentName component = null;
        if (data != null) {
            component = data.getComponent();
        }
        // update storage
        mPrefs.setAutorunApp(component);
        mPrefs.apply();
        updateAutorunAppPref(data);
    }

    private void initAutorunApp() {
        final ListPreference pref = (ListPreference) findPreference(AUTORUN_APP_PREF);
        ComponentName autorunApp = mPrefs.getAutorunApp();
        if (autorunApp == null) {
            updateAutorunAppPref(null);
        } else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setComponent(autorunApp);
            updateAutorunAppPref(intent);
        }
        pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String selection = (String) newValue;
                if (selection.equals(AUTORUN_APP_DISABLED)) {
                    saveAutorunApp(null);
                } else {
                    Intent mainIntent = new Intent(getActivity(), AllAppsActivity.class);
                    startActivityForResult(mainIntent, REQUEST_PICK_APPLICATION);
                }
                return false;
            }
        });
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
            PackageManager pm = getActivity().getPackageManager();
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
