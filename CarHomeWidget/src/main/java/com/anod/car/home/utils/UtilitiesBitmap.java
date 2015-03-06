package com.anod.car.home.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.util.DisplayMetrics;
import android.util.Log;

import com.anod.car.home.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class UtilitiesBitmap {
	private static final int SIZE_ICON = 0; // icon with higher density
	private static final int SIZE_SYSTEM = 1; // default system icon size
	private static final int SIZE_MAX = 2; // max scale size

	public static final int MAX_SCALE = 3;
	private static int sIconSize = -1;
	private static int sIconSystem = -1;
	private static int sIconMaxScale = -1;
	private static int sSystemDensity = -1;
	private static int sIconDensity = -1;

	private static final Paint sPaint = new Paint();
	private static final Rect sBounds = new Rect();
	private static final Rect sOldBounds = new Rect();
	private static final Canvas sCanvas = new Canvas();


	static {
		sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG, Paint.FILTER_BITMAP_FLAG));
	}

	private static void initStatics(Context paramContext) {
		Resources r = paramContext.getResources();

		DisplayMetrics metrics = r.getDisplayMetrics();
		sSystemDensity = metrics.densityDpi;
		sIconDensity =getHigherDensity(sSystemDensity);

		sIconSystem = (int) r.getDimension(R.dimen.icon_size);
		sIconSize = (int) convertForDensity(sIconSystem, sIconDensity, metrics.densityDpi);

		sIconMaxScale = sIconSize * MAX_SCALE;

		PaintFlagsDrawFilter localPaintFlagsDrawFilter = new PaintFlagsDrawFilter(4, 2);
		sCanvas.setDrawFilter(localPaintFlagsDrawFilter);
	}

	private static float convertForDensity(int value, int densityDpi, int deviceDensityDpi) {
		if (deviceDensityDpi == densityDpi) {
			return value;
		}
		return (densityDpi/(float)deviceDensityDpi) * value;
	}

	public static int getSystemIconSize(Context context) {
		synchronized (sCanvas) { // we share the statics :-(
			if (sIconSize == -1) {
				initStatics(context);
			}

			return sIconSystem;
		}
	}

	public static int getIconMaxSize(Context context) {
		synchronized (sCanvas) { // we share the statics :-(
			if (sIconMaxScale == -1) {
				initStatics(context);
			}

			return sIconMaxScale;
		}
	}

	public static int getIconSize(Context context) {
		synchronized (sCanvas) { // we share the statics :-(
			if (sIconSize == -1) {
				initStatics(context);
			}

			return sIconSize;
		}
	}

	public static int getTargetDensity(Context context) {
		synchronized (sCanvas) { // we share the statics :-(
			if (sIconSize == -1) {
				initStatics(context);
			}

			return sIconDensity;
		}
	}

	public static int getHigherDensity(int deviceDensity) {

		if (deviceDensity <= DisplayMetrics.DENSITY_TV) {
			return DisplayMetrics.DENSITY_XXHIGH;
		} else if (deviceDensity < DisplayMetrics.DENSITY_XXXHIGH) {
			return DisplayMetrics.DENSITY_XXXHIGH;
		}


		return deviceDensity;
	}

	public static Bitmap makeDefaultIcon(PackageManager manager) {
		Drawable d = manager.getDefaultActivityIcon();
		Bitmap b = Bitmap.createBitmap(Math.max(d.getIntrinsicWidth(), 1), Math.max(d.getIntrinsicHeight(), 1), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		d.setBounds(0, 0, b.getWidth(), b.getHeight());
		d.draw(c);
		return b;
	}

	public static Bitmap createSystemIconBitmap(Drawable icon, Context context) {
		return createIconBitmapSize(icon, SIZE_SYSTEM, context);
	}
	public static Bitmap createHiResIconBitmap(Drawable icon, Context context) {
		return createIconBitmapSize(icon, SIZE_ICON, context);
	}

	public static Bitmap createMaxSizeIcon(Drawable icon, Context context) {
		return createIconBitmapSize(icon, SIZE_MAX, context);
	}


	/**
	 * Returns a bitmap suitable for the all apps view. The bitmap will be a
	 * power of two sized ARGB_8888 bitmap that can be used as a gl texture.
	 */
	private static Bitmap createIconBitmapSize(Drawable icon, int size, Context context) {
		synchronized (sCanvas) { // we share the statics :-(
			if (sIconSize == -1) {
				initStatics(context);
			}

			int width, height;

			if (size == SIZE_ICON) {
				width = sIconSize;
				height = sIconSize;
			} else if (size == SIZE_MAX) {
				width = sIconMaxScale;
				height = sIconMaxScale;
			} else {
				width = sIconSystem;
				height = sIconSystem;
			}

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
				} else if (width > sourceWidth && height > sourceHeight) {
					// It's small, use the size they gave us.
					width = sourceWidth;
					height = sourceHeight;
				} else if (width == sourceWidth && height == sourceHeight){
					// no processing required
					if (icon instanceof BitmapDrawable) {
						return ((BitmapDrawable) icon).getBitmap();
					}
				}
			}

			int bitmapSize;
			if (size == SIZE_MAX && (width >= sIconMaxScale || height >= sIconMaxScale)){
				bitmapSize = sIconMaxScale;
			} else if (size == SIZE_ICON && (width >= sIconSize && height >= sIconSize)) {
				bitmapSize = sIconSize;
			} else {
				bitmapSize = sIconSystem;
			}


			final Bitmap bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888);
			final Canvas canvas = sCanvas;
			canvas.setBitmap(bitmap);

			sOldBounds.set(icon.getBounds());

			final int left = (bitmapSize - width) / 2;
			final int top = (bitmapSize - height) / 2;
			icon.setBounds(left, top, left + width, top + height);

			icon.setColorFilter(null);
			icon.draw(canvas);
			icon.setBounds(sOldBounds);
			return bitmap;
		}
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
	 * <p/>
	 * This method is not thread-safe and should be invoked on the UI thread
	 * only.
	 *
	 * @param bitmap  The bitmap to get a thumbnail of.
	 * @param context The application's context.
	 * @return A thumbnail for the specified bitmap or the bitmap itself if the
	 * thumbnail could not be created.
	 */
	public static Bitmap createBitmapThumbnail(Bitmap bitmap, Context context) {
		synchronized (sCanvas) { // we share the statics :-(
			if (sIconSize == -1) {
				initStatics(context);
			}

			int width = sIconSize;
			int height = sIconSize;

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

					final Bitmap.Config c = (width == sIconSize && height == sIconSize && bitmap.getConfig() != null) ? bitmap.getConfig() : Bitmap.Config.ARGB_8888;
					final Bitmap thumb = Bitmap.createBitmap(sIconSize, sIconSize, c);
					final Canvas canvas = sCanvas;
					final Paint paint = sPaint;
					canvas.setBitmap(thumb);
					paint.setDither(false);
					paint.setFilterBitmap(true);
					sBounds.set((sIconSize - width) / 2, (sIconSize - height) / 2, width, height);
					sOldBounds.set(0, 0, bitmapWidth, bitmapHeight);
					canvas.drawBitmap(bitmap, sOldBounds, sBounds, paint);
					return thumb;
				} else if (bitmapWidth < width || bitmapHeight < height) {
					final Bitmap.Config c = Bitmap.Config.ARGB_8888;
					final Bitmap thumb = Bitmap.createBitmap(sIconSize, sIconSize, c);
					final Canvas canvas = sCanvas;
					final Paint paint = sPaint;
					canvas.setBitmap(thumb);
					paint.setDither(false);
					paint.setFilterBitmap(true);
					canvas.drawBitmap(bitmap, (sIconSize - bitmapWidth) / 2, (sIconSize - bitmapHeight) / 2, paint);
					return thumb;
				}
			}

			return bitmap;
		}
	}

}
