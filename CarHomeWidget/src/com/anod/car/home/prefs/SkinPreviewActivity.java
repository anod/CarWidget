package com.anod.car.home.prefs;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.anod.car.home.R;
import com.anod.car.home.prefs.preferences.Main;

public class SkinPreviewActivity extends FragmentActivity implements OnPageChangeListener{

	private ViewPager mGallery;
	private SkinItem[] mSkinItems;
	private SkinPagerAdapter mAdapter;
	private int mCurrentPage = 0;
	private int mSelectedSkin = 0;
	private TextView mTextView;
	private int appWidgetId;
	private Context mContext;
	private Button mButton;
	
	private static int[] sPreviewRes = {
		R.drawable.scr_glossy,
		R.drawable.scr_carhome,
		R.drawable.scr_metro,
		R.drawable.scr_holo,
		R.drawable.scr_bbb
	};
	private static int[] sTextRes = {
		0,
		0,
		R.string.skin_info_metro,
		0,
		R.string.skin_info_bbb
	};
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Intent intent = getIntent();
		if (intent == null) {
			Log.e("CarWidget", "No intent");
			finish();
			return;
		}
		
		appWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			Log.e("CarWidget", "Invalid app widget id");
			finish();
			return;
		}
		mContext = this;
		Main prefs = PreferencesStorage.loadMain(mContext, appWidgetId);

		mSkinItems = createSkinList(prefs.getSkin());
		int count = mSkinItems.length;
		mAdapter = new SkinPagerAdapter(this,count, getSupportFragmentManager());
		inflateActivity();

		mCurrentPage = mSelectedSkin;
		mGallery.setCurrentItem(mSelectedSkin);	
		setButtonSelected();
		
		showText(mCurrentPage);
	}

	private void setButtonSelected() {
		mButton.setText(R.string.selected);
		mButton.setEnabled(false);
	}

	private SkinItem[] createSkinList(String skinValue) {
		
		
		Resources r = getResources();
		String[] titles = r.getStringArray(R.array.skin_titles);
		String[] values = r.getStringArray(R.array.skin_values);
		SkinItem[] skins = new SkinItem[titles.length];
		for (int i=0; i<titles.length; i++) {
			SkinItem item = new SkinItem();
			item.title = titles[i];
			item.value = values[i];
			item.previewRes = sPreviewRes[i];
			item.textRes = sTextRes[i];
			
			if (item.value.equals(skinValue)) {
				mSelectedSkin = i;
			}
			
			skins[i] = item;
		}
		return skins;
	}

	public SkinItem getSkinItem(int position) {
		return mSkinItems[position];
	}
	
	private void inflateActivity() {
		setContentView(R.layout.skin_preview);

		
		mTextView = (TextView) findViewById(R.id.skin_info);
		mTextView.setMovementMethod(LinkMovementMethod.getInstance());
		
		mGallery = (ViewPager) findViewById(R.id.gallery);
		mGallery.setHorizontalFadingEdgeEnabled(true);
		mGallery.setFadingEdgeLength(30);
		mGallery.setAdapter(mAdapter);
		mGallery.setOnPageChangeListener(this);
		
		
		mButton = (Button) findViewById(R.id.apply);
		mButton.setOnClickListener(mApplyClicked);
	}
	
	@Override
	public void onPageSelected(int position) {
		mCurrentPage = position;
		showText(position);
		
		if (mSelectedSkin == position) {
			setButtonSelected();
		} else {
			setButtonApply();
		}
	}

	private void setButtonApply() {
		mButton.setText(R.string.apply);
		mButton.setEnabled(true);
	}

	private void showText(int position) {
		int textRes = getSkinItem(position).textRes;
		if (textRes > 0) {
	    	Spanned text = Html.fromHtml(getString(textRes));
	    	mTextView.setText(text);
	    	mTextView.setVisibility(View.VISIBLE);
		} else {
			mTextView.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onPageScrollStateChanged(int state) {
		// TODO Auto-generated method stub
		
	}
	
	private final OnClickListener mApplyClicked = new OnClickListener() {
		public void onClick(View v) {
			Main prefs = PreferencesStorage.loadMain(mContext, appWidgetId);
			prefs.setSkin(getSkinItem(mCurrentPage).value);
			PreferencesStorage.saveMain(mContext, prefs, appWidgetId);
			finish();
		}
	};

	class SkinItem {
		public String value;
		public String title;
		public int previewRes;
		public int textRes;
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
 