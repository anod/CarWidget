package com.anod.car.home.prefs;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.anod.car.home.LauncherViewBuilder;
import com.anod.car.home.R;
import com.anod.car.home.actionbarcompat.ActionBarActivity;
import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.prefs.views.CarHomeColorPickerDialog;
import com.anod.car.home.utils.FastBitmapDrawable;
import com.anod.car.home.utils.Utils;

public class SkinPreviewActivity extends ActionBarActivity implements OnPageChangeListener {
	private static final int SKINS_COUNT = 5;

	interface SkinRefreshListener {
		public void refresh();
	}

	private static final int REQUEST_LOOK_ACTIVITY = 1;
	private ViewPager mGallery;
	private SkinItem[] mSkinItems;
	private SkinPagerAdapter mAdapter;
	private int mCurrentPage = 0;
	private int mSelectedSkinPosition = 0;
	private TextView mTextView;
	private int mAppWidgetId;
	private Context mContext;
	private MenuItem mItemApply;
	private MenuItem mMenuSelected;
	private MenuItem mMenuTileColor;
	private boolean mMenuInitialized = false;
	private View mLoaderView;
	private boolean[] mPreviewInitialized = { false, false, false, false, false };
	private LauncherViewBuilder mBuilder;
	private Main mPrefs;
	private SparseArray<SkinRefreshListener> mSkinRefreshListeners = new SparseArray<SkinPreviewActivity.SkinRefreshListener>(SKINS_COUNT);
	private boolean mPendingRefresh;

	private static int[] sTextRes = { 0, 0, R.string.skin_info_metro, 0, R.string.skin_info_bbb };

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
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
		setTitle(R.string.pref_look_and_feel_title);
		mContext = this;

		mBuilder = new LauncherViewBuilder(this);
		mBuilder.setAppWidgetId(mAppWidgetId).init();
		mPrefs = mBuilder.getPrefs();
		mSkinItems = createSkinList(mPrefs.getSkin());
		mCurrentPage = mSelectedSkinPosition;

		inflateActivity();

		int count = mSkinItems.length;
		
		mAdapter = new SkinPagerAdapter(this, count, getSupportFragmentManager());
		mGallery.setAdapter(mAdapter);
		mGallery.setCurrentItem(mSelectedSkinPosition);

		showText(mCurrentPage);

	}

	public LauncherViewBuilder getBuilder() {
		return mBuilder;
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.skin_preview, menu);

		mItemApply = menu.findItem(R.id.apply);
		mMenuSelected = menu.findItem(R.id.selected);
		mMenuTileColor = menu.findItem(R.id.tile_color);
		
		menu.findItem(R.id.icons_mono).setChecked(mPrefs.isIconsMono());
		
		mMenuInitialized  = true;
		refreshActionBar(mCurrentPage);
        // Calling super after populating the menu is necessary here to ensure that the
        // action bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.apply) {
			Main prefs = PreferencesStorage.loadMain(mContext, mAppWidgetId);
			prefs.setSkin(getSkinItem(mCurrentPage).value);
			PreferencesStorage.saveMain(mContext, prefs, mAppWidgetId);
			finish();
			return false;
		}
		if (itemId == R.id.tile_color){
			Main prefs = PreferencesStorage.loadMain(mContext, mAppWidgetId);
			Integer value = prefs.getTileColor();
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String prefName = PreferencesStorage.getName(PreferencesStorage.BUTTON_COLOR, mAppWidgetId);
					int color = ((CarHomeColorPickerDialog) dialog).getColor();
					PreferencesStorage.saveColor(mContext, prefName, color);
					showTileColorButton(mCurrentPage);
					refreshSkinPreview();
				}

			};
			final CarHomeColorPickerDialog d = new CarHomeColorPickerDialog(mContext, value, listener);
			d.setAlphaSliderVisible(true);
			d.show();
			return false;
		}
		if (itemId == R.id.more) {
			Intent intent = new Intent(this, ConfigurationLook.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
			startActivityForResult(intent, REQUEST_LOOK_ACTIVITY);
			return false;
		}
		if (itemId == R.id.bg_color) {
			int value = mPrefs.getBackgroundColor();
			OnClickListener listener = new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String prefName = PreferencesStorage.getName(PreferencesStorage.BG_COLOR, mAppWidgetId);
					int color = ((CarHomeColorPickerDialog) dialog).getColor();
					PreferencesStorage.saveColor(mContext, prefName, color);
					refreshSkinPreview();
				}
			};
			final CarHomeColorPickerDialog d = new CarHomeColorPickerDialog(mContext, value, listener);
			d.setAlphaSliderVisible(true);
			d.show();
			return false;
		}
		if (itemId == R.id.icons_mono) {
			// TODO
			return false;
		}
		if (itemId == R.id.icons_scale) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			final CharSequence[] items = getResources().getStringArray(R.array.icon_scale_titles);
			int idx = -1;
			for(int i=0; i<items.length;i++) {
				if (mPrefs.getIconsScale().equals(items[i])) {
					idx = i;
					break;
				}
			}
			builder.setTitle(R.string.pref_scale_icon);
			builder.setSingleChoiceItems(items, idx, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			        Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
			        refreshSkinPreview();
			    }
			});
			builder.create().show();
			return false;
		}
		return false;
    }

	private SkinItem[] createSkinList(String skinValue) {
		Resources r = getResources();
		String[] titles = r.getStringArray(R.array.skin_titles);
		String[] values = r.getStringArray(R.array.skin_values);
		SkinItem[] skins = new SkinItem[titles.length];
		for (int i = 0; i < titles.length; i++) {
			SkinItem item = new SkinItem();
			item.title = titles[i];
			item.value = values[i];
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
		mGallery.setOnPageChangeListener(this);

		mLoaderView = (View) findViewById(R.id.loading);	
	}

	public void onPreviewStart(int position) {
		mPreviewInitialized[position] = false;
	}
	
	public void onPreviewCreated(int position) {
		if (mCurrentPage == position) {
			mLoaderView.setVisibility(View.GONE);
		}
		mPreviewInitialized[position] = true;
	}
	
	@Override
	public void onPageSelected(int position) {
		mCurrentPage = position;
		showText(position);
		
		if (!mPreviewInitialized[position]) {
			mLoaderView.setVisibility(View.VISIBLE);
		}
		
		refreshActionBar(position);

	}

	private void refreshActionBar(int position) {
		if (mMenuInitialized) {
			if (mSelectedSkinPosition == position) {
				showButtonSelected();
			} else {
				showButtonApply();
			}
			showTileColorButton(mCurrentPage);
		}
	}

	private void showButtonSelected() {
		mItemApply.setVisible(false);
		mMenuSelected.setVisible(true);
	}

	private void showButtonApply() {
		mItemApply.setVisible(true);
		mMenuSelected.setVisible(false);
	}

	private void showTileColorButton(int position) {
		if (getSkinItem(position).value.equals(PreferencesStorage.SKIN_WINDOWS7)) {
			Main prefs = PreferencesStorage.loadMain(mContext, mAppWidgetId);
			int size = (int) getResources().getDimension(R.dimen.color_preview_size);

			Bitmap bitmap = Bitmap.createBitmap(size, size, Config.ARGB_8888);
			Canvas c = new Canvas(bitmap);
			c.drawColor(prefs.getTileColor());
			Drawable d = new FastBitmapDrawable(bitmap);

			mMenuTileColor.setIcon(d);
			mMenuTileColor.setVisible(true);
		} else {
			mMenuTileColor.setVisible(false);
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

	private void refreshSkinPreview() {
		Utils.logd("Refresh Skin Requested");
		mPrefs = mBuilder.reloadPrefs().getPrefs();
		if (mPreviewInitialized[mCurrentPage]) {
			mPendingRefresh = false;
			for (int i = 0; i<mSkinRefreshListeners.size(); i++) {
				SkinRefreshListener listener = mSkinRefreshListeners.valueAt(i);
				if (listener != null) {
					listener.refresh();
					Utils.logd("Call refresh on listener for page: " + i);
				} else {
					if (i == mCurrentPage) {
						mPendingRefresh = true;
						Utils.logd("No listener for current page, set pending flag");
					}
				}
			}
		}
	}

	public void onFragmentAttach(SkinRefreshListener listener, int position) {
		Utils.logd("Register listener for page: "+position);
		mSkinRefreshListeners.put(position,listener);
		if (mPendingRefresh && mCurrentPage == position) {
			Utils.logd("Pending refresh");
			listener.refresh();
		}
	}
	
	public void onFragmentDetach(int position) {
		Utils.logd("UnRegister listener for page: "+position);
		mSkinRefreshListeners.delete(position);
	}
	
	class SkinItem {
		public String value;
		public String title;
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
		public CharSequence getPageTitle(int position) {
			return mActivity.getSkinItem(position).title;
		}
	}

	public int getAppWidgetId() {
		return mAppWidgetId;
	}
}
