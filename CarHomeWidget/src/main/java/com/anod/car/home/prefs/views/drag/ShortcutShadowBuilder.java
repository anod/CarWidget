package com.anod.car.home.prefs.views.drag;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Point;
import android.view.View;

import com.anod.car.home.R;
import com.anod.car.home.utils.ApiCompat;

/**
 * @author alex
 * @date 2014-08-23
 */
@SuppressLint("NewApi")
public class ShortcutShadowBuilder extends View.DragShadowBuilder {

    private final int mColorDragBg;
    // Defines the constructor for myDragShadowBuilder

    public ShortcutShadowBuilder(View v) {
        super(v);
        mColorDragBg = ApiCompat.getColor(v.getResources(), R.color.drag_item_bg);
    }

    @Override
    public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
        shadowSize.set(getView().getWidth(), getView().getHeight());
        shadowTouchPoint.set(0, shadowSize.y / 2);
    }

    @Override
    public void onDrawShadow(Canvas canvas) {
        final View view = getView();
        canvas.drawColor(mColorDragBg);
        view.draw(canvas);
    }
}
