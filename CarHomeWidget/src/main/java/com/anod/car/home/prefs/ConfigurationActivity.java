package com.anod.car.home.prefs;

import com.anod.car.home.Provider;
import com.anod.car.home.R;
import com.anod.car.home.app.CarWidgetActivity;
import com.anod.car.home.drawer.NavigationDrawer;
import com.anod.car.home.utils.Utils;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;
import android.view.Window;


public class ConfigurationActivity extends CarWidgetActivity
        implements PreferenceFragment.OnPreferenceStartFragmentCallback {

    private static final String BACK_STACK_PREFS = ":carwidget:prefs";

    private onActivityResultListener mActivityResultListener;

    private NavigationDrawer mDrawer;

    private int mAppWidgetId;

    public void setActivityResultListener(onActivityResultListener activityResultListener) {
        mActivityResultListener = activityResultListener;
    }

    public void setNavigationItem(int navigationItem) {
        mDrawer.setSelected(navigationItem);
    }

    public interface onActivityResultListener {

        public void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    public static Intent createFragmentIntent(Context context, Class<?> fragment) {
        Intent intent = new Intent(context, ConfigurationActivity.class);
        intent.putExtra(EXTRA_FRAGMENT, fragment);
        return intent;
    }

    public static final String EXTRA_FRAGMENT = "fragment";

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawer.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawer.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        mAppWidgetId = Utils.readAppWidgetId(savedInstanceState, getIntent());
        mDrawer = new NavigationDrawer(this, mAppWidgetId);

        if (savedInstanceState == null) {

            Fragment conf = createFragmentInstance();

            conf.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction().add(R.id.content_frame, conf).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mDrawer.refresh();
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
        Bundle args = new Bundle();
        Fragment conf = Fragment.instantiate(this, fragmentClassName, args);
        return conf;
    }

    public void onApplyClick() {
        getFragmentManager().popBackStack();
    }


    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
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
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, f);
        if (titleText != null) {
            transaction.setBreadCrumbTitle(titleText);
        }
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(BACK_STACK_PREFS);
        transaction.commitAllowingStateLoss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mActivityResultListener != null) {
            mActivityResultListener.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        Provider.getInstance().performUpdate(this, null);
        super.onBackPressed();
    }

}