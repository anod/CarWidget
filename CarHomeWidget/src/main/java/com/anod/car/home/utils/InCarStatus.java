package com.anod.car.home.utils;

import android.content.Context;

import com.anod.car.home.R;
import com.anod.car.home.appwidget.WidgetHelper;
import com.anod.car.home.prefs.preferences.InCarStorage;

public class InCarStatus {

    public static final int NOT_ACTIVE = 0;

    public static final int ENABLED = 1;

    public static final int DISABLED = 2;

    public static int get(Context context) {
        int[] appWidgetIds = WidgetHelper.getAllWidgetIds(context);
        final int widgetsCount = appWidgetIds.length;

        Version version = new Version(context);
        return get(widgetsCount, version, context);
    }

    public static int get(int widgetsCount, Version version, Context context) {
        if (widgetsCount == 0) {
            return NOT_ACTIVE;
        }
        if (version.isProOrTrial()) {
            if (InCarStorage.load(context).isInCarEnabled()) {
                return ENABLED;
            } else {
                return DISABLED;
            }
        }
        return DISABLED;
    }

    public static int render(Context context) {
        int status = InCarStatus.get(context);
        return render(status);
    }

    public static int render(int status) {
        if (status == InCarStatus.NOT_ACTIVE) {
            return R.string.not_active;
        }
        if (status == InCarStatus.ENABLED) {
            return R.string.enabled;
        } else {
            return R.string.disabled;
        }
    }
}
