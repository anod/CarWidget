package com.anod.car.home.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;

import com.anod.car.home.R;
import com.anod.car.home.app.CarWidgetActivity;
import com.anod.car.home.appwidget.WidgetHelper;
import com.anod.car.home.drawer.NavigationDrawer;
import com.anod.car.home.drawer.NavigationList;
import com.anod.car.home.prefs.TrialDialogs;
import com.anod.car.home.utils.IntentUtils;
import com.anod.car.home.utils.Utils;
import com.anod.car.home.utils.Version;

/**
 * @author alex
 * @date 5/24/13
 */
public class WidgetsListActivity extends CarWidgetActivity {

    private Context mContext;
    private boolean mWizardShown;
    private NavigationDrawer mDrawer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mContext = this;

        int[] appWidgetIds = WidgetHelper.getAllWidgetIds(this);

        mDrawer = new NavigationDrawer(this, 0);
        mDrawer.setSelected(NavigationList.ID_WIDGETS);

        if (savedInstanceState == null) {
            // to give support on lower android version, we are not calling getFragmentManager()
            FragmentManager fm = getSupportFragmentManager();

            // Create the list fragment and add it as our sole content.
            if (fm.findFragmentById(R.id.content_frame) == null) {
                WidgetsListFragment f = WidgetsListFragment.newInstance();
                fm.beginTransaction().add(R.id.content_frame, f).commit();
            }
        } else {
            mWizardShown = savedInstanceState.getBoolean("wizard-shown");
        }

        Version version = new Version(this);
        if (mWizardShown) {
            if (version.isFree() && Utils.isProInstalled(mContext)) {
                TrialDialogs.buildProInstalledDialog(mContext).show();
            }
        } else {
            boolean isFreeInstalled = !version.isFree() && Utils.isFreeInstalled(this);
            if (appWidgetIds.length == 0 && !isFreeInstalled) {
                mWizardShown = true;
                startWizard();
            }
        }

    }


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
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("wizard-shown", mWizardShown);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDrawer.refresh();
    }

    private void startWizard() {
        Intent intent = new Intent(mContext, WizardActivity.class);
        startActivity(intent);
    }


	public void startConfigActivity(int appWidgetId) {
		Intent configIntent = IntentUtils.createSettingsIntent(this, appWidgetId);
		startActivity(configIntent);
	}

}