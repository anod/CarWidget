package com.anod.car.home;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.SparseArray;
import android.view.View;
import android.widget.RemoteViews;

import com.anod.car.home.incar.ModeService;
import com.anod.car.home.model.LauncherSettings;
import com.anod.car.home.model.LauncherShortcutsModel;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.prefs.PickShortcutUtils;
import com.anod.car.home.prefs.PreferencesStorage;
import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.skin.IconProcessor;
import com.anod.car.home.skin.PropertiesFactory;
import com.anod.car.home.skin.SkinProperties;
import com.anod.car.home.utils.UtilitiesBitmap;
import com.anod.car.home.utils.Utils;

public class LauncherViewBuilder {
	private Context mContext;
	private int mAppWidgetId;
	private Main mPrefs;
	private LauncherShortcutsModel mSmodel;
	private String mOverrideSkin;

	private static int[] sTextRes = { R.id.btn_text0, R.id.btn_text1, R.id.btn_text2, R.id.btn_text3, R.id.btn_text4, R.id.btn_text5 };
	private static int[] sBtnRes = { R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5 };

	public LauncherViewBuilder(Context context) {
		mContext = context;
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

		return this;
	}

	public LauncherViewBuilder reloadPrefs() {
		mPrefs = PreferencesStorage.loadMain(mContext, mAppWidgetId);
		return this;
	}

	public RemoteViews build() {

		String packageName = mContext.getPackageName();
		String skinName = (mOverrideSkin != null) ? mOverrideSkin : mPrefs.getSkin();

		SkinProperties skinProperties = PropertiesFactory.create(skinName);

		RemoteViews views = new RemoteViews(packageName, skinProperties.getLayout());

		setInCarButton(mPrefs.isIncarTransparent(), packageName, skinProperties, views);

		if (mPrefs.isSettingsTransparent()) {
			views.setImageViewResource(R.id.btn_settings, R.drawable.btn_transparent);
		}

		SparseArray<ShortcutInfo> shortcuts = mSmodel.getShortcuts();

		setBackground(mPrefs, views);

		float iconScale = Utils.calcIconsScale(mPrefs.getIconsScale());
		float scaledDensity = mContext.getResources().getDisplayMetrics().scaledDensity;

		String themePackage = mPrefs.getIconsTheme();
		Resources themeResources = (themePackage != null) ? getIconThemeResources(themePackage) : null;
		
		for (int cellId = 0; cellId < shortcuts.size(); cellId++) {
			int res = sBtnRes[cellId];
			int resText = sTextRes[cellId];
			ShortcutInfo info = mSmodel.getShortcut(cellId);
			if (info == null) {
				setNoShortcut(res, resText, views, cellId, skinProperties);
			} else {
				setShortcut(res, resText, iconScale, info, mPrefs, views, cellId, skinProperties, themePackage, themeResources);
			}
			setFont(mPrefs, res, resText, scaledDensity, views);
			if (skinName.equals(PreferencesStorage.SKIN_WINDOWS7)) {
				setTile(mPrefs.getTileColor(), res, views);
			}
		}

		PendingIntent configIntent = ShortcutPendingIntent.getSettingsPendingInent(mAppWidgetId, mContext, PickShortcutUtils.INVALID_CELL_ID);
		views.setOnClickPendingIntent(R.id.btn_settings, configIntent);
		return views;
	}

	private void setInCarButton(boolean isInCarTrans, String packageName, SkinProperties skinProp, RemoteViews views) {
		if (!Utils.isFreeVersion(packageName) && PreferencesStorage.isInCarModeEnabled(mContext)) {
			views.setViewVisibility(R.id.btn_incar_switch, View.VISIBLE);
			if (ModeService.sInCarMode == true) {
				if (isInCarTrans) {
					views.setImageViewResource(R.id.btn_incar_switch, R.drawable.btn_transparent);
				} else {
					int rImg = skinProp.getInCarButtonExitRes();
					views.setImageViewResource(R.id.btn_incar_switch, rImg);
				}
				PendingIntent contentIntent = getInCarOffIntent();
				views.setOnClickPendingIntent(R.id.btn_incar_switch, contentIntent);
			} else {
				if (isInCarTrans) {
					views.setImageViewResource(R.id.btn_incar_switch, R.drawable.btn_transparent);
				} else {
					int rImg = skinProp.getInCarButtonEnterRes();
					views.setImageViewResource(R.id.btn_incar_switch, rImg);
				}
				PendingIntent contentIntent = getInCarOnIntent();
				views.setOnClickPendingIntent(R.id.btn_incar_switch, contentIntent);
			}
		} else {
			views.setViewVisibility(R.id.btn_incar_switch, View.GONE);
		}

	}

	private PendingIntent getInCarOnIntent() {
		Intent onIntent = new Intent(mContext, ModeService.class);
		onIntent.putExtra(ModeService.EXTRA_MODE, ModeService.MODE_SWITCH_ON);
		onIntent.putExtra(ModeService.EXTRA_FORCE_STATE, true);
		Uri data = Uri.parse("com.anod.car.home.pro://mode/1/1");
		onIntent.setData(data);
		PendingIntent contentIntent = PendingIntent.getService(mContext, 0, onIntent, 0);
		return contentIntent;
	}

	private PendingIntent getInCarOffIntent() {
		Intent offIntent = new Intent(mContext, ModeService.class);
		offIntent.putExtra(ModeService.EXTRA_MODE, ModeService.MODE_SWITCH_OFF);
		offIntent.putExtra(ModeService.EXTRA_FORCE_STATE, true);
		Uri data = Uri.parse("com.anod.car.home.pro://mode/0/1");
		offIntent.setData(data);
		PendingIntent contentIntent = PendingIntent.getService(mContext, 0, offIntent, 0);
		return contentIntent;
	}

	private void setFont(Main prefs, int res, int resText, float scaledDensity, RemoteViews views) {
		views.setTextColor(resText, prefs.getFontColor());
		if (prefs.getFontSize() != PreferencesStorage.FONT_SIZE_UNDEFINED) {
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

	private void setNoShortcut(int res, int resText, RemoteViews views, int cellId, SkinProperties skinProp) {
		views.setImageViewResource(res, skinProp.getSetShortcutRes());
		String title = mContext.getResources().getString(skinProp.getSetShortcutText());
		views.setTextViewText(resText, title);
		PendingIntent configIntent = ShortcutPendingIntent.getSettingsPendingInent(mAppWidgetId, mContext, cellId);
		views.setOnClickPendingIntent(res, configIntent);
		views.setOnClickPendingIntent(resText, configIntent);
	}

	private void setShortcut(int res, int resText, float scale, ShortcutInfo info, Main prefs, RemoteViews views, int cellId, SkinProperties skinProp, String themePackage, Resources themeResources) {
		Bitmap icon = getShortcutIcon(info, themePackage, themeResources);
		if (prefs.isIconsMono()) {
			icon = UtilitiesBitmap.applyBitmapFilter(icon, mContext);
			if (prefs.getIconsColor() != null) {
				icon = UtilitiesBitmap.tint(icon, prefs.getIconsColor());
			}
		}
		;
		IconProcessor ip = skinProp.getIconProcessor();
		if (ip != null) {
			icon = ip.process(icon);
		}
		if (scale > 1.0f) {
			icon = UtilitiesBitmap.scaleBitmap(icon, scale, mContext);
		}
		views.setBitmap(res, "setImageBitmap", icon);
		String title = String.valueOf(info.title);
		views.setTextViewText(resText, title);
		PendingIntent shortcutIntent = ShortcutPendingIntent.getShortcutPendingInent(info.intent, mAppWidgetId, mContext, cellId);
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
            //e.printStackTrace();
        }
        return themeResources;
	}
	
	private Bitmap getIconThemeIcon(String className, String themePackage,  Resources themeResources) {
		String name = className;
		String resName= name.toLowerCase().replace(".", "_");
		Utils.logd("Look for icon for resource: R.drawable." + resName);
		int resource_id = themeResources.getIdentifier(resName, "drawable", themePackage);
		Drawable iconDrawable = null;
		if(resource_id!=0){
			iconDrawable = themeResources.getDrawable(resource_id);
         }
		
		if (iconDrawable != null) {
			return UtilitiesBitmap.createIconBitmap(iconDrawable, mContext);
		}
		return null;
	}
	
}
