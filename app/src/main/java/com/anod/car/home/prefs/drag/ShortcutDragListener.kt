package com.anod.car.home.prefs.drag

import android.content.ClipDescription
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.DragEvent
import android.view.View
import android.widget.ImageView
import info.anodsplace.framework.AppLog

/**
 * @author alex
 * @date 2014-10-25
 */
class ShortcutDragListener(private val deleteBackground: View, private val dropCallback: DropCallback?) : View.OnDragListener {
    private val colorFilter: PorterDuffColorFilter = PorterDuffColorFilter(Color.argb(255, 100, 100, 100),
            PorterDuff.Mode.MULTIPLY)
    private val deleteFilter: PorterDuffColorFilter = PorterDuffColorFilter(Color.argb(255, 100, 0, 0),
            PorterDuff.Mode.MULTIPLY)

    interface DropCallback {
        fun onDelete(srcCellId: Int): Boolean
        fun onDrop(srcCellId: Int, dstCellId: Int): Boolean
        fun onDragFinish()
    }

    override fun onDrag(view: View, dragEvent: DragEvent): Boolean {
        // Defines a variable to store the action type for the incoming event

        val tag = view.tag as String
        val result: Boolean
        if (tag == TAG_DELETE_SHORTCUT) {
            result = handleDeleteShortcutEvent(view, dragEvent)
        } else {
            result = handleShortcutEvent(view, dragEvent)
        }

        if (dragEvent.action == DragEvent.ACTION_DROP) {
            dropCallback!!.onDragFinish()
        }
        return result
    }

    /**
     * Update Delete button view when shortcut is over it
     * Fires onDelete callback
     */
    private fun handleDeleteShortcutEvent(view: View, dragEvent: DragEvent): Boolean {
        val v = view as ImageView

        // Handles each of the expected events
        when (val action = dragEvent.action) {

            DragEvent.ACTION_DRAG_STARTED -> {
                return dragEvent.clipDescription
                                .hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                deleteBackground.visibility = View.VISIBLE
                deleteBackground.invalidate()

                v.colorFilter = deleteFilter
                v.invalidate()
                return true
            }
            DragEvent.ACTION_DRAG_LOCATION -> return true
            DragEvent.ACTION_DRAG_EXITED -> {
                deleteBackground.visibility = View.GONE
                deleteBackground.invalidate()

                v.clearColorFilter()
                v.invalidate()
                return true
            }
            DragEvent.ACTION_DROP -> {
                // Gets the item containing the dragged data
                val item = dragEvent.clipData.getItemAt(0)
                // Gets the text data from the item.
                val dragData = item.text as String
                // Displays a message containing the dragged data.
                AppLog.d("Delete drop data is $dragData")

                deleteBackground.visibility = View.GONE
                deleteBackground.invalidate()
                v.clearColorFilter()
                v.invalidate()

                return dropCallback?.onDelete(Integer.valueOf(dragData)) ?: true
            }
        // Returns true. DragEvent.getResult() will return true.
        //return true;
            DragEvent.ACTION_DRAG_ENDED -> {
                deleteBackground.visibility = View.GONE
                deleteBackground.invalidate()
                v.clearColorFilter()
                v.invalidate()
                // Does a getResult(), and displays what happened.
                if (dragEvent.result) {
                    AppLog.i("Delete was handled.")

                }
                return true
            }

            else -> AppLog.e(
                    "handleDeleteShortcutEvent: Unknown action type received by OnDragListener: $action")
        }
        return true
    }

    /**
     * Shortcuts drop events
     */
    private fun handleShortcutEvent(view: View, dragEvent: DragEvent): Boolean {
        val v = view as ImageView
        // Handles each of the expected events
        when (val action = dragEvent.action) {

            DragEvent.ACTION_DRAG_STARTED -> {
                // Determines if this View can accept the dragged data
                if (dragEvent.clipDescription
                                .hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    v.colorFilter = colorFilter
                    v.invalidate()
                    // returns true to indicate that the View can accept the dragged data.
                    return true
                }
                return false
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                v.clearColorFilter()
                v.invalidate()
                // Ignore the event
                return true
            }
            DragEvent.ACTION_DRAG_LOCATION -> return true
            DragEvent.ACTION_DRAG_EXITED -> {
                // Re-sets the color tint to blue. Returns true; the return value is ignored.
                v.colorFilter = colorFilter
                v.invalidate()
                return true
            }
            DragEvent.ACTION_DROP -> {
                // Gets the item containing the dragged data
                val item = dragEvent.clipData.getItemAt(0)
                // Gets the text data from the item.
                val dragData = item.text as String
                // Displays a message containing the dragged data.
                AppLog.d("DragDrop, Dragged data is $dragData")

                v.clearColorFilter()
                v.invalidate()

                if (dropCallback == null) {
                    return true
                }
                val tagData = v.tag as String

                return dropCallback.onDrop(Integer.valueOf(dragData), Integer.valueOf(tagData))
            }
        // Returns true. DragEvent.getResult() will return true.
        //return true;
            DragEvent.ACTION_DRAG_ENDED -> {

                v.clearColorFilter()
                v.invalidate()
                // Does a getResult(), and displays what happened.
                if (dragEvent.result) {
                    AppLog.i("The drop was handled.")
                }
                // returns true; the value is ignored.
                return true
            }

        // An unknown action type was received.
            else -> AppLog.e("handleShortcutEvent: Unknown action type received by OnDragListener: $action")
        }
        return true
    }

    companion object {
        const val TAG_DELETE_SHORTCUT = "delete_shortcut"
    }
}