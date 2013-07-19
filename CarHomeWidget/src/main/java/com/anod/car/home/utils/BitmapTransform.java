package com.anod.car.home.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;

import com.anod.car.home.R;
import com.anod.car.home.skin.IconProcessor;

public class BitmapTransform {
	private int mIconWidth = -1;
	private int mIconHeight = -1;

	private final Rect mOldBounds = new Rect();
	private final Canvas mCanvas = new Canvas();
	private final Matrix mMatrix = new Matrix();
	private final ColorMatrixColorFilter mGreyColorMatrixColorFilter;
	private final Context mContext;
	
	public enum RotateDirection { NONE, RIGHT, LEFT };

	private boolean mApplyGrayFilter = false;
	private Integer mTintColor = null;
	private float mScaleSize = 1.0f;
	private RotateDirection mRotateDirection = RotateDirection.NONE;
	private int mPaddingTop = 0;
	private IconProcessor mIconProcessor;
	
	private static final float[] sGreyScaleMatrix = new float[] { 
			0.5f, 0.5f, 0.5f, 0, 0, // red
			0.5f, 0.5f, 0.5f, 0, 0, // green
			0.5f, 0.5f, 0.5f, 0, 0, // blue
			0, 0, 0, 1.0f, 0 // alpha
	};
	
	public BitmapTransform(Context context) {
		mCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG, Paint.FILTER_BITMAP_FLAG));
		mGreyColorMatrixColorFilter = new ColorMatrixColorFilter(sGreyScaleMatrix);
		int i = (int) context.getResources().getDimension(R.dimen.icon_size);
		mIconHeight = i;
		mIconWidth = i;
		mCanvas.setDrawFilter(new PaintFlagsDrawFilter(4, 2));
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
		if (dir != RotateDirection.NONE) {
			mMatrix.postRotate((dir == RotateDirection.LEFT) ? 90 : 270);
		} else {
			mMatrix.postRotate(0);
		}
		return this;
	}

	public BitmapTransform setPaddingTop(int paddingTop) {
		mPaddingTop = paddingTop;
		return this;
	}
	
	
	public BitmapTransform setIconProcessor(IconProcessor ip) {
		mIconProcessor = ip;
		return this;
	}

	public Bitmap transform(Bitmap src) {
		Bitmap output = src;
		
		if (mRotateDirection != RotateDirection.NONE) {
			output = rotate(output);
		}
		
		if (mApplyGrayFilter) {
			output = applyBitmapFilter(output);
		}
		if (mTintColor != null) {
			applyTint(output, mTintColor);
		}

		float sizeDiff = 0;
		if (mIconProcessor != null) {
			output = mIconProcessor.process(output);
			sizeDiff = mIconProcessor.getSizeDiff();
		}
		
		if (mPaddingTop > 0) {
			output = pad(output, 0, mPaddingTop);
		}
		
		if (mScaleSize > 1.0f) {
			output = scale(output, mScaleSize, sizeDiff );
		}
		
		return output;
	}
	
	private final Bitmap applyBitmapFilter(Bitmap src) {
		final Bitmap bitmap = Bitmap.createBitmap(mIconWidth, mIconHeight, Bitmap.Config.ARGB_8888);
		mCanvas.setBitmap(bitmap);

		BitmapDrawable d = new BitmapDrawable(mContext.getResources(), src);
		mOldBounds.set(d.getBounds());
		d.setBounds(0, 0, mIconWidth, mIconHeight);
		d.setColorFilter(mGreyColorMatrixColorFilter);
		d.draw(mCanvas);
		d.setBounds(mOldBounds);
		return bitmap;
	}

	private void applyTint(Bitmap src, int color) {
		Paint p = new Paint(color);
		LightingColorFilter filter = new LightingColorFilter(color, 0);
		p.setColorFilter(filter);

		mCanvas.drawBitmap(src, 0, 0, p);
	}
	
	private final Bitmap scale(Bitmap src, float scale, float sizeDiff) {
		int min = 0;
		if (sizeDiff > 0) {
			min = (int)(mIconWidth * sizeDiff);
		}
		int scW = (int) (mIconWidth * scale) - min;
		int scH = (int) (mIconHeight * scale) - min;

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
	

	private final Bitmap pad(Bitmap src, int left, int top){
		final Bitmap bitmap = Bitmap.createBitmap(mIconWidth + left, mIconHeight + top, Bitmap.Config.ARGB_8888);
		mCanvas.setBitmap(bitmap);
		mCanvas.drawColor(android.R.color.transparent);
		mCanvas.drawBitmap(src, left, top, null);

		return bitmap;
	}
	
	private final Bitmap rotate(Bitmap src) {
		return Bitmap.createBitmap(src, 0, 0, mIconWidth, mIconHeight, mMatrix, true);
	}

}
