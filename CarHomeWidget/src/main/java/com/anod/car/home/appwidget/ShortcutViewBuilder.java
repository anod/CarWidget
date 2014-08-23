package com.anod.car.home.appwidget;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.LruCache;
import android.view.View;
import android.widget.RemoteViews;

import com.anod.car.home.model.LauncherSettings;
import com.anod.car.home.model.WidgetShortcutsModel;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.skin.SkinProperties;
import com.anod.car.home.utils.BitmapTransform;
import com.anod.car.home.utils.IconTheme;
import com.anod.car.home.utils.UtilitiesBitmap;

/**
 * @author alex
 * @date 1/4/14
 */
public class ShortcutViewBuilder {
	private String mSkinName;
	private float mScaledDensity;
	private SkinProperties mSkinProperties;
	private IconTheme mIconTheme;
	private Main mPrefs;
	private Context mContext;
	private WidgetViewBuilder.PendingIntentHelper mPendingIntentHelper;
	private int mAppWidgetId;
	private WidgetShortcutsModel mSmodel;
	private LruCache<String, Bitmap> mBitmapMemoryCache;
	private BitmapTransform mBitmapTransform;

	public ShortcutViewBuilder(Context context,int appWidgetId, WidgetViewBuilder.PendingIntentHelper pendingIntentHelper) {
		mContext = context;
		mPendingIntentHelper = pendingIntentHelper;
		mAppWidgetId = appWidgetId;
	}

	public void init(String skinName, float scaledDensity, SkinProperties skinProperties, IconTheme iconTheme, Main prefs, WidgetShortcutsModel smodel, BitmapTransform bitmapTransform) {
		mSkinName = skinName;
		mScaledDensity = scaledDensity;
		mSkinProperties = skinProperties;
		mIconTheme = iconTheme;
		mPrefs = prefs;
		mSmodel = smodel;
		mBitmapTransform = bitmapTransform;
	}

	public void fill(RemoteViews views, int position, int resBtn, int resText) {

		ShortcutInfo info = mSmodel.getShortcut(position);

		if (info == null) {
			setNoShortcut(resBtn, resText, views, position, mSkinProperties);
		} else {
			setShortcut(resBtn, resText, info, views, position, mIconTheme);
		}
		if (mPrefs.isTitlesHide()) {
			views.setViewVisibility(resText, View.GONE);
		} else {
			setFont(resText, mScaledDensity, views);
		}
		if (mSkinName.equals(Main.SKIN_WINDOWS7)) {
			setTile(mPrefs.getTileColor(), resBtn, views);
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

	private void setNoShortcut(int res, int resText,RemoteViews views, int cellId, SkinProperties skinProp) {
		views.setImageViewResource(res, skinProp.getSetShortcutRes());

		if (!mPrefs.isTitlesHide()) {
			String title = mContext.getResources().getString(skinProp.getSetShortcutText());
			views.setTextViewText(resText, title);
		}
		PendingIntent configIntent = mPendingIntentHelper.createSettings(mAppWidgetId, cellId);
		if (configIntent != null) {
			views.setOnClickPendingIntent(res, configIntent);
			views.setOnClickPendingIntent(resText, configIntent);
		}
	}

	private void setFont(int resText, float scaledDensity, RemoteViews views) {
		views.setTextColor(resText, mPrefs.getFontColor());
		if (mPrefs.getFontSize() != Main.FONT_SIZE_UNDEFINED) {
			if (mPrefs.getFontSize() == 0) {
				views.setViewVisibility(resText, View.GONE);
			} else {
				/*
				 * Limitation of RemoteViews to use setTextSize with only one
				 * argument (without providing scale unit) size already in
				 * scaled pixel format so we revert it to pixels to get properly
				 * converted after re-applying setTextSize function
				 */
				float cSize = (float) mPrefs.getFontSize() / scaledDensity;

				views.setFloat(resText, "setTextSize", cSize);
				views.setViewVisibility(resText, View.VISIBLE);
			}
		}

	}



	private void setShortcut(int res, int resText, ShortcutInfo info, RemoteViews views, int cellId, IconTheme themeIcons) {

		String themePackage = (themeIcons == null) ? "null" : themeIcons.getPackageName();
		String transformKey = mBitmapTransform.getCacheKey();
		final String imageKey = String.valueOf(info.id)+":"+themePackage+":"+transformKey;

		Bitmap icon = getBitmapFromMemCache(imageKey);
		if (icon == null) {
			icon = getShortcutIcon(info, themeIcons);
			icon = mBitmapTransform.transform(icon);
			addBitmapToMemCache(imageKey, icon);
		}
		views.setBitmap(res, "setImageBitmap", icon);

		if (!mPrefs.isTitlesHide()) {
			String title = String.valueOf(info.title);
			views.setTextViewText(resText, title);
		}
		PendingIntent shortcutIntent = mPendingIntentHelper.createShortcut(info.intent, mAppWidgetId, cellId, info.id);
		views.setOnClickPendingIntent(res, shortcutIntent);
		views.setOnClickPendingIntent(resText, shortcutIntent);
	}

	public void addBitmapToMemCache(String key, Bitmap bitmap) {
		if (mBitmapMemoryCache == null) {
			return;
		}
		synchronized (mBitmapMemoryCache) {
			if (getBitmapFromMemCache(key) == null) {
				mBitmapMemoryCache.put(key, bitmap);
			}
		}
	}

	public Bitmap getBitmapFromMemCache(String key) {
		if (mBitmapMemoryCache == null) {
			return null;
		}
		synchronized (mBitmapMemoryCache) {
			return mBitmapMemoryCache.get(key);
		}
	}

	private Bitmap getShortcutIcon(ShortcutInfo info, IconTheme themeIcons) {
		if (themeIcons == null || info.itemType != LauncherSettings.Favorites.ITEM_TYPE_APPLICATION || info.isCustomIcon()) {
			return info.getIcon();
		}

		int resourceId = themeIcons.getIcon(info.intent.getComponent().getClassName());
		Drawable iconDrawable = null;
		if(resourceId!=0){
			iconDrawable = themeIcons.getDrawable(resourceId);
		}
		if (iconDrawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) iconDrawable).getBitmap();
		}
		if (iconDrawable != null) {
			return UtilitiesBitmap.createHiResIconBitmap(iconDrawable, mContext);
		}
		return info.getIcon();
	}

	public void setBitmapMemoryCache(LruCache<String, Bitmap> bitmapMemoryCache) {
		mBitmapMemoryCache = bitmapMemoryCache;
	}
}
