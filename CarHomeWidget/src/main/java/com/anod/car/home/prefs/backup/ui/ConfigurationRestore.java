package com.anod.car.home.prefs.backup.ui;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.anod.car.home.R;
import com.anod.car.home.prefs.ConfigurationActivity;
import com.anod.car.home.prefs.backup.PreferencesBackupManager;
import com.anod.car.home.utils.AppLog;
import com.anod.car.home.utils.Utils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ConfigurationRestore extends Fragment {

    static final int DOWNLOAD_MAIN_REQUEST_CODE = 1;
    static final int DOWNLOAD_INCAR_REQUEST_CODE = 2;
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Context mContext;

    static final int DATE_FORMAT = DateUtils.FORMAT_SHOW_DATE
            | DateUtils.FORMAT_SHOW_WEEKDAY
            | DateUtils.FORMAT_SHOW_TIME
            | DateUtils.FORMAT_SHOW_YEAR
            | DateUtils.FORMAT_ABBREV_ALL;

    @Bind(R.id.pager)
    ViewPager mViewPager;

    @Bind(R.id.tabs)
    TabLayout mTabs;

    private MenuItem mRefreshMenuItem;
    private PreferencesBackupManager mBackupManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_restore, container, false);

        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mAppWidgetId = Utils.readAppWidgetId(savedInstanceState, getActivity().getIntent());
        super.onActivityCreated(savedInstanceState);

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            AppLog.e("AppWidgetId required");
            getActivity().finish();
            return;
        } else {
            Intent defaultResultValue = new Intent();
            defaultResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            getActivity().setResult(Activity.RESULT_OK, defaultResultValue);
        }

        mContext = getActivity();

        mBackupManager = new PreferencesBackupManager(mContext);
        mViewPager.setAdapter(new RestorePagerAdapter(mAppWidgetId, getChildFragmentManager(), mContext));
        mTabs.setupWithViewPager(mViewPager);

        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.restore, menu);

        mRefreshMenuItem = menu.findItem(R.id.menu_refresh);
        mRefreshMenuItem.setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * stop refresh button animation
     */
     void stopRefreshAnim() {
        if (mRefreshMenuItem == null) {
            return;
        }
        View actionView = mRefreshMenuItem.getActionView();
        if (actionView != null) {
            actionView.clearAnimation();
            mRefreshMenuItem.setActionView(null);
        }
        mRefreshMenuItem.setVisible(false);
    }

    /**
     * Animate refresh button
     */
    void startRefreshAnim() {
        if (mRefreshMenuItem == null) {
            return;
        }
        View actionView = mRefreshMenuItem.getActionView();
        //already animating
        if (actionView != null) {
            return;
        }
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_action_view, null);

        Animation rotation = AnimationUtils.loadAnimation(mContext, R.anim.rotate);
        rotation.setRepeatCount(Animation.INFINITE);
        iv.startAnimation(rotation);

        mRefreshMenuItem.setVisible(true);
        mRefreshMenuItem.setActionView(iv);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        stopRefreshAnim();
        super.onPause();
    }

    PreferencesBackupManager getBackupManager() {
        return mBackupManager;
    }

    private static class RestorePagerAdapter extends FragmentPagerAdapter {
        private final Context mContext;
        private final int mAppWidgetId;
        int[] titles = new int[] {
            R.string.backup_current_widget,
            R.string.backup_incar_settings
        };

        RestorePagerAdapter(int appWidgetId, FragmentManager fm, Context context) {
            super(fm);
            mContext = context;
            mAppWidgetId = appWidgetId;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            return (position == 0) ? FragmentRestoreWidget.create(mAppWidgetId) : new FragmentRestoreInCar();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mContext.getString(titles[position]);
        }
    }
}
