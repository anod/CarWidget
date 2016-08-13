package com.anod.car.home.ui;

import com.anod.car.home.R;
import com.anod.car.home.app.CarWidgetActivity;
import com.anod.car.home.appwidget.WidgetHelper;
import com.anod.car.home.drawer.NavigationDrawer;
import com.anod.car.home.drawer.NavigationList;
import com.anod.car.home.prefs.LookAndFeelActivity;
import com.anod.car.home.prefs.model.PrefsMigrate;
import com.anod.car.home.utils.IntentUtils;
import com.anod.car.home.utils.TrialDialogs;
import com.anod.car.home.utils.Utils;
import com.anod.car.home.utils.Version;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;

/**
 * @author alex
 * @date 5/24/13
 */
public class WidgetsListActivity extends CarWidgetActivity {

    private Context mContext;

    private boolean mWizardShown;

    private NavigationDrawer mDrawer;

    private Version mVersion;

    private boolean mProDialogShown;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mContext = this;

        int[] largeWidgetIds = WidgetHelper.getLargeWidgetIds(this);

        PrefsMigrate.migrate(this, largeWidgetIds);

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
            mProDialogShown = savedInstanceState.getBoolean("dialog-shown");
        }

        mVersion = new Version(this);
        if (!mWizardShown) {
            if (mVersion.isFree() && Utils.isProInstalled(mContext)) {
                if (!mProDialogShown) {
                    mProDialogShown = true;
                    TrialDialogs.buildProInstalledDialog(mContext).show();
                }
            }
            boolean isFreeInstalled = !mVersion.isFree() && Utils.isFreeInstalled(this);
            int[] allWidgtIds = WidgetHelper.getAllWidgetIds(this);
            if (allWidgtIds.length == 0 && !isFreeInstalled) {
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
        outState.putBoolean("dialog-shown", mProDialogShown);
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
        Intent configIntent = new Intent(this, LookAndFeelActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        startActivity(configIntent);
    }

}