package com.anod.car.home.prefs.drag;

import com.anod.car.home.R;
import info.anodsplace.android.log.AppLog;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * @author alex
 * @date 2014-10-25
 */
public class ShortcutDragListener implements View.OnDragListener {

    public static final String TAG_DELETE_SHORTCUT = "delete_shortcut";

    private final DropCallback mDropCallback;

    private final PorterDuffColorFilter mColorFilter;

    private final PorterDuffColorFilter mDeleteFilter;

    private final View mDeleteBackground;


    public interface DropCallback {

        boolean onDelete(int srcCellId);

        boolean onDrop(int srcCellId, int dstCellId);

        void onDragFinish();
    }

    public ShortcutDragListener(Activity activity, DropCallback dropCallback) {
        mDropCallback = dropCallback;
        mColorFilter = new PorterDuffColorFilter(Color.argb(255, 100, 100, 100),
                PorterDuff.Mode.MULTIPLY);
        mDeleteFilter = new PorterDuffColorFilter(Color.argb(255, 100, 0, 0),
                PorterDuff.Mode.MULTIPLY);

        mDeleteBackground = activity.findViewById(R.id.drag_delete_bg);
    }

    @Override
    public boolean onDrag(View view, DragEvent dragEvent) {
        // Defines a variable to store the action type for the incoming event

        String tag = (String) view.getTag();
        boolean result;
        if (tag.equals(TAG_DELETE_SHORTCUT)) {
            result = handleDeleteShortcutEvent(view, dragEvent);
        } else {
            result = handleShortcutEvent(view, dragEvent);
        }

        if (dragEvent.getAction() == DragEvent.ACTION_DROP) {
            mDropCallback.onDragFinish();
        }
        return result;
    }

    /**
     * Update Delete button view when shortcut is over it
     * Fires onDelete callback
     */
    private boolean handleDeleteShortcutEvent(View view, DragEvent dragEvent) {
        ImageView v = (ImageView) view;
        final int action = dragEvent.getAction();

        // Handles each of the expected events
        switch (action) {

            case DragEvent.ACTION_DRAG_STARTED:
                if (dragEvent.getClipDescription()
                        .hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    return true;
                }
                return false;
            case DragEvent.ACTION_DRAG_ENTERED:
                mDeleteBackground.setVisibility(View.VISIBLE);
                mDeleteBackground.invalidate();

                v.setColorFilter(mDeleteFilter);
                v.invalidate();
                return true;
            case DragEvent.ACTION_DRAG_LOCATION:
                return true;
            case DragEvent.ACTION_DRAG_EXITED:
                mDeleteBackground.setVisibility(View.GONE);
                mDeleteBackground.invalidate();

                v.clearColorFilter();
                v.invalidate();
                return true;
            case DragEvent.ACTION_DROP:
                // Gets the item containing the dragged data
                ClipData.Item item = dragEvent.getClipData().getItemAt(0);
                // Gets the text data from the item.
                String dragData = (String) item.getText();
                // Displays a message containing the dragged data.
                AppLog.d("Delete drop data is " + dragData);

                mDeleteBackground.setVisibility(View.GONE);
                mDeleteBackground.invalidate();
                v.clearColorFilter();
                v.invalidate();

                if (mDropCallback == null) {
                    return true;
                }
                return mDropCallback.onDelete(Integer.valueOf(dragData));
            // Returns true. DragEvent.getResult() will return true.
            //return true;
            case DragEvent.ACTION_DRAG_ENDED:
                mDeleteBackground.setVisibility(View.GONE);
                mDeleteBackground.invalidate();
                v.clearColorFilter();
                v.invalidate();
                // Does a getResult(), and displays what happened.
                if (dragEvent.getResult()) {
                    AppLog.d("Delete was handled.");

                }
                return true;

            default:
                AppLog.e(
                        "handleDeleteShortcutEvent: Unknown action type received by OnDragListener: "
                                + action);
                break;
        }
        return true;
    }

    /**
     * Shortcuts drop events
     */
    private boolean handleShortcutEvent(View view, DragEvent dragEvent) {
        ImageView v = (ImageView) view;
        final int action = dragEvent.getAction();
        // Handles each of the expected events
        switch (action) {

            case DragEvent.ACTION_DRAG_STARTED:
                // Determines if this View can accept the dragged data
                if (dragEvent.getClipDescription()
                        .hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    v.setColorFilter(mColorFilter);
                    v.invalidate();
                    // returns true to indicate that the View can accept the dragged data.
                    return true;
                }
                return false;
            case DragEvent.ACTION_DRAG_ENTERED:
                v.clearColorFilter();
                v.invalidate();
                // Ignore the event
                return true;
            case DragEvent.ACTION_DRAG_LOCATION:
                return true;
            case DragEvent.ACTION_DRAG_EXITED:
                // Re-sets the color tint to blue. Returns true; the return value is ignored.
                v.setColorFilter(mColorFilter);
                v.invalidate();
                return true;
            case DragEvent.ACTION_DROP:
                // Gets the item containing the dragged data
                ClipData.Item item = dragEvent.getClipData().getItemAt(0);
                // Gets the text data from the item.
                String dragData = (String) item.getText();
                // Displays a message containing the dragged data.
                AppLog.d("DragDrop, Dragged data is " + dragData);

                v.clearColorFilter();
                v.invalidate();

                if (mDropCallback == null) {
                    return true;
                }
                String tagData = (String) v.getTag();

                return mDropCallback.onDrop(Integer.valueOf(dragData), Integer.valueOf(tagData));
            // Returns true. DragEvent.getResult() will return true.
            //return true;
            case DragEvent.ACTION_DRAG_ENDED:

                v.clearColorFilter();
                v.invalidate();
                // Does a getResult(), and displays what happened.
                if (dragEvent.getResult()) {
                    AppLog.d("handleShortcutEvent: The drop was handled.");
                }
                // returns true; the value is ignored.
                return true;

            // An unknown action type was received.
            default:
                AppLog.e("handleShortcutEvent: Unknown action type received by OnDragListener: "
                        + action);
                break;
        }
        return true;
    }
}