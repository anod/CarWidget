package com.anod.car.home.incar;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.WindowManager;

/**
 * @author alex
 * @date 2014-10-11
 */
public class ScreenOrientation {

    public final static int DISABLED = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

    public final static int PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

    public final static int LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

    public final static int LANDSCAPE_REVERSE = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;

    private WindowManager.LayoutParams mLayoutParams;

    private View mOverlayView;

    private boolean mViewAdded;

    private final WindowManager mWindowManager;

    private final Context mContext;

    public ScreenOrientation(Context context, WindowManager wm) {
        mContext = context;
        mWindowManager = wm;
    }

    public void set(int orientation) {
        if (orientation == ScreenOrientation.DISABLED) {
            if (mViewAdded) {
                mWindowManager.removeView(mOverlayView);
            }
            mOverlayView = null;
            mViewAdded = false;
            mLayoutParams = null;
            return;
        }
        mOverlayView = new View(mContext);
        mLayoutParams = createLayoutParams();

        mLayoutParams.screenOrientation = orientation;
        if (mViewAdded) {
            mWindowManager.updateViewLayout(this.mOverlayView, this.mLayoutParams);
            return;
        }
        mWindowManager.addView(mOverlayView, mLayoutParams);
        mViewAdded = true;
    }

    private WindowManager.LayoutParams createLayoutParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        lp.width = 0;
        lp.height = 0;
        lp.flags =
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        ;
        lp.flags = (0xFFDFFFFF & lp.flags);
        lp.flags = (0xFFFFFF7F & lp.flags);
        return lp;
    }
}
