package com.anod.car.home.utils;

import android.content.Context;
import android.graphics.Color;

public class ColorUtils {
    /**
     * Create an array of int with colors
     *
     * @param context
     * @return
     */
    public static int[] colorChoice(Context context, int resId){

        int[] mColorChoices=null;
        String[] color_array = context.getResources().getStringArray(resId);

        if (color_array!=null && color_array.length>0) {
            mColorChoices = new int[color_array.length];
            for (int i = 0; i < color_array.length; i++) {
                mColorChoices[i] = Color.parseColor(color_array[i]);
            }
        }
        return mColorChoices;
    }

    /**
     * Parse whiteColor
     *
     * @return
     */
    public static int parseWhiteColor(){
        return Color.parseColor("#FFFFFF");
    }

}