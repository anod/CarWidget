package info.anodsplace.carwidget.incar

import android.content.Context
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.preferences.InCarInterface

/**
 * @author alex
 * @date 2014-10-11
 */
class ScreenOrientation(private val context: Context, private val windowManager: WindowManager) {

    private var layoutParams: WindowManager.LayoutParams? = null
    private var overlayView: View? = null
    private var viewAdded: Boolean = false

    fun set(orientation: Int) {
        if (orientation == DISABLED) {
            if (viewAdded && overlayView != null) {
                try {
                    windowManager.removeView(overlayView)
                } catch (e: IllegalArgumentException) {
                    AppLog.e(e)
                }
            }
            overlayView = null
            viewAdded = false
            layoutParams = null
            return
        }

        if (!Settings.canDrawOverlays(context)) {
            Toast.makeText(context, info.anodsplace.carwidget.content.R.string.allow_permission_overlay, Toast.LENGTH_LONG).show()
            return
        }

        // Update the already-added overlay in place. Creating a new View here and calling
        // updateViewLayout() on it throws IllegalArgumentException ("View not attached to window
        // manager") because that new view was never added.
        val currentView = overlayView
        val currentParams = layoutParams
        if (viewAdded && currentView != null && currentParams != null) {
            currentParams.screenOrientation = orientation
            try {
                windowManager.updateViewLayout(currentView, currentParams)
                return
            } catch (e: IllegalArgumentException) {
                AppLog.e(e)
                overlayView = null
                layoutParams = null
                viewAdded = false
                // Stale/detached view: fall through and re-add the overlay below.
            }
        } else if (viewAdded && currentView != null) {
            // Inconsistent state (view present but params missing). Remove the stale overlay
            // before re-adding so we don't orphan an attached view or pass null params to
            // updateViewLayout() (which would crash with an NPE that the catch above misses).
            try {
                windowManager.removeView(currentView)
            } catch (e: IllegalArgumentException) {
                AppLog.e(e)
            }
            overlayView = null
            layoutParams = null
            viewAdded = false
        }

        val view = View(context)
        val params = createLayoutParams().apply { screenOrientation = orientation }
        try {
            windowManager.addView(view, params)
            overlayView = view
            layoutParams = params
            viewAdded = true
        } catch (e: Exception) {
            AppLog.e(e)
            overlayView = null
            layoutParams = null
            viewAdded = false
        }
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        val lp = WindowManager.LayoutParams()
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
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
        const val DISABLED = InCarInterface.SCREEN_ORIENTATION_DISABLED
    }
}