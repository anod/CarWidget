package com.anod.car.home.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.util.Log;

import com.anod.car.home.R;

public class UtilitiesBitmap {

	private static int sIconWidth = -1;
	private static int sIconHeight = -1;

	private static final Paint sPaint = new Paint();
	private static final Rect sBounds = new Rect();
	private static final Rect sOldBounds = new Rect();
	private static final Canvas sCanvas = new Canvas();
	private static final ColorMatrixColorFilter sGreyColorMatrixColorFilter;

	public enum RotateDirection { RIGHT, LEFT }
	
	static {
		sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG, Paint.FILTER_BITMAP_FLAG));
		float[] greyScaleMatrix = new float[] { 0.5f, 0.5f, 0.5f, 0, 0, // red
		0.5f, 0.5f, 0.5f, 0, 0, // green
		0.5f, 0.5f, 0.5f, 0, 0, // blue
		0, 0, 0, 1.0f, 0 // alpha
		};
		sGreyColorMatrixColorFilter = new ColorMatrixColorFilter(greyScaleMatrix);
	}

	private static void initStatics(Context paramContext) {
		int i = (int) paramContext.getResources().getDimension(R.dimen.icon_size);
		sIconHeight = i;
		sIconWidth = i;
		PaintFlagsDrawFilter localPaintFlagsDrawFilter = new PaintFlagsDrawFilter(4, 2);
		sCanvas.setDrawFilter(localPaintFlagsDrawFilter);
	}

	public static Bitmap makeDefaultIcon(PackageManager manager) {
		Drawable d = manager.getDefaultActivityIcon();
		Bitmap b = Bitmap.createBitmap(Math.max(d.getIntrinsicWidth(), 1), Math.max(d.getIntrinsicHeight(), 1), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		d.setBounds(0, 0, b.getWidth(), b.getHeight());
		d.draw(c);
		return b;
	}

	/**
	 * Returns a bitmap suitable for the all apps view. The bitmap will be a
	 * power of two sized ARGB_8888 bitmap that can be used as a gl texture.
	 */
	public static Bitmap createIconBitmap(Drawable icon, Context context) {
		synchronized (sCanvas) { // we share the statics :-(
			if (sIconWidth == -1) {
				initStatics(context);
			}

			int width = sIconWidth;
			int height = sIconHeight;

			if (icon instanceof PaintDrawable) {
				PaintDrawable painter = (PaintDrawable) icon;
				painter.setIntrinsicWidth(width);
				painter.setIntrinsicHeight(height);
			} else if (icon instanceof BitmapDrawable) {
				// Ensure the bitmap has a density.
				BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
				Bitmap bitmap = bitmapDrawable.getBitmap();
				if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
					bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
				}
			}
			int sourceWidth = icon.getIntrinsicWidth();
			int sourceHeight = icon.getIntrinsicHeight();

			if (sourceWidth > 0 && sourceWidth > 0) {
				// There are intrinsic sizes.
				if (width < sourceWidth || height < sourceHeight) {
					// It's too big, scale it down.
					final float ratio = (float) sourceWidth / sourceHeight;
					if (sourceWidth > sourceHeight) {
						height = (int) (width / ratio);
					} else if (sourceHeight > sourceWidth) {
						width = (int) (height * ratio);
					}
				} else if (sourceWidth < width && sourceHeight < height) {
					// It's small, use the size they gave us.
					width = sourceWidth;
					height = sourceHeight;
				}
			}

			final Bitmap bitmap = Bitmap.createBitmap(sIconWidth, sIconHeight, Bitmap.Config.ARGB_8888);
			final Canvas canvas = sCanvas;
			canvas.setBitmap(bitmap);

			final int left = (sIconWidth - width) / 2;
			final int top = (sIconHeight - height) / 2;

			sOldBounds.set(icon.getBounds());
			icon.setBounds(left, top, left + width, top + height);
			icon.setColorFilter(null);
			icon.draw(canvas);
			icon.setBounds(sOldBounds);
			return bitmap;
		}
	}

	public static Bitmap rotate(Bitmap icon, RotateDirection dir) {
		Matrix matrix = new Matrix();
		matrix.postRotate((dir == RotateDirection.LEFT) ? 90 : 270);
		return Bitmap.createBitmap(icon, 0, 0, icon.getWidth(), icon.getHeight(), matrix, true);
	}
	
	public static Bitmap scaleBitmap(Bitmap icon, float scale, float sizeDiff,  Context context) {
		if (sIconWidth == -1) {
			initStatics(context);
		}
		int min = 0;
		if (sizeDiff > 0) {
			min = (int)(sIconWidth * sizeDiff);
		}
		int scW = (int) (sIconWidth * scale) - min;
		int scH = (int) (sIconHeight * scale) - min;
		return Bitmap.createScaledBitmap(icon, scW, scH, true);
	}

	public static Bitmap applyBitmapFilter(Bitmap icon, Context context) {
		if (sIconWidth == -1) {
			initStatics(context);
		}
		final Bitmap bitmap = Bitmap.createBitmap(sIconWidth, sIconHeight, Bitmap.Config.ARGB_8888);
		final Canvas canvas = sCanvas;
		canvas.setBitmap(bitmap);

		BitmapDrawable d = new BitmapDrawable(context.getResources(), icon);
		sOldBounds.set(d.getBounds());
		d.setBounds(0, 0, sIconWidth, sIconHeight);
		d.setColorFilter(sGreyColorMatrixColorFilter);
		d.draw(canvas);
		d.setBounds(sOldBounds);
		return bitmap;
	}

	public static Bitmap tint(Bitmap icon, int color) {
		Paint p = new Paint(color);
		LightingColorFilter filter = new LightingColorFilter(color, 0);
		p.setColorFilter(filter);

		final Canvas canvas = sCanvas;
		canvas.drawBitmap(icon, 0, 0, p);
		return icon;
	}

	public static byte[] flattenBitmap(Bitmap bitmap) {
		// Try go guesstimate how much space the icon will take when serialized
		// to avoid unnecessary allocations/copies during the write.
		int size = bitmap.getWidth() * bitmap.getHeight() * 4;
		ByteArrayOutputStream out = new ByteArrayOutputStream(size);
		try {
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
			return out.toByteArray();
		} catch (IOException e) {
			Log.w("Favorite", "Could not write icon");
			return null;
		}
	}

	/**
	 * Returns a Bitmap representing the thumbnail of the specified Bitmap. The
	 * size of the thumbnail is defined by the dimension
	 * android.R.dimen.launcher_application_icon_size.
	 * 
	 * This method is not thread-safe and should be invoked on the UI thread
	 * only.
	 * 
	 * @param bitmap
	 *            The bitmap to get a thumbnail of.
	 * @param context
	 *            The application's context.
	 * 
	 * @return A thumbnail for the specified bitmap or the bitmap itself if the
	 *         thumbnail could not be created.
	 */
	public static Bitmap createBitmapThumbnail(Bitmap bitmap, Context context) {
		synchronized (sCanvas) { // we share the statics :-(
			if (sIconWidth == -1) {
				initStatics(context);
			}

			int width = sIconWidth;
			int height = sIconHeight;

			final int bitmapWidth = bitmap.getWidth();
			final int bitmapHeight = bitmap.getHeight();

			if (width > 0 && height > 0) {
				if (width < bitmapWidth || height < bitmapHeight) {
					final float ratio = (float) bitmapWidth / bitmapHeight;

					if (bitmapWidth > bitmapHeight) {
						height = (int) (width / ratio);
					} else if (bitmapHeight > bitmapWidth) {
						width = (int) (height * ratio);
					}

					final Bitmap.Config c = (width == sIconWidth && height == sIconHeight && bitmap.getConfig() != null) ? bitmap.getConfig() : Bitmap.Config.ARGB_8888;
					final Bitmap thumb = Bitmap.createBitmap(sIconWidth, sIconHeight, c);
					final Canvas canvas = sCanvas;
					final Paint paint = sPaint;
					canvas.setBitmap(thumb);
					paint.setDither(false);
					paint.setFilterBitmap(true);
					sBounds.set((sIconWidth - width) / 2, (sIconHeight - height) / 2, width, height);
					sOldBounds.set(0, 0, bitmapWidth, bitmapHeight);
					canvas.drawBitmap(bitmap, sOldBounds, sBounds, paint);
					return thumb;
				} else if (bitmapWidth < width || bitmapHeight < height) {
					final Bitmap.Config c = Bitmap.Config.ARGB_8888;
					final Bitmap thumb = Bitmap.createBitmap(sIconWidth, sIconHeight, c);
					final Canvas canvas = sCanvas;
					final Paint paint = sPaint;
					canvas.setBitmap(thumb);
					paint.setDither(false);
					paint.setFilterBitmap(true);
					canvas.drawBitmap(bitmap, (sIconWidth - bitmapWidth) / 2, (sIconHeight - bitmapHeight) / 2, paint);
					return thumb;
				}
			}

			return bitmap;
		}
	}
}
