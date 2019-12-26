package com.anod.car.home.prefs.drag

import com.anod.car.home.R

import android.graphics.Canvas
import android.graphics.Point
import androidx.core.content.res.ResourcesCompat
import android.view.View

/**
 * @author alex
 */
class ShortcutShadowBuilder(v: View) : View.DragShadowBuilder(v) {

    private val mColorDragBg: Int = ResourcesCompat.getColor(v.resources, R.color.drag_item_bg, null)

    override fun onProvideShadowMetrics(shadowSize: Point, shadowTouchPoint: Point) {
        shadowSize.set(view.width, view.height)
        shadowTouchPoint.set(shadowSize.x / 2, shadowSize.y / 2)
    }

    override fun onDrawShadow(canvas: Canvas) {
        val view = view
        canvas.drawColor(mColorDragBg)
        view.draw(canvas)
    }
}
