package com.anod.car.home.skin.icon;

import android.graphics.Bitmap;

public interface IconProcessor {

    Bitmap process(Bitmap icon);

    float getSizeDiff();

    String id();
}
