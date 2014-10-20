package com.anod.car.home.prefs;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.anod.car.home.R;

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author alex
 * @date 2014-10-20
 */
public class LookAndFeelDrawer {
    private final ActionBarDrawerToggle mDrawerToggle;
    private final CharSequence mTitle;
    private final CharSequence mDrawerTitle;
    @InjectView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @InjectView(R.id.left_drawer) ListView mDrawerList;

    public LookAndFeelDrawer(final LookAndFeelActivity activity) {
        ButterKnife.inject(this,activity);

        mTitle = mDrawerTitle = activity.getTitle();

        String[] items = new String[] {"InCar settings" ,"Backup / Restore", "Car Widget Pro"};
        ArrayAdapter adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, Arrays.asList(items));

        mDrawerList.setAdapter(adapter);

        mDrawerToggle = new ActionBarDrawerToggle(activity, mDrawerLayout, R.string.drawer_open, R.string.drawer_close)
        {

            // Called when a drawer has settled in a completely closed state.
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                activity.getSupportActionBar().setTitle(mTitle);
                activity.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            // Called when a drawer has settled in a completely open state.
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                activity.getSupportActionBar().setTitle(mDrawerTitle);
                activity.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setHomeButtonEnabled(true);
    }

    public void syncState() {
        mDrawerToggle.syncState();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item);
    }
}
