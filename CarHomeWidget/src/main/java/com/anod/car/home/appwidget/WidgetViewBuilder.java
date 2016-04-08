package com.anod.car.home.appwidget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.v4.util.SimpleArrayMap;
import android.util.LruCache;
import android.util.SparseArray;
import android.view.View;
import android.widget.RemoteViews;

import com.anod.car.home.R;
import com.anod.car.home.model.LauncherSettings;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.model.WidgetShortcutsModel;
import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.prefs.preferences.WidgetStorage;
import com.anod.car.home.skin.PropertiesFactory;
import com.anod.car.home.skin.SkinProperties;
import com.anod.car.home.utils.BitmapTransform;
import com.anod.car.home.utils.IconTheme;
import com.anod.car.home.utils.Utils;

public class WidgetViewBuilder {

    final private Context mContext;

    private int mAppWidgetId;

    private Main mPrefs;

    private WidgetShortcutsModel mSmodel;

    private String mOverrideSkin;

    private PendingIntentHelper mPendingIntentHelper;

    private boolean mIsKeyguard = false;

    private int mWidgetHeightDp = -1;

    private LruCache<String, Bitmap> mBitmapMemoryCache;

    private ShortcutViewBuilder mShortcutViewBuilder;

    private BitmapTransform mBitmapTransform;

    private WidgetButtonViewBuilder mWidgetButtonViewBuilder;

    private boolean mWidgetButtonAlternativeHidden;

    private static int[] sBtnIds = new int[]{
            R.id.btn0,
            R.id.btn1,//2
            R.id.btn2,
            R.id.btn3,//4
            R.id.btn4,
            R.id.btn5,//6
            R.id.btn6,
            R.id.btn7//8
    };

    private int[] mTextIds = new int[]{
            R.id.btn_text0,
            R.id.btn_text1,//2
            R.id.btn_text2,
            R.id.btn_text3,//4
            R.id.btn_text4,
            R.id.btn_text5,//6
            R.id.btn_text6,
            R.id.btn_text7//8
    };

    public static int getBtnRes(int pos) {
        return sBtnIds[pos];
    }

    public WidgetViewBuilder setBitmapMemoryCache(LruCache<String, Bitmap> bitmapMemoryCache) {
        mBitmapMemoryCache = bitmapMemoryCache;
        return this;
    }

    public interface PendingIntentHelper {

        PendingIntent createNew(int appWidgetId, int cellId);

        PendingIntent createSettings(int appWidgetId, int buttonId);

        PendingIntent createShortcut(Intent intent, int appWidgetId, int position, long shortcutId);

        PendingIntent createInCar(boolean on, int buttonId);
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
        mPrefs = WidgetStorage.load(mContext, mAppWidgetId);

        mSmodel = new WidgetShortcutsModel(mContext, mAppWidgetId);
        if (WidgetStorage.isFirstTime(mContext, mAppWidgetId)) {
            mSmodel.createDefaultShortcuts();
            WidgetStorage.setFirstTime(false, mContext, mAppWidgetId);
        }
        mSmodel.init();

        mShortcutViewBuilder = new ShortcutViewBuilder(mContext, mAppWidgetId,
                mPendingIntentHelper);
        if (mBitmapMemoryCache != null) {
            mShortcutViewBuilder.setBitmapMemoryCache(mBitmapMemoryCache);
            ;
        }
        mWidgetButtonViewBuilder = new WidgetButtonViewBuilder(mContext, mPrefs,
                mPendingIntentHelper, mAppWidgetId);
        mWidgetButtonViewBuilder.setAlternativeHidden(mWidgetButtonAlternativeHidden);
        mBitmapTransform = new BitmapTransform(mContext);
        refreshIconTransform();
        return this;
    }

    public WidgetViewBuilder setWidgetButtonAlternativeHidden(
            boolean widgetButtonAlternativeHidden) {
        mWidgetButtonAlternativeHidden = widgetButtonAlternativeHidden;
        return this;
    }

    public void refreshIconTransform() {
        applyIconTransform(mBitmapTransform, mPrefs);
    }


    public RemoteViews build() {
        SparseArray<ShortcutInfo> shortcuts = mSmodel.getShortcuts();

        Resources r = mContext.getResources();
        String skinName = (mOverrideSkin == null) ? mPrefs.getSkin() : mOverrideSkin;

        float scaledDensity = r.getDisplayMetrics().scaledDensity;

        SkinProperties skinProperties = PropertiesFactory.create(skinName, mIsKeyguard);

        int iconPaddingRes = skinProperties.getIconPaddingRes();
        if (iconPaddingRes > 0 && !mPrefs.isTitlesHide()) {
            int iconPadding = (int) r.getDimension(iconPaddingRes);
            mBitmapTransform.setPaddingBottom(iconPadding);
        }

        RemoteViews views = new RemoteViews(mContext.getPackageName(),
                skinProperties.getLayout(shortcuts.size()));


        mWidgetButtonViewBuilder.setup(skinProperties, views);

        setBackground(mPrefs, views);
        mBitmapTransform.setIconProcessor(skinProperties.getIconProcessor());

        String themePackage = mPrefs.getIconsTheme();
        IconTheme themeIcons = (themePackage == null) ? null : loadThemeIcons(themePackage);

        boolean isSmallKeyguard = mIsKeyguard && mWidgetHeightDp != -1 && mWidgetHeightDp < 200;
        int keyguardHiddenRows = 0;//isLandscape ? 1 : 2;

        if (mIsKeyguard) {
            hideKeyguardRows(views, isSmallKeyguard);
        }
        mShortcutViewBuilder
                .init(skinName, scaledDensity, skinProperties, themeIcons, mPrefs, mSmodel,
                        mBitmapTransform);

        int totalRows = shortcuts.size() / 2;
        for (int rowNum = 0; rowNum < totalRows; rowNum++) {

            if (isSmallKeyguard && rowNum > keyguardHiddenRows) {
                continue;
            }

            int firstBtn = rowNum * 2;
            int secondBtn = firstBtn + 1;

            mShortcutViewBuilder.fill(views, firstBtn, sBtnIds[firstBtn], mTextIds[firstBtn]);
            mShortcutViewBuilder.fill(views, secondBtn, sBtnIds[secondBtn], mTextIds[secondBtn]);
        }

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

        SimpleArrayMap<String, Integer> cmpMap = new SimpleArrayMap<String, Integer>(shortcuts.size());
        for (int cellId = 0; cellId < shortcuts.size(); cellId++) {
            ShortcutInfo info = mSmodel.getShortcut(cellId);
            if (info == null || info.itemType != LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
                    || info.isCustomIcon()) {
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


    private void setBackground(Main prefs, RemoteViews views) {
        int bgColor = prefs.getBackgroundColor();
        views.setInt(R.id.container, "setBackgroundColor", bgColor);
    }


}

