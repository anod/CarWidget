package com.anod.car.home.prefs.views.drag

import android.content.ClipDescription
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.DragEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.anod.car.home.R
import com.anod.car.home.app.App
import com.anod.car.home.prefs.views.ShortcutPreference

/**
 * @author alex
 * @date 5/18/13
 */
class ShortcutDragListener(context: Context,
                           private val dropCallback: ShortcutPreference.DropCallback?) : View.OnDragListener {

    private val topShadow: Drawable?
    private val background: Drawable?

    init {
        val r = context.resources
        topShadow = ResourcesCompat.getDrawable(r, R.drawable.drop_shadow_top, null)
        val colorResource = App.theme(context).backgroundResource
        val color = ResourcesCompat.getColor(r, colorResource, null)
        background = ColorDrawable(color)
    }

    override fun onDrag(view: View, dragEvent: DragEvent): Boolean {
        // Defines a variable to store the action type for the incoming event
        val v = view.parent as View
        // Handles each of the expected events
        when (dragEvent.action) {

            DragEvent.ACTION_DRAG_STARTED -> {
                // Determines if this View can accept the dragged data
                if (dragEvent.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    setBackgroundColor(v, dragEvent, Color.DKGRAY, background)
                    // returns true to indicate that the View can accept the dragged data.
                    return true
                }
                return false
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                setBackgroundColor(v, dragEvent, Color.DKGRAY, topShadow)

                dropCallback!!.onScrollRequest(v.top)
                // Ignore the event
                return true
            }
            DragEvent.ACTION_DRAG_LOCATION -> {
                // Applies a green tint to the View. Return true; the return value is ignored.
                setBackgroundColor(v, dragEvent, Color.DKGRAY, topShadow)
                return true
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                // Re-sets the color tint to blue. Returns true; the return value is ignored.
                setBackgroundColor(v, dragEvent, Color.DKGRAY, background)

                return true
            }
            DragEvent.ACTION_DROP -> {
                // Gets the item containing the dragged data
                val item = dragEvent.clipData.getItemAt(0)
                // Gets the text data from the item.
                val dragData = item.text as String
                // Displays a message containing the dragged data.
                Log.d("DragDrop", "Dragged data is $dragData")

                setBackground(v, dragEvent, background, background)

                return dropCallback?.onDrop(Integer.valueOf(dragData), v.tag as Int) ?: true
            }
            // Returns true. DragEvent.getResult() will return true.
            //return true;
            DragEvent.ACTION_DRAG_ENDED -> {

                v.background = background
                v.invalidate()
                // Does a getResult(), and displays what happened.
                if (dragEvent.result) {
                    Log.d("DragDrop", "The drop was handled.")

                } else {
                    Log.d("DragDrop", "The drop didn't work.")

                }
                // returns true; the value is ignored.
                return true
            }

            // An unknown action type was received.
            else -> Log.e("DragDrop", "Unknown action type received by OnDragListener.")
        }
        return false
    }

    private fun setBackgroundColor(v: View?, dragEvent: DragEvent, dragViewColor: Int,
                                   otherBg: Drawable?) {
        if (v == null) {
            return
        }
        val dragCellId = Integer.valueOf(dragEvent.clipDescription.label as String)
        val viewCellId = v.tag as Int
        if (dragCellId == viewCellId) {
            v.setBackgroundColor(dragViewColor)
        } else {
            v.background = otherBg
        }
        v.invalidate()
    }

    private fun setBackground(v: View, dragEvent: DragEvent, dragView: Drawable?, shadow: Drawable?) {
        val dragCellId = Integer.valueOf(dragEvent.clipDescription.label as String)
        val viewCellId = v.tag as Int
        if (dragCellId == viewCellId) {
            v.background = dragView
        } else {
            v.background = topShadow
        }
        v.invalidate()
    }

    private fun isAboveMiddle(view: View, dragEvent: DragEvent): Boolean {
        val dragY = dragEvent.y.toInt()
        val middle = (view.y + view.height / 2).toInt()
        Log.d("DragDrop", "Y: $dragY Middle: $middle")
        return dragY < middle
    }
}
