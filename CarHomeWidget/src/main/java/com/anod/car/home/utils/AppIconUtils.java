package com.anod.car.home.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import com.anod.car.home.model.AppsList;

/**
 * @author alex
 * @date 2014-09-03
 */
public class AppIconUtils {

    private PackageManager mPackageManager;
    private Context mContext;

    public AppIconUtils(Context context) {
        mContext = context.getApplicationContext();
        mPackageManager = mContext.getPackageManager();
    }

    public Bitmap fetchIcon(AppsList.Entry entry) {
        if (entry.icon != null) {
            return entry.icon;
        }

        Drawable d;
        try {
            d = mPackageManager.getApplicationIcon(entry.componentName.getPackageName());
            entry.icon = UtilitiesBitmap.createSystemIconBitmap(d, mContext);
        } catch (PackageManager.NameNotFoundException e) {
            AppLog.ex(e);
        }
        return entry.icon;
    }

    public void fetchDrawableOnThread(final AppsList.Entry entry, final ImageView imageView,final Object tag) {
        if (entry.icon != null) {
            imageView.setImageBitmap(entry.icon);
            imageView.setVisibility(View.VISIBLE);
            return;
        }
        if (entry.componentName == null) {
            imageView.setVisibility(View.INVISIBLE);
            return;
        }

        final Handler handler = new ImageViewHandler(imageView,tag);

        Thread thread = new Thread() {
            @Override
            public void run() {
                Bitmap bitmap = fetchIcon(entry);
                Message message = handler.obtainMessage(1, bitmap);
                handler.sendMessage(message);
            }
        };
        thread.start();
    }

    private static class ImageViewHandler extends Handler {
        private final ImageView mImageView;
        private final Object mTag;

        public ImageViewHandler(ImageView imageView,Object tag) {
            super();
            mImageView = imageView;
            mTag = tag;
        }

        @Override
        public void handleMessage(Message message) {
            Bitmap icon = (Bitmap) message.obj;
            if (icon != null) {
                if (mTag == null || mImageView.getTag().equals(mTag)) {
                    mImageView.setImageBitmap(icon);
                }
            }
        }
    }
}
