package com.anod.car.home;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.util.Log;

public class UtilitiesBitmap {

    private static int sIconWidth = -1;
    private static int sIconHeight = -1;
    
    private static final Rect sOldBounds = new Rect();
    private static final Canvas sCanvas = new Canvas();
    private static final ColorMatrixColorFilter sGreyColorMatrixColorFilter ;
        
    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
        float[] greyScaleMatrix = new float[] { 
        		0.5f, 0.5f, 0.5f, 0, 0, //red
        		0.5f, 0.5f, 0.5f, 0, 0, //green
        		0.5f, 0.5f, 0.5f, 0, 0, //blue
        		0, 0, 0, 1.0f, 0 //alpha
        };
        sGreyColorMatrixColorFilter = new ColorMatrixColorFilter(greyScaleMatrix);
    }
    
    private static void initStatics(Context paramContext)
    {
      int i = (int)paramContext.getResources().getDimension(R.dimen.icon_size);
      sIconHeight = i;
      sIconWidth = i;
      PaintFlagsDrawFilter localPaintFlagsDrawFilter = new PaintFlagsDrawFilter(4, 2);
      sCanvas.setDrawFilter(localPaintFlagsDrawFilter);
    }
 
    /**
     * Returns a bitmap suitable for the all apps view.  The bitmap will be a power
     * of two sized ARGB_8888 bitmap that can be used as a gl texture.
     */
    static Bitmap createIconBitmap(Drawable icon, Context context) {
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

            final Bitmap bitmap = Bitmap.createBitmap(sIconWidth, sIconHeight,
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            final int left = (sIconWidth-width) / 2;
            final int top = (sIconHeight-height) / 2;

            sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left+width, top+height);
           	icon.setColorFilter(null);
            icon.draw(canvas);
            icon.setBounds(sOldBounds);
            return bitmap;
        }
    }

 	static Bitmap scaleBitmap(Bitmap icon, float scale, Context context) {
        if (sIconWidth == -1) {
            initStatics(context);
        }
 		int scW = (int)(sIconWidth*scale);
 		int scH = (int)(sIconHeight*scale);
       	return Bitmap.createScaledBitmap(icon,scW ,scH, true);   	
    }
    static Bitmap applyBitmapFilter(Bitmap icon, Context context) {
        if (sIconWidth == -1) {
            initStatics(context);
        }
        final Bitmap bitmap = Bitmap.createBitmap(sIconWidth, sIconHeight,
                Bitmap.Config.ARGB_8888);
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
    
    static Bitmap tint(Bitmap icon, int color) {
    	Paint p = new Paint(color);
    	LightingColorFilter filter = new LightingColorFilter(color, 0);
    	p.setColorFilter(filter);

        final Canvas canvas = sCanvas;
        canvas.drawBitmap(icon, 0, 0, p);
        return icon;
    }

    
    static byte[] flattenBitmap(Bitmap bitmap) {

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
}
