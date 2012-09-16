package com.anod.car.home.prefs;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.anod.car.home.R;

public class SkinPreviewActivity extends FragmentActivity {

	private ViewPager mGallery;
	private SkinItem[] mSkinItems;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		mSkinItems = createSkinList();
		inflateActivity();

		// mGallery.setSelection(mAdapter.getMarkedPosition());
	}

	private SkinItem[] createSkinList() {
		Resources r = getResources();
		String[] titles = r.getStringArray(R.array.skin_titles);
		String[] values = r.getStringArray(R.array.skin_values);
		SkinItem[] skins = new SkinItem[titles.length];
		for (int i=0; i<titles.length; i++) {
			SkinItem item = new SkinItem();
			item.title = titles[i];
			item.value = values[i];
			item.previewRes = R.drawable.widget_preview;
			skins[i] = item;
		}
		return skins;
	}

	public SkinItem getSkinItem(int position) {
		return mSkinItems[position];
	}
	
	private void inflateActivity() {
		setContentView(R.layout.skin_preview);

		int count = mSkinItems.length;
		mGallery = (ViewPager) findViewById(R.id.gallery);
		mGallery.setHorizontalFadingEdgeEnabled(true);
		mGallery.setFadingEdgeLength(30);
		mGallery.setAdapter(new SkinPagerAdapter(this,count, getSupportFragmentManager()));

		Button button = (Button) findViewById(R.id.apply);
		button.setOnClickListener(mApplyClicked);
	}

	private final OnClickListener mApplyClicked = new OnClickListener() {
		public void onClick(View v) {
		//	int selectedPos = mGallery.getSelectedItemPosition();

		}
	};

	class SkinItem {
		public String value;
		public String title;
		public int previewRes;
	}

	public static class SkinPagerAdapter extends FragmentPagerAdapter {
		
        private int mCount;
		private SkinPreviewActivity mActivity;

		public SkinPagerAdapter(SkinPreviewActivity activity, int count, FragmentManager fm) {
            super(fm);
            mCount = count;
            mActivity = activity;
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
        public CharSequence getPageTitle (int position) {
            return mActivity.getSkinItem(position).title;
        }
    }
}
 