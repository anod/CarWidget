package com.anod.car.home.prefs.views;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.anod.car.home.R;
import com.anod.car.home.prefs.views.drag.ShortcutDragListener;
import com.anod.car.home.prefs.views.drag.ShortcutShadowBuilder;

public class ShortcutPreference extends Preference implements OnClickListener {

    private static final int INVALID_RESOURCE = 0;

    private int mAppTheme;

    public interface DropCallback {

        int onScrollRequest(int top);

        boolean onDrop(int oldCellId, int newCellId);
    }

    private Bitmap mIconBitmap;

    private int mIconResource = INVALID_RESOURCE;

    private OnPreferenceClickListener mDeleteClickListener;

    private Boolean mShowEditButton = false;

    private int mCellId;

    private DropCallback mDropCallback;

    public ShortcutPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.pref_shortcut);
    }

    public ShortcutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.pref_shortcut);
    }

    public ShortcutPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.pref_shortcut);
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

    public void setAppTheme(int theme) {
        mAppTheme = theme;
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
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        holder.itemView.setTag(mCellId);

        ImageView dragButton = (ImageView) holder.itemView.findViewById(R.id.drag_button);
        initDragButton(dragButton, holder.itemView);

        ImageView imageView = (ImageView) holder.itemView.findViewById(android.R.id.icon);
        if (mIconBitmap != null) {
            imageView.setImageBitmap(mIconBitmap);
            imageView.setVisibility(View.VISIBLE);
        }
        if (mIconResource > 0) {
            imageView.setImageResource(mIconResource);
            imageView.setVisibility(View.VISIBLE);
        }

        ImageView editButton = (ImageView) holder.itemView.findViewById(R.id.delete_button);
        ImageView replaceImage = (ImageView) holder.itemView.findViewById(R.id.edit_button);
        View divider = holder.itemView.findViewById(R.id.divider);
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

    private void initDragButton(ImageView dragButton, final View mainView) {
        dragButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ClipData data = ClipData.newPlainText(mCellId + "", mCellId + "");
                mainView.startDrag(data, new ShortcutShadowBuilder(mainView), null, 0);
                return true;
            }
        });
        dragButton.setOnDragListener(
                new ShortcutDragListener(getContext(), mAppTheme, mDropCallback));
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
