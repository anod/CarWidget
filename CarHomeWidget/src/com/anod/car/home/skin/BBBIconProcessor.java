package com.anod.car.home.skin;

import android.graphics.Bitmap;

public class BBBIconProcessor implements IconProcessor {

	@Override
	public Bitmap process(Bitmap icon) {
		int x = (int)(icon.getWidth() * 0.3f);
		int w = icon.getWidth() - x;
		int y = (int)(icon.getHeight() * 0.3f);
		int h = icon.getHeight() - y;

		Bitmap quarter = Bitmap.createBitmap(icon, x, 0, w, h);
		return quarter;
	}

}
