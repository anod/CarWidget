package com.anod.car.home.app;

import com.anod.car.home.utils.AppLog;
import com.anod.car.home.utils.ImageLoader;
import com.anod.car.home.utils.UtilitiesBitmap;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;


public class AppIconLoader extends ImageLoader {


    private final Context mContext;

    private final PackageManager mPackageManager;

    public AppIconLoader(Context context) {
        super();
        mContext = context.getApplicationContext();
        mPackageManager = context.getPackageManager();
    }

    public void precacheIcon(String appId) {
        Bitmap bmp = loadBitmap(appId);
        if (bmp != null) {
            cacheImage(appId, bmp);
        }
    }

    public Bitmap loadImageUncached(String imgUID) {
        return loadBitmap(imgUID);
    }

    @Override
    protected Bitmap loadBitmap(String imgUID) {
        Drawable d = null;
        Bitmap icon;
        ComponentName cmp = ComponentName.unflattenFromString(imgUID);
        try {
            d = mPackageManager.getActivityIcon(cmp);
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        if (d == null) {
            try {
                d = mPackageManager.getApplicationIcon(cmp.getPackageName());
            } catch (PackageManager.NameNotFoundException e1) {
                AppLog.ex(e1);
                return null;
            }
        }
        icon = UtilitiesBitmap.createSystemIconBitmap(d, mContext);

        return icon;
    }
}
