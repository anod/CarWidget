package com.anod.car.home.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;

import com.anod.car.home.skin.IconProcessor;

public class BitmapTransform {
	private int mIconSize = -1;

	public enum RotateDirection { NONE, RIGHT, LEFT }

	private boolean mApplyGrayFilter = false;
	private Integer mTintColor = null;
	private float mScaleSize = 1.0f;
	private RotateDirection mRotateDirection = RotateDirection.NONE;
	private int mPaddingBottom = 0;
	private IconProcessor mIconProcessor;

	public BitmapTransform(Context context) {
		mIconSize = UtilitiesBitmap.getSystemIconSize(context);
	}

	public String getCacheKey() {
		return String.valueOf(mApplyGrayFilter) + "," +
				String.valueOf(mTintColor) + "," +
				String.valueOf(mScaleSize) + "," +
				mRotateDirection.name() + "," +
				String.valueOf(mPaddingBottom) + "," +
				getIconProcessorId()
		;
	}

	public String getIconProcessorId() {
		if (mIconProcessor != null) {
			return mIconProcessor.id();
		}
		return "none";
	}

	public BitmapTransform setApplyGrayFilter(boolean applyGrayFilter) {
		mApplyGrayFilter = applyGrayFilter;
		return this;
	}

	public BitmapTransform setTintColor(Integer tintColor) {
		mTintColor = tintColor;
		return this;
	}

	public BitmapTransform setScaleSize(float scaleSize) {
		mScaleSize = scaleSize;
		return this;
	}

	public BitmapTransform setRotateDirection(RotateDirection dir) {
		mRotateDirection = dir;
		return this;
	}

	public BitmapTransform setPaddingBottom(int paddingBottom) {
		mPaddingBottom = paddingBottom;
		return this;
	}
	
	
	public BitmapTransform setIconProcessor(IconProcessor ip) {
		mIconProcessor = ip;
		return this;
	}

	public Bitmap transform(Bitmap src) {
		float sizeDiff = 0.0f;
		if (mIconProcessor != null) {
			src = mIconProcessor.process(src);
			sizeDiff = mIconProcessor.getSizeDiff();
		}

		int height = src.getHeight();
		int width = src.getWidth();

		final Canvas canvas = new Canvas();
		canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG, Paint.FILTER_BITMAP_FLAG));

		final Paint paint = new Paint();
		paint.setFilterBitmap(true);

		if (mApplyGrayFilter) {
			final ColorMatrix cm = new ColorMatrix();
			cm.setSaturation(0); //gray scale

			if (mTintColor != null) {
				applyTintColor(cm);
			}

			ColorMatrixColorFilter filter = new ColorMatrixColorFilter(cm);
			paint.setColorFilter(filter);
		}


		Matrix matrix = new Matrix();

		int degrees = 0;
		if (mRotateDirection != RotateDirection.NONE) {
			degrees = (mRotateDirection == RotateDirection.LEFT) ? 90 : 270;
		}

		float iconSize = mIconSize - sizeDiff;
		int scaledSize = (int) (iconSize * mScaleSize);
		float ratioX = scaledSize / (float) width;
		float ratioY = scaledSize / (float) height;
		float scaledMiddleX = scaledSize / 2.0f;
		float scaledMiddleY = scaledSize / 2.0f;

		matrix.postScale(ratioX, ratioY, scaledMiddleX, scaledMiddleY);
		matrix.postRotate(degrees, scaledMiddleX, scaledMiddleX);

		Bitmap output = Bitmap.createBitmap(scaledSize, scaledSize + mPaddingBottom, Bitmap.Config.ARGB_8888);
		canvas.setBitmap(output);

		//paint.setColor(android.graphics.Color.RED);
		//paint.setStyle(Paint.Style.FILL);
		//canvas.drawRect(0, 0, output.getWidth(), output.getHeight(), paint);

		canvas.setMatrix(matrix);
		canvas.drawBitmap(src, scaledMiddleX - width / 2, scaledMiddleY - height / 2, paint);

		return output;
	}

	private void applyTintColor(ColorMatrix cm) {
		float r = Color.red(mTintColor)/255.0f;
		float g = Color.green(mTintColor)/255.0f;
		float b = Color.blue(mTintColor)/255.0f;

		float[] matrix = {
				r, 0, 0, 0, 0, //red
				0, g, 0, 0, 0,//green
				0, 0, b, 0, 0, //blue
				0, 0, 0, 1.0f, 0 //alpha
		};

		cm.postConcat(new ColorMatrix(matrix));
	}


}
