package com.anod.car.home.appwidget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;
import android.util.SparseArray;
import android.view.View;
import android.widget.RemoteViews;

import com.anod.car.home.R;
import com.anod.car.home.incar.ModeService;
import com.anod.car.home.model.LauncherSettings;
import com.anod.car.home.model.LauncherShortcutsModel;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.prefs.PickShortcutUtils;
import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.skin.PropertiesFactory;
import com.anod.car.home.skin.SkinProperties;
import com.anod.car.home.utils.BitmapTransform;
import com.anod.car.home.utils.IconTheme;
import com.anod.car.home.utils.UtilitiesBitmap;
import com.anod.car.home.utils.Utils;

import java.util.HashMap;

public class WidgetViewBuilder {

	final private Context mContext;
	private int mAppWidgetId;
	private Main mPrefs;
	private LauncherShortcutsModel mSmodel;
	private String mOverrideSkin;
	private PendingIntentHelper mPendingIntentHelper;
	private boolean mIsKeyguard = false;
	private int mWidgetHeightDp = -1;

	private ShortcutViewBuilder mShortcutViewBuilder;
	private BitmapTransform mBitmapTransform;

	public WidgetViewBuilder setBitmapMemoryCache(LruCache<String, Bitmap> bitmapMemoryCache) {
		mShortcutViewBuilder.setBitmapMemoryCache(bitmapMemoryCache);
		return this;
	}

	public interface PendingIntentHelper {
		PendingIntent createSettings(int appWidgetId, int cellId);
		PendingIntent createShortcut(Intent intent, int appWidgetId, int position, long shortcutId);
		PendingIntent createInCar(boolean on);
	}

	public WidgetViewBuilder(Context context) {
		mContext = context;
	}

	public void setIsKeyguard(boolean isKeyguard) {
		mIsKeyguard = isKeyguard;
	}

	public void setWidgetHeightDp(int widgetHeightDp) {
		mWidgetHeightDp = widgetHeightDp;
	}

	public WidgetViewBuilder setPendingIntentHelper(PendingIntentHelper helper) {
		mPendingIntentHelper = helper;
		return this;
	}
	
	public WidgetViewBuilder setAppWidgetId(int appWidgetId) {
		mAppWidgetId = appWidgetId;
		return this;
	}

	public Main getPrefs() {
		return mPrefs;
	}

	public WidgetViewBuilder setOverrideSkin(String skin) {
		mOverrideSkin = skin;
		return this;
	}

	public WidgetViewBuilder init() {
		mPrefs = PreferencesStorage.loadMain(mContext, mAppWidgetId);

		mSmodel = new LauncherShortcutsModel(mContext, mAppWidgetId);
		if (PreferencesStorage.isFirstTime(mContext, mAppWidgetId)) {
			mSmodel.createDefaultShortcuts();
			PreferencesStorage.setFirstTime(false, mContext, mAppWidgetId);
		}
		mSmodel.init();

		mShortcutViewBuilder = new ShortcutViewBuilder(mContext, mAppWidgetId, mPendingIntentHelper);
		mBitmapTransform = new BitmapTransform(mContext);
		refreshIconTransform();
		return this;
	}

	public void refreshIconTransform() {
		applyIconTransform(mBitmapTransform, mPrefs);
	}

	public WidgetViewBuilder reloadShortcuts() {
		mSmodel.init();
		return this;
	}
	
	public WidgetViewBuilder reloadPrefs() {
		mPrefs = PreferencesStorage.loadMain(mContext, mAppWidgetId);
		return this;
	}

	public RemoteViews build() {
		Resources r = mContext.getResources();
		String skinName = (mOverrideSkin == null) ? mPrefs.getSkin() : mOverrideSkin;
		//boolean isLandscape = r.getBoolean(R.bool.is_landscape);
		float scaledDensity = r.getDisplayMetrics().scaledDensity;

		SkinProperties skinProperties = PropertiesFactory.create(skinName, mIsKeyguard);

		int iconPaddingRes = skinProperties.getIconPaddingRes();
		if (iconPaddingRes > 0 && !mPrefs.isTitlesHide()) {
			int iconPadding = (int)r.getDimension(iconPaddingRes);
			mBitmapTransform.setPaddingBottom(iconPadding);
		}

		RemoteViews views = new RemoteViews(mContext.getPackageName(), skinProperties.getLayout());

		setInCarButton(mPrefs.isIncarTransparent(), skinProperties, views);

		if (mPrefs.isSettingsTransparent()) {
			views.setImageViewResource(R.id.btn_settings, R.drawable.btn_transparent);
		} else {
			views.setImageViewResource(R.id.btn_settings, skinProperties.getSettingsButtonRes());
		}

		SparseArray<ShortcutInfo> shortcuts = mSmodel.getShortcuts();

		setBackground(mPrefs, views);

		mBitmapTransform.setIconProcessor(skinProperties.getIconProcessor());


		String themePackage = mPrefs.getIconsTheme();
		IconTheme themeIcons = (themePackage == null) ? null : loadThemeIcons(themePackage);

		boolean isSmallKeyguard = mIsKeyguard && mWidgetHeightDp != -1 && mWidgetHeightDp < 200;
		int keyguardHiddenRows = 0;//isLandscape ? 1 : 2;

		if (mIsKeyguard) {
			hideKeyguardRows(views, isSmallKeyguard);
		}
		mShortcutViewBuilder.init(skinName, scaledDensity, skinProperties, themeIcons, mPrefs, mSmodel, mBitmapTransform);

		//views.removeAllViews(R.id.workspace);
		int totalRows = shortcuts.size() / 2;
		for (int rowNum = 0; rowNum < totalRows; rowNum++) {

			if (isSmallKeyguard && rowNum > keyguardHiddenRows) {
				continue;
			}

			RemoteViews rowView = new RemoteViews(mContext.getPackageName(), skinProperties.getRowLayout());
			int position = rowNum * 2;

			mShortcutViewBuilder.fill(rowView, position, R.id.btn0, R.id.btn_text0);
			mShortcutViewBuilder.fill(rowView, position+1, R.id.btn1, R.id.btn_text1);
			views.addView(R.id.workspace, rowView);
		}


		PendingIntent configIntent = mPendingIntentHelper.createSettings(mAppWidgetId, PickShortcutUtils.INVALID_CELL_ID);
		views.setOnClickPendingIntent(R.id.btn_settings, configIntent);
		return views;
	}


	private void hideKeyguardRows(RemoteViews views, boolean smallKeyguard) {
		if (smallKeyguard) {
			views.setViewVisibility(R.id.row1, View.GONE);
			//if (!landscape) {
				views.setViewVisibility(R.id.row2, View.GONE);
			//}
		} else {
			views.setViewVisibility(R.id.row1, View.VISIBLE);
			//if (!landscape) {
				views.setViewVisibility(R.id.row2, View.VISIBLE);
			//}
		}
	}

	private IconTheme loadThemeIcons(String themePackage) {
		SparseArray<ShortcutInfo> shortcuts = mSmodel.getShortcuts();

		IconTheme theme = new IconTheme(mContext, themePackage);
		if (!theme.loadThemeResources()) {
			return null;
		}

		HashMap<String,Integer> cmpMap = new HashMap<String,Integer>(shortcuts.size());
		for (int cellId = 0; cellId < shortcuts.size(); cellId++) {
			ShortcutInfo info = mSmodel.getShortcut(cellId);
			if (info == null || info.itemType != LauncherSettings.Favorites.ITEM_TYPE_APPLICATION || info.isCustomIcon()) {
				continue;
			}
			cmpMap.put(info.intent.getComponent().getClassName(), cellId);
		}
		theme.loadFromXml(cmpMap);
		return theme;
	}

	private static void applyIconTransform(BitmapTransform bt, Main prefs) {
		if (prefs.isIconsMono()) {
			bt.setApplyGrayFilter(true);
			if (prefs.getIconsColor() != null) {
				bt.setTintColor(prefs.getIconsColor());
			}
		}

		float iconScale = Utils.calcIconsScale(prefs.getIconsScale());
		if (iconScale > 1.0f) {
			bt.setScaleSize(iconScale);
		}
		bt.setRotateDirection(prefs.getIconsRotate());
	}

	private void setInCarButton(boolean isInCarTrans, SkinProperties skinProp, RemoteViews views) {
		
		if (PreferencesStorage.isInCarModeEnabled(mContext)) {
			views.setViewVisibility(R.id.btn_incar_switch, View.VISIBLE);
			if (ModeService.sInCarMode) {
				if (isInCarTrans) {
					views.setImageViewResource(R.id.btn_incar_switch, R.drawable.btn_transparent);
				} else {
					int rImg = skinProp.getInCarButtonExitRes();
					views.setImageViewResource(R.id.btn_incar_switch, rImg);
				}
			} else {
				if (isInCarTrans) {
					views.setImageViewResource(R.id.btn_incar_switch, R.drawable.btn_transparent);
				} else {
					int rImg = skinProp.getInCarButtonEnterRes();
					views.setImageViewResource(R.id.btn_incar_switch, rImg);
				}
			}
			boolean switchOn = !ModeService.sInCarMode;
			PendingIntent contentIntent = mPendingIntentHelper.createInCar(switchOn);
			if (contentIntent != null) {
				views.setOnClickPendingIntent(R.id.btn_incar_switch, contentIntent);
			}
		} else {
			views.setViewVisibility(R.id.btn_incar_switch, View.GONE);
		}

	}

	private void setBackground(Main prefs, RemoteViews views) {
		int bgColor = prefs.getBackgroundColor();
		views.setInt(R.id.container, "setBackgroundColor", bgColor);
	}




}

