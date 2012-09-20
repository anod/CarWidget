package com.anod.car.home.prefs;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
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
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.anod.car.home.R;
import com.anod.car.home.actionbarcompat.ActionBarHelper;
import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.prefs.views.CarHomeColorPickerDialog;
import com.anod.car.home.utils.FastBitmapDrawable;

public class SkinPreviewActivity extends FragmentActivity implements OnPageChangeListener{

	private ViewPager mGallery;
	private SkinItem[] mSkinItems;
	private SkinPagerAdapter mAdapter;
	private int mCurrentPage = 0;
	private int mSelectedSkinPosition = 0;
	private TextView mTextView;
	private int mAppWidgetId;
	private Context mContext;
	private Button mButtonApply;
	private Button mButtonSelected;
	private Button mButtonTileColor;
	final private ActionBarHelper mActionBarHelper = ActionBarHelper.createInstance(this);
	
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
	public void onCreate(Bundle savedInstanceState) {
		mActionBarHelper.onCreate(savedInstanceState);
		super.onCreate(savedInstanceState);
	    
		Intent intent = getIntent();
		if (intent == null) {
			Log.e("CarWidget", "No intent");
			finish();
			return;
		}
		
		mAppWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			Log.e("CarWidget", "Invalid app widget id");
			finish();
			return;
		}
		setContentView(R.layout.skin_preview);
		setTitle(R.string.pref_skin);
		mContext = this;

		Main prefs = PreferencesStorage.loadMain(mContext, mAppWidgetId);
		mSkinItems = createSkinList(prefs.getSkin());
		mCurrentPage = mSelectedSkinPosition;

		inflateActivity();

		int count = mSkinItems.length;
		mAdapter = new SkinPagerAdapter(this,count, getSupportFragmentManager());
		mGallery.setAdapter(mAdapter);
		mGallery.setCurrentItem(mSelectedSkinPosition);	

		showButtonSelected();
		
		showText(mCurrentPage);
		showTileColorButton(mCurrentPage);


	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		mActionBarHelper.onPostCreate(savedInstanceState);
		super.onPostCreate(savedInstanceState);
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	/**
    	 * 		mButtonApply = (Button) view.findViewById(R.id.apply);
		mButtonApply.setOnClickListener(mApplyClicked);
		
		mButtonSelected = (Button) view.findViewById(R.id.selected);
		mButtonTileColor = (Button) view.findViewById(R.id.tile_color);
		mButtonTileColor.setOnClickListener(mTileColorClicked);
    	 */
        boolean retValue = false;
        retValue |= mActionBarHelper.onCreateOptionsMenu(menu);
        retValue |= super.onCreateOptionsMenu(menu);
        
        menu.findItem(R.id.apply).
        return retValue;
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
				mSelectedSkinPosition = i;
			}
			
			skins[i] = item;
		}
		return skins;
	}

	public SkinItem getSkinItem(int position) {
		return mSkinItems[position];
	}
	
	private void inflateActivity() {
		
		mTextView = (TextView) findViewById(R.id.skin_info);
		mTextView.setMovementMethod(LinkMovementMethod.getInstance());
		
		mGallery = (ViewPager) findViewById(R.id.gallery);
		mGallery.setHorizontalFadingEdgeEnabled(true);
		mGallery.setFadingEdgeLength(30);
		mGallery.setOnPageChangeListener(this);
		
	}
	
	@Override
	public void onPageSelected(int position) {
		mCurrentPage = position;
		showText(position);
		
		if (mSelectedSkinPosition == position) {
			showButtonSelected();
		} else {
			showButtonApply();
		}
		showTileColorButton(mCurrentPage);

	}

	private void showButtonSelected() {
		mButtonApply.setVisibility(View.GONE);
		mButtonSelected.setVisibility(View.VISIBLE);
	}

	private void showButtonApply() {
		mButtonApply.setVisibility(View.VISIBLE);
		mButtonSelected.setVisibility(View.GONE);
	}

	private void showTileColorButton(int position) {
		if (getSkinItem(position).value.equals(PreferencesStorage.SKIN_WINDOWS7)) {
			Main prefs = PreferencesStorage.loadMain(mContext, mAppWidgetId);
			int size = (int)getResources().getDimension(R.dimen.color_preview_size);
			
			Bitmap bitmap = Bitmap.createBitmap(size, size, Config.ARGB_8888);
			Canvas c = new Canvas(bitmap);
			c.drawColor(prefs.getTileColor());
			Drawable d = new FastBitmapDrawable(bitmap);
			/*
			 * remember to first clear the callback of the drawable you are replacing to prevent memory leaks...
			 */
			for(Drawable myOldDrawable : mButtonTileColor.getCompoundDrawables())
			{
				if (myOldDrawable!=null) {
					myOldDrawable.setCallback(null);
				}
			}
			mButtonTileColor.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
			mButtonTileColor.setVisibility(View.VISIBLE);
		} else {
			mButtonTileColor.setVisibility(View.GONE);
		}
	}
	
	private void showText(int position) {
		int textRes = getSkinItem(position).textRes;
		if (textRes > 0) {
	    	Spanned text = Html.fromHtml(getString(textRes));
	    	mTextView.setText(text);
		} else {
	    	mTextView.setText("");
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
			Main prefs = PreferencesStorage.loadMain(mContext, mAppWidgetId);
			prefs.setSkin(getSkinItem(mCurrentPage).value);
			PreferencesStorage.saveMain(mContext, prefs, mAppWidgetId);
			finish();
		}
	};
	
	private final OnClickListener mTileColorClicked = new OnClickListener() {
		public void onClick(View v) {
			Main prefs = PreferencesStorage.loadMain(mContext, mAppWidgetId);
			Integer value = prefs.getTileColor();
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String prefName = PreferencesStorage.getName(PreferencesStorage.BUTTON_COLOR, mAppWidgetId);
					int color = ((CarHomeColorPickerDialog) dialog).getColor();
					PreferencesStorage.saveColor(mContext, prefName, color);
					showTileColorButton(mCurrentPage);
				}

			};
			final CarHomeColorPickerDialog d = new CarHomeColorPickerDialog(mContext, value, listener);
			d.setAlphaSliderVisible(true);
			d.show();
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
 