package com.anod.car.home.skin.icon;

import android.graphics.Bitmap;

public class BBBIconProcessor implements IconProcessor {

	private static final float SIZE_DIFF = .3f;

	@Override
	public Bitmap process(Bitmap icon) {
		int x = (int)(icon.getWidth() * SIZE_DIFF);
		int w = icon.getWidth() - x;
		int y = (int)(icon.getHeight() * SIZE_DIFF);
		int h = icon.getHeight() - y;

		return Bitmap.createBitmap(icon, x, 0, w, h);
	}

	@Override
	public float getSizeDiff() {
		return SIZE_DIFF;
	}

	@Override
	public String id() {
		return "bbb";
	}

}
