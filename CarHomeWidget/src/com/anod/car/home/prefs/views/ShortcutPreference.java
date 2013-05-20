package com.anod.car.home.prefs.views;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.ImageView;
import com.anod.car.home.R;
import com.anod.car.home.prefs.views.drag.ShortcutDragListener;
import com.anod.car.home.prefs.views.drag.ShortcutShadowBuilder;
import com.anod.car.home.utils.Utils;

public class ShortcutPreference extends Preference implements OnClickListener {
	private static final int INVALID_RESOURCE = 0;
	public interface DropCallback {
		public int onScrollRequest(int direction);
		public boolean onDrop(int oldCellId, int newCellId);
	}
	private Bitmap mIconBitmap;

	private int mIconResource = INVALID_RESOURCE;
	private OnPreferenceClickListener mDeleteClickListener;
	private Boolean mShowEditButton = false;
	private int mCellId;
	private DropCallback mDropCallback;
	private int mLastVisibleY;

	public ShortcutPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setLayoutResource(R.layout.pref_icon);
	}

	public ShortcutPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayoutResource(R.layout.pref_icon);
	}

	public ShortcutPreference(Context context) {
		super(context);
		setLayoutResource(R.layout.pref_icon);
	}

	public void setLastVisibleY(int lastVisibleY) {
		mLastVisibleY = lastVisibleY;
	}

	public void setDropCallback(DropCallback dropCallback) {
		mDropCallback = dropCallback;
	}

	public void setShortcutPosition(int cellId) {
		mCellId = cellId;
	}

	public int getShortcutPosition() {
		return mCellId;
	}

	public void showButtons(boolean show) {
		mShowEditButton = show;
		notifyChanged();
	}

	public void setIconResource(int resId) {
		mIconBitmap = null;
		mIconResource = resId;
		notifyChanged();
	}

	public void setIconBitmap(Bitmap iconBitmap) {
		mIconBitmap = iconBitmap;
		mIconResource = INVALID_RESOURCE;
		notifyChanged();
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);

		view.setTag(mCellId);

		ImageView dragButton = (ImageView) view.findViewById(R.id.dragButton);
		initDragButton(dragButton, view);

		ImageView imageView = (ImageView) view.findViewById(R.id.pref_icon_view);
		if (imageView != null && mIconBitmap != null) {
			imageView.setImageBitmap(mIconBitmap);
		}
		if (imageView != null && mIconResource > 0) {
			imageView.setImageResource(mIconResource);
		}

		ImageView editButton = (ImageView) view.findViewById(R.id.delete_action_button);
		ImageView replaceImage = (ImageView) view.findViewById(R.id.edit_icon);
		View divider = view.findViewById(R.id.divider);
		if (mShowEditButton) {
			editButton.setOnClickListener(this);
			editButton.setVisibility(View.VISIBLE);
			divider.setVisibility(View.VISIBLE);
			replaceImage.setVisibility(View.VISIBLE);
		} else {
			editButton.setVisibility(View.GONE);
			divider.setVisibility(View.GONE);
			replaceImage.setVisibility(View.GONE);
		}
	}

	@SuppressLint("NewApi")
	private void initDragButton(ImageView dragButton, final View mainView) {
		if (!Utils.IS_HONEYCOMB_OR_GREATER) {
			dragButton.setVisibility(View.GONE);
			return;
		}

		dragButton.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
			ClipData data = ClipData.newPlainText(mCellId + "", mCellId + "");
			mainView.startDrag(data, new ShortcutShadowBuilder(mainView), null, 0);
			return true;
			}
		});
		dragButton.setOnDragListener(new ShortcutDragListener(getContext(), mLastVisibleY, mDropCallback));
	}

	@Override
	public void onClick(View v) {
		if (mDeleteClickListener != null) {
			mDeleteClickListener.onPreferenceClick(this);
		}
	}

	public void setOnDeleteClickListener(OnPreferenceClickListener listener) {
		mDeleteClickListener = listener;
	}


}
