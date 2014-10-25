package com.anod.car.home.prefs.drag;

/**
 * @author alex
 * @date 2014-10-25
 */

import android.graphics.Canvas;
import android.graphics.Point;
import android.view.View;

import com.anod.car.home.R;

/**
 * @author alex
 */
public class ShortcutShadowBuilder extends View.DragShadowBuilder {
    private final int mColorDragBg;
    // Defines the constructor for myDragShadowBuilder

    public ShortcutShadowBuilder(View v) {
        super(v);
        mColorDragBg = v.getResources().getColor(R.color.drag_item_bg);
    }

    @Override
    public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
        shadowSize.set(getView().getWidth(), getView().getHeight());
        shadowTouchPoint.set(shadowSize.x / 2, shadowSize.y / 2);
    }

    @Override
    public void onDrawShadow(Canvas canvas) {
        final View view = getView();
        canvas.drawColor(mColorDragBg);
        view.draw(canvas);
    }
}
