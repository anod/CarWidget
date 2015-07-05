package com.anod.car.home.prefs.lookandfeel;

import com.anod.car.home.R;
import com.anod.car.home.appwidget.WidgetViewBuilder;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.model.WidgetShortcutsModel;
import com.anod.car.home.prefs.LookAndFeelActivity;
import com.anod.car.home.prefs.drag.ShortcutShadowBuilder;
import com.anod.car.home.utils.AppLog;

import android.app.Activity;
import android.content.ClipData;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RemoteViews;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SkinPreviewFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<View>, View.OnLongClickListener {

    private static final String ARG_POSITION = "position";

    private int mPosition;

    private LookAndFeelActivity mActivity;

    @InjectView(R.id.container_preview)
    ViewGroup mContainer;

    private int mShotcutsCount;

    public static SkinPreviewFragment newInstance(int position) {
        SkinPreviewFragment f = new SkinPreviewFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);

        f.setArguments(args);

        return f;
    }


    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(0, null, this).forceLoad();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity.onPreviewStart(mPosition);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mPosition = getArguments().getInt(ARG_POSITION);
        mActivity = (LookAndFeelActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.skin_item, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    public static class ViewLoader extends AsyncTaskLoader<View> {

        private final LookAndFeelActivity mActivity;

        private final int mPosition;

        public ViewLoader(LookAndFeelActivity activity, int position) {
            super(activity);
            mActivity = activity;
            mPosition = position;
        }

        @Override
        public View loadInBackground() {
            WidgetViewBuilder builder = mActivity.createBuilder();
            builder.init();

            builder.setOverrideSkin(mActivity.getSkinItem(mPosition).value);
            RemoteViews rv = builder.build();

            return rv.apply(mActivity, null);
        }

    }

    @Override
    public Loader<View> onCreateLoader(int id, Bundle args) {
        return new ViewLoader(mActivity, mPosition);
    }

    @Override
    public void onLoadFinished(Loader<View> loader, View inflatedView) {
        mActivity.onPreviewCreated(mPosition);

        if (inflatedView.getParent() != null) {
//            View parent = (View) inflatedView.getParent();
//            mContainer.addView(parent);
            return;
        }

        if (mContainer.getChildCount() > 0) {
            mContainer.removeAllViews();
        }

        final WidgetShortcutsModel model = new WidgetShortcutsModel(getActivity(),
                mActivity.getAppWidgetId());
        model.init();
        mShotcutsCount = model.getCount();

        setupDragNDrop(inflatedView, model);

        mContainer.addView(inflatedView);
//        mContainer.invalidate();
    }

    @Override
    public void onDestroyView() {
        if (mContainer != null && mContainer.getChildCount() > 0) {
            for (int pos = 0; pos < mShotcutsCount; pos++) {
                int btnResId = WidgetViewBuilder.getBtnRes(pos);
                final ImageView dragButton = (ImageView) mContainer.findViewById(btnResId);
                if (dragButton == null) {
                    AppLog.e("Count: " + mShotcutsCount + ", pos: " + pos);
                    continue;
                }
                dragButton.setOnLongClickListener(null);
                dragButton.setOnDragListener(null);
            }
        }

        super.onDestroyView();
    }

    private void setupDragNDrop(View inflatedView, final WidgetShortcutsModel model) {

        for (int pos = 0; pos < mShotcutsCount; pos++) {
            int btnResId = WidgetViewBuilder.getBtnRes(pos);
            final ImageView btn = (ImageView) inflatedView.findViewById(btnResId);
            if (btn == null) {
                AppLog.e("Count: " + mShotcutsCount + ", pos: " + pos);
                continue;
            }
            ShortcutInfo shortcut = model.getShortcut(pos);

            initDragButton(pos, btn, shortcut != null);
        }
    }

    @Override
    public boolean onLongClick(View dragButton) {
        String dragData = "" + dragButton.getTag();
        ClipData data = ClipData.newPlainText(dragData, dragData);
        mActivity.onBeforeDragStart();
        dragButton.startDrag(data, new ShortcutShadowBuilder(dragButton), null, 0);
        return true;
    }


    private void initDragButton(final int cellId, ImageView dragButton,
            final boolean hasShortcut) {
        dragButton.setTag(String.valueOf(cellId));
        if (hasShortcut) {
            dragButton.setOnLongClickListener(this);
        }
        dragButton.setOnDragListener(mActivity.getDragListener());
    }

    @Override
    public void onLoaderReset(Loader<View> loader) {

    }

}
