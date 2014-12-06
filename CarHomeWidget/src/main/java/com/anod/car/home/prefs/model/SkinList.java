package com.anod.car.home.prefs.model;

import android.content.Context;
import android.content.res.Resources;

import com.anod.car.home.R;

/**
 * @author alex
 * @date 2014-10-20
 */
public class SkinList {
    private static int[] sTextRes = { 0, 0, 0, 0, 0, R.string.skin_info_bbb };

    private Item[] mSkinItems;
    private boolean mIsKeyguard;

    public SkinList(boolean isKeyguard, Context context) {
        mIsKeyguard = isKeyguard;
        mContext = context;
    }

    private int mSelectedSkinPosition;
    private Context mContext;

    public static SkinList newInstance(String skin, boolean isKeyguard, Context context) {
        SkinList list = new SkinList(isKeyguard, context);
        list.build(skin);
        return list;
    }

    public int getSelectedSkinPosition() {
        return mSelectedSkinPosition;
    }

    public int getCount() {
        return mSkinItems.length;
    }

    public Item get(int position) {
        return mSkinItems[position];
    }

    public static class Item {
        public String value;
        public String title;
        public int textRes;
    }

    public void build(String skinValue) {

        if (mIsKeyguard) {
            mSkinItems = new Item[1];
            Item item = new Item();
            item.title = "Keyguard";
            item.value = "holo";
            item.textRes = 0;
            mSkinItems[0] = item;
            mSelectedSkinPosition = 0;
            return;
        }

        Resources r = mContext.getResources();
        String[] titles = r.getStringArray(R.array.skin_titles);
        String[] values = r.getStringArray(R.array.skin_values);
        mSkinItems = new Item[titles.length];
        for (int i = 0; i < titles.length; i++) {
            Item item = new Item();
            item.title = titles[i];
            item.value = values[i];
            item.textRes = sTextRes[i];

            if (item.value.equals(skinValue)) {
                mSelectedSkinPosition = i;
            }

            mSkinItems[i] = item;
        }
    }
}
