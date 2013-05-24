package com.anod.car.home.appwidget;

import java.util.Locale;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import com.anod.car.home.utils.UtilitiesBitmap;
import com.anod.car.home.utils.Utils;

public class LauncherViewBuilder {
	private static int[] sTextRes = { 
		R.id.btn_text0,
		R.id.btn_text1,
		R.id.btn_text2,
		R.id.btn_text3,
		R.id.btn_text4,
		R.id.btn_text5
	};

	private static int[] sBtnRes = { 
		R.id.btn0, 
		R.id.btn1, 
		R.id.btn2, 
		R.id.btn3, 
		R.id.btn4, 
		R.id.btn5 
	};

	final private Context mContext;
	private int mAppWidgetId;
	private Main mPrefs;
	private LauncherShortcutsModel mSmodel;
	private String mOverrideSkin;
	private PendingIntentHelper mPendingIntentHelper;
	private BitmapTransform mBitmapTransform;
	
	public interface PendingIntentHelper {
		PendingIntent createSettings(int appWidgetId, int cellId);
		PendingIntent createShortcut(Intent intent, int appWidgetId, int position, long shortcutId);
		PendingIntent createInCar(boolean on);
	}

	public LauncherViewBuilder(Context context) {
		mContext = context;
	}

	public LauncherViewBuilder setPendingIntentHelper(PendingIntentHelper helper) {
		mPendingIntentHelper = helper;
		return this;
	}
	
	public LauncherViewBuilder setAppWidgetId(int appWidgetId) {
		mAppWidgetId = appWidgetId;
		return this;
	}

	public Main getPrefs() {
		return mPrefs;
	}

	public LauncherViewBuilder setOverrideSkin(String skin) {
		mOverrideSkin = skin;
		return this;
	}

	public LauncherViewBuilder init() {
		mPrefs = PreferencesStorage.loadMain(mContext, mAppWidgetId);

		mSmodel = new LauncherShortcutsModel(mContext, mAppWidgetId);
		if (PreferencesStorage.isFirstTime(mContext, mAppWidgetId)) {
			mSmodel.createDefaultShortcuts();
			PreferencesStorage.setFirstTime(false, mContext, mAppWidgetId);
		}
		mSmodel.init();
		
		mBitmapTransform = new BitmapTransform(mContext);
		applyIconTransform(mBitmapTransform, mPrefs);
		return this;
	}

	public LauncherViewBuilder reloadShortcuts() {
		mSmodel.init();
		return this;
	}
	
	public LauncherViewBuilder reloadPrefs() {
		mPrefs = PreferencesStorage.loadMain(mContext, mAppWidgetId);
		return this;
	}

	public RemoteViews build() {

		String packageName = mContext.getPackageName();
		String skinName = (mOverrideSkin == null) ? mPrefs.getSkin() : mOverrideSkin;

		SkinProperties skinProperties = PropertiesFactory.create(skinName);

		RemoteViews views = new RemoteViews(packageName, skinProperties.getLayout());

		setInCarButton(mPrefs.isIncarTransparent(), skinProperties, views);

		if (mPrefs.isSettingsTransparent()) {
			views.setImageViewResource(R.id.btn_settings, R.drawable.btn_transparent);
		}

		SparseArray<ShortcutInfo> shortcuts = mSmodel.getShortcuts();

		setBackground(mPrefs, views);

		mBitmapTransform.setIconProcessor(skinProperties.getIconProcessor());
		
		float scaledDensity = mContext.getResources().getDisplayMetrics().scaledDensity;

		String themePackage = mPrefs.getIconsTheme();
		Resources themeResources = (themePackage == null) ? null : getIconThemeResources(themePackage);
		

		for (int cellId = 0; cellId < shortcuts.size(); cellId++) {
			int res = sBtnRes[cellId];
			int resText = sTextRes[cellId];
			ShortcutInfo info = mSmodel.getShortcut(cellId);
			if (info == null) {
				setNoShortcut(res, resText, mPrefs, views, cellId, skinProperties);
			} else {
				setShortcut(res, resText, info, mPrefs, views, cellId, themePackage, themeResources);
			}
			setFont(mPrefs, resText, scaledDensity, views);
			if (skinName.equals(Main.SKIN_WINDOWS7)) {
				setTile(mPrefs.getTileColor(), res, views);
			}
		}

		PendingIntent configIntent = mPendingIntentHelper.createSettings(mAppWidgetId, PickShortcutUtils.INVALID_CELL_ID);
		views.setOnClickPendingIntent(R.id.btn_settings, configIntent);
		return views;
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

	private void setFont(Main prefs, int resText, float scaledDensity, RemoteViews views) {
		views.setTextColor(resText, prefs.getFontColor());
		if (prefs.getFontSize() != Main.FONT_SIZE_UNDEFINED) {
			if (prefs.getFontSize() == 0) {
				views.setViewVisibility(resText, View.GONE);
			} else {
				/*
				 * Limitation of RemoteViews to use setTextSize with only one
				 * argument (without providing scale unit) size already in
				 * scaled pixel format so we revert it to pixels to get properly
				 * converted after re-applying setTextSize function
				 */
				float cSize = (float) prefs.getFontSize() / scaledDensity;

				views.setFloat(resText, "setTextSize", cSize);
				views.setViewVisibility(resText, View.VISIBLE);
			}
		}

	}

	private void setTile(int tileColor, int res, RemoteViews views) {
		if (Color.alpha(tileColor) == 0) {
			views.setViewVisibility(res, View.GONE);
		} else {
			views.setViewVisibility(res, View.VISIBLE);
			views.setInt(res, "setBackgroundColor", tileColor);
		}
	}

	private void setNoShortcut(int res, int resText, Main prefs, RemoteViews views, int cellId, SkinProperties skinProp) {
		views.setImageViewResource(res, skinProp.getSetShortcutRes());

		if (prefs.isTitlesHide()) {
			views.setViewVisibility(resText, View.GONE);
		} else {
			String title = mContext.getResources().getString(skinProp.getSetShortcutText());
			views.setTextViewText(resText, title);
		}
		PendingIntent configIntent = mPendingIntentHelper.createSettings(mAppWidgetId, cellId);
		if (configIntent != null) {
			views.setOnClickPendingIntent(res, configIntent);
			views.setOnClickPendingIntent(resText, configIntent);
		}
	}

	private void setShortcut(int res, int resText, ShortcutInfo info, Main prefs, RemoteViews views, int cellId, String themePackage, Resources themeResources) {
		Bitmap icon = getShortcutIcon(info, themePackage, themeResources);
		icon = mBitmapTransform.transform(icon);
		views.setBitmap(res, "setImageBitmap", icon);

		if (prefs.isTitlesHide()) {
			views.setViewVisibility(resText, View.GONE);
		} else {
			String title = String.valueOf(info.title);
			views.setTextViewText(resText, title);
		}
		PendingIntent shortcutIntent = mPendingIntentHelper.createShortcut(info.intent, mAppWidgetId, cellId, info.id);
		views.setOnClickPendingIntent(res, shortcutIntent);
		views.setOnClickPendingIntent(resText, shortcutIntent);
	}

	private void setBackground(Main prefs, RemoteViews views) {
		int bgColor = prefs.getBackgroundColor();
		views.setInt(R.id.container, "setBackgroundColor", bgColor);
	}

	private Bitmap getShortcutIcon(ShortcutInfo info,String themePackage,Resources themeResources) {
		if (themeResources == null || info.itemType != LauncherSettings.Favorites.ITEM_TYPE_APPLICATION || info.isCustomIcon()) {
			return info.getIcon();
		}
		Bitmap icon = getIconThemeIcon(info.intent.getComponent().getClassName(), themePackage, themeResources);
		if (icon == null) {
			return info.getIcon();
		}
		return icon;
	}
	
	private Resources getIconThemeResources(String themePackage) {
		 // get from theme
        Resources themeResources = null;
        try {
            themeResources = mContext.getPackageManager().getResourcesForApplication(themePackage);
        } catch (NameNotFoundException e) {
            Utils.logd(e.getMessage());
        }
        return themeResources;
	}
	
	private Bitmap getIconThemeIcon(String className, String themePackage,  Resources themeResources) {
		String name = className;
		String resName= name.toLowerCase(Locale.US).replace(".", "_"); 
		Utils.logd("Look for icon for resource: R.drawable." + resName);
		int resourceId = themeResources.getIdentifier(resName, "drawable", themePackage);
		Drawable iconDrawable = null;
		if(resourceId!=0){
			iconDrawable = themeResources.getDrawable(resourceId);
         }
		
		if (iconDrawable != null) {
			return UtilitiesBitmap.createIconBitmap(iconDrawable, mContext);
		}
		return null;
	}
	
}

	