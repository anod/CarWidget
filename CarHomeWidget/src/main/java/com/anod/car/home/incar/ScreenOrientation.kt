package com.anod.car.home.incar

import android.content.Context
import android.content.pm.ActivityInfo
import android.view.View
import android.view.WindowManager

/**
 * @author alex
 * @date 2014-10-11
 */
class ScreenOrientation(private val mContext: Context, private val mWindowManager: WindowManager) {

    private var mLayoutParams: WindowManager.LayoutParams? = null
    private var mOverlayView: View? = null
    private var mViewAdded: Boolean = false

    fun set(orientation: Int) {
        if (orientation == ScreenOrientation.DISABLED) {
            if (mViewAdded) {
                mWindowManager.removeView(mOverlayView)
            }
            mOverlayView = null
            mViewAdded = false
            mLayoutParams = null
            return
        }
        mOverlayView = View(mContext)
        mLayoutParams = createLayoutParams()

        mLayoutParams!!.screenOrientation = orientation

        if (mViewAdded) {
            mWindowManager.updateViewLayout(this.mOverlayView, this.mLayoutParams)
            return
        }
        mWindowManager.addView(mOverlayView, mLayoutParams)
        mViewAdded = true
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        val lp = WindowManager.LayoutParams()
        lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        lp.width = 0
        lp.height = 0
        lp.flags = (WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        lp.flags = -0x200001 and lp.flags
        lp.flags = -0x81 and lp.flags
        return lp
    }

    companion object {
        val DISABLED = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        val PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        val LANDSCAPE_REVERSE = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
    }
}
