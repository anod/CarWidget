package info.anodsplace.carwidget.incar

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.framework.app.AlertWindow

class ScreenOnAlert(private val context: Context, private val prefs: InCarInterface, private val alertWindow: AlertWindow) : View.OnTouchListener {

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var lastX = 0
    private var lastY = 0

    fun show() {
        val loc = prefs.screenOnAlert.loc
        val size = context.resources.getDimension(R.dimen.screen_on_alert).toInt()
        val params = WindowManager.LayoutParams(
                size, size, 0, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 0
        ).also {
            it.gravity = Gravity.TOP or Gravity.START
            it.x = loc[0]
            it.y = loc[1]
        }

        alertWindow.show(params) { view ->
            view.alpha = 0.5f
            view.background = ResourcesCompat.getDrawable(context.resources, R.drawable.screen_on, null)
            view.setOnTouchListener(this)
        }
    }

    fun hide() {
        alertWindow.hide()
        if (lastX > 0 && lastY > 0) {
            prefs.screenOnAlert = prefs.screenOnAlert.withLocation(lastX, lastY)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val params = v.layoutParams as WindowManager.LayoutParams
                initialX = params.x
                initialY = params.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                return true
            }
            MotionEvent.ACTION_UP -> {
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                lastX = (initialX
                        + (event.rawX - initialTouchX).toInt())
                lastY = (initialY
                        + (event.rawY - initialTouchY).toInt())
                alertWindow.move(lastX, lastY)
                return true
            }
        }
        return false
    }
}