package com.anod.car.home.app;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import com.anod.car.home.utils.AppLog;
import com.anod.car.home.utils.ImageLoader;
import com.anod.car.home.utils.UtilitiesBitmap;


public class AppIconLoader extends ImageLoader {


    private final Context mContext;
    private final PackageManager mPackageManager;

    public AppIconLoader(Context context) {
		super();
        mContext = context.getApplicationContext();
        mPackageManager = context.getPackageManager();
	}
	
	public void precacheIcon(String appId) {
        Bitmap bmp=loadBitmap(appId);
        if (bmp != null) {
        	cacheImage(appId, bmp);
        }
	}
	
	public Bitmap loadImageUncached(String imgUID) {
		return loadBitmap(imgUID);
	}
	
	@Override
	protected Bitmap loadBitmap(String imgUID) {
            Drawable d;
            Bitmap icon;
            try {
                d = mPackageManager.getApplicationIcon(imgUID);
                icon = UtilitiesBitmap.createSystemIconBitmap(d, mContext);
            } catch (PackageManager.NameNotFoundException e) {
                AppLog.ex(e);
                return null;
            }
            return icon;
	}	
}
