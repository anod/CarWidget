package com.anod.car.home.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.anod.car.home.skin.IconProcessor;

public class BitmapTransform {
	private int mIconSize = -1;

	private final Rect mOldBounds = new Rect();
	private final Canvas mCanvas = new Canvas();
	private final Context mContext;
	
	public enum RotateDirection { NONE, RIGHT, LEFT };

	private boolean mApplyGrayFilter = false;
	private Integer mTintColor = null;
	private float mScaleSize = 1.0f;
	private RotateDirection mRotateDirection = RotateDirection.NONE;
	private int mPaddingBottom = 0;
	private IconProcessor mIconProcessor;

	public BitmapTransform(Context context) {
		mCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG, Paint.FILTER_BITMAP_FLAG));
		mIconSize = UtilitiesBitmap.getSystemIconSize(context);
		mContext = context;
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

		int height = src.getHeight();
		int width = src.getWidth();

		int degrees = 0;
		if (mRotateDirection != RotateDirection.NONE) {
			degrees = (mRotateDirection == RotateDirection.LEFT) ? 90 : 270;
		}

		Paint paint = new Paint();

		if (mApplyGrayFilter) {
			final ColorMatrix cm = new ColorMatrix();
			cm.setSaturation(0);

			if (mTintColor != null) {

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

			ColorMatrixColorFilter filter = new ColorMatrixColorFilter(cm);
			paint.setColorFilter(filter);
		}


		paint.setFilterBitmap(true);

		float middleX = width / 2.0f;
		float middleY = height / 2.0f;


		Matrix matrix = new Matrix();
		matrix.postRotate(degrees, middleX, middleY);

		//height += mPaddingBottom;

		Bitmap output = Bitmap.createBitmap(width, height + mPaddingBottom, Bitmap.Config.ARGB_8888);
		mCanvas.setBitmap(output);

		mCanvas.setMatrix(matrix);
		mCanvas.drawBitmap(src, 0, 0, paint);



		float sizeDiff = 0;
		if (mIconProcessor != null) {
			output = mIconProcessor.process(output);
			sizeDiff = mIconProcessor.getSizeDiff();
		}


//		output = scale(output, mScaleSize, sizeDiff );

		return output;
	}

	private final Bitmap scale(Bitmap src, float scale, float sizeDiff) {
		int min = 0;
		if (sizeDiff > 0) {
			min = (int)(mIconSize * sizeDiff);
		}
		int scW = (int) (mIconSize * scale) - min;
		int scH = (int) (mIconSize * scale) - min;

		Bitmap scaledBitmap = Bitmap.createBitmap(scW, scH, Bitmap.Config.ARGB_8888);

		float ratioX = scW / (float) src.getWidth();
		float ratioY = scH / (float) src.getHeight();
		float middleX = scW / 2.0f;
		float middleY = scH / 2.0f;

		Matrix scaleMatrix = new Matrix();
		scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

		Canvas canvas = new Canvas(scaledBitmap);
		canvas.setMatrix(scaleMatrix);
		canvas.drawBitmap(src, middleX - src.getWidth() / 2, middleY - src.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

		return scaledBitmap;
		//return Bitmap.createScaledBitmap(src, scW, scH, true);
	}
	

	private final Bitmap pad(Bitmap src, int bottom){
		int height = src.getHeight();
		int width = src.getWidth();
		final Bitmap bitmap = Bitmap.createBitmap(width, height + bottom, Bitmap.Config.ARGB_8888);
		mCanvas.setBitmap(bitmap);
		mCanvas.drawColor(android.R.color.transparent);
		mCanvas.drawBitmap(src, 0, 0, null);


		return bitmap;
	}
	

}
