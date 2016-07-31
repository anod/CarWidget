package com.anod.car.home.prefs;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.anod.car.home.R;
import com.anod.car.home.prefs.views.SeekBarDialogPreference;
import com.anod.car.home.prefs.views.SeekBarPreferenceDialogFragment;
import info.anodsplace.android.log.AppLog;
import com.anod.car.home.utils.Utils;

/**
 * @author alex
 * @date 11/19/13
 */
abstract public class ConfigurationPreferenceFragment extends PreferenceFragmentCompat {

    protected int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    abstract protected int getXmlResource();

    abstract protected void onCreateImpl(Bundle savedInstanceState);

    abstract protected String getSharedPreferencesName();

    protected boolean isAppWidgetIdRequired() {
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        int res = getOptionsMenuResource();
        if (res == 0) {
            super.onCreateOptionsMenu(menu, inflater);
            return;
        }
        inflater.inflate(res, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (isAppWidgetIdRequired()) {
            mAppWidgetId = Utils.readAppWidgetId(savedInstanceState, getActivity().getIntent());
        }
        super.onCreate(savedInstanceState);
        if (getOptionsMenuResource() > 0) {
            setHasOptionsMenu(true);
        }

        onCreateImpl(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(getSharedPreferencesName());
        addPreferencesFromResource(getXmlResource());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (isAppWidgetIdRequired()) {
            if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                Intent defaultResultValue = new Intent();
                defaultResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                getActivity().setResult(Activity.RESULT_OK, defaultResultValue);
            } else {
                AppLog.w("AppWidgetId required");
                getActivity().finish();
            }
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (isAppWidgetIdRequired()) {
            outState.putInt("appWidgetId", mAppWidgetId);
        }
    }

    protected int getOptionsMenuResource() {
        return 0;
    }

    protected void setIntent(String key, Class<?> cls, int appWidgetId) {
        Preference pref = findPreference(key);
        Intent intent = new Intent(getActivity(), cls);
        if (appWidgetId > 0) {
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        }
        pref.setIntent(intent);
    }

    protected void showFragmentOnClick(final String key, final Class<?> fragmentCls) {
        Preference pref = findPreference(key);
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((ConfigurationActivity) getActivity())
                        .startPreferencePanel(fragmentCls.getName(), preference);
                return true;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.apply) {
            ((ConfigurationActivity) getActivity()).onApplyClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof SeekBarDialogPreference) {
            if (getFragmentManager().findFragmentByTag("android.support.v7.preference.PreferenceFragment.DIALOG") == null) {

                DialogFragment f = SeekBarPreferenceDialogFragment.newInstance(preference.getKey());
                f.setTargetFragment(this, 0);
                f.show(this.getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
            }
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

}
