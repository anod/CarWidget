package com.anod.car.home.prefs.views.drag;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import com.anod.car.home.R;
import com.anod.car.home.prefs.views.ShortcutPreference;

/**
 * @author alex
 * @date 5/18/13
 */
@SuppressLint("NewApi")
public class ShortcutDragListener implements View.OnDragListener {

	private final Drawable mTopShadow;
	private final ShortcutPreference.DropCallback mDropCallback;
	private final int mLastVisibleY;

	public ShortcutDragListener(Context context, int lastVisibleY, ShortcutPreference.DropCallback dropCallback) {
		Resources r = context.getResources();
		mTopShadow = r.getDrawable(R.drawable.drop_shadow_top);
		mDropCallback = dropCallback;
		mLastVisibleY = lastVisibleY;
	}

	@Override
	public boolean onDrag(View view, DragEvent dragEvent) {
		// Defines a variable to store the action type for the incoming event
		View v = (View) view.getParent();
		final int action = dragEvent.getAction();
		// Handles each of the expected events
		switch (action) {

			case DragEvent.ACTION_DRAG_STARTED:
				// Determines if this View can accept the dragged data
				if (dragEvent.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
					setBgColor(v, dragEvent, Color.DKGRAY, Color.TRANSPARENT);
					// returns true to indicate that the View can accept the dragged data.
					return true;
				}
				return false;
			case DragEvent.ACTION_DRAG_ENTERED:
				setBg(v, dragEvent, Color.DKGRAY, mTopShadow);

				int top = v.getTop();
				// TODO: if (top)
				// Ignore the event
				return true;
			case DragEvent.ACTION_DRAG_LOCATION:
				// Applies a green tint to the View. Return true; the return value is ignored.
				setBg(v, dragEvent, Color.DKGRAY, mTopShadow);
				return true;
			case DragEvent.ACTION_DRAG_EXITED:
				// Re-sets the color tint to blue. Returns true; the return value is ignored.
				setBgColor(v, dragEvent, Color.DKGRAY, Color.TRANSPARENT);

				return true;
			case DragEvent.ACTION_DROP:
				// Gets the item containing the dragged data
				ClipData.Item item = dragEvent.getClipData().getItemAt(0);
				// Gets the text data from the item.
				String dragData = (String) item.getText();
				// Displays a message containing the dragged data.
				Log.d("DragDrop", "Dragged data is " + dragData);

				setBgColor(v, dragEvent, Color.TRANSPARENT, Color.TRANSPARENT);

				if (mDropCallback == null) {
					return true;
				}
				return mDropCallback.onDrop(Integer.valueOf(dragData), (Integer) v.getTag());
				// Returns true. DragEvent.getResult() will return true.
				//return true;
			case DragEvent.ACTION_DRAG_ENDED:

				v.setBackgroundColor(Color.TRANSPARENT);
				v.invalidate();
				// Does a getResult(), and displays what happened.
				if (dragEvent.getResult()) {
					Log.d("DragDrop", "The drop was handled.");

				} else {
					Log.d("DragDrop", "The drop didn't work.");

				}
				// returns true; the value is ignored.
				return true;

			// An unknown action type was received.
			default:
				Log.e("DragDrop", "Unknown action type received by OnDragListener.");
				break;
		}
		return false;
	}

	private void setBgColor(View v, DragEvent dragEvent, int dragViewColor, int otherColor) {
		int dragCellId = Integer.valueOf((String) dragEvent.getClipDescription().getLabel());
		int viewCellId = (Integer) v.getTag();
		if (dragCellId == viewCellId) {
			v.setBackgroundColor(dragViewColor);
		} else {
			v.setBackgroundColor(otherColor);
		}
		v.invalidate();
	}

	private void setBg(View v, DragEvent dragEvent, int dragViewColor, Drawable shadow) {
		int dragCellId = Integer.valueOf((String) dragEvent.getClipDescription().getLabel());
		int viewCellId = (Integer) v.getTag();
		if (dragCellId == viewCellId) {
			v.setBackgroundColor(dragViewColor);
		} else {
			v.setBackground(mTopShadow);
		}
		v.invalidate();
	}

	private boolean isAboveMiddle(View view, DragEvent dragEvent) {
		int dragY = (int) dragEvent.getY();
		int middle = (int) (view.getY() + (view.getHeight() / 2));
		Log.d("DragDrop", "Y: " + dragY + " Middle: " + middle);
		if (dragY < middle) {
			return true;
		} else {
			return false;
		}
	}

}
