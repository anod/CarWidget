package com.anod.car.home.prefs.lookandfeel;

import com.anod.car.home.prefs.LookAndFeelActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

/**
 * @author alex
 * @date 2014-10-20
 */
public class SkinPagerAdapter extends FragmentPagerAdapter {

    private final int mCount;
    private final LookAndFeelActivity mActivity;
    private SparseArray<SkinPreviewFragment> mFragments;


    public SkinPagerAdapter(LookAndFeelActivity activity, int count, FragmentManager fm) {
        super(fm);
        mCount = count;
        mActivity = activity;
        mFragments = new SparseArray<>(count);
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public Fragment getItem(int position) {
        return SkinPreviewFragment.Companion.newInstance(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mActivity.getSkinItem(position).title;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        SkinPreviewFragment fragment = (SkinPreviewFragment) super.instantiateItem(container, position);
        mFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        mFragments.delete(position);
        super.destroyItem(container, position, object);
    }

    public void refresh() {
        for (int key = 0; key < mCount; key++) {
            SkinPreviewFragment fragment = mFragments.get(key);
            if (fragment != null) {
                fragment.refresh();
            }
        }
    }
}
