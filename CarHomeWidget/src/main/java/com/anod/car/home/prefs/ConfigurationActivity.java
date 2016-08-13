package com.anod.car.home.prefs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.view.MenuItem;

import com.anod.car.home.appwidget.Provider;
import com.anod.car.home.R;
import com.anod.car.home.app.CarWidgetActivity;
import com.anod.car.home.utils.Utils;


public class ConfigurationActivity extends CarWidgetActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback, PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    private static final String BACK_STACK_PREFS = ":carwidget:prefs";
    public static final String EXTRA_FRAGMENT = "fragment";
    private int mAppWidgetId;

    public static Intent createFragmentIntent(Context context, Class<?> fragment) {
        Intent intent = new Intent(context, ConfigurationActivity.class);
        intent.putExtra(EXTRA_FRAGMENT, fragment);
        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAppWidgetId = Utils.readAppWidgetId(savedInstanceState, getIntent());

        if (savedInstanceState == null) {

            Fragment conf = createFragmentInstance();
            conf.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, conf).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Utils.saveAppWidgetId(outState, mAppWidgetId);
    }

    private Fragment createFragmentInstance() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        Class fragmentClass = (Class) extras.get(EXTRA_FRAGMENT);
        String fragmentClassName = fragmentClass.getName();
        Fragment conf = Fragment.instantiate(this, fragmentClassName, new Bundle());
        return conf;
    }

    public void onApplyClick() {
        getFragmentManager().popBackStack();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference preference) {

        return false;
    }

    public void startPreferencePanel(String fragmentClass, Preference pref) {
        startPreferencePanel(fragmentClass, pref.getTitle(), null, 0);
    }

    public void startPreferencePanel(String fragmentClass, CharSequence titleText,
            Fragment resultTo, int resultRequestCode) {
        Bundle args = new Bundle();
        Fragment f = Fragment.instantiate(this, fragmentClass, args);
        if (resultTo != null) {
            f.setTargetFragment(resultTo, resultRequestCode);
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(android.R.id.content, f);
        if (titleText != null) {
            transaction.setBreadCrumbTitle(titleText);
        }
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(BACK_STACK_PREFS);
        transaction.commitAllowingStateLoss();
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat preferenceFragmentCompat, PreferenceScreen preferenceScreen) {
        preferenceFragmentCompat.setPreferenceScreen(preferenceScreen);
        return true;
    }

    @Override
    public void onBackPressed() {
        Provider.getInstance().performUpdate(this, null);
        super.onBackPressed();
    }

}