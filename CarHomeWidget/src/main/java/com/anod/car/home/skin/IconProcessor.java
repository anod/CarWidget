package com.anod.car.home.skin;

import android.graphics.Bitmap;

public interface IconProcessor {

	Bitmap process(Bitmap icon);

	float getSizeDiff();
}