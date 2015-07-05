package com.anod.car.home.prefs.lookandfeel;

import com.anod.car.home.prefs.LookAndFeelActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * @author alex
 * @date 2014-10-20
 */
public class SkinPagerAdapter extends FragmentPagerAdapter {

    private final int mCount;

    private final LookAndFeelActivity mActivity;

    public SkinPagerAdapter(LookAndFeelActivity activity, int count, FragmentManager fm) {
        super(fm);
        mCount = count;
        mActivity = activity;
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public Fragment getItem(int position) {
        return SkinPreviewFragment.newInstance(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mActivity.getSkinItem(position).title;
    }

}
