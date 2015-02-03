package com.anod.car.home.appwidget;

import android.app.PendingIntent;
import android.content.Context;
import android.support.annotation.IdRes;
import android.view.View;
import android.widget.RemoteViews;

import com.anod.car.home.R;
import com.anod.car.home.incar.ModeService;
import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.skin.SkinProperties;

/**
 * @author alex
 * @date 2015-01-31
 */
public class WidgetButtonViewBuilder {
    public static final int BUTTON_1 = 1;
    public static final int BUTTON_2 = 2;

    private Context mContext;
    private Main mPrefs;
    private WidgetViewBuilder.PendingIntentHelper mPendingIntentHelper;
    private int mAppWidgetId;
    private boolean mAlternativeHidden = false;

    public WidgetButtonViewBuilder(Context context, Main prefs, WidgetViewBuilder.PendingIntentHelper pendingIntentHelper, int appWidgetId) {
        mContext = context;
        mPrefs = prefs;
        mPendingIntentHelper = pendingIntentHelper;
        mAppWidgetId = appWidgetId;
    }

    public void setAlternativeHidden(boolean enabled) {
        mAlternativeHidden = enabled;
    }

    public void setup(SkinProperties skinProperties, RemoteViews views) {
        if (skinProperties.hasWidgetButton1()) {
            setup(R.id.widget_btn1, mPrefs.getWidgetButton1(), skinProperties, views, BUTTON_1);
        }
        setup(R.id.widget_btn2, mPrefs.getWidgetButton2(), skinProperties, views, BUTTON_2);
    }

    private void setup(@IdRes int btnResId, int widgetButtonPref, SkinProperties skinProperties, RemoteViews views, int buttonId) {
        if (widgetButtonPref == Main.WIDGET_BUTTON_HIDDEN) {
            if (mAlternativeHidden) {
                views.setImageViewResource(btnResId, R.drawable.ic_action_cancel);
                PendingIntent configIntent = mPendingIntentHelper.createSettings(mAppWidgetId, buttonId);
                views.setOnClickPendingIntent(btnResId, configIntent);
            } else {
                views.setViewVisibility(btnResId, View.GONE);
            }
        } else if (widgetButtonPref == Main.WIDGET_BUTTON_INCAR) {
            if (PreferencesStorage.isInCarModeEnabled(mContext)) {
                setInCarButton(btnResId, mPrefs.isIncarTransparent(), skinProperties, views, buttonId);
            } else {
                views.setViewVisibility(btnResId, View.GONE);
            }
        } else if (widgetButtonPref == Main.WIDGET_BUTTON_SETTINGS) {
            setSettingsButton(btnResId, skinProperties, views, buttonId);
        }
    }

    private void setSettingsButton(@IdRes int resId, SkinProperties skinProperties, RemoteViews views, int buttonId) {
        if (mPrefs.isSettingsTransparent()) {
            views.setImageViewResource(resId, R.drawable.btn_transparent);
        } else {
            views.setImageViewResource(resId, skinProperties.getSettingsButtonRes());
        }
        PendingIntent configIntent = mPendingIntentHelper.createSettings(mAppWidgetId, buttonId);
        views.setOnClickPendingIntent(resId, configIntent);
    }

    private void setInCarButton(@IdRes int btnId, boolean isInCarTrans, SkinProperties skinProp, RemoteViews views, int buttonId) {
        views.setViewVisibility(btnId, View.VISIBLE);
        if (ModeService.sInCarMode) {
            if (isInCarTrans) {
                views.setImageViewResource(btnId, R.drawable.btn_transparent);
            } else {
                int rImg = skinProp.getInCarButtonExitRes();
                views.setImageViewResource(btnId, rImg);
            }
        } else {
            if (isInCarTrans) {
                views.setImageViewResource(btnId, R.drawable.btn_transparent);
            } else {
                int rImg = skinProp.getInCarButtonEnterRes();
                views.setImageViewResource(btnId, rImg);
            }
        }
        boolean switchOn = !ModeService.sInCarMode;
        PendingIntent contentIntent = mPendingIntentHelper.createInCar(switchOn, buttonId);
        if (contentIntent != null) {
            views.setOnClickPendingIntent(btnId, contentIntent);
        }
    }
}
