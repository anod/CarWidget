package info.anodsplace.carwidget.incar

import android.content.Context
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.Toast
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
            if (viewAdded) {
                windowManager.removeView(overlayView)
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

        overlayView = View(context)
        layoutParams = createLayoutParams()

        layoutParams!!.screenOrientation = orientation

        if (viewAdded) {
            windowManager.updateViewLayout(this.overlayView, this.layoutParams)
            return
        }
        windowManager.addView(overlayView, layoutParams)
        viewAdded = true
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